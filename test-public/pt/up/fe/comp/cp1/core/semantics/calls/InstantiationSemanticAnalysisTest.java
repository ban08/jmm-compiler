package pt.up.fe.comp.cp1.core.semantics.calls;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class InstantiationSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/calls/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public InstantiationSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void instantiateSelf() {
        setDescription("Test that the class can create an instance of itself");
        semantics("InstantiateSelfFail.jmm", true);
        semantics("InstantiateSelfOk.jmm", false);
    }

    @Test
    public void instantiateJavaLangClass() {
        setDescription("Test that the class can create an instance of java.lang.* without needing to import it");
        semantics("InstantiateJavaLangClassFail.jmm", true);
        semantics("InstantiateJavaLangClassOk.jmm", false);
    }

    @Test
    public void instantiateImportedClass() {
        setDescription("Test that the class can create an instance of an imported class");
        semantics("InstantiateImportedClassFail.jmm", true);
        semantics("InstantiateImportedClassOk.jmm", false);
    }
}
