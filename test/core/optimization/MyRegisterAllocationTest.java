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
        var result = new JmmOptimizationImpl().transformOllir(new OllirResult("""
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
                """, Map.of("registerAllocation", "2")));

        var errors = result.reports().stream()
                .filter(report -> report.getType() == ReportType.ERROR)
                .toList();

        assertEquals("Expected exactly one register-allocation error", 1, errors.size());
        assertEquals("Expected the error to belong to the OLLIR optimization stage",
                Stage.LLIR_OPTIMIZATION, errors.getFirst().getStage());
        assertTrue("Expected the report to include the minimum required JVM local count",
                errors.getFirst().getMessage().contains("Minimum required: 3 JVM local register(s)"));
    }
}
