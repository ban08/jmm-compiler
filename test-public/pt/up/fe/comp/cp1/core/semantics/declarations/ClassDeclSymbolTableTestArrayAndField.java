package pt.up.fe.comp.cp1.core.semantics.declarations;

import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.test.env.JmmTestEnv;


/**
 * Test variable lookup.
 */
public class ClassDeclSymbolTableTestArrayAndField extends JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/declarations/symboltable/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ClassDeclSymbolTableTestArrayAndField() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }



    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void FieldArrayType() {
        var semantics = symbolTable("FieldArrayType.jmm", false);
        var st = semantics.getSymbolTable();

        var fInts = st.getField("ints");

        super.assertTrue("ints field exists", fInts.isPresent());

        var fIntsType = fInts.get().type();

        super.assertTrue("ints should be array of ints", fIntsType.isArray() && fIntsType.asArray().itemType().equals(JmmPrimitiveType.INT));
    }

}
