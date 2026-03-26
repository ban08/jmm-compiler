// java
package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.symboltable.JmmSymbolTable;

public class MySymbolTableTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/symboltable/";
    private static final String RESOURCES_LOCATION = "test";

    public MySymbolTableTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void testFirstExample() {
        var st = symbolTable("Factorial.jmm", false).getSymbolTable();
        assertEquals("Expected class name to be Factorial", "Factorial", st.getClassName());
        var mainMethodsList = st.getMethods("main");
        assertTrue("Expected to encounter main method", !mainMethodsList.isEmpty());
    }

    @Test
    public void duplicateFieldName() {
        symbolTable("DuplicateFieldName.jmm", true);
        symbolTable("DuplicateFieldNameOk.jmm", false);
    }

    @Test
    public void importedSymbolTableLookupAcceptsSimpleName() {
        var st = (JmmSymbolTable) symbolTable("Factorial.jmm", false).getSymbolTable();

        var bySimpleName = st.getImportedSymbolTable("io");
        assertTrue("Expected imported symbol table lookup by simple name to succeed", bySimpleName.isPresent());
        assertEquals("Expected simple-name lookup to resolve util.io", "util.io",
                bySimpleName.orElseThrow().getFullyQualifiedName());

        var byFullyQualifiedName = st.getImportedSymbolTable("util.io");
        assertTrue("Expected imported symbol table lookup by fully qualified name to succeed", byFullyQualifiedName.isPresent());
        assertEquals("Expected fully-qualified lookup to resolve util.io", "util.io",
                byFullyQualifiedName.orElseThrow().getFullyQualifiedName());
    }
}
