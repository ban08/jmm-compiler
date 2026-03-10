// java
package pt.up.fe.comp.cp1.extensions.parser.declarations;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;

public class FieldInitializerParserTest extends JmmTestEnv {


    public FieldInitializerParserTest() {
        super("", "");
    }

    @Test
    public void testFieldsWithInitializersAndMethods() {
        setDescription("Fields with initializers and methods together should parse");
        parseSnippet("class A { int a = 1; void foo() {} }", CLASS);
    }

    @Test
    public void testFieldInitializerIntLiteral() {
        setDescription("Field initializer with integer literal should parse");
        parseSnippet("class A { int a = 123; }", CLASS);
    }

    @Test
    public void testFieldInitializerBooleanLiteral() {
        setDescription("Field initializer with boolean literal should parse");
        parseSnippet("class A { boolean b = false; }", CLASS);
    }

    @Test
    public void testFieldInitializerExpression() {
        setDescription("Field initializer with simple expression should parse");
        parseSnippet("class A { int c = 1 + 2 * 3; }", CLASS);
    }

    @Test
    public void testFieldInitializerMethodInvocation() {
        setDescription("Field initializer using a method invocation (with method defined) should parse");
        parseSnippet("class A { int a = this.init(); int init(){return 0;} }", CLASS);
    }

    @Test
    public void testFieldInitializerNewObject() {
        setDescription("Field initializer using new object should parse");
        parseSnippet("class A { B b = new B(); }", CLASS);
    }

    @Test
    public void testFieldInitializerMissingExpression() {
        setDescription("Malformed field initializer (missing expression) should produce parse errors");
        parseSnippetWithErrors("class A { int a = ; }", CLASS);
        parseSnippet("class A { int a = 1; }", CLASS);
    }
}
