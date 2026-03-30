// java
package pt.up.fe.comp.cp1.extensions.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class DoWhileParserTest extends JmmTestEnv {


    public DoWhileParserTest() {
        super("", "");
    }

    @Test
    public void testDoWhileLoop() {
        setDescription("Test that a do-while loop is parsed correctly");
        parseSnippet("do {} while (i < 10);", STATEMENT);
    }

    @Test
    public void testDoWhileWithStatements() {
        setDescription("Test that a do-while loop with statements in its body is parsed correctly");
        parseSnippet("""
                do {
                    i= i +1;
                } while (i < 10);""", STATEMENT);
    }

    @Test
    public void testSingleStatementBody() {
        setDescription("Test that a do-while with a single statement body is parsed correctly");
        parseSnippet("do a.foo(); while (i < 10);", STATEMENT);
    }

    @Test
    public void testComplexCondition() {
        setDescription("Test that a do-while with a complex condition is parsed correctly");
        parseSnippet("do {} while (i < 10 && 0 < j);", STATEMENT);
    }

    @Test
    public void testEmptyBodyWithTrueCondition() {
        setDescription("Test that a do-while with an empty body and a true-like condition is parsed correctly");
        parseSnippet("do {} while (true);", STATEMENT);
    }

    @Test
    public void testNestedDoWhile() {
        setDescription("Test that nested do-while loops are parsed correctly");
        parseSnippet("do { do { a.foo(); } while (b); } while (c);", STATEMENT);
    }

}
