package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.ForStmtUtils;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.List;
import java.util.Optional;

public class ReturnPathValidation extends SemanticValidationPass {

    public ReturnPathValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable ignored) {
        var methodOpt = types.getEnclosingMethod(methodDecl);
        if (methodOpt.isEmpty()) {
            return null;
        }

        var method = methodOpt.get();
        if (!TypeUtils.voidType().equals(method.returnType())
                && !alwaysReturns(methodDecl.getChildren(JmmKind.STMT))) {
            addReport(newError(methodDecl,
                    "Method '" + method.name() + "' must return a value on every execution path"));
        }

        return null;
    }

    private boolean alwaysReturns(List<JmmNode> statements) {
        for (var statement : statements) {
            if (alwaysReturns(statement)) {
                return true;
            }
        }

        return false;
    }

    private boolean alwaysReturns(JmmNode statement) {
        if (JmmKind.RETURN_STMT.check(statement)) {
            return true;
        }

        if (JmmKind.COMPOUND_STMT.check(statement)) {
            return alwaysReturns(statement.getChildren(JmmKind.STMT));
        }

        if (JmmKind.IF_STMT.check(statement)) {
            var branches = statement.getChildren(JmmKind.STMT);
            return branches.size() == 2
                    && alwaysReturns(branches.get(0))
                    && alwaysReturns(branches.get(1));
        }

        if (JmmKind.DO_WHILE_STMT.check(statement)) {
            var bodyStatements = statement.getChildren(JmmKind.STMT);
            return isStaticallyTrueLoop(statement)
                    || (!bodyStatements.isEmpty() && alwaysReturns(bodyStatements.getFirst()));
        }

        if (JmmKind.WHILE_STMT.check(statement) || JmmKind.FOR_STMT.check(statement)) {
            // The current language has no break statement, so a statically-true loop
            // cannot complete normally even if the body does not contain an explicit return.
            return isStaticallyTrueLoop(statement);
        }

        return false;
    }

    private boolean isStaticallyTrueLoop(JmmNode loopStmt) {
        if (JmmKind.FOR_STMT.check(loopStmt)) {
            var condition = ForStmtUtils.split(loopStmt).condition();
            if (condition == null) {
                return true;
            }

            return tryEvaluateBooleanConstant(condition).orElse(false);
        }

        if (JmmKind.WHILE_STMT.check(loopStmt)) {
            return tryEvaluateBooleanConstant(loopStmt.getChild(0)).orElse(false);
        }

        if (JmmKind.DO_WHILE_STMT.check(loopStmt)) {
            var conditions = loopStmt.getChildren(JmmKind.EXPR);
            return !conditions.isEmpty()
                    && tryEvaluateBooleanConstant(conditions.getFirst()).orElse(false);
        }

        return false;
    }

    private Optional<Boolean> tryEvaluateBooleanConstant(JmmNode expr) {
        if (JmmKind.PAREN_EXPR.check(expr)) {
            return tryEvaluateBooleanConstant(expr.getChild(0));
        }

        if (JmmKind.BOOLEAN_LITERAL.check(expr)) {
            return Optional.of(Boolean.parseBoolean(expr.get(JmmAttributes.BOOLEAN_LITERAL.VALUE)));
        }

        if (JmmKind.NOT_EXPR.check(expr)) {
            return tryEvaluateBooleanConstant(expr.getChild(0)).map(value -> !value);
        }

        if (JmmKind.BINARY_EXPR.check(expr)) {
            var operator = expr.get(JmmAttributes.BINARY_EXPR.OP);
            var leftExpr = expr.getChild(0);
            var rightExpr = expr.getChild(1);

            return switch (operator) {
                case "&&" -> tryEvaluateLogicalAnd(leftExpr, rightExpr);
                case "||" -> tryEvaluateLogicalOr(leftExpr, rightExpr);
                case "<", ">", "<=", ">=" -> tryEvaluateComparison(leftExpr, rightExpr, operator);
                case "==", "!=" -> tryEvaluateEquality(leftExpr, rightExpr, operator);
                default -> Optional.empty();
            };
        }

        return Optional.empty();
    }

    private Optional<Integer> tryEvaluateIntConstant(JmmNode expr) {
        if (JmmKind.PAREN_EXPR.check(expr)) {
            return tryEvaluateIntConstant(expr.getChild(0));
        }

        if (JmmKind.INTEGER_LITERAL.check(expr)) {
            return Optional.of(Integer.parseInt(expr.get(JmmAttributes.INTEGER_LITERAL.VALUE)));
        }

        if (JmmKind.UNARY_EXPR.check(expr)) {
            var operator = expr.get(JmmAttributes.UNARY_EXPR.OP);
            var operand = tryEvaluateIntConstant(expr.getChild(0));

            return switch (operator) {
                case "+" -> operand;
                case "-" -> operand.map(value -> -value);
                default -> Optional.empty();
            };
        }

        if (!JmmKind.BINARY_EXPR.check(expr)) {
            return Optional.empty();
        }

        var operator = expr.get(JmmAttributes.BINARY_EXPR.OP);
        var left = tryEvaluateIntConstant(expr.getChild(0));
        var right = tryEvaluateIntConstant(expr.getChild(1));

        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }

        return switch (operator) {
            case "+" -> Optional.of(left.get() + right.get());
            case "-" -> Optional.of(left.get() - right.get());
            case "*" -> Optional.of(left.get() * right.get());
            case "/" -> right.get() == 0 ? Optional.empty() : Optional.of(left.get() / right.get());
            case "%" -> right.get() == 0 ? Optional.empty() : Optional.of(left.get() % right.get());
            default -> Optional.empty();
        };
    }

    private Optional<Boolean> tryEvaluateLogicalAnd(JmmNode leftExpr, JmmNode rightExpr) {
        var left = tryEvaluateBooleanConstant(leftExpr);
        if (left.isPresent() && !left.get()) {
            return Optional.of(false);
        }

        var right = tryEvaluateBooleanConstant(rightExpr);
        if (right.isPresent() && !right.get()) {
            return Optional.of(false);
        }

        if (left.isPresent() && left.get()) {
            return right;
        }

        if (right.isPresent() && right.get()) {
            return left;
        }

        return Optional.empty();
    }

    private Optional<Boolean> tryEvaluateLogicalOr(JmmNode leftExpr, JmmNode rightExpr) {
        var left = tryEvaluateBooleanConstant(leftExpr);
        if (left.isPresent() && left.get()) {
            return Optional.of(true);
        }

        var right = tryEvaluateBooleanConstant(rightExpr);
        if (right.isPresent() && right.get()) {
            return Optional.of(true);
        }

        if (left.isPresent() && !left.get()) {
            return right;
        }

        if (right.isPresent() && !right.get()) {
            return left;
        }

        return Optional.empty();
    }

    private Optional<Boolean> tryEvaluateComparison(JmmNode leftExpr, JmmNode rightExpr, String operator) {
        var left = tryEvaluateIntConstant(leftExpr);
        var right = tryEvaluateIntConstant(rightExpr);

        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(switch (operator) {
            case "<" -> left.get() < right.get();
            case ">" -> left.get() > right.get();
            case "<=" -> left.get() <= right.get();
            case ">=" -> left.get() >= right.get();
            default -> throw new IllegalArgumentException("Unexpected comparison operator '" + operator + "'");
        });
    }

    private Optional<Boolean> tryEvaluateEquality(JmmNode leftExpr, JmmNode rightExpr, String operator) {
        var leftBool = tryEvaluateBooleanConstant(leftExpr);
        var rightBool = tryEvaluateBooleanConstant(rightExpr);
        if (leftBool.isPresent() && rightBool.isPresent()) {
            return Optional.of(operator.equals("==")
                    ? leftBool.get().equals(rightBool.get())
                    : !leftBool.get().equals(rightBool.get()));
        }

        var leftInt = tryEvaluateIntConstant(leftExpr);
        var rightInt = tryEvaluateIntConstant(rightExpr);
        if (leftInt.isPresent() && rightInt.isPresent()) {
            return Optional.of(operator.equals("==")
                    ? leftInt.get().equals(rightInt.get())
                    : !leftInt.get().equals(rightInt.get()));
        }

        return Optional.empty();
    }
}
