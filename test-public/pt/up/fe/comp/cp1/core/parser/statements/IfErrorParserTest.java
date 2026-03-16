// java
package pt.up.fe.comp.cp1.core.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class IfErrorParserTest extends JmmTestEnv {


    public IfErrorParserTest() {
        super("", "");
    }


    @Test
    public void testElseIf() {
        setDescription("Parse an if-else statement with an else-if in the middle, but with the else-if written as elseif, which is not valid");
        parseSnippetWithErrors("package x; class A{ void foo(){if(a) {} elseif(b) {} else {}}}");
        parseSnippet("package x; class A{ void foo(){if(a) {} else if(b) {} else {}}}");
    }

    @Test
    public void statementInIfCondition() {
        setDescription("Parse an if statement with a statement in the expression, which is not valid");
        parseSnippetWithErrors("if(a=0) {} else {}", STATEMENT);
        parseSnippet("if(a) {} else {}", STATEMENT);

    }

    @Test
    public void ifInsideAClass() {
        setDescription("Parse an if statement inside a class, which is not valid");
        parseSnippetWithErrors("class A { if(a) {} else {} }", CLASS);
        parseSnippet("class A { void foo() { if(a) {} else {} } }", CLASS);
    }


    @Test
    public void testIfKeywordAsID() {
        setDescription("Parse a method with the name if, which is a reserved keyword and cannot be used as an identifier");
        parseSnippetWithErrors("class A {void if() {}}", CLASS);
        parseSnippet("class A {void foo() {}}", CLASS);
    }

}
