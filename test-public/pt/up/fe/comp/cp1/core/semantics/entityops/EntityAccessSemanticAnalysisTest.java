package pt.up.fe.comp.cp1.core.semantics.entityops;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class EntityAccessSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/entityops/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public EntityAccessSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void accessLocalVariables() {
        setDescription("Test access to variables declared in the same method");
        semantics("AccessLocalVariablesFail.jmm", true);
        semantics("AccessLocalVariablesOk.jmm", false);
    }


}
