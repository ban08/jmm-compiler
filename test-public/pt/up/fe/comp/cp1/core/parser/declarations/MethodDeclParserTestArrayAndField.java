// java
package pt.up.fe.comp.cp1.core.parser.declarations;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class MethodDeclParserTestArrayAndField extends JmmTestEnv {

    public MethodDeclParserTestArrayAndField() {
        super("", "");
    }


    @Test
    public void testInstanceMethodEmpty() {
        setDescription("Parse an instance method with multiple parameters and empty body");
        parseSnippet("int foo(int anInt, int[] anArray, boolean aBool, String aString) {}",
                METHOD);
    }

    @Test
    public void testMethodWithMultipleLocalVars() {
        setDescription("Parse a method with multiple local variable declarations");
        parseSnippet("void foo() {int a; int[] b; int c; boolean d; Bar e;}", METHOD);
    }

    @Test
    public void testMethodWithMultipleParametersAndLocalVars() {
        parseSnippet("""
                void foo(int anInt, int[] anArray, boolean aBool, String aString) {
                    int a; int[] b; int c; boolean d; Bar e;
                }
                """, METHOD);
    }

    @Test
    public void testMethodReturnsArray() {
        setDescription("Parse a method that returns an array type");
        parseSnippet("int[] foo() {}", METHOD);
    }

    @Test
    public void testMethodWithMultilineParameters() {
        setDescription("Parse a method with parameters split across multiple lines");
        parseSnippet("""
                void foo(
                    int a,
                    int[] b,
                    String s
                ) {}
                """, METHOD);
    }

}
