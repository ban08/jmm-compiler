// java
package pt.up.fe.comp.cp1.core.parser.arrays;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class WriteArrayParserErrorTest extends JmmTestEnv {


    public WriteArrayParserErrorTest() {
        super("", "");
    }

    @Test
    public void testWriteArrayWitMalformattedBrackets() {
        setDescription("Test that writing to an array with bad brackets is not parsed");
        parseSnippetWithErrors("void method(){foo[=10;}", METHOD);
        parseSnippetWithErrors("void method(){foo]=10;}", METHOD);
        parseSnippetWithErrors("void method(){foo [1=10;}", METHOD);
        parseSnippetWithErrors("void method(){foo 1]=10;}", METHOD);
        parseSnippetWithErrors("void method(){foo 1=10;}", METHOD);
        parseSnippet("void method(){foo[1] = 10;}", METHOD);
    }

}
