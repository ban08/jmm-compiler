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
