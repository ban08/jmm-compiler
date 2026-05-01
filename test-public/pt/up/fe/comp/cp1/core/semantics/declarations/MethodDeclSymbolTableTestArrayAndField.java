package pt.up.fe.comp.cp1.core.semantics.declarations;

import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.Visibility;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;

import java.util.List;


/**
 * Test variable lookup.
 */
public class MethodDeclSymbolTableTestArrayAndField extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/declarations/symboltable/";
    private static final String RESOURCES_LOCATION = "test-public";

    public MethodDeclSymbolTableTestArrayAndField() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }



    @Test
    public void ParameterWithSameNameAsField() {
        var semantics = symbolTable("ParameterWithSameNameAsField.jmm", false);
        var st = semantics.getSymbolTable();
        super.assertTrue("Field a exists", st.getField("a").isPresent());
        var methods = st.getMethods("foo");
        super.assertEquals("Should have one foo method", 1, methods.size());
        var foo = methods.getFirst();
        super.assertTrue("Parameter a exists", foo.getParameter("a").isPresent());

    }
}
