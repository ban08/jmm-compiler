// java
package pt.up.fe.comp.cp1.core.parser.declarations;

import org.junit.Test;
import pt.up.fe.comp.jmm.ast.Kind;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;

public class ClassDeclParserErrorTest extends JmmTestEnv {

    public ClassDeclParserErrorTest() {
        super("", "");
    }

    @Test
    public void testInvalidNestedMultiLineComment() {
        parseSnippetWithErrors("package x; /* /* Multi-line comment */ class A{}*/ class A{}");
        parseSnippet("package x; /* Multi-line comment */ /* class A{}*/ class A{}");
    }



    @Test
    public void testInvalidIncompleteExtends() {
        parseSnippetWithErrors("class A extends {}", CLASS);
        parseSnippet("class A extends Object {}", CLASS);
    }

    @Test
    public void testInvalidExtends() {
        parseSnippetWithErrors("class A extends 10 {}", CLASS);
        parseSnippet("class A extends Object {}", CLASS);
    }

    // New error tests for class declarations
    @Test
    public void testMissingClassName() {
        // class keyword without an identifier
        parseSnippetWithErrors("class {}", CLASS);
        parseSnippet("class A {}", CLASS);
    }

    @Test
    public void testMissingClassBraces() {
        // class name but missing body braces
        parseSnippetWithErrors("class A", CLASS);
        parseSnippet("class A {}", CLASS);
    }



    @Test
    public void testInvalidMethodMissingParentheses() {
        // method declaration without parentheses
        parseSnippetWithErrors("class A { void foo {} }", CLASS);
        parseSnippet("class A { void foo () {} }", CLASS);
    }

    @Test
    public void testInvalidClassNameStartingWithNumber() {
        // identifier starting with a digit is invalid
        parseSnippetWithErrors("class 1A {}", CLASS);
        parseSnippet("class A1 {}", CLASS);
    }

    @Test
    public void testMissingPackageSemicolon() {
        // package declaration missing semicolon
        parseSnippetWithErrors("package x class A{}");
        parseSnippet("package x; class A{}");
    }

    @Test
    public void testExtraTokensAfterClass() {
        // unexpected tokens after a valid class declaration
        parseSnippetWithErrors("package x; class A {} extra");
        parseSnippet("package x; class A {}");
    }

}
