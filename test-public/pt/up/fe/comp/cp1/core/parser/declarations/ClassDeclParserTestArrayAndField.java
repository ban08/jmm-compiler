// java
package pt.up.fe.comp.cp1.core.parser.declarations;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.TYPE;

public class ClassDeclParserTestArrayAndField extends JmmTestEnv {


    public ClassDeclParserTestArrayAndField() {
        super("", "");
    }

    @Test
    public void testIntArray() {
        parseSnippet("int[]", TYPE);
    }


    @Test
    public void testSingleFieldDecl() {
        parseSnippet("package comp; class Foo {int a;}");
    }

    @Test
    public void testMultipleFieldDecls() {
        parseSnippet("package comp; class Foo {int a; int[] b; int c; boolean d; Bar e;}");
    }



    @Test
    public void testSingleCharId() {
        parseSnippet("class A{int a;}", CLASS);
    }

    @Test
    public void testStringField() {
        parseSnippet("class A{String a;}", CLASS);
    }

    @Test
    public void testIdStartingChar1() {
        parseSnippet("class A{String _a;}", CLASS);
    }

    @Test
    public void testIdStartingChar2() {
        parseSnippet("class A{String $a;}", CLASS);
    }

    @Test
    public void testClassWithCommentsInside() {
        parseSnippet("class A { // comment\n int a; /* multi */ }", CLASS);
    }


    @Test
    public void testClassWithComplexIdentifiers() {
        parseSnippet("class A{int _a$1; String $s_2;}", CLASS);
    }


    @Test
    public void testClassWithOneFieldsAndOneMethod() {
        parseSnippet("""
                package foo;
                class A {
                    int a;
                    void bar() {}
                }
                """);
    }

    @Test
    public void testClassWithMultipleMethods() {
        parseSnippet("""
                package foo;
                class A {
                    public static void main(String[] args) {}
                    void bar() {}
                    void foo(int anInt, int[] anArray, boolean aBool, String aString) {}
                }
                """);
    }

    @Test
    public void testClassWithMultipleFieldsAndMethods() {
        parseSnippet("""
                package foo;
                class A {
                    int a; int[] b; int c; boolean d; Bar e;
                    public static void main(String[] args) {}
                    void bar() {}
                    void foo(int anInt, int[] anArray, boolean aBool, String aString) {}
                }
                """);
    }


    @Test
    public void testClassWithFieldArrayAndMethod() {
        parseSnippet("package p; class A { int[] arr; void m(int[] a) {} }");
    }
}
