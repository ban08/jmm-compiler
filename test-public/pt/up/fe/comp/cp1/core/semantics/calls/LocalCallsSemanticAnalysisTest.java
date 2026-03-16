package pt.up.fe.comp.cp1.core.semantics.calls;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class LocalCallsSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/calls/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public LocalCallsSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void localMethodExists() {
        setDescription("Test that a method exists on the same class when called");
        semantics("LocalMethodExistsFail.jmm", true);
        semantics("LocalMethodExistsOk.jmm", false);
    }

    @Test
    public void localMethodCorrectArgNumber() {
        setDescription("Test right number of arguments on a local method call");
        semantics("LocalMethodCorrectArgNumberFail.jmm", true);
        semantics("LocalMethodCorrectArgNumberOk.jmm", false);
    }

    @Test
    public void localMethodCorrectArgTypes() {
        setDescription("Test right types of arguments on a local method call");
        semantics("LocalMethodCorrectArgTypesFail.jmm", true);
        semantics("LocalMethodCorrectArgTypesOk.jmm", false);
    }

    @Test
    public void localMethodParentCall() {
        setDescription("Test a call to an inherited method");
        semantics("LocalMethodParentCallFail.jmm", true);
        semantics("LocalMethodParentCallOk.jmm", false);
    }
}
