package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.NodeUtils;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.List;
import java.util.Optional;

public class DeclarationSemanticsValidation extends SemanticValidationPass {

    public DeclarationSemanticsValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.TYPE, this::visitType);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable ignored) {
        var methodOpt = types.getEnclosingMethod(methodDecl);
        if (methodOpt.isEmpty()) {
            return null;
        }

        var method = methodOpt.get();
        if (!TypeUtils.voidType().equals(method.returnType())
                && !alwaysReturns(methodDecl.getChildren(JmmKind.STMT))) {
            addReport(newError(methodDecl,
                    "Method '" + method.name() + "' must return a value on every execution path"));
        }

        return null;
    }

    private boolean alwaysReturns(List<JmmNode> statements) {
        for (var statement : statements) {
            if (alwaysReturns(statement)) {
                return true;
            }
        }

        return false;
    }

    private boolean alwaysReturns(JmmNode statement) {
        if (JmmKind.RETURN_STMT.check(statement)) {
            return true;
        }

        if (JmmKind.COMPOUND_STMT.check(statement)) {
            return alwaysReturns(statement.getChildren(JmmKind.STMT));
        }

        if (JmmKind.IF_STMT.check(statement)) {
            var branches = statement.getChildren(JmmKind.STMT);
            return branches.size() == 2
                    && alwaysReturns(branches.get(0))
                    && alwaysReturns(branches.get(1));
        }

        if (JmmKind.DO_WHILE_STMT.check(statement)) {
            var bodyStatements = statement.getChildren(JmmKind.STMT);
            return !bodyStatements.isEmpty() && alwaysReturns(bodyStatements.getFirst());
        }

        if (JmmKind.WHILE_STMT.check(statement) || JmmKind.FOR_STMT.check(statement)) {
            var bodyStatements = statement.getChildren(JmmKind.STMT);
            return !bodyStatements.isEmpty()
                    && isStaticallyTrueLoop(statement)
                    && alwaysReturns(bodyStatements.getFirst());
        }

        return false;
    }

    private boolean isStaticallyTrueLoop(JmmNode loopStmt) {
        if (JmmKind.FOR_STMT.check(loopStmt)) {
            var conditions = loopStmt.getChildren(JmmKind.EXPR);
            if (conditions.isEmpty()) {
                return true;
            }

            return tryEvaluateBooleanConstant(conditions.getFirst()).orElse(false);
        }

        if (JmmKind.WHILE_STMT.check(loopStmt)) {
            return tryEvaluateBooleanConstant(loopStmt.getChild(0)).orElse(false);
        }

        return false;
    }

    private Optional<Boolean> tryEvaluateBooleanConstant(JmmNode expr) {
        if (JmmKind.PAREN_EXPR.check(expr)) {
            return tryEvaluateBooleanConstant(expr.getChild(0));
        }

        if (JmmKind.BOOLEAN_LITERAL.check(expr)) {
            return Optional.of(Boolean.parseBoolean(expr.get(JmmAttributes.BOOLEAN_LITERAL.VALUE)));
        }

        if (JmmKind.NOT_EXPR.check(expr)) {
            return tryEvaluateBooleanConstant(expr.getChild(0)).map(value -> !value);
        }

        return Optional.empty();
    }

    private Void visitType(JmmNode typeNode, SymbolTable ignored) {
        var typeName = typeNode.get(JmmAttributes.TYPE.NAME);

        if (!types.isKnownTypeName(typeName)) {
            addReport(newError(typeNode, "Type '" + typeName + "' is not available in the current compilation unit"));
            return null;
        }

        if (isArrayType(typeNode) && !isSupportedArrayType(typeNode)) {
            addReport(newError(typeNode, unsupportedArrayTypeMessage(typeNode)));
            return null;
        }

        if (!TypeUtils.voidType().equals(types.convertType(typeNode))) {
            return null;
        }

        var parent = typeNode.getParent();
        if (parent == null) {
            return null;
        }

        if (JmmKind.PARAM.check(parent)) {
            addReport(newError(typeNode,
                    "Parameter '" + parent.get(JmmAttributes.PARAM.NAME) + "' cannot have type 'void'"));
            return null;
        }

        if (!JmmKind.VAR_DECL.check(parent) && !JmmKind.FIELD_DECL.check(parent)) {
            return null;
        }

        var declarationKind = JmmKind.FIELD_DECL.check(parent) ? "Field" : "Local variable";
        addReport(newError(typeNode,
                declarationKind + " '" + getDeclarationName(parent) + "' cannot have type 'void'"));

        return null;
    }

    private boolean isArrayType(JmmNode typeNode) {
        return NodeUtils.getBooleanAttribute(typeNode, JmmAttributes.TYPE.IS_ARRAY.getKey(), "false");
    }

    private int getArrayDepth(JmmNode typeNode) {
        return NodeUtils.getIntegerAttribute(typeNode, JmmAttributes.TYPE.ARRAY_DEPTH.getKey(), "0");
    }

    private boolean isSupportedArrayType(JmmNode typeNode) {
        var typeName = typeNode.get(JmmAttributes.TYPE.NAME);

        if ("int".equals(typeName)) {
            return true;
        }

        return "String".equals(typeName)
                && getArrayDepth(typeNode) == 1
                && isMainMethodParameter(typeNode);
    }

    private boolean isMainMethodParameter(JmmNode typeNode) {
        var parent = typeNode.getParent();
        if (parent == null || !JmmKind.PARAM.check(parent)) {
            return false;
        }

        var methodDeclOpt = typeNode.getAncestor(JmmKind.METHOD_DECL);
        if (methodDeclOpt.isEmpty()) {
            return false;
        }

        var methodDecl = methodDeclOpt.get();
        if (!"main".equals(methodDecl.get(JmmAttributes.METHOD_DECL.NAME))) {
            return false;
        }

        var methodOpt = types.getEnclosingMethod(typeNode);
        if (methodOpt.isEmpty()
                || !methodOpt.get().isStatic()
                || !TypeUtils.voidType().equals(methodOpt.get().returnType())) {
            return false;
        }

        var params = methodDecl.getChildren(JmmKind.PARAM);
        return params.size() == 1 && params.getFirst() == parent;
    }

    private String unsupportedArrayTypeMessage(JmmNode typeNode) {
        var renderedType = typeNode.get(JmmAttributes.TYPE.NAME) + "[]".repeat(getArrayDepth(typeNode));

        if ("String[]".equals(renderedType)) {
            return "Type 'String[]' is only supported as the parameter of the static void main method";
        }

        return "Type '" + renderedType + "' is not supported; J-- only allows arrays of int";
    }

    private String getDeclarationName(JmmNode declarationNode) {
        if (JmmKind.FIELD_DECL.check(declarationNode)) {
            return declarationNode.get(JmmAttributes.FIELD_DECL.NAME);
        }

        return declarationNode.get(JmmAttributes.VAR_DECL.NAME);
    }
}
