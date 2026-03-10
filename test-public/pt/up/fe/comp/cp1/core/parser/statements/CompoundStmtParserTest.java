// java
package pt.up.fe.comp.cp1.core.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class CompoundStmtParserTest extends JmmTestEnv {

    public CompoundStmtParserTest() {
        super("", "");
    }

    @Test
    public void emptyCompound() {
        setDescription("Parse an empty compound statement");
        parseSnippet("{}", STATEMENT);
    }

    @Test
    public void compoundWithOneStatement() {
        setDescription("Parse a compound statement with one statement");
        parseSnippet("{ a=0; }", STATEMENT);


    }

    @Test
    public void compoundWithMultipleStatements() {
        setDescription("Parse a compound statement with multiple statements");
        parseSnippet("{ a=0; b=1; c=2; }", STATEMENT);
    }

    @Test
    public void compoundWithCompound() {
        setDescription("Parse an empty compound statement insider another compound statement");
        parseSnippet("{{}}", STATEMENT);
    }

    @Test
    public void compoundWithSeqCompounds() {
        setDescription("Parse a compound statement with two sequential compound statements inside");
        parseSnippet("{{}{}}", STATEMENT);
    }


    @Test
    public void compoundWithNestedCompound() {
        setDescription("Parse a compound statement with a nested compound statement");
        parseSnippet("{ a=0; { b=1; } c=2; }", STATEMENT);
    }


    @Test
    public void testMethodWithNestedBlocks() {
        setDescription("Parse a method with nested blocks and variable declaration");
        parseSnippet("void foo(){ int a;{  {  } } }", METHOD);
    }
}
