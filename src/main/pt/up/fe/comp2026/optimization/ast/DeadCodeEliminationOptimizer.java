package pt.up.fe.comp2026.optimization.ast;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;

import static pt.up.fe.comp2026.optimization.ast.AstOptimizationUtils.isPureExpression;
import static pt.up.fe.comp2026.optimization.ast.AstOptimizationUtils.readBoolean;

/**
 * Removes statement-level code that cannot affect program behavior.
 */
public class DeadCodeEliminationOptimizer {

    public boolean optimize(JmmNode root) {
        return eliminate(root);
    }

    private boolean eliminate(JmmNode node) {
        boolean changed = false;

        for (var child : new ArrayList<>(node.getChildren())) {
            changed |= eliminate(child);
        }

        changed |= pruneStatementChildren(node);
        return changed;
    }

    private boolean pruneStatementChildren(JmmNode parent) {
        boolean changed = false;
        boolean unreachable = false;

        for (var child : new ArrayList<>(parent.getChildren())) {
            if (!JmmKind.STMT.check(child)) {
                continue;
            }

            if (unreachable) {
                child.delete();
                changed = true;
                continue;
            }

            if (isDeadExpressionStatement(child)) {
                child.delete();
                changed = true;
                continue;
            }

            if (statementCannotCompleteNormally(child)) {
                unreachable = true;
            }
        }

        return changed;
    }

    private boolean isDeadExpressionStatement(JmmNode statement) {
        return JmmKind.EXPR_STMT.check(statement)
                && statement.getNumChildren() == 1
                && isPureExpression(statement.getChild(0));
    }

    private boolean statementCannotCompleteNormally(JmmNode statement) {
        if (JmmKind.RETURN_STMT.check(statement)) {
            return true;
        }

        if (JmmKind.COMPOUND_STMT.check(statement)) {
            for (var child : statement.getChildren()) {
                if (JmmKind.STMT.check(child) && statementCannotCompleteNormally(child)) {
                    return true;
                }
            }

            return false;
        }

        if (JmmKind.IF_STMT.check(statement)) {
            var branches = statement.getChildren(JmmKind.STMT);
            return branches.size() == 2
                    && statementCannotCompleteNormally(branches.get(0))
                    && statementCannotCompleteNormally(branches.get(1));
        }

        if (JmmKind.WHILE_STMT.check(statement)) {
            return readBoolean(statement.getChild(0)).orElse(false);
        }

        if (JmmKind.DO_WHILE_STMT.check(statement)) {
            var conditions = statement.getChildren(JmmKind.EXPR);
            return !conditions.isEmpty() && readBoolean(conditions.getFirst()).orElse(false);
        }

        if (JmmKind.FOR_STMT.check(statement)) {
            var conditions = statement.getChildren(JmmKind.EXPR);
            return conditions.isEmpty() || readBoolean(conditions.getFirst()).orElse(false);
        }

        return false;
    }
}
