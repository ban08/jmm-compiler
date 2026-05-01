// java
package pt.up.fe.comp.cp1.core.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class EntitiesAccessParserTest extends JmmTestEnv {


    public EntitiesAccessParserTest() {
        super("", "");
    }

    /*
        @Test
        public void testIDAccess() {
            setDescription("Test that accessing an entity by its ID is correctly parsed");
            parseSnippet("a;", STATEMENT);
        }
    */

    @Test
    public void testLengthAccess() {
        setDescription("Test that accessing this is correctly parsed");
        parseSnippet("this.foo();", STATEMENT);
    }


}
