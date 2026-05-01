
package pt.up.fe.comp.cp1.core.semantics.declarations;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class ImportsSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/declarations/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ImportsSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void superWithImplicitImport() {
        setDescription("Test that a class that extends another class that is implicitly imported (like Object) is accepted");
        semantics("SuperWithImplicitImport.jmm", false);
    }

    @Test
    public void methodReturnTypeIsImplicitlyImported() {
        setDescription("Test that a method with a return type that is implicitly imported (like Object) is accepted");
        semantics("MethodReturnTypeIsImplicitlyImported.jmm", false);
    }

    
}
