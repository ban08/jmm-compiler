// java
package pt.up.fe.comp.cp1.core.parser.declarations;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ast.Kind;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.*;

public class ClassDeclParserTest extends JmmTestEnv {


    public ClassDeclParserTest() {
        super("", "");
    }


    @Test
    public void testSingleLineComment() {
        //use of comment should be independent of starting rule (it is ignored)
        parseSnippet("// Single line comment\nclass A{}", CLASS);
    }

    @Test
    public void testMultiLineComment() {
        parseSnippet("/* Multi-line comment*/\nclass A{}", CLASS);
    }



    @Test
    public void testClass() {
        parseSnippet("class Foo {}", CLASS);
    }

    @Test
    public void testClassWithSuper() {
        parseSnippet("class Foo extends Bar {}", CLASS);
    }

    @Test
    public void testClassFromRoot() {
        parseSnippet("package comp; class Foo {}");
    }



    @Test
    public void testPrimitiveBool() {
        parseSnippet("boolean", TYPE);
    }

    @Test
    public void testVoid() {
        parseSnippet("void", TYPE);
    }

    @Test
    public void testCustomType() {
        parseSnippet("MyClass", TYPE);
    }

    @Test
    public void testStringTypeArray() {
        parseSnippet("String[]", TYPE);
    }

    @Test
    public void testClassWithSimpleMethod() {
        parseSnippet("class A {void foo() {}}", CLASS);
    }


    @Test
    public void testClassWithMainMethod() {
        parseSnippet("package foo; class A {public static void main(String[] args) {}}");
    }



    @Test
    public void testClassWithMultipleMethods() {
        parseSnippet("""
                package foo;
                class A {
                    public static void main(String[] args) {}
                    void bar() {}
                    void foo(int anInt, boolean aBool, String aString) {}
                }
                """);
    }




}
