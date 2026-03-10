// java
package pt.up.fe.comp.cp1.core.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class IfParserTest extends JmmTestEnv {


    public IfParserTest() {
        super("", "");
    }


    @Test
    public void testSimpleIfElse() {
        setDescription("Parse a simple if-else statement");
        parseSnippet("if(a) {} else {}", STATEMENT);
    }

    @Test
    public void testIfWithSingleStatement() {
        setDescription("Parse an if statement with a single statement in the body");
        parseSnippet("if(a) a=0; else a=1;", STATEMENT);
    }

    @Test
    public void testIfElseIfElseStatement() {
        setDescription("Parse an if-else statement with an else-if in the middle");
        parseSnippet("if(a) {} else if(b) {} else {}", STATEMENT);
    }


    @Test
    public void testNestedIfStatements() {
        setDescription("Parse nested if statements");
        parseSnippet("if(a) { if(b) {} else {} } else { if(c) {} else {} }", STATEMENT);
    }

    @Test
    public void testMethodWithIfStatement() {
        setDescription("Parse a method with an if statement inside its body");
        parseSnippet("void foo(){ if(a) {} else {} }", METHOD);
    }


}
