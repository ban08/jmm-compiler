// java
package pt.up.fe.comp.cp1.core.parser.declarations;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class MethodDeclParserTest extends JmmTestEnv {

    public MethodDeclParserTest() {
        super("", "");
    }


    @Test
    public void testSimpleMethodEmpty() {
        setDescription("Parse a simple method with empty body");
        parseSnippet("void foo() {}",
                METHOD);
    }

    @Test
    public void testPublicMethodEmpty() {
        setDescription("Parse a public method with empty body");
        parseSnippet("public void foo() {}",
                METHOD);
    }

    @Test
    public void testStaticMethodEmpty() {
        setDescription("Parse a static method with empty body");
        parseSnippet("static void foo() {}",
                METHOD);
    }

    @Test
    public void testMethodSingleArgEmpty() {
        setDescription("Parse a method with a single argument and empty body");
        parseSnippet("void foo(int a) {}",
                METHOD);
    }

    @Test
    public void testMainMethodEmpty() {
        setDescription("Parse the public static void main method");
        parseSnippet("public static void main(String[] args) {}", METHOD);
    }

    @Test
    public void testInstanceMethodEmpty() {
        setDescription("Parse an instance method with multiple parameters and empty body");
        parseSnippet("int foo(int anInt, boolean aBool, String aString) {}",
                METHOD);
    }

    @Test
    public void testMethodWithOneLocalVar() {
        setDescription("Parse a method with a local variable declaration");
        parseSnippet("void foo() {int a;}", METHOD);
    }

    @Test
    public void testMethodWithMultipleLocalVars() {
        setDescription("Parse a method with multiple local variable declarations");
        parseSnippet("void foo() {int a; int c; boolean d; Bar e;}", METHOD);
    }

    @Test
    public void testMethodWithMultipleParametersAndLocalVars() {
        parseSnippet("""
                void foo(int anInt, boolean aBool, String aString) {
                    int a; int c; boolean d; Bar e;
                }
                """, METHOD);
    }


    @Test
    public void testMethodReturnsCustomType() {
        setDescription("Parse a method that returns a custom (non-primitive) type");
        parseSnippet("MyClass foo() {}", METHOD);
    }

    @Test
    public void testMethodWithMultipleModifiers() {
        setDescription("Parse a method with public and static modifiers together");
        parseSnippet("public static int foo() {}", METHOD);
    }

    @Test
    public void testMethodSignatureWithComments() {
        setDescription("Parse a method signature containing inline comments and whitespace");
        parseSnippet("public /*c*/ int foo(/*p*/int a/*e*/, int b) {}", METHOD);
    }


    @Test
    public void testMethodWithMultilineParameters() {
        setDescription("Parse a method with parameters split across multiple lines");
        parseSnippet("""
                void foo(
                    int a,
                    String s
                ) {}
                """, METHOD);
    }

    @Test
    public void testMethodWithManyParameters() {
        setDescription("Parse a method with many parameters on a single line");
        parseSnippet("void foo(int a,int b,int c,int d,int e) {}", METHOD);
    }
}
