// java
package pt.up.fe.comp.cp1.core.parser.entitiesandops;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;

public class EntitiesAccessParserErrorTest extends JmmTestEnv {


    public EntitiesAccessParserErrorTest() {
        super("", "");
    }

    @Test
    public void testInvalidIDAccess() {
        setDescription("Test that accessing an entity with an invalid ID is not parsed");
        parseSnippetWithErrors("1a", EXPRESSION);
        parseSnippet("a1", EXPRESSION);
    }

    @Test
    public void testInvalidThisAccess() {
        setDescription("Test that 'this' cannot be accessed with an invalid syntax");
        parseSnippetWithErrors("this.", EXPRESSION);
        parseSnippet("this.a", EXPRESSION);
    }

    @Test
    public void testKeywordsInExpressions() {
        setDescription("Test that keywords cannot be used as identifiers in expressions");
        parseSnippetWithErrors("class", EXPRESSION);
        parseSnippetWithErrors("if", EXPRESSION);
        parseSnippetWithErrors("else", EXPRESSION);
        parseSnippetWithErrors("while", EXPRESSION);
        parseSnippetWithErrors("return", EXPRESSION);
        parseSnippet("ok", EXPRESSION);
    }


}
