// java
package pt.up.fe.comp.cp1.core.parser.declarations;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class MethodDeclParserErrorTest extends JmmTestEnv {


    public MethodDeclParserErrorTest() {
        super("", "");
    }

    @Test
    public void testMethodWithMissingReturnType() {
        setDescription("Parse a method declaration missing the return type");
        parseSnippetWithErrors("foo() {}", METHOD);
        parseSnippet("int foo() {}", METHOD);
    }

    @Test
    public void testMethodWithMissingName() {
        setDescription("Parse a method declaration missing the method name");
        parseSnippetWithErrors("void () {}", METHOD);
        parseSnippet("void foo() {}", METHOD);
    }

    @Test
    public void testMethodWithMissingParameters() {
        setDescription("Parse a method declaration missing the parameter list");
        parseSnippetWithErrors("void foo {}", METHOD);
        parseSnippet("void foo() {}", METHOD);
    }

    @Test
    public void testMethodWithMissingBody() {
        setDescription("Parse a method declaration missing the method body");
        parseSnippetWithErrors("void foo();", METHOD);
        parseSnippet("void foo() {}", METHOD);
    }

    @Test
    public void testMethodWithInvalidParameter() {
        setDescription("Parse a method declaration with an invalid parameter");
        parseSnippetWithErrors("void foo(int a, String) {}", METHOD);
        parseSnippet("void foo(int a, String s) {}", METHOD);
    }

    @Test
    public void testMethodWithInvalidModifier() {
        setDescription("Parse a method declaration with an invalid modifier");
        parseSnippetWithErrors("timid void foo() {}", METHOD);
        parseSnippet("public void foo() {}", METHOD);
    }

    @Test
    public void testMethodWithMissingColonInParameter() {
        setDescription("Parse a method declaration with a parameter missing the colon");
        parseSnippetWithErrors("void foo(int a String s) {}", METHOD);
        parseSnippet("void foo(int a, String s) {}", METHOD);
    }

    @Test
    public void testMethodMissingOpeningParen() {
        setDescription("Method missing opening parenthesis");
        parseSnippetWithErrors("void foo) {}", METHOD);
        parseSnippet("void foo() {}", METHOD);
    }

    @Test
    public void testMethodMissingClosingParen() {
        setDescription("Method missing closing parenthesis");
        parseSnippetWithErrors("void foo( {}", METHOD);
        parseSnippet("void foo() {}", METHOD);

    }

    @Test
    public void testMethodParameterMissingType() {
        setDescription("Method parameter missing its type");
        parseSnippetWithErrors("void foo(a) {}", METHOD);
        parseSnippet("void foo(int a) {}", METHOD);
    }

    @Test
    public void testMethodParameterMissingName() {
        setDescription("Method parameter missing its name");
        parseSnippetWithErrors("void foo(int) {}", METHOD);
        parseSnippet("void foo(int a) {}", METHOD);
    }

    @Test
    public void testMethodUnbalancedBracesInBody() {
        setDescription("Method body with unbalanced braces (missing closing brace)");
        parseSnippetWithErrors("void foo() { int a; ", METHOD);
        parseSnippet("void foo() { int a; }", METHOD);

    }

    @Test
    public void testMethodInvalidReturnTypeNumber() {
        setDescription("Method has an invalid return type (number)");
        parseSnippetWithErrors("10 foo() {}", METHOD);
        parseSnippet("int foo() {}", METHOD);

    }



    @Test
    public void testMethodDuplicateModifier() {
        setDescription("Method with duplicate modifiers should be invalid");
        parseSnippetWithErrors("public static static void foo() {}", METHOD);
        parseSnippet("public static void foo() {}", METHOD);

    }


    @Test
    public void testMethodExtraTokensAfterSignature() {
        setDescription("Extra tokens after method signature before body");
        parseSnippetWithErrors("package x; class A{void foo() extra {}}");
        parseSnippet("package x; class A { void foo() {} }");
    }

}
