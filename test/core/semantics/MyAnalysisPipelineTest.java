package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp2026.analysis.AnalysisPass;
import pt.up.fe.comp2026.analysis.JmmAnalysisImpl;
import pt.up.fe.comp.test.env.JmmTestEnv;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class MyAnalysisPipelineTest extends JmmTestEnv {

    public MyAnalysisPipelineTest() {
        super("", "");
    }

    @Test
    public void crashingPassProducesAReportWithoutPrintingToStdout() {
        var parserResult = parseSnippet("""
                package p;
                class A {
                }
                """);

        var originalOut = System.out;
        var stdoutCapture = new ByteArrayOutputStream();

        try (var capturedPrintStream = new PrintStream(stdoutCapture, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedPrintStream);

            var semanticsResult = new ThrowingAnalysis().semanticAnalysis(parserResult);

            assertTrue("A crashing pass should be converted into a semantic error report", semanticsResult.hasErrors());
            assertTrue("The analysis pipeline should stay quiet on stdout when reporting internal pass failures",
                    stdoutCapture.toString(StandardCharsets.UTF_8).isBlank());
            assertTrue("The generated error report should identify the crashing pass",
                    semanticsResult.reports().stream()
                            .anyMatch(report -> report.getMessage() != null
                                    && report.getMessage().contains("ExplodingPass")));
        } finally {
            System.setOut(originalOut);
        }
    }

    private static class ThrowingAnalysis extends JmmAnalysisImpl {
        @Override
        protected List<AnalysisPass> buildPasses(SymbolTable table) {
            return List.of(new ExplodingPass());
        }
    }

    private static class ExplodingPass implements AnalysisPass {
        @Override
        public List<pt.up.fe.comp.jmm.report.Report> analyze(pt.up.fe.comp.jmm.ast.JmmNode root, SymbolTable table) {
            throw new RuntimeException("Boom");
        }
    }
}
