// java
package pt.up.fe.comp.cp1.core.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class ArithmeticParserTest extends JmmTestEnv {


    public ArithmeticParserTest() {
        super("", "");
    }

    /*
        @Test
        public void testIntegerLiteralAccess() {
            setDescription("Test that accessing an integer  literal is correctly parsed");
            parseSnippet("0;", STATEMENT);
            parseSnippet("42;", STATEMENT);
            parseSnippet(String.valueOf(Integer.MAX_VALUE), EXPRESSION);
        }
    */
    /*
        @Test
        public void testExprAdd() {
            parseSnippet("2 + 3;", STATEMENT);
        }
    */
    @Test
    public void testExprSub() {
        /*TestUtils.*/
        parseSnippet("2 - 3;", STATEMENT);
    }


    @Test
    public void testExprAddChain() {
        /*TestUtils.*/
        parseSnippet("1 + 2 - 3 + 4;", STATEMENT);
    }

    @Test
    public void testWithID() {
        setDescription("Test that an expression with an identifier is correctly parsed");
        parseSnippet("a / 1;", STATEMENT);
    }

    /*
        @Test
        public void testExprMult() {
            parseSnippet("2 * 3;", STATEMENT);
        }
    */
    @Test
    public void testExprDiv() {
        /*TestUtils.*/
        parseSnippet("2 / 3;", STATEMENT);
    }


    @Test
    public void testSimpleIntWithMultipleExpressions() {
        setDescription("Test that an expression with multiple expressions is correctly parsed");
        parseSnippet("0 + 1 * 2 / 3 - 4;", STATEMENT);
    }

    @Test
    public void testExprMultChain() {
        /*TestUtils.*/
        parseSnippet("1 * 2 / 3 * 4;", STATEMENT);
    }

    @Test
    public void testParenthesizedAccess() {
        setDescription("Test that accessing an entity with parentheses is correctly parsed");
        parseSnippet("(a);", STATEMENT);
    }

    @Test
    public void testNestedParenthesizedAccess() {
        setDescription("Test that accessing an entity with nested parentheses is correctly parsed");
        parseSnippet("((a));", STATEMENT);
    }

    @Test
    public void testIntWithParentheses() {
        setDescription("Test that an expression with parentheses is correctly parsed");
        parseSnippet("(0 + 1) * 2;", STATEMENT);
    }

    @Test
    public void testIntWithMultipleParentheses() {
        setDescription("Test that an expression with multiple parentheses is correctly parsed");
        parseSnippet("((0 + 1) * 2) / (3 - 4);", STATEMENT);
    }
}
