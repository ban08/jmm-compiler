package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates class field initializers against their declared type.
 */
public class FieldInitializerValidation extends AnalysisVisitorWithTable {

    public FieldInitializerValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.VAR_DECL, this::visitVarDecl);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitVarDecl(JmmNode varDecl, SymbolTable ignored) {
        if (!JmmKind.CLASS_DECL.check(varDecl.getParent())) {
            return null;
        }

        var initializerExprs = varDecl.getChildren(JmmKind.EXPR);
        if (initializerExprs.isEmpty()) {
            return null;
        }

        var declaredType = types.convertType(varDecl.getObject("typeNode", JmmNode.class));
        var initializerType = inferExprType(initializerExprs.getFirst());

        if (initializerType != null && !isAssignable(declaredType, initializerType)) {
            addReport(newError(
                    varDecl,
                    "Initializer of field '" + varDecl.get("name") + "' has incompatible type '" +
                            initializerType.print() + "' (expected '" + declaredType.print() + "')"));
        }

        return null;
    }

    private JmmType inferExprType(JmmNode expr) {
        if (JmmKind.INTEGER_LITERAL.check(expr)) {
            return JmmPrimitiveType.INT;
        }

        if (JmmKind.BOOLEAN_LITERAL.check(expr)) {
            return JmmPrimitiveType.BOOLEAN;
        }

        if (JmmKind.PAREN_EXPR.check(expr)) {
            return inferExprType(expr.getChild(0));
        }

        if (JmmKind.NOT_EXPR.check(expr)) {
            return JmmPrimitiveType.BOOLEAN;
        }

        if (JmmKind.THIS_EXPR.check(expr)) {
            return JmmClassType.ofInstance(table.getFullyQualifiedName(), false);
        }

        if (JmmKind.NEW_EXPR.check(expr)) {
            return resolveClassType(expr.get("name"));
        }

        if (JmmKind.VAR_REF_EXPR.check(expr)) {
            return table.getField(expr.get("name")).map(field -> field.type()).orElse(null);
        }

        if (JmmKind.BINARY_EXPR.check(expr)) {
            return inferBinaryExprType(expr.get("op"));
        }

        if (JmmKind.METHOD_CALL_EXPR.check(expr)) {
            return inferMethodCallType(expr);
        }

        return null;
    }

    private JmmType inferBinaryExprType(String operator) {
        return switch (operator) {
            case "+", "-", "*", "/" -> JmmPrimitiveType.INT;
            case "<", ">", "<=", ">=", "==", "!=", "&&", "||" -> JmmPrimitiveType.BOOLEAN;
            default -> null;
        };
    }

    private JmmType inferMethodCallType(JmmNode callExpr) {
        var children = callExpr.getChildren();
        if (children.isEmpty()) {
            return null;
        }

        var receiverExpr = children.getFirst();
        var receiverType = inferExprType(receiverExpr);
        var isThisReceiver = JmmKind.THIS_EXPR.check(receiverExpr)
                || (receiverType instanceof JmmClassType classType
                && classType.fullyQualifiedName().equals(table.getFullyQualifiedName()));

        if (!isThisReceiver) {
            return null;
        }

        var argTypes = new ArrayList<JmmType>();
        for (int i = 1; i < children.size(); i++) {
            var argType = inferExprType(children.get(i));
            if (argType == null) {
                return null;
            }
            argTypes.add(argType);
        }

        var methodName = callExpr.get("name");
        for (var method : table.getMethods(methodName)) {
            if (matchesMethodArgs(method, argTypes)) {
                return method.returnType();
            }
        }

        return null;
    }

    private boolean matchesMethodArgs(MethodSymbol method, List<JmmType> argTypes) {
        var params = method.parameters();
        if (params.size() != argTypes.size()) {
            return false;
        }

        for (int i = 0; i < params.size(); i++) {
            if (!isAssignable(params.get(i).type(), argTypes.get(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean isAssignable(JmmType targetType, JmmType valueType) {
        if (targetType.equals(valueType)) {
            return true;
        }

        if (targetType.isClass() && valueType.isClass()) {
            var targetClass = targetType.asClass();
            var valueClass = valueType.asClass();

            if (targetClass.fullyQualifiedName().equals(valueClass.fullyQualifiedName())) {
                return true;
            }

            if (targetClass.name().equals(valueClass.name())) {
                return true;
            }

            var superFqn = table.getSuperFullyQualifiedName();
            return superFqn != null
                    && targetClass.fullyQualifiedName().equals(superFqn)
                    && valueClass.fullyQualifiedName().equals(table.getFullyQualifiedName());
        }

        return false;
    }

    private JmmType resolveClassType(String className) {
        var importedClass = table.getImportedFullyQualifiedName(className);
        if (importedClass.isPresent()) {
            return JmmClassType.ofInstance(importedClass.get(), true);
        }

        if (className.equals(table.getClassName())) {
            return JmmClassType.ofInstance(table.getFullyQualifiedName(), false);
        }

        var implicitImport = table.getImplicitImport(className);
        if (implicitImport.isPresent()) {
            return JmmClassType.ofInstance(implicitImport.get().getFullyQualifiedName(), true);
        }

        return JmmClassType.ofInstance(className, false);
    }
}
