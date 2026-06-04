package core.backend;

import org.junit.Assert;
import org.junit.Test;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp2026.backend.JasminBackendImpl;

import java.util.Map;
import java.util.regex.Pattern;

public class MyJasminBackendStructureTest {

    @Test
    public void emitsClassStructureDescriptorsAndModifiers() {
        var jasmin = toJasmin("""
                package core.backend.jasmin;
                import examples.Quicksort;

                StructureFixture extends Quicksort {
                    .field public count.i32;
                    .field public flag.bool;
                    .field public values.array.i32;
                    .field public label.String;

                    .construct "<init>"().V {
                        invokespecial(this."examples.Quicksort", "<init>").V;
                    }

                    .method public static main(args.array.String).V {
                        ret.V;
                    }

                    .method private static hidden(a.i32, ok.bool, text.String, values.array.i32).String {
                        ret.String text.String;
                    }
                }
                """);

        Assert.assertTrue("Class header should be public and package-qualified",
                jasmin.contains(".class public core/backend/jasmin/StructureFixture"));
        Assert.assertTrue("Superclass should be the resolved import, not java/lang/Object",
                jasmin.contains(".super examples/Quicksort"));

        Assert.assertTrue("Int field descriptor should be I", jasmin.contains(".field public count I"));
        Assert.assertTrue("Boolean field descriptor should be Z", jasmin.contains(".field public flag Z"));
        Assert.assertTrue("Int array field descriptor should be [I", jasmin.contains(".field public values [I"));
        Assert.assertTrue("String field descriptor should be Ljava/lang/String;",
                jasmin.contains(".field public label Ljava/lang/String;"));

        var constructor = method(jasmin, "<init>");
        Assert.assertTrue("Constructor should call the real superclass constructor",
                constructor.contains("invokespecial examples/Quicksort/<init>()V"));
        Assert.assertTrue("Constructor should load this before invokespecial", constructor.contains("aload_0"));
        Assert.assertTrue("Constructor should return explicitly", constructor.contains("return"));

        Assert.assertTrue("main descriptor should encode String[] and void",
                jasmin.contains(".method public static main([Ljava/lang/String;)V"));
        Assert.assertTrue("private static method should preserve modifiers and descriptors",
                jasmin.contains(".method private static hidden(IZLjava/lang/String;[I)Ljava/lang/String;"));
        Assert.assertFalse("Generated limits must not use the skeleton placeholder 99",
                jasmin.contains(".limit stack 99") || jasmin.contains(".limit locals 99"));
    }

    @Test
    public void emitsExactLocalAndStackLimitsForKnownPeak() {
        var jasmin = toJasmin("""
                package core.backend.jasmin;

                LimitFixture extends Object {
                    .construct "<init>"().V {
                        invokespecial(this."java.lang.Object", "<init>").V;
                    }

                    .method public exact(a.i32, b.i32).i32 {
                        callResult.i32 :=.i32 invokevirtual(this.LimitFixture, "sum", a.i32, b.i32).i32;
                        high.i32 :=.i32 callResult.i32;
                        ret.i32 high.i32;
                    }

                    .method public sum(a.i32, b.i32).i32 {
                        result.i32 :=.i32 a.i32 +.i32 b.i32;
                        ret.i32 result.i32;
                    }
                }
                """);

        var exact = method(jasmin, "exact");
        Assert.assertTrue("exact() should need this, two params, and two locals: max reg 4 means .limit locals 5",
                exact.contains(".limit locals 5"));
        Assert.assertTrue("exact() call peak should be receiver + two args, so .limit stack 3",
                exact.contains(".limit stack 3"));
        Assert.assertFalse("exact() should not use placeholder limits",
                exact.contains(".limit stack 99") || exact.contains(".limit locals 99"));
    }

    @Test
    public void emitsIincForSameRegisterIncrementAssignments() {
        var jasmin = toJasmin("""
                package core.backend.jasmin;

                IincFixture extends Object {
                    .construct "<init>"().V {
                        invokespecial(this."java.lang.Object", "<init>").V;
                    }

                    .method public bump(a.i32).i32 {
                        a.i32 :=.i32 a.i32 +.i32 1.i32;
                        a.i32 :=.i32 a.i32 -.i32 2.i32;
                        ret.i32 a.i32;
                    }
                }
                """);

        var bump = method(jasmin, "bump");
        Assert.assertTrue("a := a + 1 should become iinc", bump.contains("iinc 1 1"));
        Assert.assertTrue("a := a - 2 should become iinc with a negative delta", bump.contains("iinc 1 -2"));
        Assert.assertFalse("simple increments should not use stack arithmetic",
                bump.contains("iadd") || bump.contains("isub"));
    }

    @Test
    public void emitsZeroBranchesForComparisonsAgainstLiteralZero() {
        var jasmin = toJasmin("""
                package core.backend.jasmin;

                ZeroBranchFixture extends Object {
                    .construct "<init>"().V {
                        invokespecial(this."java.lang.Object", "<init>").V;
                    }

                    .method public negative(a.i32).bool {
                        res.bool :=.bool a.i32 <.i32 0.i32;
                        ret.bool res.bool;
                    }

                    .method public positive(a.i32).bool {
                        res.bool :=.bool 0.i32 <.i32 a.i32;
                        ret.bool res.bool;
                    }
                }
                """);

        var negative = method(jasmin, "negative");
        Assert.assertTrue("a < 0 should branch directly against zero", negative.contains("iflt "));
        Assert.assertFalse("a < 0 should not need if_icmp", negative.contains("if_icmp"));

        var positive = method(jasmin, "positive");
        Assert.assertTrue("0 < a should flip to a > 0", positive.contains("ifgt "));
        Assert.assertFalse("0 < a should not need if_icmp", positive.contains("if_icmp"));
    }

    @Test
    public void comparisonValueStackLimitDoesNotCountDiscardedOperands() {
        var jasmin = toJasmin("""
                package core.backend.jasmin;

                ComparisonLimitFixture extends Object {
                    .construct "<init>"().V {
                        invokespecial(this."java.lang.Object", "<init>").V;
                    }

                    .method public less(a.i32, b.i32).bool {
                        res.bool :=.bool a.i32 <.i32 b.i32;
                        ret.bool res.bool;
                    }
                }
                """);

        var less = method(jasmin, "less");
        Assert.assertTrue("comparison value should only need both operands on the stack",
                less.contains(".limit stack 2"));
    }

    @Test
    public void emitsReferenceArrayDescriptorsLoadsStoresAndAnewarray() {
        var jasmin = toJasmin("""
                package core.backend.jasmin;
                import java.util.ArrayList;

                ReferenceArrayFixture extends Object {
                    .construct "<init>"().V {
                        invokespecial(this."java.lang.Object", "<init>").V;
                    }

                    .method public make(size.i32).array.ArrayList {
                        items.array.ArrayList :=.array.ArrayList new(array, size.i32).array.ArrayList;
                        first.ArrayList :=.ArrayList new(ArrayList).ArrayList;
                        invokespecial(first.ArrayList, "<init>").V;
                        items.array.ArrayList[0.i32].ArrayList :=.ArrayList first.ArrayList;
                        got.ArrayList :=.ArrayList items.array.ArrayList[0.i32].ArrayList;
                        invokevirtual(got.ArrayList, "size").i32;
                        ret.array.ArrayList items.array.ArrayList;
                    }

                    .method public strings(size.i32).array.String {
                        values.array.String :=.array.String new(array, size.i32).array.String;
                        ret.array.String values.array.String;
                    }

                    .method public use(list.ArrayList, strings.array.String, self.ReferenceArrayFixture).ArrayList {
                        ret.ArrayList list.ArrayList;
                    }
                }
                """);

        Assert.assertTrue("Imported reference arrays should use the correct method descriptor",
                jasmin.contains(".method public make(I)[Ljava/util/ArrayList;"));
        Assert.assertTrue("java.lang String arrays should use the correct method descriptor",
                jasmin.contains(".method public strings(I)[Ljava/lang/String;"));
        Assert.assertTrue("Mixed descriptors should resolve imported, java.lang, and current classes exactly",
                jasmin.contains(".method public use(Ljava/util/ArrayList;[Ljava/lang/String;Lcore/backend/jasmin/ReferenceArrayFixture;)Ljava/util/ArrayList;"));

        var make = method(jasmin, "make");
        Assert.assertTrue("One-dimensional imported object arrays should use anewarray",
                make.contains("anewarray java/util/ArrayList"));
        Assert.assertTrue("Reference array stores should use aastore",
                make.contains("aastore"));
        Assert.assertTrue("Reference array loads should use aaload",
                make.contains("aaload"));
        Assert.assertTrue("Reference locals should use astore",
                make.contains("astore_2") && make.contains("astore_3"));
        Assert.assertTrue("Reference locals should use aload",
                make.contains("aload_2") && make.contains("aload_3"));
        Assert.assertTrue("Discarded non-void virtual calls must be popped",
                make.contains("invokevirtual java/util/ArrayList/size()I") && make.contains("pop"));

        var strings = method(jasmin, "strings");
        Assert.assertTrue("One-dimensional String arrays should use anewarray",
                strings.contains("anewarray java/lang/String"));
    }

    @Test
    public void emitsReferenceMultianewarrayDescriptor() {
        var jasmin = toJasmin("""
                package core.backend.jasmin;

                ReferenceMultidimFixture extends Object {
                    .construct "<init>"().V {
                        invokespecial(this."java.lang.Object", "<init>").V;
                    }

                    .method public matrix(rows.i32, cols.i32).array.array.String {
                        values.array.array.String :=.array.array.String new(array, rows.i32, cols.i32).array.array.String;
                        ret.array.array.String values.array.array.String;
                    }
                }
                """);

        var matrix = method(jasmin, "matrix");
        Assert.assertTrue("String[][] allocation should use JVM reference-array descriptor",
                matrix.contains("multianewarray [[Ljava/lang/String; 2"));
    }

    @Test
    public void emitsConstructorArgumentDescriptorsForImplicitJavaLangClasses() {
        var jasmin = toJasmin("""
                package core.backend.jasmin;

                ConstructorArgFixture extends Object {
                    .construct "<init>"().V {
                        invokespecial(this."java.lang.Object", "<init>").V;
                    }

                    .method public build(text.String).String {
                        builder.StringBuilder :=.StringBuilder new(StringBuilder).StringBuilder;
                        invokespecial(builder.StringBuilder, "<init>", text.String).V;
                        out.String :=.String invokevirtual(builder.StringBuilder, "toString").String;
                        ret.String out.String;
                    }
                }
                """);

        var build = method(jasmin, "build");
        Assert.assertTrue("Implicit java.lang constructor owner should resolve to java/lang/StringBuilder",
                build.contains("new java/lang/StringBuilder"));
        Assert.assertTrue("Constructor argument descriptor should include java.lang.String",
                build.contains("invokespecial java/lang/StringBuilder/<init>(Ljava/lang/String;)V"));
        Assert.assertTrue("Returned java.lang object descriptor should be exact",
                build.contains("invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;"));
    }

    @Test
    public void keepsStackNeutralAroundMaterializedComparisonLabelsAndDiscardedCalls() {
        var jasmin = toJasmin("""
                package core.backend.jasmin;

                StackNeutralFixture extends Object {
                    .construct "<init>"().V {
                        invokespecial(this."java.lang.Object", "<init>").V;
                    }

                    .method public id(x.i32).i32 {
                        ret.i32 x.i32;
                    }

                    .method public branch(a.i32, b.i32).i32 {
                        cond.bool :=.bool a.i32 <.i32 b.i32;
                        if (cond.bool) goto true_label;
                        invokevirtual(this.StackNeutralFixture, "id", a.i32).i32;
                        goto end_label;
                    true_label:
                        tmp.i32 :=.i32 invokevirtual(this.StackNeutralFixture, "id", b.i32).i32;
                        ret.i32 tmp.i32;
                    end_label:
                        ret.i32 0.i32;
                    }
                }
                """);

        var branch = method(jasmin, "branch");
        var popIndex = branch.indexOf("pop");
        var gotoIndex = branch.indexOf("goto end_label");

        Assert.assertTrue("Materialized comparison should still produce a branchable local",
                branch.contains("istore") && branch.contains("ifne true_label"));
        Assert.assertTrue("Discarded call result should be popped before jumping to a label",
                popIndex >= 0 && popIndex < gotoIndex);
        Assert.assertTrue("Both OLLIR labels should be emitted as clean Jasmin labels",
                branch.contains("true_label:") && branch.contains("end_label:"));
    }

    @Test
    public void emitsConstantLoadingBoundaryInstructions() {
        var jasmin = toJasmin("""
                package core.backend.jasmin;

                ConstantBoundaryFixture extends Object {
                    .construct "<init>"().V {
                        invokespecial(this."java.lang.Object", "<init>").V;
                    }

                    .method public constants().i32 {
                        c0.i32 :=.i32 0.i32;
                        c5.i32 :=.i32 5.i32;
                        c6.i32 :=.i32 6.i32;
                        c127.i32 :=.i32 127.i32;
                        c128.i32 :=.i32 128.i32;
                        c32767.i32 :=.i32 32767.i32;
                        c32768.i32 :=.i32 32768.i32;
                        ret.i32 c32768.i32;
                    }
                }
                """);

        var constants = method(jasmin, "constants");
        Assert.assertTrue("0 should use iconst_0", constants.contains("iconst_0"));
        Assert.assertTrue("5 should use iconst_5", constants.contains("iconst_5"));
        Assert.assertTrue("6 should use bipush", constants.contains("bipush 6"));
        Assert.assertTrue("127 should still use bipush", constants.contains("bipush 127"));
        Assert.assertTrue("128 should use sipush", constants.contains("sipush 128"));
        Assert.assertTrue("32767 should still use sipush", constants.contains("sipush 32767"));
        Assert.assertTrue("32768 should fall back to ldc", constants.contains("ldc 32768"));
    }

    private static String toJasmin(String ollirCode) {
        var result = new JasminBackendImpl().toJasmin(new OllirResult(ollirCode, Map.of()));
        assertNoBackendErrors(result);
        return result.getJasminCode();
    }

    private static void assertNoBackendErrors(JasminResult result) {
        Assert.assertTrue("Jasmin generation should not emit errors: " + result.reports(),
                result.reports().stream().noneMatch(report -> report.getType() == ReportType.ERROR));
    }

    private static String method(String jasmin, String methodName) {
        var regex = "(?ms)^\\.method\\b[^\\n]*\\s" + Pattern.quote(methodName) + "\\([^\\n]*\\n.*?^\\.end method$";
        var matcher = Pattern.compile(regex).matcher(jasmin);
        Assert.assertTrue("Expected to find Jasmin method '" + methodName + "' in:\n" + jasmin, matcher.find());
        return matcher.group();
    }
}
