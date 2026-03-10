// java
package pt.up.fe.comp.cp1.core.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;

public class ArithmeticParserErrorTest extends JmmTestEnv {


    public ArithmeticParserErrorTest() {
        super("", "");
    }

    @Test
    public void testInvalidOperators() {
        setDescription("Test that expressions with invalid operators are not parsed");
        parseSnippetWithErrors("1 $ 2", EXPRESSION);
        parseSnippetWithErrors("1 @ 2", EXPRESSION);
        parseSnippetWithErrors("1 # 2", EXPRESSION);
        parseSnippetWithErrors("1 a 2", EXPRESSION);
        parseSnippet("1 / 2", EXPRESSION);
    }

    @Test
    public void testInvalidOperands() {
        setDescription("Test that expressions with invalid operands are not parsed");
        parseSnippetWithErrors("1 + ", EXPRESSION);
        parseSnippetWithErrors("1 / ", EXPRESSION);
        parseSnippetWithErrors("/ 1", EXPRESSION);
        parseSnippet("1 + 2 / 3", EXPRESSION);
    }


    @Test
    public void testWrongNumberOfParentheses() {
        setDescription("Test that expressions with wrong number of parentheses are not parsed");
        parseSnippetWithErrors("()", EXPRESSION);
        parseSnippetWithErrors("(a", EXPRESSION);
        parseSnippetWithErrors("a)", EXPRESSION);
        parseSnippetWithErrors("((a)", EXPRESSION);
        parseSnippetWithErrors("(a))", EXPRESSION);
        parseSnippetWithErrors("(a)(a)", EXPRESSION);
        parseSnippet("((a))", EXPRESSION);
    }
}
