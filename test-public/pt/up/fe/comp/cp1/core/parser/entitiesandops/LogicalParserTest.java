// java
package pt.up.fe.comp.cp1.core.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class LogicalParserTest extends JmmTestEnv {


    public LogicalParserTest() {
        super("", "");
    }

    /*
    @Test
    public void testBooleanLiteralAccess() {
        setDescription("Test that accessing a boolean literal is correctly parsed");
        parseSnippetWithErrors("true = false;", STATEMENT);
        parseSnippet("true;", STATEMENT);
        parseSnippet("false;", STATEMENT);
    }
     */

    @Test
    public void testExprNot() {
        /*TestUtils.*/
        parseSnippet("!true;", STATEMENT);
    }

    @Test
    public void testExprNotID() {
        /*TestUtils.*/
        parseSnippet("!a;", STATEMENT);
    }

    @Test
    public void testExprNotWithParenthesis() {
        /*TestUtils.*/
        parseSnippet("!(a);", STATEMENT);
    }

    @Test
    public void testExprRelational() {
        /*TestUtils.*/
        parseSnippet("1 < 2;", STATEMENT);
    }

    @Test
    public void testExprRelationalChain() {
        /*TestUtils.*/
        parseSnippet("1 < 2 < 3 < 4;", STATEMENT);
    }

    @Test
    public void testExprLogical() {
        /*TestUtils.*/
        parseSnippet("1 && 2;", STATEMENT);
    }

    @Test
    public void testExprLogicalChain() {
        /*TestUtils.*/
        parseSnippet("1 && 2 && 3 && 4;", STATEMENT);
    }

    @Test
    public void testExprChain() {
        /*TestUtils.*/
        parseSnippet("1 && 2 < 3 + 4 - 5 * 6 / 7;", STATEMENT);
    }

}
