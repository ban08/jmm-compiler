package pt.up.fe.comp2026.optimization.ast;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp2026.ast.NodeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.Optional;

final class AstOptimizationUtils {

    enum ConstantKind {
        INT,
        BOOLEAN
    }

    record Constant(ConstantKind kind, int intValue, boolean booleanValue) {
        static Constant ofInt(int value) {
            return new Constant(ConstantKind.INT, value, false);
        }

        static Constant ofBoolean(boolean value) {
            return new Constant(ConstantKind.BOOLEAN, 0, value);
        }
    }

    private AstOptimizationUtils() {
    }

    static Optional<Constant> readLiteral(JmmNode node) {
        if (JmmKind.INTEGER_LITERAL.check(node)) {
            return readInteger(node).map(Constant::ofInt);
        }

        if (JmmKind.BOOLEAN_LITERAL.check(node)) {
            return readBoolean(node).map(Constant::ofBoolean);
        }

        return Optional.empty();
    }

    static Optional<Integer> readInteger(JmmNode node) {
        if (!JmmKind.INTEGER_LITERAL.check(node)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(node.get(JmmAttributes.INTEGER_LITERAL.VALUE)));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    static Optional<Boolean> readBoolean(JmmNode node) {
        if (!JmmKind.BOOLEAN_LITERAL.check(node)) {
            return Optional.empty();
        }

        return Optional.of(Boolean.parseBoolean(node.get(JmmAttributes.BOOLEAN_LITERAL.VALUE)));
    }

    static JmmNode literalNode(JmmNode source, Constant constant) {
        return switch (constant.kind()) {
            case INT -> integerLiteral(source, constant.intValue());
            case BOOLEAN -> booleanLiteral(source, constant.booleanValue());
        };
    }

    static JmmNode integerLiteral(JmmNode source, int value) {
        var literal = new JmmNodeImpl(JmmKind.INTEGER_LITERAL);
        literal.put(JmmAttributes.INTEGER_LITERAL.VALUE, Integer.toString(value));
        NodeUtils.copyPositionAttributes(source, literal);
        return literal;
    }

    static JmmNode booleanLiteral(JmmNode source, boolean value) {
        var literal = new JmmNodeImpl(JmmKind.BOOLEAN_LITERAL);
        literal.put(JmmAttributes.BOOLEAN_LITERAL.VALUE, Boolean.toString(value));
        NodeUtils.copyPositionAttributes(source, literal);
        return literal;
    }

    static JmmNode replacementCopy(JmmNode source, JmmNode replacement) {
        var copy = deepCopy(replacement);
        NodeUtils.copyPositionAttributes(source, copy);
        return copy;
    }

    static JmmNode deepCopy(JmmNode node) {
        var copy = node.copy();
        for (var child : node.getChildren()) {
            copy.add(deepCopy(child));
        }
        return copy;
    }

    static JmmNode emptyCompound(JmmNode source) {
        var compound = new JmmNodeImpl(JmmKind.COMPOUND_STMT);
        NodeUtils.copyPositionAttributes(source, compound);
        return compound;
    }

    static boolean isPureExpression(JmmNode node) {
        if (JmmKind.PAREN_EXPR.check(node)) {
            return isPureExpression(node.getChild(0));
        }

        if (JmmKind.INTEGER_LITERAL.check(node)
                || JmmKind.BOOLEAN_LITERAL.check(node)
                || JmmKind.VAR_REF_EXPR.check(node)
                || JmmKind.THIS_EXPR.check(node)) {
            return true;
        }

        if (JmmKind.NOT_EXPR.check(node)) {
            return isPureExpression(node.getChild(0));
        }

        if (JmmKind.UNARY_EXPR.check(node)) {
            var operator = node.get(JmmAttributes.UNARY_EXPR.OP);
            return ("+".equals(operator) || "-".equals(operator)) && isPureExpression(node.getChild(0));
        }

        if (JmmKind.BINARY_EXPR.check(node)) {
            var operator = node.get(JmmAttributes.BINARY_EXPR.OP);
            if ("/".equals(operator) || "%".equals(operator)) {
                return false;
            }

            return isPureExpression(node.getChild(0)) && isPureExpression(node.getChild(1));
        }

        return false;
    }
}
