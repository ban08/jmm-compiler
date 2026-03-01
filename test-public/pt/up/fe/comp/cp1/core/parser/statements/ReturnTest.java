// java
package pt.up.fe.comp.cp1.core.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.jmm.ast.Kind;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class ReturnTest extends JmmTestEnv {

    public ReturnTest() {
        super("", "");
    }


    @Test
    public void testMethodWithReturnStatement() {
        setDescription("Parse a method that contains a return statement in the body");
        parseSnippet("int foo() { return 1; }", METHOD);
    }
}
