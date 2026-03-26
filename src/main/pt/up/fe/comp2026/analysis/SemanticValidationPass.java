package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.AccessType;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shared helpers used by the semantic-analysis passes.
 */
public abstract class SemanticValidationPass extends AnalysisVisitorWithTable {

    protected SemanticValidationPass(SymbolTable table) {
        super(table);
    }

    protected void validateBooleanCondition(JmmNode condition, String statementName) {
        var conditionType = types.getExprType(condition);
        if (conditionType != null && !isBoolean(conditionType)) {
            addReport(newError(condition,
                    statementName + " condition must have type 'boolean'"));
        }
    }

    protected void validateSimpleAssignment(JmmNode assignmentNode, String targetName, JmmNode valueExpr) {
        var target = resolveAssignmentTarget(assignmentNode, targetName);
        if (target == null) {
            return;
        }

        var exprType = types.getExprType(valueExpr);
        if (exprType == null) {
            return;
        }

        if (!types.isAssignable(target.type(), exprType)) {
            addReport(newError(assignmentNode,
                    "Cannot assign expression of type '" + exprType.print() + "' to '" + targetName +
                            "' of type '" + target.type().print() + "'"));
        }
    }

    protected void validateIndexedAssignment(JmmNode assignmentNode, String targetName) {
        var target = resolveAssignmentTarget(assignmentNode, targetName);
        if (target == null) {
            return;
        }

        var indexedType = getIndexedType(assignmentNode, target.type(), targetName);
        if (indexedType == null) {
            return;
        }

        var valueType = types.getExprType(assignmentNode.getChild(assignmentNode.getNumChildren() - 1));
        if (valueType == null) {
            return;
        }

        if (!types.isAssignable(indexedType, valueType)) {
            addReport(newError(assignmentNode,
                    "Cannot assign expression of type '" + valueType.print() + "' to array slot of type '" +
                            indexedType.print() + "'"));
        }
    }

    protected void validateCurrentClassMethodCall(JmmNode callExpr, int firstArgIndex, boolean requireStatic) {
        var argTypesOpt = getArgumentTypes(callExpr, firstArgIndex);
        if (argTypesOpt.isEmpty()) {
            return;
        }

        var methodName = getCallMethodName(callExpr);
        var allMethods = getCurrentAndInheritedMethods(methodName);

        if (findMatchingMethod(allMethods, argTypesOpt.get(), requireStatic) != null) {
            return;
        }

        if (requireStatic && findMatchingMethod(allMethods, argTypesOpt.get(), false) != null) {
            addReport(newError(callExpr,
                    "Method '" + methodName + "' is not static and cannot be called from a static context"));
            return;
        }

        if (allMethods.isEmpty()) {
            addReport(newError(callExpr,
                    "Method '" + methodName + "' does not exist in class '" + table.getClassName() + "'"));
            return;
        }

        var visibleMethods = requireStatic
                ? allMethods.stream().filter(MethodSymbol::isStatic).toList()
                : allMethods;

        if (visibleMethods.isEmpty()) {
            addReport(newError(callExpr,
                    "Method '" + methodName + "' is not static and cannot be called from a static context"));
            return;
        }

        var argCount = argTypesOpt.get().size();
        var anyWithSameCount = visibleMethods.stream().anyMatch(method -> method.parameters().size() == argCount);
        if (!anyWithSameCount) {
            addReport(newError(callExpr,
                    "Wrong number of arguments for method '" + methodName + "' in class '" + table.getClassName() + "'"));
            return;
        }

        addReport(newError(callExpr,
                "Wrong argument types for method '" + methodName + "' in class '" + table.getClassName() + "'"));
    }

    protected Optional<List<JmmType>> getArgumentTypes(JmmNode callExpr, int firstArgIndex) {
        var argTypes = new ArrayList<JmmType>();

        for (int i = firstArgIndex; i < callExpr.getNumChildren(); i++) {
            var argType = types.tryGetExprType(callExpr.getChild(i));
            if (argType.isEmpty()) {
                addReport(newError(callExpr, "Cannot determine type of argument " + (i - firstArgIndex + 1)));
                return Optional.empty();
            }

            argTypes.add(argType.get());
        }

        return Optional.of(argTypes);
    }

    protected String getCallMethodName(JmmNode callExpr) {
        if (JmmKind.METHOD_CALL_EXPR.check(callExpr)) {
            return callExpr.get(JmmAttributes.METHOD_CALL_EXPR.NAME);
        }

        if (JmmKind.IMPLICIT_THIS_CALL_EXPR.check(callExpr)) {
            return callExpr.get(JmmAttributes.IMPLICIT_THIS_CALL_EXPR.NAME);
        }

        throw new IllegalArgumentException("Expected a method-call node, got: " + callExpr.getKind());
    }

    protected List<MethodSymbol> getCurrentAndInheritedMethods(String methodName) {
        var methods = new ArrayList<MethodSymbol>(table.getMethods(methodName));

        var visitedSupers = new java.util.HashSet<String>();
        var superQualifiedName = table.getSuperFullyQualifiedName();
        while (superQualifiedName != null && visitedSupers.add(superQualifiedName)) {
            var superTableOpt = table.getImportedSymbolTable(superQualifiedName);
            if (superTableOpt.isEmpty()) {
                break;
            }

            var superTable = superTableOpt.get();
            methods.addAll(superTable.getMethods(methodName));
            superQualifiedName = superTable.getSuperFullyQualifiedName();
        }

        return methods;
    }

    protected MethodSymbol findMatchingMethod(List<MethodSymbol> methods, List<JmmType> argTypes, boolean requireStatic) {
        for (var method : methods) {
            if (requireStatic && !method.isStatic()) {
                continue;
            }

            if (method.parameters().size() != argTypes.size()) {
                continue;
            }

            var matches = true;
            for (int i = 0; i < argTypes.size(); i++) {
                if (!types.isAssignable(method.parameters().get(i).type(), argTypes.get(i))) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                return method;
            }
        }

        return null;
    }

    protected TypeUtils.ResolvedIdentifier resolveAssignmentTarget(JmmNode assignmentNode, String targetName) {
        var target = types.resolveIdentifier(assignmentNode, targetName);

        if (target.isEmpty()) {
            addReport(newError(assignmentNode, "Identifier '" + targetName + "' is not declared"));
            return null;
        }

        if (target.get().accessType() == AccessType.IMPORT || target.get().accessType() == AccessType.CLASS) {
            addReport(newError(assignmentNode, "Cannot assign to class name '" + targetName + "'"));
            return null;
        }

        if (target.get().accessType() == AccessType.FIELD && types.isStaticMethodContext(assignmentNode)) {
            addReport(newError(assignmentNode, "Field '" + targetName + "' cannot be assigned inside a static method"));
            return null;
        }

        return target.get();
    }

    protected boolean isCurrentClass(JmmType receiverType) {
        if (!receiverType.isClass()) {
            return false;
        }

        var receiverClass = receiverType.asClass();
        return receiverClass.fullyQualifiedName().equals(table.getFullyQualifiedName());
    }

    protected boolean isCurrentClass(JmmClassType classType) {
        return classType.fullyQualifiedName().equals(table.getFullyQualifiedName());
    }

    protected boolean hasLoadableConstructorOwner(JmmClassType classType) {
        try {
            Class.forName(classType.fullyQualifiedName());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected List<Constructor<?>> getPublicConstructors(String fullyQualifiedName) {
        try {
            return List.of(Class.forName(fullyQualifiedName).getConstructors());
        } catch (ClassNotFoundException e) {
            return List.of();
        }
    }

    protected boolean hasMatchingConstructor(List<Constructor<?>> constructors, List<JmmType> argTypes) {
        for (var constructor : constructors) {
            if (constructor.isVarArgs()) {
                continue;
            }

            var parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length != argTypes.size()) {
                continue;
            }

            var matches = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                var parameterType = toJmmType(parameterTypes[i]);
                if (parameterType == null || !types.isAssignable(parameterType, argTypes.get(i))) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                return true;
            }
        }

        return false;
    }

    protected JmmType toJmmType(Class<?> javaType) {
        if (javaType == void.class) {
            return JmmPrimitiveType.VOID;
        }

        if (javaType == boolean.class) {
            return JmmPrimitiveType.BOOLEAN;
        }

        if (javaType == int.class) {
            return JmmPrimitiveType.INT;
        }

        if (javaType.isArray()) {
            var itemType = toJmmType(javaType.getComponentType());
            return itemType == null ? null : JmmArrayType.of(itemType);
        }

        return JmmClassType.ofInstance(javaType.getName(), true);
    }

    protected boolean isBoolean(JmmType type) {
        return TypeUtils.booleanType().equals(type);
    }

    protected boolean isInt(JmmType type) {
        return TypeUtils.intType().equals(type);
    }

    protected boolean isAssignableEntity(JmmNode expr) {
        return JmmKind.VAR_REF_EXPR.check(expr) || JmmKind.ARRAY_ACCESS_EXPR.check(expr);
    }

    protected JmmType getIndexedType(JmmNode arrayAssignStmt, JmmType baseType, String targetName) {
        var currentType = baseType;
        var lastIndex = arrayAssignStmt.getNumChildren() - 1;

        for (int i = 0; i < lastIndex; i++) {
            var indexType = types.getExprType(arrayAssignStmt.getChild(i));
            if (indexType != null && !isInt(indexType)) {
                addReport(newError(arrayAssignStmt.getChild(i), "Array index expression must have type 'int'"));
            }

            if (!currentType.isArray()) {
                addReport(newError(arrayAssignStmt,
                        "Identifier '" + targetName + "' does not have enough array dimensions for this store"));
                return null;
            }

            currentType = currentType.asArray().itemType();
        }

        return currentType;
    }
}
