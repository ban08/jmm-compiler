// java
package pt.up.fe.comp.cp1.core.parser.arrays;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class NewArrayParserTest extends JmmTestEnv {


    public NewArrayParserTest() {
        super("", "");
    }

    @Test
    public void testNewIntArray() {
        setDescription("Test that creating a new int array is correctly parsed");
        parseSnippet("new int[10];", STATEMENT);
    }

    @Test
    public void testNewIntArrayWithExpression() {
        setDescription("Test that creating a new int array with an expression is correctly parsed");
        parseSnippet("new int[5 + 5];", STATEMENT);
    }

    @Test
    public void testNewIntArrayWithVariable() {
        setDescription("Test that creating a new int array with a variable is correctly parsed");
        parseSnippet("new int[foo];", STATEMENT);
    }

    @Test
    public void testNewIntArrayWithMethodCall() {
        setDescription("Test that creating a new int array with a method call is correctly parsed");
        parseSnippet("new int[a.foo()];", STATEMENT);
    }

}
