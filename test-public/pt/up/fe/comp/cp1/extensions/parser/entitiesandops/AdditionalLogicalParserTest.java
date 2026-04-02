// java
package pt.up.fe.comp.cp1.extensions.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class AdditionalLogicalParserTest extends JmmTestEnv {


    public AdditionalLogicalParserTest() {
        super("", "");
    }

    @Test
    public void testEqualsOperator() {
        setDescription("Test parsing of equality operator (==) in expressions");
        parseSnippet("a == b;", STATEMENT);
    }

    @Test
    public void testNotEqualsOperator() {
        setDescription("Test parsing of inequality operator (!=) in expressions");
        parseSnippet("a != b;", STATEMENT);
    }

    @Test
    public void testGreaterThanOperator() {
        setDescription("Test parsing of greater-than operator (>) in expressions");
        parseSnippet("x > y;", STATEMENT);
    }

    @Test
    public void testGreaterOrEqualOperator() {
        setDescription("Test parsing of greater-or-equal operator (>=) in expressions");
        parseSnippet("x >= y;", STATEMENT);
    }

    @Test
    public void testLessOrEqualOperator() {
        setDescription("Test parsing of less-or-equal operator (<=) in expressions");
        parseSnippet("x <= y;", STATEMENT);
    }

    @Test
    public void testLogicalOrOperator() {
        setDescription("Test parsing of logical-or operator (||) in expressions");
        parseSnippet("a || b;", STATEMENT);
    }

    @Test
    public void testComplexLogicalExpression() {
        setDescription("Test parsing of a combined logical/comparison expression");
        parseSnippet("(a == b) || (c >= d);", STATEMENT);
    }

    @Test
    public void testAndOrCombined() {
        setDescription("Test parsing of combined logical AND and OR operators");
        parseSnippet("a && b || c;", STATEMENT);
    }

}
