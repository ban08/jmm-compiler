// java
package pt.up.fe.comp.cp1.core.parser.arrays;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class WriteArrayParserTest extends JmmTestEnv {


    public WriteArrayParserTest() {
        super("", "");
    }

    @Test
    public void testWriteArray() {
        setDescription("Test that writing to an array is correctly parsed");
        parseSnippet("void method(){foo[1] = 10;}", METHOD);
    }


    @Test
    public void testWriteArrayWithVariable() {
        setDescription("Test that writing to an array with a variable is correctly parsed");
        parseSnippet("void method(){foo[bar] = 10;}", METHOD);
    }

    @Test
    public void testWriteArrayWithExpression() {
        setDescription("Test that writing to an array with an expression is correctly parsed");
        parseSnippet("void method(){foo[1 + 2] = 10;}", METHOD);
    }

    @Test
    public void testWriteArrayWithMethodCall() {
        setDescription("Test that writing to an array with a method call is correctly parsed");
        parseSnippet("void method(){foo[a.bar()] = 10;}", METHOD);
    }

    @Test
    public void testWriteArrayWithExpressionValue() {
        setDescription("Test that writing to an array with an expression as value is correctly parsed");
        parseSnippet("void method(){foo[1] = 10 + 20;}", METHOD);
    }

    @Test
    public void testWriteArrayWithArrayAccess() {
        setDescription("Test that writing to an array with an array access as value is correctly parsed");
        parseSnippet("void method(){foo[1] = bar[2];}", METHOD);
    }
}
