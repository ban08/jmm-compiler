package core.optimization;

import org.junit.Assert;
import org.junit.Test;
import org.specs.comp.ollir.ArrayOperand;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.NewInstruction;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp2026.analysis.JmmAnalysisImpl;
import pt.up.fe.comp2026.lexer.JmmLexerImpl;
import pt.up.fe.comp2026.optimization.JmmOptimizationImpl;
import pt.up.fe.comp2026.parser.JmmParserImpl;

import java.util.HashMap;

public class MyArrayOllirGenerationTest {

    @Test
    public void lowersArrayInitializerIntoAllocationAndStores() {
        var result = toOllir("""
                package core.optimization;

                class ArrayInit {
                    int[] init() {
                        int[] values;
                        values = new int[] {1, 2, 3};
                        return values;
                    }
                }
                """);

        assertNoErrors(result);

        var method = result.getOllirClass().getMethods().stream()
                .filter(candidate -> candidate.getMethodName().equals("init"))
                .findFirst()
                .orElseThrow();

        long newArrays = method.getInstructions().stream()
                .filter(AssignInstruction.class::isInstance)
                .map(AssignInstruction.class::cast)
                .filter(assign -> assign.getRhs() instanceof NewInstruction)
                .count();

        long arrayStores = method.getInstructions().stream()
                .filter(AssignInstruction.class::isInstance)
                .map(AssignInstruction.class::cast)
                .filter(assign -> assign.getDest() instanceof ArrayOperand)
                .count();

        Assert.assertTrue("Array initializer should allocate a new array", newArrays >= 1);
        Assert.assertEquals("Array initializer should lower each element into a store", 3, arrayStores);
    }

    @Test
    public void lowersMultidimensionalArraysIntoStoresAndReads() {
        var result = toOllir("""
                package core.optimization;

                class MultiArray {
                    int method(int n, int m) {
                        int[][] matrix;
                        int value;
                        matrix = new int[n][m];
                        matrix[0][1] = 7;
                        value = matrix[0][1];
                        return value;
                    }
                }
                """);

        assertNoErrors(result);

        var method = result.getOllirClass().getMethods().stream()
                .filter(candidate -> candidate.getMethodName().equals("method"))
                .findFirst()
                .orElseThrow();

        var assigns = method.getInstructions().stream()
                .filter(AssignInstruction.class::isInstance)
                .map(AssignInstruction.class::cast)
                .toList();

        long arrayStores = assigns.stream()
                .filter(assign -> assign.getDest() instanceof ArrayOperand)
                .count();

        long arrayReads = assigns.stream()
                .flatMap(assign -> assign.getRhs().getChildren().stream())
                .filter(ArrayOperand.class::isInstance)
                .count();

        Assert.assertTrue("Multidimensional array lowering should emit stores into array elements", arrayStores >= 2);
        Assert.assertTrue("Multidimensional array lowering should materialize at least one array read", arrayReads >= 1);
    }

    private static OllirResult toOllir(String code) {
        var config = new HashMap<String, String>();

        var lexerResult = new JmmLexerImpl().lex(code, config);
        Assert.assertFalse("Lexing should succeed", lexerResult.hasErrors());

        var parserResult = new JmmParserImpl().parse(lexerResult, config);
        Assert.assertFalse("Parsing should succeed", parserResult.hasErrors());

        JmmSemanticsResult semanticsResult = new JmmAnalysisImpl().semanticAnalysis(parserResult);
        Assert.assertTrue("Semantic analysis should not emit errors",
                semanticsResult.getReports(ReportType.ERROR).isEmpty());

        return new JmmOptimizationImpl().toOllir(semanticsResult);
    }

    private static void assertNoErrors(OllirResult result) {
        Assert.assertTrue("OLLIR generation should not emit errors",
                result.reports().stream().noneMatch(report -> report.getType() == ReportType.ERROR));
    }
}
