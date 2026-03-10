// java
package pt.up.fe.comp.cp1.core.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class ReturnParserTest extends JmmTestEnv {

    public ReturnParserTest() {
        super("", "");
    }


    @Test
    public void testMethodWithReturnStatement() {
        setDescription("Parse a method that contains a return statement in the body");
        parseSnippet("int foo() { return 1; }", METHOD);
    }

    @Test
    public void testMethodWithReturnStatementWithoutExpression() {
        setDescription("Parse a method that contains a return statement without an expression in the body");
        parseSnippet("void foo() { return; }", METHOD);
    }

}
