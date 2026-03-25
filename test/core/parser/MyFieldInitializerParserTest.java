package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;

public class MyFieldInitializerParserTest extends JmmTestEnv {

    public MyFieldInitializerParserTest() {
        super("", "");
    }

    @Test
    public void fieldInitializerAllowedButLocalInitializerRejected() {
        parseSnippet("class A { int field = 1; }", CLASS);
        parseSnippetWithErrors("class A { void foo() { int local = 1; } }", CLASS);
    }
}
