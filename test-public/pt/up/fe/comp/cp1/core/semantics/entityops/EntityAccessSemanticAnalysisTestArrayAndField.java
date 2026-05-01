package pt.up.fe.comp.cp1.core.semantics.entityops;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class EntityAccessSemanticAnalysisTestArrayAndField extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/entityops/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public EntityAccessSemanticAnalysisTestArrayAndField() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }


    @Test
    public void accessClassFields() {
        setDescription("Test access to class fields");
        semantics("AccessClassFieldsFail.jmm", true);
        semantics("AccessClassFieldsOk.jmm", false);
    }
}
