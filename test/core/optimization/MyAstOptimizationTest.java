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
