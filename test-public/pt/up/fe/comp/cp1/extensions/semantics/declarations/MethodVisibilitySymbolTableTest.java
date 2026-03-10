// java
package pt.up.fe.comp.cp1.extensions.semantics.declarations;

import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.table.Visibility;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class MethodVisibilitySymbolTableTest extends JmmTestEnv {


    public MethodVisibilitySymbolTableTest() {
        super("", "");
    }

    @Test
    public void testPrivateMethodDeclaration() {
        setDescription("See if private instance method declaration is correctly added to the symbol table with the right visibility");
        var res = symbolTableFromSnippet("package x; class A{private void foo() {} }", false);
        var st = res.getSymbolTable();
        var methods = st.getMethods("foo");
        assertEquals("Number of methods should be 1", 1, methods.size());
        var method = methods.getFirst();
        assertEquals("Method should be private", Visibility.PRIVATE, method.visibility());
    }

    @Test
    public void testProtectedMethodDeclaration() {
        setDescription("Parse a protected instance method declaration");
        var res = symbolTableFromSnippet("package x; class A{protected int bar() { } }", false);
        var st = res.getSymbolTable();
        var methods = st.getMethods("bar");
        assertEquals("Number of methods should be 1", 1, methods.size());
        var method = methods.getFirst();
        assertEquals("Method should be protected", Visibility.PROTECTED, method.visibility());
    }

}
