// java
package pt.up.fe.comp.cp1.core.parser.arrays;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;

public class NewArrayParserErrorTest extends JmmTestEnv {


    public NewArrayParserErrorTest() {
        super("", "");
    }

    @Test
    public void testNewIntArrayWithoutSize() {
        setDescription("Test that creating a new int array without a size is not parsed");
        parseSnippetWithErrors("new int[]", EXPRESSION);
        parseSnippet("new int[10]", EXPRESSION);
    }

    @Test
    public void testNewWithMalformattedBrackets() {
        setDescription("Test that creating a new int array with malformatted brackets is not parsed");
        parseSnippetWithErrors("new int[10", EXPRESSION);
        parseSnippetWithErrors("new int10]", EXPRESSION);
        parseSnippetWithErrors("new int 10", EXPRESSION);
        parseSnippet("new int[10]", EXPRESSION);
    }

}
