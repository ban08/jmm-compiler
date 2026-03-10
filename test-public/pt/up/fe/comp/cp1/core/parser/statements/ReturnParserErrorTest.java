// java
package pt.up.fe.comp.cp1.core.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class ReturnParserErrorTest extends JmmTestEnv {


    public ReturnParserErrorTest() {
        super("", "");
    }

    @Test
    public void statementInReturnedExpression() {
        setDescription("Parse a return statement with a statement in the expression, which is not valid");
        parseSnippetWithErrors("return a=0;", STATEMENT);
        parseSnippet("return;", STATEMENT);

    }

    @Test
    public void returnInsideAClass() {
        setDescription("Parse a return statement inside a class, which is not valid");
        parseSnippetWithErrors("class A { return 0;}", CLASS);
        parseSnippet("class A { void foo() { } }", CLASS);
    }


    @Test
    public void testReturnKeywordAsID() {
        setDescription("Parse a method with the name return, which is a reserved keyword and cannot be used as an identifier");
        parseSnippetWithErrors("class A {void return() {}}", CLASS);
        parseSnippet("class A {void foo() {}}", CLASS);
    }

}
