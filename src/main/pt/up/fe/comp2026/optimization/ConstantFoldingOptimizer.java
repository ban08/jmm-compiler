package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;
import java.util.Optional;

import static pt.up.fe.comp2026.optimization.AstOptimizationUtils.booleanLiteral;
import static pt.up.fe.comp2026.optimization.AstOptimizationUtils.integerLiteral;
import static pt.up.fe.comp2026.optimization.AstOptimizationUtils.readBoolean;
import static pt.up.fe.comp2026.optimization.AstOptimizationUtils.readInteger;
import static pt.up.fe.comp2026.optimization.AstOptimizationUtils.replacementCopy;

/**
 * Small AST optimization pass for side-effect-free constant expressions.
 */
public class ConstantFoldingOptimizer {

    public boolean optimize(JmmNode root) {
        boolean changedAny = false;
        boolean changed;

        do {
            changed = fold(root);
            changedAny |= changed;
        } while (changed);

        return changedAny;
    }

    private boolean fold(JmmNode node) {
        boolean changed = false;

        for (var child : new ArrayList<>(node.getChildren())) {
            changed |= fold(child);
        }

        var replacement = foldExpression(node);
        if (replacement.isPresent()) {
            node.replace(replacement.get());
            return true;
        }

        return changed;
    }

    private Optional<JmmNode> foldExpression(JmmNode node) {
        if (JmmKind.PAREN_EXPR.check(node)) {
            return Optional.of(replacementCopy(node, node.getChild(0)));
        }

        if (JmmKind.NOT_EXPR.check(node)) {
            var value = readBoolean(node.getChild(0));
            return value.map(v -> booleanLiteral(node, !v));
        }

        if (JmmKind.UNARY_EXPR.check(node)) {
            return foldUnary(node);
        }

        if (JmmKind.BINARY_EXPR.check(node)) {
            return foldBinary(node);
        }

        return Optional.empty();
    }

    private Optional<JmmNode> foldUnary(JmmNode node) {
        var operator = node.get(JmmAttributes.UNARY_EXPR.OP);
        var value = readInteger(node.getChild(0));

        if (value.isEmpty()) {
            return Optional.empty();
        }

        return switch (operator) {
            case "+" -> Optional.of(integerLiteral(node, value.get()));
            case "-" -> Optional.of(integerLiteral(node, -value.get()));
            default -> Optional.empty();
        };
    }

    private Optional<JmmNode> foldBinary(JmmNode node) {
        var operator = node.get(JmmAttributes.BINARY_EXPR.OP);

        var foldedInteger = foldIntegerBinary(node, operator);
        if (foldedInteger.isPresent()) {
            return foldedInteger;
        }

        var foldedBoolean = foldBooleanBinary(node, operator);
        if (foldedBoolean.isPresent()) {
            return foldedBoolean;
        }

        return foldBooleanIdentities(node, operator);
    }

    private Optional<JmmNode> foldIntegerBinary(JmmNode node, String operator) {
        var left = readInteger(node.getChild(0));
        var right = readInteger(node.getChild(1));

        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }

        int lhs = left.get();
        int rhs = right.get();

        return switch (operator) {
            case "+" -> Optional.of(integerLiteral(node, lhs + rhs));
            case "-" -> Optional.of(integerLiteral(node, lhs - rhs));
            case "*" -> Optional.of(integerLiteral(node, lhs * rhs));
            case "/" -> rhs == 0 ? Optional.empty() : Optional.of(integerLiteral(node, lhs / rhs));
            case "%" -> rhs == 0 ? Optional.empty() : Optional.of(integerLiteral(node, lhs % rhs));
            case "<" -> Optional.of(booleanLiteral(node, lhs < rhs));
            case ">" -> Optional.of(booleanLiteral(node, lhs > rhs));
            case "<=" -> Optional.of(booleanLiteral(node, lhs <= rhs));
            case ">=" -> Optional.of(booleanLiteral(node, lhs >= rhs));
            case "==" -> Optional.of(booleanLiteral(node, lhs == rhs));
            case "!=" -> Optional.of(booleanLiteral(node, lhs != rhs));
            default -> Optional.empty();
        };
    }

    private Optional<JmmNode> foldBooleanBinary(JmmNode node, String operator) {
        var left = readBoolean(node.getChild(0));
        var right = readBoolean(node.getChild(1));

        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }

        boolean lhs = left.get();
        boolean rhs = right.get();

        return switch (operator) {
            case "&&" -> Optional.of(booleanLiteral(node, lhs && rhs));
            case "||" -> Optional.of(booleanLiteral(node, lhs || rhs));
            case "==" -> Optional.of(booleanLiteral(node, lhs == rhs));
            case "!=" -> Optional.of(booleanLiteral(node, lhs != rhs));
            default -> Optional.empty();
        };
    }

    private Optional<JmmNode> foldBooleanIdentities(JmmNode node, String operator) {
        if (!"&&".equals(operator) && !"||".equals(operator)) {
            return Optional.empty();
        }

        var left = readBoolean(node.getChild(0));
        if (left.isPresent()) {
            return switch (operator) {
                case "&&" -> Optional.of(left.get()
                        ? replacementCopy(node, node.getChild(1))
                        : booleanLiteral(node, false));
                case "||" -> Optional.of(left.get()
                        ? booleanLiteral(node, true)
                        : replacementCopy(node, node.getChild(1)));
                default -> Optional.empty();
            };
        }

        var right = readBoolean(node.getChild(1));
        if (right.isEmpty()) {
            return Optional.empty();
        }

        return switch (operator) {
            case "&&" -> right.get()
                    ? Optional.of(replacementCopy(node, node.getChild(0)))
                    : Optional.empty();
            case "||" -> right.get()
                    ? Optional.empty()
                    : Optional.of(replacementCopy(node, node.getChild(0)));
            default -> Optional.empty();
        };
    }
}
