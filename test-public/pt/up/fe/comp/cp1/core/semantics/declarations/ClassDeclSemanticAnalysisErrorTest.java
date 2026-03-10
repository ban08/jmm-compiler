
package pt.up.fe.comp.cp1.core.semantics.declarations;

import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;

import java.util.List;


/**
 * Test variable lookup.
 */
public class ClassDeclSemanticAnalysisErrorTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/declarations/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ClassDeclSemanticAnalysisErrorTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void classWithSameNameAsExtends() {
        setDescription("Test that a class with the same name as its superclass is not accepted");
        semantics("ClassWithSameNameAsExtends.jmm", true);
        semantics("ClassWithSameNameAsExtendsOk.jmm", false);
    }
}
