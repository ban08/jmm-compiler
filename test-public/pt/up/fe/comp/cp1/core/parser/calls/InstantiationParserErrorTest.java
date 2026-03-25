// java
package pt.up.fe.comp.cp1.core.parser.calls;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;

public class InstantiationParserErrorTest extends JmmTestEnv {


    public InstantiationParserErrorTest() {
        super("", "");
    }

    @Test
    public void testNewKeywordAsID() {
        setDescription("Parse a method with the name new, which is a reserved keyword and cannot be used as an identifier");
        parseSnippetWithErrors("class A {void new() {}}", CLASS);
        parseSnippet("class A {void foo() {}}", CLASS);
    }
}
