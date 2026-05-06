package pt.up.fe.comp2026.optimization.ast;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.AccessType;
import pt.up.fe.comp2026.ast.ForStmtUtils;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static pt.up.fe.comp2026.optimization.ast.AstOptimizationUtils.Constant;
import static pt.up.fe.comp2026.optimization.ast.AstOptimizationUtils.literalNode;
import static pt.up.fe.comp2026.optimization.ast.AstOptimizationUtils.readLiteral;

/**
 * Propagates known int/boolean constants through local and parameter reads.
 */
public class ConstantPropagationOptimizer {

    private final TypeUtils types;
    private boolean changed;

    public ConstantPropagationOptimizer(SymbolTable table) {
        this.types = new TypeUtils(table);
    }

    public boolean optimize(JmmNode root) {
        changed = false;

        for (var method : new ArrayList<>(root.getDescendants(JmmKind.METHOD_DECL))) {
            processStatementChildren(method, new HashMap<>());
        }

        return changed;
    }

    private Map<String, Constant> processStatementChildren(JmmNode parent, Map<String, Constant> env) {
        for (var child : new ArrayList<>(parent.getChildren())) {
            if (JmmKind.STMT.check(child)) {
                env = processStatement(child, env);
            }
        }

        return env;
    }

    private Map<String, Constant> processStatement(JmmNode stmt, Map<String, Constant> env) {
        if (JmmKind.COMPOUND_STMT.check(stmt)) {
            return processStatementChildren(stmt, env);
        }

        if (JmmKind.ASSIGN_STMT.check(stmt)) {
            return processSimpleAssignment(stmt, stmt.get(JmmAttributes.ASSIGN_STMT.VAR), stmt.getChild(0), env);
        }

        if (JmmKind.ARRAY_ASSIGN_STMT.check(stmt)) {
            for (var child : new ArrayList<>(stmt.getChildren())) {
                env = optimizeExpressionAndInvalidate(child, env);
            }
            return env;
        }

        if (JmmKind.RETURN_STMT.check(stmt) || JmmKind.EXPR_STMT.check(stmt)) {
            for (var child : new ArrayList<>(stmt.getChildren())) {
                env = optimizeExpressionAndInvalidate(child, env);
            }
            return env;
        }

        if (JmmKind.IF_STMT.check(stmt)) {
            env = optimizeExpressionAndInvalidate(stmt.getChild(0), env);

            var branches = stmt.getChildren(JmmKind.STMT);
            var thenEnv = processStatement(branches.get(0), new HashMap<>(env));
            var elseEnv = branches.size() > 1
                    ? processStatement(branches.get(1), new HashMap<>(env))
                    : new HashMap<>(env);

            return meet(thenEnv, elseEnv);
        }

        if (JmmKind.WHILE_STMT.check(stmt)) {
            var assignedInLoop = assignedNames(stmt.getChild(1));
            assignedInLoop.addAll(assignedExpressionNames(stmt.getChild(0)));
            var loopEnv = without(env, assignedInLoop);

            optimizeExpressionAndInvalidate(stmt.getChild(0), loopEnv);
            processStatement(stmt.getChild(1), new HashMap<>(loopEnv));

            return without(env, assignedInLoop);
        }

        if (JmmKind.DO_WHILE_STMT.check(stmt)) {
            var body = stmt.getChild(0);
            var assignedInLoop = assignedNames(body);
            var conditions = stmt.getChildren(JmmKind.EXPR);
            if (!conditions.isEmpty()) {
                assignedInLoop.addAll(assignedExpressionNames(conditions.getFirst()));
            }
            var loopEnv = without(env, assignedInLoop);

            processStatement(body, new HashMap<>(loopEnv));

            if (!conditions.isEmpty()) {
                optimizeExpressionAndInvalidate(conditions.getFirst(), loopEnv);
            }

            return without(env, assignedInLoop);
        }

        if (JmmKind.FOR_STMT.check(stmt)) {
            return processForStatement(stmt, env);
        }

        return env;
    }

    private Map<String, Constant> processForStatement(JmmNode forStmt, Map<String, Constant> env) {
        var parts = ForStmtUtils.split(forStmt);

        if (parts.init() != null) {
            env = processForHeaderAssign(parts.init(), env);
        }

        var assignedInLoop = new HashSet<String>();
        if (parts.body() != null) {
            assignedInLoop.addAll(assignedNames(parts.body()));
        }
        if (parts.condition() != null) {
            assignedInLoop.addAll(assignedExpressionNames(parts.condition()));
        }
        if (parts.update() != null) {
            assignedInLoop.addAll(assignedNames(parts.update()));
        }

        var loopEnv = without(env, assignedInLoop);

        if (parts.condition() != null) {
            optimizeExpressionAndInvalidate(parts.condition(), loopEnv);
        }
        if (parts.body() != null) {
            processStatement(parts.body(), new HashMap<>(loopEnv));
        }
        if (parts.update() != null) {
            processForHeaderAssign(parts.update(), new HashMap<>(loopEnv));
        }

        return without(env, assignedInLoop);
    }

    private Map<String, Constant> processForHeaderAssign(JmmNode header, Map<String, Constant> env) {
        if (header.getNumChildren() != 1) {
            for (var child : new ArrayList<>(header.getChildren())) {
                env = optimizeExpressionAndInvalidate(child, env);
            }
            return env;
        }

        return processSimpleAssignment(header, header.get(JmmAttributes.FOR_HEADER_ASSIGN.VAR), header.getChild(0), env);
    }

    private Map<String, Constant> processSimpleAssignment(
            JmmNode context,
            String targetName,
            JmmNode rhs,
            Map<String, Constant> env) {

        env = optimizeExpressionAndInvalidate(rhs, env);

        if (!isTrackableLocalOrParam(context, targetName)) {
            return env;
        }

        var literal = readLiteral(rhs);
        if (literal.isPresent()) {
            env.put(targetName, literal.get());
        } else {
            env.remove(targetName);
        }

        return env;
    }

    private Map<String, Constant> optimizeExpressionAndInvalidate(JmmNode expr, Map<String, Constant> env) {
        var assignedNames = assignedExpressionNames(expr);
        var expressionEnv = without(env, assignedNames);
        optimizeExpression(expr, expressionEnv);
        return without(env, assignedNames);
    }

    private void optimizeExpression(JmmNode expr, Map<String, Constant> env) {
        if (isMutatingUnary(expr)) {
            return;
        }

        for (var child : new ArrayList<>(expr.getChildren())) {
            optimizeExpression(child, env);
        }

        if (!JmmKind.VAR_REF_EXPR.check(expr)) {
            return;
        }

        var name = expr.get(JmmAttributes.VAR_REF_EXPR.NAME);
        var constant = env.get(name);
        if (constant == null || !isTrackableLocalOrParam(expr, name)) {
            return;
        }

        expr.replace(literalNode(expr, constant));
        changed = true;
    }

    private boolean isTrackableLocalOrParam(JmmNode context, String name) {
        var resolved = types.resolveIdentifier(context, name);
        if (resolved.isEmpty()) {
            return false;
        }

        var accessType = resolved.get().accessType();
        if (accessType != AccessType.LOCAL && accessType != AccessType.PARAM) {
            return false;
        }

        var type = resolved.get().type();
        return TypeUtils.intType().equals(type) || TypeUtils.booleanType().equals(type);
    }

    private Map<String, Constant> meet(Map<String, Constant> first, Map<String, Constant> second) {
        var result = new HashMap<String, Constant>();

        for (var entry : first.entrySet()) {
            var other = second.get(entry.getKey());
            if (entry.getValue().equals(other)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    private Map<String, Constant> without(Map<String, Constant> env, Set<String> names) {
        var result = new HashMap<>(env);
        for (var name : names) {
            result.remove(name);
        }
        return result;
    }

    private Set<String> assignedNames(JmmNode node) {
        var names = new HashSet<String>();

        if (JmmKind.ASSIGN_STMT.check(node)) {
            names.add(node.get(JmmAttributes.ASSIGN_STMT.VAR));
        } else if (JmmKind.ARRAY_ASSIGN_STMT.check(node)) {
            names.add(node.get(JmmAttributes.ARRAY_ASSIGN_STMT.VAR));
        } else if (JmmKind.FOR_HEADER_ASSIGN.check(node)) {
            names.add(node.get(JmmAttributes.FOR_HEADER_ASSIGN.VAR));
        } else if (isMutatingUnary(node)) {
            assignedNameFromMutatingUnary(node).ifPresent(names::add);
        }

        for (var child : node.getChildren()) {
            names.addAll(assignedNames(child));
        }

        return names;
    }

    private Set<String> assignedExpressionNames(JmmNode node) {
        var names = new HashSet<String>();

        if (isMutatingUnary(node)) {
            assignedNameFromMutatingUnary(node).ifPresent(names::add);
        }

        for (var child : node.getChildren()) {
            names.addAll(assignedExpressionNames(child));
        }

        return names;
    }

    private boolean isMutatingUnary(JmmNode node) {
        if (!JmmKind.UNARY_EXPR.check(node)) {
            return false;
        }

        var operator = node.get(JmmAttributes.UNARY_EXPR.OP);
        return "++".equals(operator) || "--".equals(operator);
    }

    private Optional<String> assignedNameFromMutatingUnary(JmmNode node) {
        var target = unwrapParen(node.getChild(0));
        if (!JmmKind.VAR_REF_EXPR.check(target)) {
            return Optional.empty();
        }

        return Optional.of(target.get(JmmAttributes.VAR_REF_EXPR.NAME));
    }

    private JmmNode unwrapParen(JmmNode node) {
        var current = node;
        while (JmmKind.PAREN_EXPR.check(current)) {
            current = current.getChild(0);
        }

        return current;
    }

}
