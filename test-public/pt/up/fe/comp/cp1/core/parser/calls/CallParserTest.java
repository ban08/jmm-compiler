// java
package pt.up.fe.comp.cp1.core.parser.calls;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class CallParserTest extends JmmTestEnv {


    public CallParserTest() {
        super("", "");
    }

    @Test
    public void testSimpleMethodCall() {
        setDescription("Test that a simple method call is correctly parsed");
        parseSnippet("a.foo();", STATEMENT);
    }

    @Test
    public void testMethodCallWithArgs() {
        setDescription("Test that a method call with arguments is correctly parsed");
        parseSnippet("a.foo(1, 2);", STATEMENT);
    }

    @Test
    public void testMethodCallWithExpressionArgs() {
        setDescription("Test that a method call with expression arguments is correctly parsed");
        parseSnippet("a.foo(1 + 2, this.bar());", STATEMENT);
    }

    @Test
    public void testMethodCallInExpression() {
        setDescription("Test that a method call in an expression is correctly parsed");
        parseSnippet("a.foo(1, 2) + 3;", STATEMENT);
    }

    @Test
    public void testMethodCallInAssignment() {
        setDescription("Test that a method call in an assignment is correctly parsed");
        parseSnippet("a = a.foo(1, 2);", STATEMENT);
    }

    @Test
    public void testMethodCallOnThis() {
        setDescription("Test that a method call on this is correctly parsed");
        parseSnippet("this.foo(1, 2);", STATEMENT);
    }

    @Test
    public void testMethodCallOnThisWithExpressionArgs() {
        setDescription("Test that a method call on this with expression arguments is correctly parsed");
        parseSnippet("this.foo(1 + 2, this.bar());", STATEMENT);
    }

    @Test
    public void targetWithMethodCall() {
        setDescription("Test that a method call with a method call as target is correctly parsed");
        parseSnippet("a.foo().bar();", STATEMENT);
    }

    @Test
    public void targetWithExpression() {
        setDescription("Test that a method call with an expression as target is correctly parsed");
        parseSnippet("(new Foo().get()).bar();", STATEMENT);
    }

    @Test
    public void targetIsNewWithoutParenthesis() {
        setDescription("Test that a method call with anew without parenthesis as target is correctly parsed");
        parseSnippet("new Foo().bar();", STATEMENT);
    }

}
