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
    public void myTestOllirKeywordClassNamesAreEscapedInClassTypes() {
        var result = toOllir("""
                package array.ret;

                class array {
                    array method(array value) {
                        array local;
                        local = new array();
                        return local;
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        Assert.assertTrue("Package segments that are OLLIR keywords must be emitted as quoted ids",
                ollir.contains("package \"array\".\"ret\";"));
        Assert.assertTrue("Class names that are OLLIR keywords must be emitted as quoted ids",
                ollir.contains("\"array\" extends Object"));
        Assert.assertTrue("Class type suffixes that are OLLIR keywords must be emitted as quoted ids",
                ollir.contains("local.\"array\"") && ollir.contains("new(\"array\").\"array\""));
    }

    @Test
    public void myTestOllirKeywordClassNamesAreEscapedOnThisOperands() {
        var result = toOllir("""
                package array.ret;

                class array {
                    array method() {
                        return this;
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        Assert.assertTrue("The implicit this operand type must be escaped when the class name is an OLLIR keyword",
                ollir.contains("ret.\"array\" this.\"array\""));
    }

    @Test
    public void myTestOllirKeywordLocalNamesAreEscaped() {
        var result = toOllir("""
                package core.optimization;

                class KeywordLocals {
                    int method(int final) {
                        int varargs;
                        varargs = final + 1;
                        return varargs;
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        Assert.assertTrue("Local names that collide with OLLIR keywords must be quoted",
                ollir.contains("\"varargs\".i32"));
        Assert.assertTrue("Parameter names that collide with OLLIR keywords must be quoted",
                ollir.contains("\"final\".i32"));
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
    public void myTestExplicitThisReceiverLowersToBareThisForFieldAndMethodAccess() {
        // OLLIR-COMP2026.pdf, Figure 4: source-level `this.a` and `this.method()`
        // lower to `getfield(this, ...)` and `invokevirtual(this, ...)` with bare
        // `this`. The OLLIR grammar's objectRef rule lets THIS appear without a
        // type qualifier in those positions.
        var result = toOllir("""
                package core.optimization;

                class ExplicitThis {
                    int a;

                    int read() {
                        return this.a;
                    }

                    int chain() {
                        return this.read();
                    }
                }
                """);

        assertNoErrors(result);

        String ollir = result.getOllirCode();
        Assert.assertTrue("Source `this.a` should lower to getfield with bare `this` receiver",
                ollir.contains("getfield(this, a.i32).i32"));
        Assert.assertTrue("Source `this.read()` should lower to invokevirtual with bare `this` receiver",
                ollir.contains("invokevirtual(this, \"read\").i32"));
        Assert.assertFalse("getfield/invokevirtual on `this` must not carry the class type qualifier",
                ollir.contains("getfield(this.ExplicitThis,")
                        || ollir.contains("invokevirtual(this.ExplicitThis,"));
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
