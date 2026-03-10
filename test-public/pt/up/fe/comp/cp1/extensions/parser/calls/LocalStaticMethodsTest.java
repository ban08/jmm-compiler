// java
package pt.up.fe.comp.cp1.extensions.parser.calls;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class LocalStaticMethodsTest extends JmmTestEnv {


    public LocalStaticMethodsTest() {
        super("", "");
    }

    @Test
    public void testLocalStaticMethodCall() {
        setDescription("Test parsing of a call to a local static method");
        parseSnippet("static void foo() {}", METHOD);
    }

    @Test
    public void testLocalStaticMethodCallWithArgs() {
        setDescription("Test parsing of a call to a local static method with arguments");
        parseSnippet("static void bar(int x) { }", METHOD);
    }

    @Test
    public void testStaticMethodWithPublicModifier() {
        setDescription("Test parsing of a local static method with the public modifier");
        parseSnippet("public static void baz() { }", METHOD);
    }


}
