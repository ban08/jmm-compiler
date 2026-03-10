// java
package pt.up.fe.comp.cp1.core.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class AssignmentsParserTest extends JmmTestEnv {


    public AssignmentsParserTest() {
        super("", "");
    }

    @Test
    public void testSimpleIntAssignment() {
        setDescription("Test that a simple assignment is correctly parsed");
        parseSnippet("a[0] = 0;", STATEMENT);
    }

    @Test
    public void testSimpleIntAssignmentWithExpression() {
        setDescription("Test that a simple assignment with an expression is correctly parsed");
        parseSnippet("a[0   ] = 0 + 1;", STATEMENT);
    }


    @Test
    public void testAssignmentWithMethodCall() {
        setDescription("Test that an assignment with a method call is correctly parsed");
        parseSnippet("a = b.foo();", STATEMENT);
    }
}
