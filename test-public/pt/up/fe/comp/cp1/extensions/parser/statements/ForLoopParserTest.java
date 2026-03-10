// java
package pt.up.fe.comp.cp1.extensions.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class ForLoopParserTest extends JmmTestEnv {


    public ForLoopParserTest() {
        super("", "");
    }

    @Test
    public void testForLoop() {
        setDescription("Test that a for loop is parsed correctly");
        parseSnippet("for (i = 0; i < 10; i=i+1) {}", STATEMENT);
    }

    @Test
    public void testForLoopWithStatements() {
        setDescription("Test that a for loop with statements in its body is parsed correctly");
        var jmmRes = parseSnippet("""
                for (i = 0; i < 10; i=i+1) {
                    a.foo();
                    b.bar();
                }""", STATEMENT);
        assertNotNull("Method should not be null", jmmRes.rootNode());
        var root = jmmRes.rootNode();
        var forLoop = root.getChildren(JmmKind.STMT).getFirst();// First is a compound statement.
        assertEquals("For loop should have 2 statements in its body", 2, forLoop.getChildren(JmmKind.STMT).size());
    }

    @Test
    public void testNoCondition() {
        setDescription("Test that a for loop without a condition is parsed correctly");
        parseSnippet("for (i = 0; ; i=i+1) {}", STATEMENT);
    }

    @Test
    public void testNoUpdate() {
        setDescription("Test that a for loop without an update is parsed correctly");
        parseSnippet("for (i = 0; i < 10; ) {}", STATEMENT);
    }

    @Test
    public void testNoInitialization() {
        setDescription("Test that a for loop without an initialization is parsed correctly");
        parseSnippet("for (; i < 10; i=i+1) {}", STATEMENT);
    }

    @Test
    public void testEmptyForLoop() {
        setDescription("Test that an empty for loop is parsed correctly");
        parseSnippet("for (;;) {}", STATEMENT);
    }

    @Test
    public void testForForLoop() {
        setDescription("Test that a for loop with another for loop as its body is parsed correctly");
        parseSnippet("""
                for (i = 0; i < 10; i=i+1) {
                    for (j = 0; j < 10; j=j+1) {
                        a.foo(i,j);
                    }
                }""", STATEMENT);
    }


}
