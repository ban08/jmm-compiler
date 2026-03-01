// java
package pt.up.fe.comp.cp1.core.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.jmm.ast.Kind;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class CompoundStmtErrorTest extends JmmTestEnv {


    private static final Kind STATEMENT = JmmKind.STMT;


    public CompoundStmtErrorTest() {
        super("", "");
    }
    

    @Test
    public void unclosedCompound() {
        setDescription("Parse an unclosed compound statement");
        parseSnippetWithErrors("{ a=0; ", STATEMENT);
        parseSnippet("{ a=0; }", STATEMENT);
    }


}
