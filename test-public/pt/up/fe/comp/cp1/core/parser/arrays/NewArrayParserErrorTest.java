// java
package pt.up.fe.comp.cp1.core.parser.arrays;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.*;

public class NewArrayParserErrorTest extends JmmTestEnv {


    public NewArrayParserErrorTest() {
        super("", "");
    }

    @Test
    public void testNewWithMalformattedBrackets() {
        setDescription("Test that creating a new int array with malformatted brackets is not parsed");
        parseSnippetWithErrors("new int[10;", STATEMENT);
        parseSnippetWithErrors("new int10];", STATEMENT);
        parseSnippetWithErrors("new int 10;", STATEMENT);
        parseSnippet("new int[10];", STATEMENT);
    }

}
