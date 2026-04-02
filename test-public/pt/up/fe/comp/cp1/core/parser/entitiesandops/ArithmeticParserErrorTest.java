// java
package pt.up.fe.comp.cp1.core.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class ArithmeticParserErrorTest extends JmmTestEnv {


    public ArithmeticParserErrorTest() {
        super("", "");
    }

    @Test
    public void testInvalidOperators() {
        setDescription("Test that expressions with invalid operators are not parsed");
        parseSnippetWithErrors("1 $ 2;", STATEMENT);
        parseSnippetWithErrors("1 @ 2;", STATEMENT);
        parseSnippetWithErrors("1 # 2;", STATEMENT);
        parseSnippetWithErrors("1 a 2;", STATEMENT);
        parseSnippet("1 / 2;", STATEMENT);
    }

    @Test
    public void testInvalidOperands() {
        setDescription("Test that expressions with invalid operands are not parsed");
        parseSnippetWithErrors("1 + ;", STATEMENT);
        parseSnippetWithErrors("1 / ;", STATEMENT);
        parseSnippetWithErrors("/ 1;", STATEMENT);
        parseSnippet("1 + 2 / 3;", STATEMENT);
    }

    @Test
    public void testWrongNumberOfParentheses() {
        setDescription("Test that expressions with wrong number of parentheses are not parsed");
        parseSnippetWithErrors("();", STATEMENT);
        parseSnippetWithErrors("(a;", STATEMENT);
        parseSnippetWithErrors("a);", STATEMENT);
        parseSnippetWithErrors("((a);", STATEMENT);
        parseSnippetWithErrors("(a));", STATEMENT);
        parseSnippetWithErrors("(a)(a);", STATEMENT);
        parseSnippet("((a));", STATEMENT);
    }
}
