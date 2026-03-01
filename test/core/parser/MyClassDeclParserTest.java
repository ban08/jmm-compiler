// java
package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.*;

public class MyClassDeclParserTest extends JmmTestEnv {


    public MyClassDeclParserTest() {
        super("", "");
    }


    @Test
    public void testPrimitiveInt() {
        parseSnippet("int", TYPE);
    }

    @Test
    public void testSinglePackage() {
        var res = parseSnippet("package bar;", PACKAGE);
    }

    @Test
    public void testMultiPackage() {
        parseSnippet("package bar.foo.org;", PACKAGE);
    }
}
