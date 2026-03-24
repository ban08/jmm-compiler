package pt.up.fe.comp.cp1.core.semantics.declarations;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class ImportsSemanticAnalysisErrorTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/declarations/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ImportsSemanticAnalysisErrorTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void superWithClassNotImported() {
        setDescription("Test that a class that extends another class that is not imported is not accepted");
        symbolTable("SuperWithClassNotImported.jmm", true);
        symbolTable("SuperWithClassNotImportedOk.jmm", false);

    }

    @Test
    public void importedClassExists() {
        setDescription("Test that an imported class exists");
        semantics("ImportedClassExistsFail.jmm", true);
        semantics("ImportedClassExistsOk.jmm", false);
    }

    @Test
    public void declaredImportedClass() {
        setDescription("Test if an imported class exists");
        semantics("DeclaredImportedClassFail.jmm", true);
        semantics("DeclaredImportedClassOk.jmm", false);
    }
}
