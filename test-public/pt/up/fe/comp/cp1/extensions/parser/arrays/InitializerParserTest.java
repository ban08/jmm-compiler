// java
package pt.up.fe.comp.cp1.extensions.parser.arrays;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class InitializerParserTest extends JmmTestEnv {


    public InitializerParserTest() {
        super("", "");
    }

    @Test
    public void testArrayInitializerSimple() {
        setDescription("Parse a simple int array creation with initializer");
        parseSnippet("new int[] {1, 2, 3};", STATEMENT);
    }

    @Test
    public void testArrayInitializerSingleElement() {
        setDescription("Parse an int array creation with a single element in the initializer");
        parseSnippet("new int[]{42};", STATEMENT);
    }

    @Test
    public void testArrayInitializerEmpty() {
        setDescription("Parse an int array creation with an empty initializer");
        parseSnippet("new int[] { };", STATEMENT);
    }

    @Test
    public void testArrayInitializerWithExpressions() {
        setDescription("Parse an int array creation whose initializer contains arbitrary expressions");
        parseSnippet("new int[] { a, b + 1, 2 * c };", STATEMENT);
    }

    @Test
    public void testWithoutBraces() {
        setDescription("Test that an array initializer without braces is not accepted");
        parseSnippetWithErrors("new int[] 1, 2, 3;", STATEMENT);
        parseSnippet("new int[] {1, 2, 3};", STATEMENT);
    }

    @Test
    public void testNoSimultaneousSizeAndList() {
        setDescription("Test that an array is initialized either with a size, or a list");
        parseSnippetWithErrors("new int[3]{1, 2, 3};", STATEMENT);
        parseSnippet("new int[3];", STATEMENT);
        parseSnippet("new int[] {1, 2, 3};", STATEMENT);
    }
}
