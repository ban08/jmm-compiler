// java
package pt.up.fe.comp.cp1.extensions.parser.declarations;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;

public class ArbitraryPositionParserTest extends JmmTestEnv {

    public ArbitraryPositionParserTest() {
        super("", "");
    }


    @Test
    public void testMethodThenField() {
        setDescription("Method followed by field inside a class should parse");
        parseSnippet("""
                class A {
                   void foo() {}
                   int a;
                }""", CLASS);
    }

    @Test
    public void testFieldMethodField() {
        setDescription("Field, followed by method, followed by field inside a class should parse");
        parseSnippet("""
                class A {
                   int b;
                   void foo() {}
                   int b;
                }""", CLASS);
    }


    @Test
    public void testMethodsWithParametersAndFields() {
        setDescription("Methods with parameters appearing alongside fields should parse");
        parseSnippet("""
                class A {
                    void add(int x) { }
                    int count;
                    void reset() { }
                }""", CLASS);
    }


    @Test
    public void testInterleavedFieldsAndMethods() {
        setDescription("Interleaved fields and methods should parse in arbitrary order");
        parseSnippet("""
                class A {
                    int a;
                    void f1() {}
                    int b;
                    void f2() {}
                    String s;
                }""", CLASS);
    }

}
