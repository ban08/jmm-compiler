package pt.up.fe.comp.cp1.core.semantics.declarations;

import org.junit.Test;

import java.util.List;


/**
 * Test variable lookup.
 */
public class ImportsSymbolTableTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/declarations/symboltable/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ImportsSymbolTableTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void Imports() {
        var semantics = symbolTable("Imports.jmm", false);
        var st = semantics.getSymbolTable();
        super.assertEquals("Expected to have ${expected} imports.", 2, st.getImports().size());
        var io = st.getImportedFullyQualifiedName("io");
        var ioPlus = st.getImportedFullyQualifiedName("ioPlus");

        super.assertTrue("Expected to find imported classed named io", io.isPresent());
        super.assertTrue("Expected to find imported classed named ioPlus", ioPlus.isPresent());
        super.assertEquals("Expected symbol table imports to return the fully qualified name of the imported class", "util.io", io.orElseThrow());
        super.assertEquals("Expected symbol table imports to return the fully qualified name of the imported class", "util.ioPlus", ioPlus.orElseThrow());
    }

    @Test
    public void DuplicateImportsAreIgnored() {
        var semantics = symbolTable("ImportsWithDuplicate.jmm", false);
        super.assertEquals("A duplicate import is not an error and should be ignored", semantics.getSymbolTable().getImports().size(), 1);
    }


    @Test
    public void ClassAndSuper() {
        var semantics = symbolTable("Super.jmm", false);
        super.assertEquals("Class name should be ${expected}", "Super", semantics.getSymbolTable().getClassName());
        super.assertEquals("Super class should be ${expected}", "Quicksort", semantics.getSymbolTable().getSuper());
        super.assertEquals("Super fully qualified name should be ${expected}", "examples.Quicksort", semantics.getSymbolTable().getSuperFullyQualifiedName());
    }

    @Test
    public void MethodReturnsImportedClass() {
        var semantics = symbolTable("MethodReturnsImportedClass.jmm", false);
        var st = semantics.getSymbolTable();
        var methods = st.getMethods();
        super.assertTrue("Expected to have methods.", !methods.isEmpty());
        var mList = st.getMethods("make");
        super.assertTrue("Expected to have method 'make'", !mList.isEmpty());
        var m = mList.getFirst();
        var rt = m.returnType();
        super.assertTrue("Expected method to return the imported class util.io",
                rt.isClass() &&
                        rt.asClass().isImported() &&
                        rt.asClass().name().equals("io"));
    }


    @Test
    public void ParameterOfImportedClass() {
        var semantics = symbolTable("ParameterOfImportedClass.jmm", false);
        var st = semantics.getSymbolTable();
        var methods = st.getMethods();
        super.assertTrue("Expected to have methods.", !methods.isEmpty());
        var mList = st.getMethods("accept");
        super.assertTrue("Expected to have method 'accept'", !mList.isEmpty());
        var m = mList.getFirst();
        var params = m.parameters();
        super.assertEquals("Expected method to have ${expected} parameters", 1, params.size());
        var p = params.getFirst();
        var pt = p.type();
        super.assertTrue("Expected parameter to be of type of the imported class util.io",
                pt.isClass() &&
                        pt.asClass().isImported() &&
                        pt.asClass().name().equals("io"));
    }
}
