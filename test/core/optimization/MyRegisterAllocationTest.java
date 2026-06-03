package core.optimization;

import org.junit.Test;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.optimization.JmmOptimizationImpl;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MyRegisterAllocationTest {

    @Test
    public void reportsMinimumRegistersWhenLimitIsTooSmall() {
        var result = optimize(simpleOllir(), "2");

        var errors = result.reports().stream()
                .filter(report -> report.getType() == ReportType.ERROR)
                .toList();

        assertEquals("Expected exactly one register-allocation error", 1, errors.size());
        assertEquals("Expected the error to belong to the OLLIR optimization stage",
                Stage.LLIR_OPTIMIZATION, errors.getFirst().getStage());
        assertTrue("Expected the report to include the minimum required JVM local count",
                errors.getFirst().getMessage().contains("Minimum required: 3 JVM local register(s)"));
    }

    @Test
    public void minusOneSkipsRegisterAllocation() {
        var result = optimize(simpleOllir(), "-1");

        assertTrue("Register allocation should not emit reports when disabled with -r=-1",
                result.reports().isEmpty());
    }

    @Test
    public void zeroRunsRegisterAllocationAndReportsMapping() {
        var result = optimize(simpleOllir(), "0");

        assertTrue("Register allocation with -r=0 should not emit errors for this method",
                result.reports().stream().noneMatch(report -> report.getType() == ReportType.ERROR));
        assertTrue("Register allocation should report the produced variable mapping",
                result.reports().stream().anyMatch(report -> report.getMessage().contains("a ->")));
    }

    @Test
    public void reportsWhenLimitCannotFitThisAndParameters() {
        var result = optimize(parameterOllir(), "2");

        var errors = result.reports().stream()
                .filter(report -> report.getType() == ReportType.ERROR)
                .toList();

        assertEquals("Expected one error because this plus two params already require three JVM locals",
                1, errors.size());
        assertTrue("Expected the report to include the minimum required JVM local count",
                errors.getFirst().getMessage().contains("Minimum required: 3 JVM local register(s)"));
    }

    @Test
    public void isolatedDeadLocalsStillReceiveReusableRegisters() {
        var result = optimize(isolatedLocalsOllir(), "2");

        assertTrue("Register allocation should fit isolated locals into one reusable local register",
                result.reports().stream().noneMatch(report -> report.getType() == ReportType.ERROR));

        var method = result.getOllirClass().getMethods().stream()
                .filter(candidate -> candidate.getMethodName().equals("method"))
                .findFirst()
                .orElseThrow();
        var varTable = method.getVarTable();

        assertEquals("Both isolated locals should reuse the first allocatable register",
                varTable.get("a").getVirtualReg(), varTable.get("b").getVirtualReg());
        assertEquals("Instance method without params should start allocatable locals at JVM register 1",
                1, varTable.get("a").getVirtualReg());
    }

    @Test
    public void generatedTemporariesRespectStrictRegisterLimit() {
        var result = optimize(strictTemporaryOllir(), "1");

        var errors = result.reports().stream()
                .filter(report -> report.getType() == ReportType.ERROR)
                .toList();

        assertEquals("Expected one error when a generated temporary needs a second JVM local slot",
                1, errors.size());
        assertTrue("The strict allocator should report that two JVM locals are required",
                errors.getFirst().getMessage().contains("Minimum required: 2 JVM local register(s)"));
    }

    private OllirResult optimize(String ollirCode, String registerAllocation) {
        return new JmmOptimizationImpl().transformOllir(new OllirResult(
                ollirCode,
                Map.of("registerAllocation", registerAllocation)));
    }

    private String simpleOllir() {
        return """
                package core.optimization;

                Test extends Object {
                    .construct "<init>"().V {
                        invokespecial(this, "<init>" "java.lang.Object").V;
                    }

                    .method public method().i32 {
                        a.i32 :=.i32 1.i32;
                        b.i32 :=.i32 2.i32;
                        c.i32 :=.i32 a.i32 +.i32 b.i32;
                        ret.i32 c.i32;
                    }
                }
                """;
    }

    private String parameterOllir() {
        return """
                package core.optimization;

                Test extends Object {
                    .construct "<init>"().V {
                        invokespecial(this, "<init>" "java.lang.Object").V;
                    }

                    .method public method(a.i32, b.i32).i32 {
                        ret.i32 a.i32;
                    }
                }
                """;
    }

    private String isolatedLocalsOllir() {
        return """
                package core.optimization;

                Test extends Object {
                    .construct "<init>"().V {
                        invokespecial(this, "<init>" "java.lang.Object").V;
                    }

                    .method public method().i32 {
                        a.i32 :=.i32 1.i32;
                        b.i32 :=.i32 2.i32;
                        ret.i32 0.i32;
                    }
                }
                """;
    }

    private String strictTemporaryOllir() {
        return """
                package core.optimization;

                Test extends Object {
                    .construct "<init>"().V {
                        invokespecial(this, "<init>" "java.lang.Object").V;
                    }

                    .method public static method().i32 {
                        a.i32 :=.i32 1.i32;
                        tmp0.i32 :=.i32 2.i32;
                        b.i32 :=.i32 a.i32 +.i32 tmp0.i32;
                        ret.i32 b.i32;
                    }
                }
                """;
    }
}
