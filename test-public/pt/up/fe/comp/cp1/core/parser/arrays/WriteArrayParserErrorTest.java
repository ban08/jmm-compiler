// java
package pt.up.fe.comp.cp1.core.parser.arrays;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class WriteArrayParserErrorTest extends JmmTestEnv {


    public WriteArrayParserErrorTest() {
        super("", "");
    }

    @Test
    public void testWriteArrayWitMalformattedBrackets() {
        setDescription("Test that writing to an array with bad brackets is not parsed");
        parseSnippetWithErrors("foo[=10;", STATEMENT);
        parseSnippetWithErrors("foo]=10;", STATEMENT);
        parseSnippetWithErrors("foo [1=10;", STATEMENT);
        parseSnippetWithErrors("foo 1]=10;", STATEMENT);
        parseSnippetWithErrors("foo 1=10;", STATEMENT);
        parseSnippet("foo[1] = 10;", STATEMENT);
    }

}
