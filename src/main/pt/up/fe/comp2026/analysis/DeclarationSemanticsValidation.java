package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
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
            return isStaticallyTrueLoop(statement)
                    || (!bodyStatements.isEmpty() && alwaysReturns(bodyStatements.getFirst()));
        }

        if (JmmKind.WHILE_STMT.check(statement) || JmmKind.FOR_STMT.check(statement)) {
            // The current language has no break statement, so a statically-true loop
            // cannot complete normally even if the body does not contain an explicit return.
            return isStaticallyTrueLoop(statement);
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

        if (JmmKind.DO_WHILE_STMT.check(loopStmt)) {
            var conditions = loopStmt.getChildren(JmmKind.EXPR);
            return !conditions.isEmpty()
                    && tryEvaluateBooleanConstant(conditions.getFirst()).orElse(false);
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

        if (JmmKind.BINARY_EXPR.check(expr)) {
            var operator = expr.get(JmmAttributes.BINARY_EXPR.OP);
            var leftExpr = expr.getChild(0);
            var rightExpr = expr.getChild(1);

            return switch (operator) {
                case "&&" -> tryEvaluateLogicalAnd(leftExpr, rightExpr);
                case "||" -> tryEvaluateLogicalOr(leftExpr, rightExpr);
                case "<", ">", "<=", ">=" -> tryEvaluateComparison(leftExpr, rightExpr, operator);
                case "==", "!=" -> tryEvaluateEquality(leftExpr, rightExpr, operator);
                default -> Optional.empty();
            };
        }

        return Optional.empty();
    }

    private Optional<Integer> tryEvaluateIntConstant(JmmNode expr) {
        if (JmmKind.PAREN_EXPR.check(expr)) {
            return tryEvaluateIntConstant(expr.getChild(0));
        }

        if (JmmKind.INTEGER_LITERAL.check(expr)) {
            return Optional.of(Integer.parseInt(expr.get("value")));
        }

        if (JmmKind.UNARY_EXPR.check(expr)) {
            var operator = expr.get(JmmAttributes.UNARY_EXPR.OP);
            var operand = tryEvaluateIntConstant(expr.getChild(0));

            return switch (operator) {
                case "+" -> operand;
                case "-" -> operand.map(value -> -value);
                default -> Optional.empty();
            };
        }

        if (!JmmKind.BINARY_EXPR.check(expr)) {
            return Optional.empty();
        }

        var operator = expr.get(JmmAttributes.BINARY_EXPR.OP);
        var left = tryEvaluateIntConstant(expr.getChild(0));
        var right = tryEvaluateIntConstant(expr.getChild(1));

        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }

        return switch (operator) {
            case "+" -> Optional.of(left.get() + right.get());
            case "-" -> Optional.of(left.get() - right.get());
            case "*" -> Optional.of(left.get() * right.get());
            case "/" -> right.get() == 0 ? Optional.empty() : Optional.of(left.get() / right.get());
            case "%" -> right.get() == 0 ? Optional.empty() : Optional.of(left.get() % right.get());
            default -> Optional.empty();
        };
    }

    private Optional<Boolean> tryEvaluateLogicalAnd(JmmNode leftExpr, JmmNode rightExpr) {
        var left = tryEvaluateBooleanConstant(leftExpr);
        if (left.isPresent() && !left.get()) {
            return Optional.of(false);
        }

        var right = tryEvaluateBooleanConstant(rightExpr);
        if (right.isPresent() && !right.get()) {
            return Optional.of(false);
        }

        if (left.isPresent() && left.get()) {
            return right;
        }

        if (right.isPresent() && right.get()) {
            return left;
        }

        return Optional.empty();
    }

    private Optional<Boolean> tryEvaluateLogicalOr(JmmNode leftExpr, JmmNode rightExpr) {
        var left = tryEvaluateBooleanConstant(leftExpr);
        if (left.isPresent() && left.get()) {
            return Optional.of(true);
        }

        var right = tryEvaluateBooleanConstant(rightExpr);
        if (right.isPresent() && right.get()) {
            return Optional.of(true);
        }

        if (left.isPresent() && !left.get()) {
            return right;
        }

        if (right.isPresent() && !right.get()) {
            return left;
        }

        return Optional.empty();
    }

    private Optional<Boolean> tryEvaluateComparison(JmmNode leftExpr, JmmNode rightExpr, String operator) {
        var left = tryEvaluateIntConstant(leftExpr);
        var right = tryEvaluateIntConstant(rightExpr);

        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(switch (operator) {
            case "<" -> left.get() < right.get();
            case ">" -> left.get() > right.get();
            case "<=" -> left.get() <= right.get();
            case ">=" -> left.get() >= right.get();
            default -> throw new IllegalArgumentException("Unexpected comparison operator '" + operator + "'");
        });
    }

    private Optional<Boolean> tryEvaluateEquality(JmmNode leftExpr, JmmNode rightExpr, String operator) {
        var leftBool = tryEvaluateBooleanConstant(leftExpr);
        var rightBool = tryEvaluateBooleanConstant(rightExpr);
        if (leftBool.isPresent() && rightBool.isPresent()) {
            return Optional.of(operator.equals("==")
                    ? leftBool.get().equals(rightBool.get())
                    : !leftBool.get().equals(rightBool.get()));
        }

        var leftInt = tryEvaluateIntConstant(leftExpr);
        var rightInt = tryEvaluateIntConstant(rightExpr);
        if (leftInt.isPresent() && rightInt.isPresent()) {
            return Optional.of(operator.equals("==")
                    ? leftInt.get().equals(rightInt.get())
                    : !leftInt.get().equals(rightInt.get()));
        }

        return Optional.empty();
    }

    private Void visitType(JmmNode typeNode, SymbolTable ignored) {
        if (!isOutermostTypeNode(typeNode)) {
            return null;
        }

        var typeName = getBaseTypeName(typeNode);

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

    private boolean isOutermostTypeNode(JmmNode typeNode) {
        var parent = typeNode.getParent();
        return parent == null || !JmmKind.TYPE.check(parent);
    }

    private boolean isArrayType(JmmNode typeNode) {
        return JmmKind.ARRAY_TYPE.check(typeNode);
    }

    private int getArrayDepth(JmmNode typeNode) {
        if (!JmmKind.ARRAY_TYPE.check(typeNode)) {
            return 0;
        }

        return 1 + getArrayDepth(typeNode.getObject(JmmAttributes.ARRAY_TYPE.ELEMENT_TYPE.getKey(), JmmNode.class));
    }

    private String getBaseTypeName(JmmNode typeNode) {
        if (JmmKind.SIMPLE_TYPE.check(typeNode)) {
            return typeNode.get(JmmAttributes.SIMPLE_TYPE.NAME);
        }

        if (JmmKind.ARRAY_TYPE.check(typeNode)) {
            return getBaseTypeName(typeNode.getObject(JmmAttributes.ARRAY_TYPE.ELEMENT_TYPE.getKey(), JmmNode.class));
        }

        throw new IllegalArgumentException("Unexpected type node kind: " + typeNode.getKind());
    }

    private boolean isSupportedArrayType(JmmNode typeNode) {
        var typeName = getBaseTypeName(typeNode);

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
        var renderedType = getBaseTypeName(typeNode) + "[]".repeat(getArrayDepth(typeNode));

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
