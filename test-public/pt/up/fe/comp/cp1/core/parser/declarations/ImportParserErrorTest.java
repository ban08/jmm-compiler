// java
package pt.up.fe.comp.cp1.core.parser.declarations;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ast.Kind;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.IMPORT;

public class ImportParserErrorTest extends JmmTestEnv {


    public ImportParserErrorTest() {
        super("", "");
    }


    @Test
    public void testInvalidTwoDotsImport() {
        parseSnippetWithErrors("import a..e;", IMPORT);
        parseSnippet("import a.b.c;", IMPORT);
    }

    @Test
    public void testInvalidIncompleteImport() {
        parseSnippetWithErrors("import a.b.;", IMPORT);
        parseSnippet("import a.b.c;", IMPORT);
    }

    @Test
    public void testInvalidImportWithoutSemicolon() {
        parseSnippetWithErrors("package x; import a.e class A{}");
        parseSnippet("package x; import a.e; class A{}");
    }

    @Test
    public void testInvalidNumericImport() {
        parseSnippetWithErrors("import a.1a.e;", IMPORT);
        parseSnippet("import a.b.c;", IMPORT);
    }

    @Test
    public void testImportEmpty() {
        parseSnippetWithErrors("import ;", IMPORT);
        parseSnippet("import a.b.c;", IMPORT);
    }

    @Test
    public void testImportStartsWithDot() {
        parseSnippetWithErrors("import .a.b;", IMPORT);
        parseSnippet("import a.b.c;", IMPORT);
    }

    @Test
    public void testImportWithKeywordSegment() {
        // using a Java keyword as an identifier segment should be invalid
        parseSnippetWithErrors("import a.class.b;", IMPORT);
        parseSnippet("import a.b.c;", IMPORT);
    }

}
