// java
package pt.up.fe.comp.cp1.extensions.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.*;

public class IfNoElseParserTest extends JmmTestEnv {


    public IfNoElseParserTest() {
        super("", "");
    }

    @Test
    public void testIfWithoutElse() {
        setDescription("Test that an if statement without an else branch is parsed correctly");
        parseSnippet("void foo(){if (q) { }}", METHOD);
    }

    @Test
    public void testWithSingleStatement() {
        setDescription("Test that an if statement without an else branch and with a single statement is parsed correctly");
        parseSnippet("void foo(){if (q) a.foo();}", METHOD);
    }


    @Test
    public void testStatementAfterIf() {
        setDescription("Test that an if statement without an else branch and with a single statement is parsed correctly and that the statement after the if is also parsed correctly");
        var jmmRes = parseSnippet("""
                 void foo(){
                    if (q)
                        a.foo();
                    b.bar();
                }""", METHOD);
        assertNotNull("Method should not be null", jmmRes.rootNode());
        var root = jmmRes.rootNode();
        assertEquals("Method should have 2 statements", 2, root.getChildren(JmmKind.STMT).size());
    }

    @Test
    public void testIfIfElse() {
        setDescription("Test that an if statement without an else branch and with an if-else statement as its body is parsed correctly");
        //Else reflects in second METHOD!
        parseSnippet("""
                void foo(){
                    if (q)
                        if (r)
                            { }
                        else
                            { }
                }""", METHOD);
    }

    @Test
    public void testIfIfIf() {
        setDescription("Test that an if statement without an else branch and with an if statement as its body is parsed correctly");
        parseSnippet("""
                void foo(){
                    if (q)
                        if (r)
                            if (s)
                                { }
                }""", METHOD);
    }
}
