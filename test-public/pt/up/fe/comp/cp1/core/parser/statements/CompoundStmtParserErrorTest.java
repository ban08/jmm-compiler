// java
package pt.up.fe.comp.cp1.core.parser.statements;

import org.junit.Test;
import pt.up.fe.comp.jmm.ast.Kind;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class CompoundStmtParserErrorTest extends JmmTestEnv {


    private static final Kind STATEMENT = JmmKind.STMT;


    public CompoundStmtParserErrorTest() {
        super("", "");
    }


    @Test
    public void unclosedCompound() {
        setDescription("Parse an unclosed compound statement");
        parseSnippetWithErrors("{ a=0; ", STATEMENT);
        parseSnippet("{ a=0; }", STATEMENT);
    }


    @Test
    public void unopenedComplexCompound() {
        setDescription("Parse an unopened complex compound statement");
        parseSnippetWithErrors("package x; class A{ void foo(){ {{}}} } }");
        parseSnippet("package x; class A{ void foo(){ {{{}}} } }");
    }


}
