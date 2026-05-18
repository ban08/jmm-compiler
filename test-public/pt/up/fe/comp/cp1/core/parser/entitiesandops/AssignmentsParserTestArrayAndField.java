// java
package pt.up.fe.comp.cp1.core.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class AssignmentsParserTestArrayAndField extends JmmTestEnv {


    public AssignmentsParserTestArrayAndField() {
        super("", "");
    }

    @Test
    public void testSimpleIntAssignment() {
        setDescription("Test that a simple assignment is correctly parsed");
        parseSnippet("void foo(){a[0] = 0;}", METHOD);
    }

    @Test
    public void testSimpleIntAssignmentWithExpression() {
        setDescription("Test that a simple assignment with an expression is correctly parsed");
        parseSnippet("void foo(){a[0   ] = 0 + 1;}", METHOD);
    }


}
