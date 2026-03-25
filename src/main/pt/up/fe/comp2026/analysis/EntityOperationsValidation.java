package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.AccessType;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates entity access, assignments and arithmetic/logical expressions.
 */
public class EntityOperationsValidation extends AnalysisVisitorWithTable {

    public EntityOperationsValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.TYPE, this::visitType);
        addVisit(JmmKind.NEW_EXPR, this::visitNewExpr);
        addVisit(JmmKind.VAR_REF_EXPR, this::visitVarRefExpr);
        addVisit(JmmKind.THIS_EXPR, this::visitThisExpr);
        addVisit(JmmKind.IF_STMT, this::visitIfStmt);
        addVisit(JmmKind.WHILE_STMT, this::visitWhileStmt);
        addVisit(JmmKind.DO_WHILE_STMT, this::visitDoWhileStmt);
        addVisit(JmmKind.FOR_STMT, this::visitForStmt);
        addVisit(JmmKind.FOR_HEADER_ASSIGN, this::visitForHeaderAssign);
        addVisit(JmmKind.ASSIGN_STMT, this::visitAssignStmt);
        addVisit(JmmKind.ARRAY_ASSIGN_STMT, this::visitArrayAssignStmt);
        addVisit(JmmKind.RETURN_STMT, this::visitReturnStmt);
        addVisit(JmmKind.BINARY_EXPR, this::visitBinaryExpr);
        addVisit(JmmKind.NOT_EXPR, this::visitNotExpr);
        addVisit(JmmKind.UNARY_EXPR, this::visitUnaryExpr);
        addVisit(JmmKind.METHOD_CALL_EXPR, this::visitMethodCallExpr);
        addVisit(JmmKind.IMPLICIT_THIS_CALL_EXPR, this::visitImplicitThisCallExpr);
        addVisit(JmmKind.FIELD_ACCESS_EXPR, this::visitFieldAccessExpr);
        addVisit(JmmKind.ARRAY_ACCESS_EXPR, this::visitArrayAccessExpr);
        addVisit(JmmKind.NEW_INT_ARRAY_EXPR, this::visitNewIntArrayExpr);
        addVisit(JmmKind.ARRAY_INITIALIZER_EXPR, this::visitArrayInitializerExpr);
        addVisit(JmmKind.LENGTH_EXPR, this::visitLengthExpr);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable ignored) {
        var methodOpt = types.getEnclosingMethod(methodDecl);
        if (methodOpt.isEmpty()) {
            return null;
        }

        var method = methodOpt.get();
        if (!TypeUtils.voidType().equals(method.returnType())
                && methodDecl.getDescendants(JmmKind.RETURN_STMT).isEmpty()) {
            addReport(newError(methodDecl,
                    "Method '" + method.name() + "' must contain a return statement"));
        }

        return null;
    }

    private Void visitType(JmmNode typeNode, SymbolTable ignored) {
        var typeName = typeNode.get("name");

        if (!types.isKnownTypeName(typeName)) {
            addReport(newError(typeNode, "Type '" + typeName + "' is not available in the current compilation unit"));
        }

        return null;
    }

    private Void visitNewExpr(JmmNode newExpr, SymbolTable ignored) {
        var className = newExpr.get("name");
        if (!types.isKnownTypeName(className)) {
            addReport(newError(newExpr, "Class '" + className + "' is not imported"));
        }

        return null;
    }

    private Void visitVarRefExpr(JmmNode varRefExpr, SymbolTable ignored) {
        var identifier = varRefExpr.get("name");
        var resolved = types.resolveIdentifier(varRefExpr, identifier);

        if (resolved.isEmpty()) {
            addReport(newError(varRefExpr, "Identifier '" + identifier + "' is not declared"));
            return null;
        }

        if (resolved.get().accessType() == AccessType.FIELD && types.isStaticMethodContext(varRefExpr)) {
            addReport(newError(varRefExpr, "Field '" + identifier + "' cannot be accessed from a static method"));
        }

        return null;
    }

    private Void visitThisExpr(JmmNode thisExpr, SymbolTable ignored) {
        if (types.isStaticMethodContext(thisExpr)) {
            addReport(newError(thisExpr, "Keyword 'this' cannot be used inside a static method"));
        }

        return null;
    }

    private Void visitIfStmt(JmmNode ifStmt, SymbolTable ignored) {
        validateBooleanCondition(ifStmt.getChild(0), "IF");
        return null;
    }

    private Void visitWhileStmt(JmmNode whileStmt, SymbolTable ignored) {
        validateBooleanCondition(whileStmt.getChild(0), "WHILE");
        return null;
    }

    private Void visitDoWhileStmt(JmmNode doWhileStmt, SymbolTable ignored) {
        var conditions = doWhileStmt.getChildren(JmmKind.EXPR);
        if (!conditions.isEmpty()) {
            validateBooleanCondition(conditions.getFirst(), "DO-WHILE");
        }

        return null;
    }

    private Void visitForStmt(JmmNode forStmt, SymbolTable ignored) {
        var conditions = forStmt.getChildren(JmmKind.EXPR);
        if (!conditions.isEmpty()) {
            validateBooleanCondition(conditions.getFirst(), "FOR");
        }

        return null;
    }

    private Void visitForHeaderAssign(JmmNode forHeaderAssign, SymbolTable ignored) {
        var targetName = forHeaderAssign.get("var");

        if (forHeaderAssign.getNumChildren() == 1) {
            validateSimpleAssignment(forHeaderAssign, targetName, forHeaderAssign.getChild(0));
            return null;
        }

        validateIndexedAssignment(forHeaderAssign, targetName);
        return null;
    }

    private Void visitAssignStmt(JmmNode assignStmt, SymbolTable ignored) {
        validateSimpleAssignment(assignStmt, assignStmt.get("var"), assignStmt.getChild(0));
        return null;
    }

    private Void visitReturnStmt(JmmNode returnStmt, SymbolTable ignored) {
        var methodOpt = types.getEnclosingMethod(returnStmt);
        if (methodOpt.isEmpty()) {
            return null;
        }

        var method = methodOpt.get();
        var hasExpression = returnStmt.getNumChildren() > 0;

        if (TypeUtils.voidType().equals(method.returnType())) {
            if (hasExpression) {
                addReport(newError(returnStmt,
                        "Void method '" + method.name() + "' cannot return a value"));
            }

            return null;
        }

        if (!hasExpression) {
            addReport(newError(returnStmt,
                    "Method '" + method.name() + "' must return an expression of type '" +
                            method.returnType().print() + "'"));
            return null;
        }

        var exprType = types.getExprType(returnStmt.getChild(0));
        if (exprType != null && !types.isAssignable(method.returnType(), exprType)) {
            addReport(newError(returnStmt,
                    "Method '" + method.name() + "' returns '" + exprType.print() +
                            "' but expected '" + method.returnType().print() + "'"));
        }

        return null;
    }

    private Void visitBinaryExpr(JmmNode binaryExpr, SymbolTable ignored) {
        var leftType = types.getExprType(binaryExpr.getChild(0));
        var rightType = types.getExprType(binaryExpr.getChild(1));

        if (leftType == null || rightType == null) {
            return null;
        }

        var operator = binaryExpr.get("op");
        var valid = switch (operator) {
            case "+", "-", "*", "/", "%" -> isInt(leftType) && isInt(rightType);
            case "<", ">", "<=", ">=" -> isInt(leftType) && isInt(rightType);
            case "&&", "||" -> isBoolean(leftType) && isBoolean(rightType);
            case "==", "!=" -> types.areComparable(leftType, rightType);
            default -> true;
        };

        if (!valid) {
            addReport(newError(binaryExpr,
                    "Operator '" + operator + "' cannot be applied to '" + leftType.print() +
                            "' and '" + rightType.print() + "'"));
        }

        return null;
    }

    private Void visitArrayAssignStmt(JmmNode arrayAssignStmt, SymbolTable ignored) {
        validateIndexedAssignment(arrayAssignStmt, arrayAssignStmt.get("var"));
        return null;
    }

    private Void visitNotExpr(JmmNode notExpr, SymbolTable ignored) {
        var exprType = types.getExprType(notExpr.getChild(0));
        if (exprType != null && !isBoolean(exprType)) {
            addReport(newError(notExpr, "Operator '!' requires a boolean operand"));
        }

        return null;
    }

    private Void visitUnaryExpr(JmmNode unaryExpr, SymbolTable ignored) {
        var exprType = types.getExprType(unaryExpr.getChild(0));
        if (exprType != null && !isInt(exprType)) {
            addReport(newError(unaryExpr, "Operator '" + unaryExpr.get("op") + "' requires an integer operand"));
        }

        var operator = unaryExpr.get("op");
        if ((operator.equals("++") || operator.equals("--"))
                && !isAssignableEntity(unaryExpr.getChild(0))) {
            addReport(newError(unaryExpr, "Operator '" + operator + "' requires a variable or array access"));
        }

        return null;
    }

    private Void visitMethodCallExpr(JmmNode methodCallExpr, SymbolTable ignored) {
        var receiverType = types.getExprType(methodCallExpr.getChild(0));
        if (receiverType == null) {
            return null;
        }

        if (!receiverType.isClass()) {
            addReport(newError(methodCallExpr, "Method calls require a class receiver"));
            return null;
        }

        if (isCurrentClass(receiverType)) {
            validateCurrentClassMethodCall(methodCallExpr, 1, receiverType.asClass().staticRef());
            return null;
        }

        var resolvedOpt = types.resolveMethodCall(methodCallExpr);
        if (resolvedOpt.isEmpty()) {
            var receiverClassType = receiverType.asClass();
            var importedTableOpt = table.getImportedSymbolTable(receiverClassType.fullyQualifiedName());
            if (importedTableOpt.isPresent()) {
                var importedTable = importedTableOpt.get();
                var methodName = methodCallExpr.get("name");
                var methods = importedTable.getMethods(methodName);
                if (methods.isEmpty()) {
                    addReport(newError(methodCallExpr, "Method '" + methodName + "' does not exist in imported class '" + receiverClassType.fullyQualifiedName() + "'"));
                } else {
                    var argTypes = new java.util.ArrayList<pt.up.fe.comp.jmm.analysis.table.type.JmmType>();
                    for (int i = 1; i < methodCallExpr.getNumChildren(); i++) {
                        var argType = types.tryGetExprType(methodCallExpr.getChild(i));
                        if (argType.isEmpty()) {
                            addReport(newError(methodCallExpr, "Cannot determine type of argument " + (i)));
                            return null;
                        }
                        argTypes.add(argType.get());
                    }
                    boolean foundMatching = false;
                    for (var method : methods) {
                        if (method.parameters().size() != argTypes.size()) {
                            continue;
                        }
                        boolean allTypesMatch = true;
                        for (int i = 0; i < argTypes.size(); i++) {
                            if (!types.isAssignable(method.parameters().get(i).type(), argTypes.get(i))) {
                                allTypesMatch = false;
                                break;
                            }
                        }
                        if (allTypesMatch) {
                            foundMatching = true;
                            break;
                        }
                    }
                    if (!foundMatching) {
                        boolean anyWithSameCount = methods.stream().anyMatch(m -> m.parameters().size() == argTypes.size());
                        if (!anyWithSameCount) {
                            addReport(newError(methodCallExpr, "Wrong number of arguments for method '" + methodName + "' in imported class '" + receiverClassType.fullyQualifiedName() + "'"));
                        } else {
                            addReport(newError(methodCallExpr, "Wrong argument types for method '" + methodName + "' in imported class '" + receiverClassType.fullyQualifiedName() + "'"));
                        }
                    }
                }
            } else {
                addReport(newError(methodCallExpr, "Imported class '" + receiverClassType.fullyQualifiedName() + "' not found or has no symbol table"));
            }
        }

        return null;
    }

    private Void visitImplicitThisCallExpr(JmmNode implicitThisCallExpr, SymbolTable ignored) {
        validateCurrentClassMethodCall(
                implicitThisCallExpr,
                0,
                types.isStaticMethodContext(implicitThisCallExpr)
        );

        return null;
    }

    private Void visitArrayAccessExpr(JmmNode arrayAccessExpr, SymbolTable ignored) {
        var receiverType = types.getExprType(arrayAccessExpr.getChild(0));
        if (receiverType != null && !receiverType.isArray()) {
            addReport(newError(arrayAccessExpr, "Array access requires an array receiver"));
            return null;
        }

        var indexType = types.getExprType(arrayAccessExpr.getChild(1));
        if (indexType != null && !isInt(indexType)) {
            addReport(newError(arrayAccessExpr, "Array index expression must have type 'int'"));
        }

        return null;
    }

    private Void visitFieldAccessExpr(JmmNode fieldAccessExpr, SymbolTable ignored) {
        var receiverType = types.getExprType(fieldAccessExpr.getChild(0));
        if (receiverType == null) {
            return null;
        }

        if (!receiverType.isClass()) {
            addReport(newError(fieldAccessExpr, "Field access requires a class receiver"));
            return null;
        }

        if (types.resolveFieldAccessType(fieldAccessExpr).isEmpty()) {
            addReport(newError(fieldAccessExpr,
                    "Field '" + fieldAccessExpr.get("name") + "' is not available on receiver '" +
                            receiverType.print() + "'"));
        }

        return null;
    }

    private Void visitNewIntArrayExpr(JmmNode newArrayExpr, SymbolTable ignored) {
        for (int i = 0; i < newArrayExpr.getNumChildren(); i++) {
            var sizeType = types.getExprType(newArrayExpr.getChild(i));
            if (sizeType != null && !isInt(sizeType)) {
                addReport(newError(newArrayExpr.getChild(i), "Array size expression must have type 'int'"));
            }
        }

        return null;
    }

    private Void visitArrayInitializerExpr(JmmNode arrayInitializerExpr, SymbolTable ignored) {
        for (int i = 0; i < arrayInitializerExpr.getNumChildren(); i++) {
            var elementType = types.getExprType(arrayInitializerExpr.getChild(i));
            if (elementType != null && !isInt(elementType)) {
                addReport(newError(arrayInitializerExpr.getChild(i),
                        "Array initializer elements must have type 'int'"));
            }
        }

        return null;
    }

    private Void visitLengthExpr(JmmNode lengthExpr, SymbolTable ignored) {
        var receiverType = types.getExprType(lengthExpr.getChild(0));
        if (receiverType != null && !receiverType.isArray()) {
            addReport(newError(lengthExpr, "Expression '.length' requires an array receiver"));
        }

        return null;
    }

    private void validateSimpleAssignment(JmmNode assignmentNode, String targetName, JmmNode valueExpr) {
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

    private void validateIndexedAssignment(JmmNode assignmentNode, String targetName) {
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

    private TypeUtils.ResolvedIdentifier resolveAssignmentTarget(JmmNode assignmentNode, String targetName) {
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

    private void validateCurrentClassMethodCall(JmmNode callExpr, int firstArgIndex, boolean requireStatic) {
        var argTypesOpt = getArgumentTypes(callExpr, firstArgIndex);
        if (argTypesOpt.isEmpty()) {
            return;
        }

        var methodName = callExpr.get("name");
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

    private List<MethodSymbol> getCurrentAndInheritedMethods(String methodName) {
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

    private MethodSymbol findMatchingMethod(List<MethodSymbol> methods, List<JmmType> argTypes, boolean requireStatic) {
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

    private java.util.Optional<List<JmmType>> getArgumentTypes(JmmNode callExpr, int firstArgIndex) {
        var argTypes = new ArrayList<JmmType>();

        for (int i = firstArgIndex; i < callExpr.getNumChildren(); i++) {
            var argType = types.tryGetExprType(callExpr.getChild(i));
            if (argType.isEmpty()) {
                addReport(newError(callExpr, "Cannot determine type of argument " + (i - firstArgIndex + 1)));
                return java.util.Optional.empty();
            }

            argTypes.add(argType.get());
        }

        return java.util.Optional.of(argTypes);
    }

    private boolean isCurrentClass(JmmType receiverType) {
        if (!receiverType.isClass()) {
            return false;
        }

        var receiverClass = receiverType.asClass();
        return receiverClass.fullyQualifiedName().equals(table.getFullyQualifiedName())
                || receiverClass.name().equals(table.getClassName());
    }

    private boolean isBoolean(JmmType type) {
        return TypeUtils.booleanType().equals(type);
    }

    private void validateBooleanCondition(JmmNode condition, String statementName) {
        var conditionType = types.getExprType(condition);
        if (conditionType != null && !isBoolean(conditionType)) {
            addReport(newError(condition,
                    statementName + " condition must have type 'boolean'"));
        }
    }

    private boolean isInt(JmmType type) {
        return TypeUtils.intType().equals(type);
    }

    private boolean isAssignableEntity(JmmNode expr) {
        return JmmKind.VAR_REF_EXPR.check(expr) || JmmKind.ARRAY_ACCESS_EXPR.check(expr);
    }

    private JmmType getIndexedType(JmmNode arrayAssignStmt, JmmType baseType, String targetName) {
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
