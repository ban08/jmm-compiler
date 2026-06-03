package pt.up.fe.comp2026.optimization;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.ollir.OllirUtils;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2026.CompilerConfig;
import pt.up.fe.comp2026.optimization.ast.AstOptimizer;
import pt.up.fe.comp2026.optimization.register.RegisterAllocationOptimizer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JmmOptimizationImpl implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        // Create visitor that will generate the OLLIR code
        var visitor = new OllirGeneratorVisitor(semanticsResult.getSymbolTable());

        // Visit the AST and obtain OLLIR code
        var ollirCode = visitor.visit(semanticsResult.getRootNode());

//        System.out.println("\nOLLIR:\n\n" + ollirCode);

        return buildOllirResult(semanticsResult, ollirCode);
    }

    private static OllirResult buildOllirResult(JmmSemanticsResult semanticsResult, String ollirCode) {
        var parseResult = OllirUtils.parse(ollirCode);
        if (!parseResult.hasErrors() && parseResult.classUnit() != null) {
            return new SourcePreservingOllirResult(
                    parseResult.classUnit(),
                    semanticsResult.reports(),
                    semanticsResult.config(),
                    ollirCode);
        }

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

    private static final class SourcePreservingOllirResult extends OllirResult {

        private final String sourceCode;

        private SourcePreservingOllirResult(ClassUnit ollirClass, List<Report> reports,
                                           Map<String, String> config, String sourceCode) {
            super(ollirClass, reports, config);
            this.sourceCode = sourceCode;
        }

        @Override
        public String getOllirCode() {
            return sourceCode;
        }
    }

    @Override
    public JmmSemanticsResult transformAst(JmmSemanticsResult semanticsResult) {

        if (!CompilerConfig.getOptimize(semanticsResult.config())) {
            return semanticsResult;
        }

        new AstOptimizer(semanticsResult.getSymbolTable()).optimize(semanticsResult.getRootNode());
        return semanticsResult;
    }

    @Override
    public OllirResult transformOllir(OllirResult ollirResult) {

        if (ollirResult.config().getOrDefault("debug", "false").equals("true")) {
            System.out.println("OLLIR CODE:");
            System.out.println(ollirResult.getOllirCode());
        }

        var registerAllocation = CompilerConfig.getRegisterAllocation(ollirResult.config());
        if (registerAllocation >= 0 && ollirResult.getOllirClass() != null) {
            ollirResult.reports().addAll(new RegisterAllocationOptimizer()
                    .optimize(ollirResult.getOllirClass(), registerAllocation));
        }

        return ollirResult;
    }

}
