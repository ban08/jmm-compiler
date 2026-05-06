package core.optimization;

import org.junit.Assert;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp2026.analysis.JmmAnalysisImpl;
import pt.up.fe.comp2026.lexer.JmmLexerImpl;
import pt.up.fe.comp2026.optimization.JmmOptimizationImpl;
import pt.up.fe.comp2026.parser.JmmParserImpl;

import java.util.HashMap;

final class MyOllirTestSupport {

    private MyOllirTestSupport() {
    }

    static OllirResult toOllir(String code) {
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

    static void assertNoErrors(OllirResult result) {
        Assert.assertTrue("OLLIR generation should not emit errors",
                result.reports().stream().noneMatch(report -> report.getType() == ReportType.ERROR));
    }

    static int countOccurrences(String text, String needle) {
        int count = 0;
        int index = text.indexOf(needle);

        while (index >= 0) {
            count++;
            index = text.indexOf(needle, index + needle.length());
        }

        return count;
    }
}
