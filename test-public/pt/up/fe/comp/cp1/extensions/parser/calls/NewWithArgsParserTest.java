// java
package pt.up.fe.comp.cp1.extensions.parser.calls;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;

public class NewWithArgsParserTest extends JmmTestEnv {


    public NewWithArgsParserTest() {
        super("", "");
    }

    @Test
    public void testNewWithSingleArgument() {
        parseSnippet("new Foo(42)", EXPRESSION);
    }

    @Test
    public void testNewWithTwoArguments() {
        parseSnippet("new Foo(1, 2)", EXPRESSION);
    }

    @Test
    public void testNewWithMultipleArguments() {
        parseSnippet("new Foo(1, 2, 3, 4)", EXPRESSION);
    }

    @Test
    public void testNewWithExpressionArguments() {
        parseSnippet("new Foo(1 + 2, this.bar())", EXPRESSION);
    }
}
