package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2026.CompilerConfig;
import pt.up.fe.comp2026.optimization.ast.AstOptimizer;
import pt.up.fe.comp2026.optimization.register.RegisterAllocationOptimizer;

import java.util.Collections;

public class JmmOptimizationImpl implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        // Create visitor that will generate the OLLIR code
        var visitor = new OllirGeneratorVisitor(semanticsResult.getSymbolTable());

        // Visit the AST and obtain OLLIR code
        var ollirCode = visitor.visit(semanticsResult.getRootNode());

//        System.out.println("\nOLLIR:\n\n" + ollirCode);

        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
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
