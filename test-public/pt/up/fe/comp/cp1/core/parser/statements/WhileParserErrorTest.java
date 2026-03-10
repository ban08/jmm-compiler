// java
package pt.up.fe.comp.cp1.core.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class WhileParserErrorTest extends JmmTestEnv {


    public WhileParserErrorTest() {
        super("", "");
    }


    @Test
    public void statementInWhileCondition() {
        setDescription("Parse a while statement with a statement in the expression, which is not valid");
        parseSnippetWithErrors("while(a=0) {} else {}", STATEMENT);
        parseSnippet("while(a) {} else {}", STATEMENT);

    }

    @Test
    public void whileInsideAClass() {
        setDescription("Parse a while statement inside a class, which is not valid");
        parseSnippetWithErrors("class A { while(a) {}}", CLASS);
        parseSnippet("class A { void foo() { while(a) {}} }", CLASS);
    }


    @Test
    public void testWhileKeywordAsID() {
        setDescription("Parse a method with the name while, which is a reserved keyword and cannot be used as an identifier");
        parseSnippetWithErrors("class A {void while() {}}", CLASS);
        parseSnippet("class A {void foo() {}}", CLASS);
    }

}
