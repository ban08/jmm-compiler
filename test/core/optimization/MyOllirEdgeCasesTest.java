package core.optimization;

import org.junit.Assert;
import org.junit.Test;

import static core.optimization.MyOllirTestSupport.assertNoErrors;
import static core.optimization.MyOllirTestSupport.countOccurrences;
import static core.optimization.MyOllirTestSupport.toOllir;

public class MyOllirEdgeCasesTest {

    @Test
    public void myTestStaticCallsUseInvokeStatic() {
        var result = toOllir("""
                package core.optimization;

                class StaticCall {
                    public static int readNumber() {
                        return 7;
                    }

                    int method() {
                        return readNumber();
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        Assert.assertTrue("Static local calls should lower to invokestatic",
                ollir.contains("invokestatic(") && ollir.contains("\"readNumber\""));
        Assert.assertFalse("Static local calls should not lower to invokevirtual",
                ollir.contains("invokevirtual(this") && ollir.contains("\"readNumber\""));
    }

    @Test
    public void myTestConstructorArgumentsArePassedToInvokeSpecial() {
        var result = toOllir("""
                package core.optimization;
                import java.util.Date;

                class ConstructorArgs {
                    Date method() {
                        return new Date(2026, 5, 8);
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        Assert.assertTrue("Constructor call should emit invokespecial",
                ollir.contains("invokespecial(") && ollir.contains("\"<init>\""));
        Assert.assertTrue("Constructor arguments should be forwarded to invokespecial",
                ollir.contains("2026.i32") && ollir.contains("5.i32") && ollir.contains("8.i32"));
    }

    @Test
    public void myTestPrefixIncrementWritesBackToLocal() {
        var result = toOllir("""
                package core.optimization;

                class PrefixWriteback {
                    int method() {
                        int x;
                        x = 1;
                        return ++x;
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        Assert.assertTrue("Prefix increment should compute x + 1",
                ollir.contains("+.i32 1.i32"));
        Assert.assertTrue("Prefix increment should write the updated value back to x",
                countOccurrences(ollir, "x.i32 :=.i32") >= 2);
    }

    @Test
    public void myTestFieldArrayWritesLoadTheFieldArrayBeforeStore() {
        var result = toOllir("""
                package core.optimization;

                class FieldArrayWrite {
                    int[] values;

                    int method() {
                        values = new int[2];
                        values[1] = 5;
                        return values[1];
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        Assert.assertTrue("Field-backed array writes should first read the array field",
                ollir.contains("getfield(this, values.array.i32).array.i32"));
        Assert.assertTrue("Field-backed array writes should store into the loaded array element",
                ollir.contains("].i32 :=.i32 5.i32"));
    }

    @Test
    public void myTestIfElseReturningBothBranchesDoesNotLeaveDanglingEndLabel() {
        var result = toOllir("""
                package core.optimization;

                class IfElseReturns {
                    int method(boolean flag) {
                        if (flag) {
                            return 1;
                        } else {
                            return 2;
                        }
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        Assert.assertFalse("An if/else whose branches both return should not emit an unused end label",
                ollir.contains("if_end_"));
        Assert.assertEquals("Both source branches should lower to direct returns",
                2, countOccurrences(ollir, "ret.i32"));
    }

    @Test
    public void myTestTrueLoopAtMethodEndDoesNotLeaveDanglingEndLabel() {
        var result = toOllir("""
                package core.optimization;

                class TrueLoopReturns {
                    int method() {
                        while (true) {
                            return 5;
                        }
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        Assert.assertTrue("Trailing loop end labels must be followed by a parseable return instruction",
                ollir.contains("while_end_") && ollir.contains("ret.i32 0.i32"));
    }

    @Test
    public void myTestReferenceTrueLoopFallbackUsesDefinedValue() {
        var result = toOllir("""
                package core.optimization;

                class ReferenceLoopReturns {
                    ReferenceLoopReturns method() {
                        while (true) {
                            return new ReferenceLoopReturns();
                        }
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        Assert.assertTrue("Reference fallback return should initialize the synthetic value before returning it",
                ollir.contains("new(ReferenceLoopReturns).ReferenceLoopReturns")
                        && ollir.contains("invokespecial(unreach"));
    }

    @Test
    public void myTestForWithoutInitializerKeepsUpdateInsideLoop() {
        var result = toOllir("""
                package core.optimization;

                class ForWithoutInitializer {
                    int method(int limit) {
                        int i;
                        i = 0;
                        for (; i < limit; i = i + 1) {
                        }
                        return i;
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        int condIndex = ollir.indexOf("for_cond_");
        Assert.assertTrue("The for condition label should be emitted", condIndex >= 0);

        String beforeCondition = ollir.substring(0, condIndex);
        Assert.assertFalse("The update slot of for (; cond; update) must not execute before the loop test",
                beforeCondition.contains("+.i32 1.i32"));
    }

    @Test
    public void myTestForWithoutInitializerOrConditionKeepsUpdateInsideLoop() {
        var result = toOllir("""
                package core.optimization;

                class ForOnlyUpdate {
                    void method() {
                        int i;
                        i = 0;
                        for (;; i = i + 1) {
                        }
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        int condIndex = ollir.indexOf("for_cond_");
        Assert.assertTrue("The for condition label should be emitted", condIndex >= 0);

        String beforeCondition = ollir.substring(0, condIndex);
        Assert.assertFalse("The update slot of for (;; update) must not execute before entering the loop",
                beforeCondition.contains("+.i32 1.i32"));
    }
}
