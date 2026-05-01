
package pt.up.fe.comp.cp1.core.semantics.declarations;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class ImportsSemanticAnalysisTestArrayAndField extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/declarations/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ImportsSemanticAnalysisTestArrayAndField() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }


    @Test
    public void fieldTypeIsImplicitlyImported() {
        setDescription("Test that a field with a type that is implicitly imported (like Object) is accepted");
        semantics("FieldTypeIsImplicitlyImported.jmm", false);
    }
}
