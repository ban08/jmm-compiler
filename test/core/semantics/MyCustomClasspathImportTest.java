package core.semantics;

import org.junit.Assert;
import org.junit.Test;
import pt.up.fe.comp2026.analysis.JmmAnalysisImpl;
import pt.up.fe.comp2026.lexer.JmmLexerImpl;
import pt.up.fe.comp2026.parser.JmmParserImpl;

import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.util.HashMap;

public class MyCustomClasspathImportTest {

    @Test
    public void customClasspathIsUsedForImportResolution() throws Exception {
        var tempDir = Files.createTempDirectory("jmm-extra-classpath");
        var packageDir = Files.createDirectories(tempDir.resolve("ext"));
        var javaFile = packageDir.resolve("External.java");

        Files.writeString(javaFile, """
                package ext;

                public class External {
                }
                """);

        var compiler = ToolProvider.getSystemJavaCompiler();
        Assert.assertNotNull("Expected a JDK compiler to be available during tests", compiler);

        var compileResult = compiler.run(null, null, null,
                "-d", tempDir.toString(),
                javaFile.toString());
        Assert.assertEquals("Failed to compile helper class for custom classpath test", 0, compileResult);

        var config = new HashMap<String, String>();
        config.put("classpath", tempDir.toString());

        var code = """
                package p;
                import ext.External;
                class A {
                    public External foo() {
                        return new External();
                    }
                }
                """;

        var lexerResult = new JmmLexerImpl().lex(code, config);
        Assert.assertFalse("Lexing should succeed", lexerResult.hasErrors());

        var parserResult = new JmmParserImpl().parse(lexerResult, config);
        Assert.assertFalse("Parsing should succeed", parserResult.hasErrors());

        var semanticsResult = new JmmAnalysisImpl().semanticAnalysis(parserResult);
        Assert.assertFalse("Semantic analysis should honor the configured extra classpath", semanticsResult.hasErrors());
    }
}
