// java
package pt.up.fe.comp.cp1.extensions.parser.declarations;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class MethodVisibilityParserTest extends JmmTestEnv {


    public MethodVisibilityParserTest() {
        super("", "");
    }

    @Test
    public void testPrivateMethodDeclaration() {
        setDescription("Parse a private instance method declaration");
        parseSnippet("private void foo() {} ", METHOD);
    }

    @Test
    public void testProtectedMethodDeclaration() {
        setDescription("Parse a protected instance method declaration");
        parseSnippet("protected int bar() { } ", METHOD);
    }

    @Test
    public void testPrivateStaticMethodDeclaration() {
        setDescription("Parse a private static method declaration");
        parseSnippet("private static void helper() {} ", METHOD);
    }

    @Test
    public void testProtectedStaticMethodDeclaration() {
        setDescription("Parse a protected static method declaration");
        parseSnippet("protected static int compute() {  } ", METHOD);
    }

    @Test
    public void testClassWithMixedMethodVisibilities() {
        setDescription("Parse a class with methods of varying visibilities");
        parseSnippet("""
                class A {
                    private void privateMethod() {}
                    protected void protectedMethod() {}
                    public void publicMethod() {}
                    void packageProtectedMethod() {}
                    private static void privateStaticMethod() {}
                    protected static void protectedStaticMethod() {}
                    public static void publicStaticMethod() {}
                    static void packageProtectedStaticMethod() {}
                }""", CLASS);
    }
}
