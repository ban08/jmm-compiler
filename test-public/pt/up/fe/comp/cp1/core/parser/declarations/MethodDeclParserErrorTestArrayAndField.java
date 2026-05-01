// java
package pt.up.fe.comp.cp1.core.parser.declarations;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class MethodDeclParserErrorTestArrayAndField extends JmmTestEnv {


    public MethodDeclParserErrorTestArrayAndField() {
        super("", "");
    }

    @Test
    public void testMethodInvalidArrayTypeSyntax() {
        setDescription("Method declaration with malformed array type syntax");
        parseSnippetWithErrors("int[]] foo() {}", METHOD);
        parseSnippet("int[] foo() {}", METHOD);

    }


}
