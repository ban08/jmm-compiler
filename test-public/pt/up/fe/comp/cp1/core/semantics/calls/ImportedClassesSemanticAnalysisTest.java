package pt.up.fe.comp.cp1.core.semantics.calls;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class ImportedClassesSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/calls/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ImportedClassesSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void callImportedMethod() {
        setDescription("Test that an imported method can be called");
        semantics("CallImportedMethodFail.jmm", true);
        semantics("CallImportedMethodOk.jmm", false);
    }

    @Test
    public void importedMethodCorrectArgNumber() {
        setDescription("Test right number of arguments on an imported method call");
        semantics("ImportedMethodCorrectArgNumberFail.jmm", true);
        semantics("ImportedMethodCorrectArgNumberOk.jmm", false);
    }

    @Test
    public void importedMethodCorrectArgTypes() {
        setDescription("Test right types of arguments on an imported method call");
        semantics("ImportedMethodCorrectArgTypesFail.jmm", true);
        semantics("ImportedMethodCorrectArgTypesOk.jmm", false);
    }
}
