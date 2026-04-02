// java
package pt.up.fe.comp.cp1.core.parser.calls;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class InstantiationParserTest extends JmmTestEnv {


    public InstantiationParserTest() {
        super("", "");
    }

    @Test
    public void testExprNewClass() {
        parseSnippet("new Foo();", STATEMENT);
    }
    

    @Test
    public void testInAssignment() {
        parseSnippet("a = new Foo();", STATEMENT);
    }

    @Test
    public void testInMethodCall() {
        parseSnippet("a.foo(new Foo());", STATEMENT);
    }

}
