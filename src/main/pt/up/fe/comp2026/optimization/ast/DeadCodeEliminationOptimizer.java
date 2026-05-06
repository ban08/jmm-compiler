package pt.up.fe.comp2026.optimization.ast;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.AccessType;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static pt.up.fe.comp2026.optimization.ast.AstOptimizationUtils.isPureExpression;
import static pt.up.fe.comp2026.optimization.ast.AstOptimizationUtils.readBoolean;

/**
 * Removes statement-level code that cannot affect program behavior.
 */
public class DeadCodeEliminationOptimizer {

    private final TypeUtils types;

    public DeadCodeEliminationOptimizer(SymbolTable table) {
        this.types = new TypeUtils(table);
    }

    public boolean optimize(JmmNode root) {
        return eliminate(root);
    }

    private boolean eliminate(JmmNode node) {
        boolean changed = false;

        for (var child : new ArrayList<>(node.getChildren())) {
            changed |= eliminate(child);
        }

        changed |= pruneStatementChildren(node);
        changed |= pruneDeadStores(node);
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

    private boolean pruneDeadStores(JmmNode parent) {
        if (!canOwnStatementSequence(parent)) {
            return false;
        }

        return processStatementSequence(parent, new HashSet<>());
    }

    private boolean canOwnStatementSequence(JmmNode node) {
        return JmmKind.METHOD_DECL.check(node);
    }

    private boolean processStatementSequence(JmmNode parent, Set<String> liveAfter) {
        boolean changed = false;

        for (var child : parent.getChildren().reversed()) {
            if (JmmKind.STMT.check(child)) {
                changed |= processStatement(child, liveAfter);
            }
        }

        return changed;
    }

    private boolean processStatement(JmmNode statement, Set<String> liveAfter) {
        if (JmmKind.RETURN_STMT.check(statement)) {
            for (var child : statement.getChildren()) {
                collectLocalReads(child, liveAfter);
            }
            return false;
        }

        if (JmmKind.ASSIGN_STMT.check(statement)) {
            return processAssignment(statement, liveAfter);
        }

        if (JmmKind.ARRAY_ASSIGN_STMT.check(statement)) {
            return processArrayAssignment(statement, liveAfter);
        }

        if (JmmKind.EXPR_STMT.check(statement)) {
            for (var child : statement.getChildren()) {
                collectLocalReads(child, liveAfter);
            }
            return false;
        }

        if (JmmKind.IF_STMT.check(statement)) {
            return processIf(statement, liveAfter);
        }

        if (JmmKind.COMPOUND_STMT.check(statement)) {
            return processStatementSequence(statement, liveAfter);
        }

        collectLocalReads(statement, liveAfter);
        return false;
    }

    private boolean processAssignment(JmmNode statement, Set<String> liveAfter) {
        String name = statement.get(JmmAttributes.ASSIGN_STMT.VAR);
        var resolved = types.resolveIdentifier(statement, name);
        boolean localValue = resolved.map(identifier ->
                identifier.accessType() == AccessType.LOCAL || identifier.accessType() == AccessType.PARAM
        ).orElse(false);

        var rhs = statement.getChild(0);
        if (localValue && !liveAfter.contains(name) && canDropAssignmentRhs(rhs)) {
            statement.delete();
            return true;
        }

        if (localValue) {
            liveAfter.remove(name);
        }
        collectLocalReads(rhs, liveAfter);
        return false;
    }

    private boolean processArrayAssignment(JmmNode statement, Set<String> liveAfter) {
        String name = statement.get(JmmAttributes.ARRAY_ASSIGN_STMT.VAR);
        var resolved = types.resolveIdentifier(statement, name);
        boolean localArray = resolved.map(identifier -> identifier.accessType() == AccessType.LOCAL).orElse(false);

        if (localArray
                && !liveAfter.contains(name)
                && arrayAssignmentChildrenAreDroppable(statement)
                && isDefinitelyFreshLocalArrayAtStore(statement, name)) {
            statement.delete();
            return true;
        }

        resolved.ifPresent(identifier -> {
            if (identifier.accessType() == AccessType.LOCAL || identifier.accessType() == AccessType.PARAM) {
                liveAfter.add(name);
            }
        });
        for (var child : statement.getChildren()) {
            collectLocalReads(child, liveAfter);
        }
        return false;
    }

    private boolean processIf(JmmNode statement, Set<String> liveAfter) {
        var liveAfterIf = new HashSet<>(liveAfter);
        var branches = statement.getChildren(JmmKind.STMT);

        Set<String> thenLive = new HashSet<>(liveAfterIf);
        boolean changed = false;
        if (!branches.isEmpty()) {
            changed |= processStatement(branches.get(0), thenLive);
        }

        Set<String> elseLive = new HashSet<>(liveAfterIf);
        if (branches.size() > 1) {
            changed |= processStatement(branches.get(1), elseLive);
        }

        liveAfter.clear();
        liveAfter.addAll(thenLive);
        liveAfter.addAll(elseLive);
        collectLocalReads(statement.getChild(0), liveAfter);
        return changed;
    }

    private boolean canDropAssignmentRhs(JmmNode rhs) {
        if (isPureExpression(rhs)) {
            return true;
        }

        if (JmmKind.PAREN_EXPR.check(rhs)) {
            return canDropAssignmentRhs(rhs.getChild(0));
        }

        if (JmmKind.NEW_INT_ARRAY_EXPR.check(rhs)) {
            return rhs.getChildren(JmmKind.ARRAY_CREATION_DIM).stream()
                    .allMatch(dimension -> dimension.getNumChildren() == 0 || canDropAssignmentRhs(dimension.getChild(0)));
        }

        if (JmmKind.ARRAY_INITIALIZER_EXPR.check(rhs)) {
            return rhs.getChildren().stream().allMatch(this::canDropAssignmentRhs);
        }

        return false;
    }

    private boolean arrayAssignmentChildrenAreDroppable(JmmNode statement) {
        return statement.getChildren().stream().allMatch(this::canDropAssignmentRhs);
    }

    private boolean isDefinitelyFreshLocalArrayAtStore(JmmNode store, String name) {
        var parent = store.getParent();
        if (parent == null) {
            return false;
        }

        boolean fresh = false;
        boolean escaped = false;

        for (var sibling : parent.getChildren()) {
            if (sibling == store) {
                break;
            }

            if (!JmmKind.STMT.check(sibling)) {
                continue;
            }

            if (JmmKind.ASSIGN_STMT.check(sibling)
                    && sibling.get(JmmAttributes.ASSIGN_STMT.VAR).equals(name)) {
                fresh = isFreshArrayExpression(sibling.getChild(0));
                escaped = false;
                continue;
            }

            if (mayAssignName(sibling, name)) {
                fresh = false;
                escaped = false;
                continue;
            }

            if (fresh && statementMayExposeName(sibling, name)) {
                escaped = true;
            }
        }

        return fresh && !escaped;
    }

    private boolean isFreshArrayExpression(JmmNode node) {
        if (JmmKind.PAREN_EXPR.check(node)) {
            return isFreshArrayExpression(node.getChild(0));
        }

        return JmmKind.NEW_INT_ARRAY_EXPR.check(node) || JmmKind.ARRAY_INITIALIZER_EXPR.check(node);
    }

    private boolean mayAssignName(JmmNode node, String name) {
        if (JmmKind.ASSIGN_STMT.check(node)) {
            return node.get(JmmAttributes.ASSIGN_STMT.VAR).equals(name);
        }

        if (JmmKind.FOR_HEADER_ASSIGN.check(node)) {
            return node.get(JmmAttributes.FOR_HEADER_ASSIGN.VAR).equals(name);
        }

        for (var child : node.getChildren()) {
            if (mayAssignName(child, name)) {
                return true;
            }
        }

        return false;
    }

    private boolean statementMayExposeName(JmmNode node, String name) {
        if (JmmKind.ARRAY_ASSIGN_STMT.check(node)
                && node.get(JmmAttributes.ARRAY_ASSIGN_STMT.VAR).equals(name)) {
            for (var child : node.getChildren()) {
                if (expressionMayExposeName(child, name)) {
                    return true;
                }
            }

            return false;
        }

        return expressionMayExposeName(node, name);
    }

    private boolean expressionMayExposeName(JmmNode node, String name) {
        if (JmmKind.VAR_REF_EXPR.check(node)
                && node.get(JmmAttributes.VAR_REF_EXPR.NAME).equals(name)) {
            return true;
        }

        for (var child : node.getChildren()) {
            if (expressionMayExposeName(child, name)) {
                return true;
            }
        }

        return false;
    }

    private void collectLocalReads(JmmNode node, Set<String> reads) {
        if (JmmKind.VAR_REF_EXPR.check(node)) {
            String name = node.get(JmmAttributes.VAR_REF_EXPR.NAME);
            types.resolveIdentifier(node, name).ifPresent(identifier -> {
                if (identifier.accessType() == AccessType.LOCAL || identifier.accessType() == AccessType.PARAM) {
                    reads.add(name);
                }
            });
            return;
        }

        for (var child : node.getChildren()) {
            collectLocalReads(child, reads);
        }
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
