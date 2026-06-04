package core.optimization;

import org.junit.Test;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;
import pt.up.fe.comp2026.optimization.JmmOptimizationImpl;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MyAstOptimizationTest extends JmmTestEnv {

    public MyAstOptimizationTest() {
        super("", "", Map.of("optimize", "true", "registerAllocation", "-1"));
    }

    @Test
    public void propagatesAndFoldsLocalConstantsToReturn() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        int y;
                        x = 2 + 3;
                        y = x * 4;
                        return y;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("Expected the propagated return expression to be an integer literal",
                JmmKind.INTEGER_LITERAL.check(returnExpr));
        assertEquals("Expected x/y constants to fold to 20",
                "20", returnExpr.get(JmmAttributes.INTEGER_LITERAL.VALUE));
    }

    @Test
    public void doesNotPropagateFields() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int x;

                    int method() {
                        x = 1;
                        return x;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("Field reads must not be replaced by propagated constants",
                JmmKind.VAR_REF_EXPR.check(returnExpr));
        assertEquals("Expected the field read name to stay intact",
                "x", returnExpr.get(JmmAttributes.VAR_REF_EXPR.NAME));
    }

    @Test
    public void doesNotFreezeVariablesAssignedInsideLoops() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        x = 0;
                        while (x < 3) {
                            x = x + 1;
                        }
                        return x;
                    }
                }
                """);

        var whileStmt = method.getDescendants(JmmKind.WHILE_STMT).getFirst();
        var conditionRefs = whileStmt.getChild(0).getDescendants(JmmKind.VAR_REF_EXPR);
        assertFalse("The loop condition must keep reading x at runtime", conditionRefs.isEmpty());
        assertEquals("Expected the loop condition to still read x",
                "x", conditionRefs.getFirst().get(JmmAttributes.VAR_REF_EXPR.NAME));
    }

    @Test
    public void eliminatesConstantBranchesAndUnreachableTail() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        x = 1;
                        if (x < 2) {
                            return 3;
                        } else {
                            return 4;
                        }
                        return 5;
                    }
                }
                """);

        var returns = method.getDescendants(JmmKind.RETURN_STMT);
        assertEquals("Only the reachable return should remain", 1, returns.size());

        var returnExpr = returns.getFirst().getChild(0);
        assertTrue("Expected the reachable branch return to be a literal",
                JmmKind.INTEGER_LITERAL.check(returnExpr));
        assertEquals("Expected the true branch return to remain",
                "3", returnExpr.get(JmmAttributes.INTEGER_LITERAL.VALUE));
    }

    @Test
    public void keepsDivisionByZeroAsRuntimeExpression() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        return 1 / 0;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("Division by zero should not be folded away",
                JmmKind.BINARY_EXPR.check(returnExpr));
        assertEquals("Expected the division operator to remain",
                "/", returnExpr.get(JmmAttributes.BINARY_EXPR.OP));
    }

    @Test
    public void keepsDiscardedDivisionExpression() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        1 / 0;
                        return 1;
                    }
                }
                """);

        var divisions = method.getDescendants(JmmKind.BINARY_EXPR).stream()
                .filter(expr -> expr.get(JmmAttributes.BINARY_EXPR.OP).equals("/"))
                .toList();

        assertEquals("A discarded division expression may still fail at runtime", 1, divisions.size());
    }

    @Test
    public void doesNotPropagateAcrossIncrementExpressionStatement() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        x = 1;
                        ++x;
                        return x;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("Increment mutates x, so the later return must still read x",
                JmmKind.VAR_REF_EXPR.check(returnExpr));
        assertEquals("Expected the return expression to still read x",
                "x", returnExpr.get(JmmAttributes.VAR_REF_EXPR.NAME));
    }

    @Test
    public void doesNotPropagateAcrossParenthesizedIncrementExpressionStatement() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        x = 1;
                        ++(x);
                        return x;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("Parenthesized increment still mutates x, so the later return must still read x",
                JmmKind.VAR_REF_EXPR.check(returnExpr));
        assertEquals("Expected the return expression to still read x",
                "x", returnExpr.get(JmmAttributes.VAR_REF_EXPR.NAME));
    }

    @Test
    public void doesNotRewriteIncrementOperandToLiteral() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        x = 1;
                        return ++x;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("Expected the return expression to remain a mutating unary expression",
                JmmKind.UNARY_EXPR.check(returnExpr));
        assertTrue("The operand of ++ must remain assignable",
                JmmKind.VAR_REF_EXPR.check(returnExpr.getChild(0)));
    }

    @Test
    public void invalidatesVariablesMutatedInConditions() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        x = 1;
                        if (++x < 3) {
                        }
                        return x;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("The if condition mutates x, so the later return must not become literal 1",
                JmmKind.VAR_REF_EXPR.check(returnExpr));
    }

    @Test
    public void doWhileFalseRunsBodyOnce() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        x = 1;
                        do {
                            x = 2;
                        } while (false);
                        return x;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("do-while(false) should keep the body once and propagate its assignment",
                JmmKind.INTEGER_LITERAL.check(returnExpr));
        assertEquals("Expected the body assignment to determine the return value",
                "2", returnExpr.get(JmmAttributes.INTEGER_LITERAL.VALUE));
    }

    @Test
    public void forFalseKeepsInitializerOnly() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        int y;
                        x = 0;
                        for (y = 3; false; y = y + 1) {
                            x = 1;
                        }
                        return y;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("for(false) should preserve the initializer and remove body/update",
                JmmKind.INTEGER_LITERAL.check(returnExpr));
        assertEquals("Expected the initializer value to be propagated",
                "3", returnExpr.get(JmmAttributes.INTEGER_LITERAL.VALUE));
    }

    @Test
    public void forFalseWithoutInitializerDoesNotRunUpdate() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int y;
                        y = 3;
                        for (; false; y = y + 1) {
                            y = 9;
                        }
                        return y;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("for(; false; update) must remove the loop without preserving the update",
                JmmKind.INTEGER_LITERAL.check(returnExpr));
        assertEquals("Expected the pre-loop value to remain",
                "3", returnExpr.get(JmmAttributes.INTEGER_LITERAL.VALUE));
    }

    @Test
    public void foldsShortCircuitWithoutKeepingDeadDivision() {
        var method = optimizedMethod("""
                package p;
                class A {
                    boolean method() {
                        return false && (1 / 0 < 1);
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("false && expr should fold to false without keeping a non-executed RHS",
                JmmKind.BOOLEAN_LITERAL.check(returnExpr));
        assertEquals("Expected the short-circuited expression to fold to false",
                "false", returnExpr.get(JmmAttributes.BOOLEAN_LITERAL.VALUE));
        assertTrue("The discarded RHS division is not executed and should disappear",
                method.getDescendants(JmmKind.BINARY_EXPR).stream()
                        .noneMatch(expr -> expr.get(JmmAttributes.BINARY_EXPR.OP).equals("/")));
    }

    @Test
    public void keepsPotentiallyExecutingDivisionThroughBooleanIdentity() {
        var method = optimizedMethod("""
                package p;
                class A {
                    boolean method() {
                        return (1 / 0 < 1) && true;
                    }
                }
                """);

        assertTrue("The left side of expr && true still executes, so its division must stay",
                method.getDescendants(JmmKind.BINARY_EXPR).stream()
                        .anyMatch(expr -> expr.get(JmmAttributes.BINARY_EXPR.OP).equals("/")));
    }

    @Test
    public void skippedShortCircuitRhsMutationKeepsEarlierConstant() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        x = 1;
                        true || (++x < 3);
                        return x;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("The RHS mutation is skipped, so x can still propagate",
                JmmKind.INTEGER_LITERAL.check(returnExpr));
        assertEquals("Expected x to keep its pre-short-circuit value",
                "1", returnExpr.get(JmmAttributes.INTEGER_LITERAL.VALUE));
    }

    @Test
    public void executingShortCircuitRhsMutationInvalidatesConstant() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        x = 1;
                        false || (++x < 3);
                        return x;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("The RHS mutation executes, so x must still be read at runtime",
                JmmKind.VAR_REF_EXPR.check(returnExpr));
    }

    @Test
    public void propagatesConstantsAfterMatchingBranchAssignments() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method(boolean flag) {
                        int x;
                        if (flag) {
                            x = 7;
                        } else {
                            x = 7;
                        }
                        return x;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("Both branches assign the same constant, so the merge point can know x",
                JmmKind.INTEGER_LITERAL.check(returnExpr));
        assertEquals("Expected x to propagate after matching branch assignments",
                "7", returnExpr.get(JmmAttributes.INTEGER_LITERAL.VALUE));
    }

    @Test
    public void doesNotPropagateAfterConflictingBranchAssignments() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method(boolean flag) {
                        int x;
                        if (flag) {
                            x = 7;
                        } else {
                            x = 8;
                        }
                        return x;
                    }
                }
                """);

        var returnExpr = method.getDescendants(JmmKind.RETURN_STMT).getFirst().getChild(0);
        assertTrue("Conflicting branch constants must not propagate past the merge",
                JmmKind.VAR_REF_EXPR.check(returnExpr));
    }

    @Test
    public void myTestDeadStoreEliminationRemovesOverwrittenAndExitDeadStores() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        x = 1;
                        x = 2;
                        return 0;
                    }
                }
                """);

        var storesToX = method.getDescendants(JmmKind.ASSIGN_STMT).stream()
                .filter(assign -> assign.get(JmmAttributes.ASSIGN_STMT.VAR).equals("x"))
                .toList();

        assertTrue("Assignments to x are dead before overwrite or method exit and should be removed",
                storesToX.isEmpty());
    }

    @Test
    public void myTestDeadStoreEliminationKeepsAssignmentsWhoseRhsMayHaveSideEffects() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method() {
                        int x;
                        x = this.sideEffect();
                        return 0;
                    }

                    int sideEffect() {
                        return 1;
                    }
                }
                """);

        var storesToX = method.getDescendants(JmmKind.ASSIGN_STMT).stream()
                .filter(assign -> assign.get(JmmAttributes.ASSIGN_STMT.VAR).equals("x"))
                .toList();

        assertEquals("The dead local value can be ignored, but the call-producing assignment must stay",
                1, storesToX.size());
        assertTrue("The side-effecting method call should still be present",
                method.getDescendants(JmmKind.METHOD_CALL_EXPR).stream()
                        .anyMatch(call -> call.get(JmmAttributes.METHOD_CALL_EXPR.NAME).equals("sideEffect")));
    }

    @Test
    public void myTestDeadStoreEliminationKeepsAssignmentsWhoseRhsReadsAField() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int value;

                    int method(A other) {
                        int x;
                        x = other.value;
                        return 0;
                    }
                }
                """);

        var storesToX = method.getDescendants(JmmKind.ASSIGN_STMT).stream()
                .filter(assign -> assign.get(JmmAttributes.ASSIGN_STMT.VAR).equals("x"))
                .toList();

        assertEquals("The dead local value can be ignored, but the field read must still execute",
                1, storesToX.size());
        assertEquals("The RHS should remain a field access",
                1, method.getDescendants(JmmKind.FIELD_ACCESS_EXPR).size());
    }

    @Test
    public void myTestDeadStoreEliminationKeepsStoresThroughArrayAliases() {
        var method = optimizedMethod("""
                package p;
                class A {
                    int method(int[] input) {
                        int[] alias;
                        alias = input;
                        alias[0] = 4;
                        return 0;
                    }
                }
                """);

        var arrayStores = method.getDescendants(JmmKind.ARRAY_ASSIGN_STMT).stream()
                .filter(assign -> assign.get(JmmAttributes.ARRAY_ASSIGN_STMT.VAR).equals("alias"))
                .toList();

        assertEquals("A store through a local alias of a parameter array is externally visible",
                1, arrayStores.size());
    }

    private JmmNode optimizedMethod(String code) {
        var semanticsResult = semanticsFromSnippet(code, false);
        var optimized = new JmmOptimizationImpl().transformAst(semanticsResult);

        return optimized.getRootNode()
                .getDescendants(JmmKind.METHOD_DECL)
                .stream()
                .filter(method -> method.get(JmmAttributes.METHOD_DECL.NAME).equals("method"))
                .findFirst()
                .orElseThrow();
    }
}
