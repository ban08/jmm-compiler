// java
package pt.up.fe.comp.cp1.extensions.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class AdditionalArithmeticParserTest extends JmmTestEnv {


    public AdditionalArithmeticParserTest() {
        super("", "");
    }

    @Test
    public void testModuloBinaryExpression() {
        setDescription("Test parsing of binary modulo operator in expressions");
        parseSnippet("a % b;", STATEMENT);
    }

    @Test
    public void testModuloBinaryExpressionOk() {
        setDescription("Valid modulo binary expression variant");
        parseSnippet("(a + 1) % (b - 2);", STATEMENT);
    }

    @Test
    public void testPreDecrementUnaryExpression() {
        setDescription("Test parsing of pre-decrement unary operator (--x)");
        parseSnippet("--x;", STATEMENT);
    }

    @Test
    public void testPreDecrementUnaryExpressionOk() {
        setDescription("Valid pre-decrement used in larger expression");
        parseSnippet("y + --x;", STATEMENT);
    }

    @Test
    public void testPreIncrementUnaryExpression() {
        setDescription("Test parsing of pre-increment unary operator (++x)");
        parseSnippet("++x;", STATEMENT);
    }

    @Test
    public void testPreIncrementUnaryExpressionOk() {
        setDescription("Valid pre-increment used in larger expression");
        parseSnippet("z * ++x;", STATEMENT);
    }

    @Test
    public void testUnaryPlusExpression() {
        setDescription("Test parsing of unary plus operator (+x)");
        parseSnippet("+x;", STATEMENT);
    }

    @Test
    public void testUnaryPlusExpressionOk() {
        setDescription("Valid unary plus in numeric expression");
        parseSnippet("+5 + 3;", STATEMENT);
    }

    @Test
    public void testUnaryMinusExpression() {
        setDescription("Test parsing of unary minus operator (-x)");
        parseSnippet("-x;", STATEMENT);
    }

    @Test
    public void testUnaryMinusExpressionOk() {
        setDescription("Valid unary minus in numeric expression");
        parseSnippet("-5 - 2;", STATEMENT);
    }

}
