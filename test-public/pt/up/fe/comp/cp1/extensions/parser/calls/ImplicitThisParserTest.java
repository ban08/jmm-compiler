// java
package pt.up.fe.comp.cp1.extensions.parser.calls;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class ImplicitThisParserTest extends JmmTestEnv {


    public ImplicitThisParserTest() {
        super("", "");
    }

    @Test
    public void testImplicitThis() {
        setDescription("Test that method calls without an explicit object are parsed as implicit this calls");
        parseSnippet("foo();", STATEMENT);
    }

    @Test
    public void testImplicitThisWithArguments() {
        setDescription("Test that method calls with arguments but without an explicit object are parsed as implicit this calls");
        parseSnippet("foo(1, true, a);", STATEMENT);
    }

    @Test
    public void testImplicitThisWithMethodCallArgument() {
        setDescription("Test that method calls with a method call as an argument and without an explicit object are parsed as implicit this calls");
        parseSnippet("foo(bar());", STATEMENT);
    }
}
