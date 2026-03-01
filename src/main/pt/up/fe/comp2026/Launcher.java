package pt.up.fe.comp2026;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2026.analysis.JmmAnalysisImpl;
import pt.up.fe.comp2026.backend.JasminBackendImpl;
import pt.up.fe.comp2026.lexer.JmmLexerImpl;
import pt.up.fe.comp2026.optimization.JmmOptimizationImpl;
import pt.up.fe.comp2026.parser.JmmParserImpl;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsSystem;

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



//        // Semantic Analysis stage
//        JmmAnalysisImpl sema = new JmmAnalysisImpl();
//        JmmSemanticsResult semanticsResult = sema.semanticAnalysis(parserResult);
//
//        System.out.println("Annotated AST:");
//        System.out.println(semanticsResult.getRootNode().toTree());
//
//        semanticsResult.throwIfErrors();
//
//        // Optimization stage
//        JmmOptimizationImpl ollirGen = new JmmOptimizationImpl();
//
//        // AST-based optimizations
//        if(semanticsResult.config().get("optimize") != null && semanticsResult.config().get("optimize").equals("true")){
//            semanticsResult = ollirGen.optimize(semanticsResult);
//
//            System.out.println("Optimized AST:");
//            System.out.println("AST: " + semanticsResult.getRootNode().toTree());
//        }
//
//        OllirResult ollirResult = ollirGen.toOllir(semanticsResult);
//        ollirResult.throwIfErrors();
//
//        // Print OLLIR code
//        System.out.println(ollirResult.getOllirCode());
//
//        // Code generation stage
//        JasminBackendImpl jasminGen = new JasminBackendImpl();
//        JasminResult jasminResult = jasminGen.toJasmin(ollirResult);
//        jasminResult.throwIfErrors();
//
//        // Print Jasmin code
//        System.out.println(jasminResult.getJasminCode());
    }

}
