package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.AccessType;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Validates entity access, assignments and arithmetic/logical expressions.
 */
public class EntityOperationsValidation extends AnalysisVisitorWithTable {

    public EntityOperationsValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.TYPE, this::visitType);
        addVisit(JmmKind.NEW_EXPR, this::visitNewExpr);
        addVisit(JmmKind.VAR_REF_EXPR, this::visitVarRefExpr);
        addVisit(JmmKind.THIS_EXPR, this::visitThisExpr);
        addVisit(JmmKind.ASSIGN_STMT, this::visitAssignStmt);
        addVisit(JmmKind.ARRAY_ASSIGN_STMT, this::visitArrayAssignStmt);
        addVisit(JmmKind.BINARY_EXPR, this::visitBinaryExpr);
        addVisit(JmmKind.NOT_EXPR, this::visitNotExpr);
        addVisit(JmmKind.UNARY_EXPR, this::visitUnaryExpr);
        addVisit(JmmKind.METHOD_CALL_EXPR, this::visitMethodCallExpr);
        addVisit(JmmKind.FIELD_ACCESS_EXPR, this::visitFieldAccessExpr);
        addVisit(JmmKind.ARRAY_ACCESS_EXPR, this::visitArrayAccessExpr);
        addVisit(JmmKind.NEW_INT_ARRAY_EXPR, this::visitNewIntArrayExpr);
        addVisit(JmmKind.ARRAY_INITIALIZER_EXPR, this::visitArrayInitializerExpr);
        addVisit(JmmKind.LENGTH_EXPR, this::visitLengthExpr);
        addVisit(JmmKind.IF_STMT, this::visitIfStmt);
        addVisit(JmmKind.WHILE_STMT, this::visitWhileStmt);
        addVisit(JmmKind.FOR_STMT, this::visitForStmt);

        setDefaultVisit((node, st) -> null);
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

    private Void visitAssignStmt(JmmNode assignStmt, SymbolTable ignored) {
        var targetName = assignStmt.get("var");
        var target = types.resolveIdentifier(assignStmt, targetName);

        if (target.isEmpty()) {
            addReport(newError(assignStmt, "Identifier '" + targetName + "' is not declared"));
            return null;
        }

        if (target.get().accessType() == AccessType.IMPORT) {
            addReport(newError(assignStmt, "Cannot assign to imported class '" + targetName + "'"));
            return null;
        }

        if (target.get().accessType() == AccessType.FIELD && types.isStaticMethodContext(assignStmt)) {
            addReport(newError(assignStmt, "Field '" + targetName + "' cannot be assigned inside a static method"));
            return null;
        }

        var exprType = types.getExprType(assignStmt.getChild(0));
        if (exprType == null) {
            return null;
        }

        if (!types.isAssignable(target.get().type(), exprType)) {
            addReport(newError(assignStmt,
                    "Cannot assign expression of type '" + exprType.print() + "' to '" + targetName +
                            "' of type '" + target.get().type().print() + "'"));
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
        var targetName = arrayAssignStmt.get("var");
        var target = types.resolveIdentifier(arrayAssignStmt, targetName);

        if (target.isEmpty()) {
            addReport(newError(arrayAssignStmt, "Identifier '" + targetName + "' is not declared"));
            return null;
        }

        if (target.get().accessType() == AccessType.IMPORT) {
            addReport(newError(arrayAssignStmt, "Cannot assign to imported class '" + targetName + "'"));
            return null;
        }

        if (target.get().accessType() == AccessType.FIELD && types.isStaticMethodContext(arrayAssignStmt)) {
            addReport(newError(arrayAssignStmt, "Field '" + targetName + "' cannot be assigned inside a static method"));
            return null;
        }

        var indexedType = getIndexedType(arrayAssignStmt, target.get().type(), targetName);
        if (indexedType == null) {
            return null;
        }

        var valueType = types.getExprType(arrayAssignStmt.getChild(arrayAssignStmt.getNumChildren() - 1));
        if (valueType == null) {
            return null;
        }

        if (!types.isAssignable(indexedType, valueType)) {
            addReport(newError(arrayAssignStmt,
                    "Cannot assign expression of type '" + valueType.print() + "' to array slot of type '" +
                            indexedType.print() + "'"));
        }

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

    private Void visitIfStmt(JmmNode ifStmt, SymbolTable ignored) {
        // O primeiro filho (índice 0) é a condição
        var condition = ifStmt.getChild(0);
        var condType = types.getExprType(condition);

        if (condType != null && !isBoolean(condType)) {
            addReport(newError(ifStmt, "IF condition must be of type 'boolean', but is '" + condType.print() + "'"));
        }

        // Opcional: Se precisares de analisar o bloco 'then' ou 'else',
        // fazes o check de segurança:
        // JmmNode thenBlock = ifStmt.getChild(1);

        // Check para a extensão: else opcional
        if (ifStmt.getNumChildren() > 2) {
            // JmmNode elseBlock = ifStmt.getChild(2);
            // Validar algo no else se necessário
        }

        return null;
    }

    private Void visitWhileStmt(JmmNode whileStmt, SymbolTable ignored) {
        var condition = whileStmt.getChild(0);
        var condType = types.getExprType(condition);

        if (condType != null && !isBoolean(condType)) {
            addReport(newError(whileStmt, "WHILE condition must be of type 'boolean'"));
        }
        return null;
    }

    private Void visitForStmt(JmmNode forStmt, SymbolTable ignored) {
        // 1. Validar a condição (é um NÓ/JmmNode, não uma String)
        // Usamos getOptionalObject porque na gramática definimos (condition=expr)?
        var condition = forStmt.getOptionalObject("condition", JmmNode.class);

        if (condition.isPresent()) {
            JmmNode condNode = condition.get();
            var condType = types.getExprType(condNode);
            if (condType != null && !isBoolean(condType)) {
                addReport(newError(condNode, "FOR condition must be boolean"));
            }
        }

        // 2. Validar IDs (estes SIM são Strings/Atributos)
        forStmt.getOptional("initId").ifPresent(id -> this.myValidateId(forStmt, id));
        forStmt.getOptional("stepId").ifPresent(id -> this.myValidateId(forStmt, id));

        return null;
    }

    // Adiciona este método auxiliar no fim da tua classe (antes do último })
    private void myValidateId(JmmNode node, String id) {
        if (types.resolveIdentifier(node, id).isEmpty()) {
            addReport(newError(node, "Identifier '" + id + "' in FOR loop is not declared"));
        }
    }

    private boolean isBoolean(JmmType type) {
        return TypeUtils.booleanType().equals(type);
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
