// java
package pt.up.fe.comp.cp1.core.parser.arrays;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class ReadArrayParserErrorTest extends JmmTestEnv {


    public ReadArrayParserErrorTest() {
        super("", "");
    }

    @Test
    public void testReadArrayWithoutBrackets() {
        setDescription("Test that reading an array without brackets is not parsed");
        parseSnippetWithErrors("a = foo[;", STATEMENT);
        parseSnippetWithErrors("a = foo];", STATEMENT);
        parseSnippetWithErrors("a = foo [1;", STATEMENT);
        parseSnippetWithErrors("a = foo 1];", STATEMENT);
        parseSnippetWithErrors("a = foo 1;", STATEMENT);
        parseSnippet("a = foo[1];", STATEMENT);
    }
}
