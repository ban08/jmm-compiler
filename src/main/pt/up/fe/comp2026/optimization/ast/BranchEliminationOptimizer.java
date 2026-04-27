package pt.up.fe.comp2026.optimization.ast;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp2026.ast.NodeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;
import java.util.Optional;

import static pt.up.fe.comp2026.optimization.ast.AstOptimizationUtils.deepCopy;
import static pt.up.fe.comp2026.optimization.ast.AstOptimizationUtils.emptyCompound;
import static pt.up.fe.comp2026.optimization.ast.AstOptimizationUtils.readBoolean;
import static pt.up.fe.comp2026.optimization.ast.AstOptimizationUtils.replacementCopy;

/**
 * Removes branches whose condition is statically known after folding/propagation.
 */
public class BranchEliminationOptimizer {

    public boolean optimize(JmmNode root) {
        return eliminate(root);
    }

    private boolean eliminate(JmmNode node) {
        boolean changed = false;

        for (var child : new ArrayList<>(node.getChildren())) {
            changed |= eliminate(child);
        }

        if (JmmKind.IF_STMT.check(node)) {
            return eliminateIf(node) || changed;
        }

        if (JmmKind.WHILE_STMT.check(node)) {
            return eliminateWhile(node) || changed;
        }

        if (JmmKind.DO_WHILE_STMT.check(node)) {
            return eliminateDoWhile(node) || changed;
        }

        if (JmmKind.FOR_STMT.check(node)) {
            return eliminateFor(node) || changed;
        }

        return changed;
    }

    private boolean eliminateIf(JmmNode ifStmt) {
        var condition = readBoolean(ifStmt.getChild(0));
        if (condition.isEmpty()) {
            return false;
        }

        var branches = ifStmt.getChildren(JmmKind.STMT);
        if (condition.get()) {
            ifStmt.replace(replacementCopy(ifStmt, branches.get(0)));
            return true;
        }

        if (branches.size() > 1) {
            ifStmt.replace(replacementCopy(ifStmt, branches.get(1)));
        } else {
            ifStmt.replace(emptyCompound(ifStmt));
        }

        return true;
    }

    private boolean eliminateWhile(JmmNode whileStmt) {
        var condition = readBoolean(whileStmt.getChild(0));
        if (condition.isEmpty() || condition.get()) {
            return false;
        }

        whileStmt.replace(emptyCompound(whileStmt));
        return true;
    }

    private boolean eliminateDoWhile(JmmNode doWhileStmt) {
        var conditions = doWhileStmt.getChildren(JmmKind.EXPR);
        if (conditions.isEmpty()) {
            return false;
        }

        var condition = readBoolean(conditions.getFirst());
        if (condition.isEmpty() || condition.get()) {
            return false;
        }

        doWhileStmt.replace(replacementCopy(doWhileStmt, doWhileStmt.getChild(0)));
        return true;
    }

    private boolean eliminateFor(JmmNode forStmt) {
        var parts = splitFor(forStmt);
        if (parts.condition() == null) {
            return false;
        }

        var condition = readBoolean(parts.condition());
        if (condition.isEmpty() || condition.get()) {
            return false;
        }

        if (parts.init() == null) {
            forStmt.replace(emptyCompound(forStmt));
            return true;
        }

        var initStmt = simpleAssignmentFromHeader(parts.init());
        if (initStmt.isEmpty()) {
            return false;
        }

        var replacement = emptyCompound(forStmt);
        replacement.add(initStmt.get());
        forStmt.replace(replacement);
        return true;
    }

    private Optional<JmmNode> simpleAssignmentFromHeader(JmmNode header) {
        if (header.getNumChildren() != 1) {
            return Optional.empty();
        }

        var assign = new JmmNodeImpl(JmmKind.ASSIGN_STMT);
        NodeUtils.copyPositionAttributes(header, assign);
        assign.put(JmmAttributes.ASSIGN_STMT.VAR, header.get(JmmAttributes.FOR_HEADER_ASSIGN.VAR));
        assign.add(deepCopy(header.getChild(0)));
        return Optional.of(assign);
    }

    private ForParts splitFor(JmmNode forStmt) {
        JmmNode init = null;
        JmmNode condition = null;
        JmmNode update = null;
        JmmNode body = null;

        boolean firstHeaderSeen = false;
        for (var child : forStmt.getChildren()) {
            if (JmmKind.FOR_HEADER_ASSIGN.check(child)) {
                if (!firstHeaderSeen) {
                    init = child;
                    firstHeaderSeen = true;
                } else {
                    update = child;
                }
            } else if (JmmKind.EXPR.check(child)) {
                condition = child;
            } else if (JmmKind.STMT.check(child)) {
                body = child;
            }
        }

        return new ForParts(init, condition, update, body);
    }

    private record ForParts(JmmNode init, JmmNode condition, JmmNode update, JmmNode body) {
    }
}
