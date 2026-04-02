// java
package pt.up.fe.comp.cp1.core.parser.arrays;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class ReadArrayParserTest extends JmmTestEnv {


    public ReadArrayParserTest() {
        super("", "");
    }

    @Test
    public void simpleReadArray() {
        setDescription("Test that reading from an array is correctly parsed");
        parseSnippet("a[0];", STATEMENT);
    }

    @Test
    public void readArrayWithVariable() {
        setDescription("Test that reading from an array with a variable is correctly parsed");
        parseSnippet("a[foo];", STATEMENT);
    }

    @Test
    public void readArrayWithExpression() {
        setDescription("Test that reading from an array with an expression is correctly parsed");
        parseSnippet("a[1 + 2];", STATEMENT);
    }

    @Test
    public void readArrayWithMethodCall() {
        setDescription("Test that reading from an array with a method call is correctly parsed");
        parseSnippet("a[b.foo()];", STATEMENT);
    }

    @Test
    public void arrayTargetWithMethodCall() {
        setDescription("Test that reading from an array with a method call as target is correctly parsed");
        parseSnippet("a.foo()[0];", STATEMENT);
    }

    @Test
    public void arrayTargetWithExpression() {
        setDescription("Test that reading from an array with an expression as target is correctly parsed");
        parseSnippet("(new Foo().get())[0];", STATEMENT);
    }
}
