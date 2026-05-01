package pt.up.fe.comp.cp1.core.semantics.declarations;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class ImportsSymbolTableTestArrayAndField extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/declarations/symboltable/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ImportsSymbolTableTestArrayAndField() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }


    @Test
    public void FieldOfImportedClass() {
        var semantics = symbolTable("FieldOfImportedClass.jmm", false);
        var st = semantics.getSymbolTable();
        var fields = st.getFields();
        super.assertEquals("Expected to have ${expected} fields.", 1, fields.size());
        var f = fields.getFirst();
        var ft = f.type();
        super.assertTrue("Expected field to be of type of the imported class util.io",
                ft.isClass() &&
                        ft.asClass().isImported() &&
                        ft.asClass().name().equals("io"));
    }
}
