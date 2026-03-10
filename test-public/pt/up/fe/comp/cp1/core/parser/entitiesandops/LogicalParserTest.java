// java
package pt.up.fe.comp.cp1.core.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;

public class LogicalParserTest extends JmmTestEnv {


    public LogicalParserTest() {
        super("", "");
    }

    /*
    @Test
    public void testBooleanLiteralAccess() {
        setDescription("Test that accessing a boolean literal is correctly parsed");
        parseSnippetWithErrors("true = false", EXPRESSION);
        parseSnippet("true", EXPRESSION);
        parseSnippet("false", EXPRESSION);
    }
     */

    @Test
    public void testExprNot() {
        /*TestUtils.*/
        parseSnippet("!true", EXPRESSION);
    }

    @Test
    public void testExprNotID() {
        /*TestUtils.*/
        parseSnippet("!a", EXPRESSION);
    }

    @Test
    public void testExprNotWithParenthesis() {
        /*TestUtils.*/
        parseSnippet("!(a)", EXPRESSION);
    }

    @Test
    public void testExprRelational() {
        /*TestUtils.*/
        parseSnippet("1 < 2", EXPRESSION);
    }

    @Test
    public void testExprRelationalChain() {
        /*TestUtils.*/
        parseSnippet("1 < 2 < 3 < 4", EXPRESSION);
    }

    @Test
    public void testExprLogical() {
        /*TestUtils.*/
        parseSnippet("1 && 2", EXPRESSION);
    }

    @Test
    public void testExprLogicalChain() {
        /*TestUtils.*/
        parseSnippet("1 && 2 && 3 && 4", EXPRESSION);
    }

    @Test
    public void testExprChain() {
        /*TestUtils.*/
        parseSnippet("1 && 2 < 3 + 4 - 5 * 6 / 7", EXPRESSION);
    }

}
