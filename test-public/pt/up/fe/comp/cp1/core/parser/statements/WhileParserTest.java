// java
package pt.up.fe.comp.cp1.core.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class WhileParserTest extends JmmTestEnv {

    public WhileParserTest() {
        super("", "");
    }

    @Test
    public void testSimpleWhile() {
        setDescription("Parse a simple while statement");
        parseSnippet("while (a) {}", STATEMENT);
    }

    @Test
    public void testWhileWithSingleStatement() {
        setDescription("Parse a while statement with a single statement in the body");
        parseSnippet("while(a) a=0;", STATEMENT);
    }


    @Test
    public void testNestedWhileStatements() {
        setDescription("Parse nested while statements");
        parseSnippet("while(a) { while(b) {} }", STATEMENT);
    }

    @Test
    public void testNestedWhileStatementsWithoutBrackets() {
        setDescription("Parse nested while statements");
        parseSnippet("while(a) while(b) {}", STATEMENT);
    }

    @Test
    public void testMethodWithWhileStatement() {
        setDescription("Parse a method with a while statement inside its body");
        parseSnippet("void foo(){ while(a) {} }", METHOD);
    }


}
