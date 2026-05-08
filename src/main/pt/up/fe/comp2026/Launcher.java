package pt.up.fe.comp2026;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp2026.analysis.JmmAnalysisImpl;
import pt.up.fe.comp2026.lexer.JmmLexerImpl;
import pt.up.fe.comp2026.optimization.JmmOptimizationImpl;
import pt.up.fe.comp2026.parser.JmmParserImpl;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Launcher {

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        Map<String, String> config = CompilerConfig.parseArgs(args);

        var inputFile = CompilerConfig.getInputFile(config).orElseThrow();
        if (!inputFile.isFile()) {
            throw new RuntimeException("Option '-i' expects a path to an existing input file, got '" + args[0] + "'.");
        }
        String code = SpecsIo.read(inputFile);

        var lexer = new JmmLexerImpl();
        var lexerResult = lexer.lex(code, config);
        lexerResult.throwIfErrors();
        var parser = new JmmParserImpl();
        var parserResult = parser.parse(lexerResult, config);
        parserResult.throwIfErrors();

        System.out.println("AST:");
        System.out.println(parserResult.rootNode().toTree());

        var sema = new JmmAnalysisImpl();
        JmmSemanticsResult semanticsResult = sema.semanticAnalysis(parserResult);

        System.out.println("Symbol Table:");
        System.out.println(semanticsResult.getSymbolTable().print());

        System.out.println("Annotated AST:");
        System.out.println(semanticsResult.getRootNode().toTree());

        printReports(semanticsResult.reports());
        semanticsResult.throwIfErrors();

        var optimization = new JmmOptimizationImpl();
        semanticsResult = optimization.transformAst(semanticsResult);
        var ollirResult = optimization.toOllir(semanticsResult);
        ollirResult = optimization.transformOllir(ollirResult);

        System.out.println("OLLIR CODE:");
        System.out.println(ollirResult.getOllirCode());

        printReports(ollirResult.reports());
        ollirResult.throwIfErrors();
    }

    private static void printReports(List<Report> reports) {
        reports.stream()
                .filter(report -> report.getType() != ReportType.ERROR)
                .sorted(Comparator.naturalOrder())
                .forEach(System.out::println);
    }

}
