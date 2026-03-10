// java
package pt.up.fe.comp.cp1.core.parser.calls;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;

public class CallParserErrorTest extends JmmTestEnv {


    public CallParserErrorTest() {
        super("", "");
    }

    @Test
    public void testMethodWithMalformattedArgs() {
        setDescription("Test that a method call with the wrong format is not parsed");
        parseSnippetWithErrors("a.foo(,)", EXPRESSION);
        parseSnippetWithErrors("a.foo(1,)", EXPRESSION);
        parseSnippetWithErrors("a.foo(,1)", EXPRESSION);
        parseSnippetWithErrors("a.foo(1 2)", EXPRESSION);
        parseSnippetWithErrors("a.foo(1,,2)", EXPRESSION);
        parseSnippet("a.foo(1, 2)", EXPRESSION);
    }

    @Test
    public void testCallsWithBadDots() {
        setDescription("Test that a method call with the wrong format is not parsed");
        parseSnippetWithErrors("a..foo(1, 2)", EXPRESSION);
        parseSnippetWithErrors("a.foo..bar(1, 2)", EXPRESSION);
        parseSnippetWithErrors("a.foo.(1, 2)", EXPRESSION);
        parseSnippetWithErrors(".foo(1, 2)", EXPRESSION);
        parseSnippet("a.foo(1, 2)", EXPRESSION);
    }
}
