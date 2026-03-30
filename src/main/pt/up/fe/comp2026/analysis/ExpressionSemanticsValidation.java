package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class ExpressionSemanticsValidation extends SemanticValidationPass {

    public ExpressionSemanticsValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.BINARY_EXPR, this::visitBinaryExpr);
        addVisit(JmmKind.NOT_EXPR, this::visitNotExpr);
        addVisit(JmmKind.UNARY_EXPR, this::visitUnaryExpr);
        addVisit(JmmKind.ARRAY_ACCESS_EXPR, this::visitArrayAccessExpr);
        addVisit(JmmKind.NEW_INT_ARRAY_EXPR, this::visitNewIntArrayExpr);
        addVisit(JmmKind.ARRAY_INITIALIZER_EXPR, this::visitArrayInitializerExpr);
        addVisit(JmmKind.LENGTH_EXPR, this::visitLengthExpr);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitBinaryExpr(JmmNode binaryExpr, SymbolTable ignored) {
        var leftType = types.getExprType(binaryExpr.getChild(0));
        var rightType = types.getExprType(binaryExpr.getChild(1));

        if (leftType == null || rightType == null) {
            return null;
        }

        var operator = binaryExpr.get(JmmAttributes.BINARY_EXPR.OP);
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
            addReport(newError(unaryExpr, "Operator '" + unaryExpr.get(JmmAttributes.UNARY_EXPR.OP) + "' requires an integer operand"));
        }

        var operator = unaryExpr.get(JmmAttributes.UNARY_EXPR.OP);
        if ((operator.equals("++") || operator.equals("--"))
                && !isAssignableEntity(unaryExpr.getChild(0))) {
            addReport(newError(unaryExpr, "Operator '" + operator + "' requires a variable or array access"));
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

    private Void visitNewIntArrayExpr(JmmNode newArrayExpr, SymbolTable ignored) {
        var dimensions = newArrayExpr.getChildren(JmmKind.ARRAY_CREATION_DIM);
        var sawUnsizedDimension = false;

        for (int i = 0; i < dimensions.size(); i++) {
            var dimension = dimensions.get(i);
            var sizeExprs = dimension.getChildren(JmmKind.EXPR);

            if (sizeExprs.isEmpty()) {
                if (i == 0) {
                    addReport(newError(dimension, "The first array dimension must specify a size"));
                }

                sawUnsizedDimension = true;
                continue;
            }

            var sizeExpr = sizeExprs.getFirst();
            var sizeType = types.getExprType(sizeExpr);
            if (sizeType != null && !isInt(sizeType)) {
                addReport(newError(sizeExpr, "Array size expression must have type 'int'"));
            }

            if (sawUnsizedDimension) {
                addReport(newError(dimension,
                        "Array dimensions with explicit sizes must come before unsized dimensions"));
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
}
