package core.semantics;

import org.junit.Assert;
import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp2026.analysis.JmmAnalysisImpl;
import pt.up.fe.comp2026.lexer.JmmLexerImpl;
import pt.up.fe.comp2026.parser.JmmParserImpl;

import javax.tools.ToolProvider;
import java.nio.file.Path;
import java.util.ArrayList;
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

    @Test
    public void customClasspathConstructorsAreValidated() throws Exception {
        var tempDir = Files.createTempDirectory("jmm-extra-classpath-constructors");
        var packageDir = Files.createDirectories(tempDir.resolve("ext"));
        var javaFile = packageDir.resolve("External.java");

        Files.writeString(javaFile, """
                package ext;

                public class External {
                    public External(int value) {
                    }
                }
                """);

        compileHelpers(tempDir, javaFile);

        var code = """
                package p;
                import ext.External;
                class A {
                    public External foo() {
                        return new External(true);
                    }
                }
                """;

        var semanticsResult = analyze(code, tempDir);
        Assert.assertTrue("Semantic analysis should reject invalid constructor calls from the configured extra classpath",
                semanticsResult.hasErrors());
    }

    @Test
    public void importedHierarchyFromCustomClasspathIsAssignableAndComparable() throws Exception {
        var tempDir = Files.createTempDirectory("jmm-extra-classpath-hierarchy");
        var packageDir = Files.createDirectories(tempDir.resolve("ext"));
        var baseFile = packageDir.resolve("Base.java");
        var childFile = packageDir.resolve("Child.java");

        Files.writeString(baseFile, """
                package ext;

                public class Base {
                }
                """);

        Files.writeString(childFile, """
                package ext;

                public class Child extends Base {
                }
                """);

        compileHelpers(tempDir, baseFile, childFile);

        var code = """
                package p;
                import ext.Base;
                import ext.Child;
                class A {
                    public boolean foo() {
                        Base a;
                        Child b;
                        b = new Child();
                        a = b;
                        return a == b;
                    }
                }
                """;

        var semanticsResult = analyze(code, tempDir);
        Assert.assertFalse(
                "Semantic analysis should accept assignment and comparison across imported inheritance from the configured extra classpath",
                semanticsResult.hasErrors());
    }

    @Test
    public void importedSubclassCanFlowIntoCurrentClassReference() throws Exception {
        var tempDir = Files.createTempDirectory("jmm-extra-classpath-current-class");
        var currentPackageDir = Files.createDirectories(tempDir.resolve("p"));
        var importedPackageDir = Files.createDirectories(tempDir.resolve("ext"));
        var currentClassStub = currentPackageDir.resolve("A.java");
        var childFile = importedPackageDir.resolve("Child.java");

        Files.writeString(currentClassStub, """
                package p;

                public class A {
                }
                """);

        Files.writeString(childFile, """
                package ext;

                import p.A;

                public class Child extends A {
                }
                """);

        compileHelpers(tempDir, currentClassStub, childFile);

        var code = """
                package p;
                import ext.Child;
                class A {
                    public A foo() {
                        A a;
                        Child child;
                        child = new Child();
                        a = child;
                        return a;
                    }
                }
                """;

        var semanticsResult = analyze(code, tempDir);
        Assert.assertFalse("Semantic analysis should accept imported subclasses flowing into the current class type",
                semanticsResult.hasErrors());
    }

    private static void compileHelpers(Path outputDir, Path... javaFiles) {
        var compiler = ToolProvider.getSystemJavaCompiler();
        Assert.assertNotNull("Expected a JDK compiler to be available during tests", compiler);

        var args = new ArrayList<String>();
        args.add("-d");
        args.add(outputDir.toString());
        for (var javaFile : javaFiles) {
            args.add(javaFile.toString());
        }

        var compileResult = compiler.run(null, null, null, args.toArray(String[]::new));
        Assert.assertEquals("Failed to compile helper class for custom classpath test", 0, compileResult);
    }

    private static JmmSemanticsResult analyze(String code, Path classpathDir) {
        var config = new HashMap<String, String>();
        config.put("classpath", classpathDir.toString());

        var lexerResult = new JmmLexerImpl().lex(code, config);
        Assert.assertFalse("Lexing should succeed", lexerResult.hasErrors());

        var parserResult = new JmmParserImpl().parse(lexerResult, config);
        Assert.assertFalse("Parsing should succeed", parserResult.hasErrors());

        return new JmmAnalysisImpl().semanticAnalysis(parserResult);
    }
}
