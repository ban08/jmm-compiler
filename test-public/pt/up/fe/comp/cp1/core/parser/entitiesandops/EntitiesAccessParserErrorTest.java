// java
package pt.up.fe.comp.cp1.core.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class EntitiesAccessParserErrorTest extends JmmTestEnv {


    public EntitiesAccessParserErrorTest() {
        super("", "");
    }

    @Test
    public void testInvalidIDAccess() {
        setDescription("Test that accessing an entity with an invalid ID is not parsed");
        parseSnippetWithErrors("1a;", STATEMENT);
        parseSnippet("a1;", STATEMENT);
    }

    @Test
    public void testInvalidThisAccess() {
        setDescription("Test that 'this' cannot be accessed with an invalid syntax");
        parseSnippetWithErrors("this.;", STATEMENT);
        parseSnippet("this.foo();", STATEMENT);
    }

    @Test
    public void testKeywordsInExpressions() {
        setDescription("Test that keywords cannot be used as identifiers in expressions");
        parseSnippetWithErrors("class = 10;", STATEMENT);
        parseSnippetWithErrors("if = 10;", STATEMENT);
        parseSnippetWithErrors("else = 10;", STATEMENT);
        parseSnippetWithErrors("while = 10;", STATEMENT);
        parseSnippetWithErrors("return = 10;", STATEMENT);
        parseSnippet("ok = 10;", STATEMENT);
    }


}
