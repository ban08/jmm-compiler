// java
package pt.up.fe.comp.cp1.core.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class EntitiesAccessParserTestArrayAndField extends JmmTestEnv {


    public EntitiesAccessParserTestArrayAndField() {
        super("", "");
    }

    @Test
    public void testLengthAccess() {
        setDescription("Test that accessing the length of an array is correctly parsed");
        parseSnippet("a.length;", STATEMENT);
    }


}
