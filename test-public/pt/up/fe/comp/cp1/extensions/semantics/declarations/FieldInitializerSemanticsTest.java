// java
package pt.up.fe.comp.cp1.extensions.semantics.declarations;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;

public class FieldInitializerSemanticsTest extends JmmTestEnv {


    public FieldInitializerSemanticsTest() {
        super("", "");
    }

    @Test
    public void testFieldInitializerIntLiteral() {
        setDescription("Fields with integer literal initializers should be correctly validated in semantic analysis");
        semanticsFromSnippet("""
                package x;
                class A {
                    int a = 1;
                }""", false);
    }

    @Test
    public void testFieldInitializerBooleanLiteral() {
        setDescription("Field initializer with boolean literal should be correctly validated in semantic analysis");
        semanticsFromSnippet("""
                package x;
                class A {
                    boolean b = false;
                }""", false);
    }


    @Test
    public void testFieldsWithWrongLiteralInitializer() {
        setDescription("Fields with an initializer with different type should report an error in semantic analysis");
        semanticsFromSnippet("""
                package x;
                class A {
                    int a = true;
                }""", true);
        semanticsFromSnippet("""
                package x;
                class A {
                    boolean b = 123;
                }""", true);
        semanticsFromSnippet("""
                package x;
                class A {
                    int a = 1;
                }""", false);
    }

    @Test
    public void testFieldInitializerExpression() {
        setDescription("Field initializer with simple expression should be correctly validated in semantic analysis");
        semanticsFromSnippet("""
                package x;
                class A {
                    int c = 1 + 2 * 3;
                }""", false);
    }

    @Test
    public void testFieldInitializerMethodInvocation() {
        setDescription("Field initializer using a method invocation (with method defined) should parse");
        semanticsFromSnippet("""
                package x;
                class A {
                    int a = this.init();
                    int init(){return 0;}
                }""", false);
    }

    @Test
    public void testFieldInitializerNewObject() {
        setDescription("Field initializer using new object should parse when class exists");
        semanticsFromSnippet("""
                package x;
                class A {
                    A b = new A();
                }""", false);
    }

    @Test
    public void testFieldInitializerOtherClass() {
        setDescription("Field initializer using new object should parse when class exists");
        semanticsFromSnippet("""
                package x;
                class A {
                    String b = new String();
                }""", false);
    }

}
