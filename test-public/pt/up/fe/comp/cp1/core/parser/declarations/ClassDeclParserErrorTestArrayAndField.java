// java
package pt.up.fe.comp.cp1.core.parser.declarations;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;

public class ClassDeclParserErrorTestArrayAndField extends JmmTestEnv {

    public ClassDeclParserErrorTestArrayAndField() {
        super("", "");
    }

    @Test
    public void testInvalidStuffOutsideClasses() {
        parseSnippetWithErrors("package x; class A{} int a;");
        parseSnippet("package x; class A{ int a; }");
    }

    @Test
    public void testFieldMissingSemicolon() {
        // field declaration inside class without terminating semicolon
        parseSnippetWithErrors("class A { int a }", CLASS);
        parseSnippet("class A { int a; }", CLASS);
    }

    @Test
    public void testInvalidFieldType() {
        // field type cannot be a number
        parseSnippetWithErrors("class A { 10 a; }", CLASS);
        parseSnippet("class A { int a; }", CLASS);
    }


}
