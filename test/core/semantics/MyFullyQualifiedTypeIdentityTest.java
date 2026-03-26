package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyFullyQualifiedTypeIdentityTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/fullyqualified/";
    private static final String RESOURCES_LOCATION = "test";

    public MyFullyQualifiedTypeIdentityTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void importedTypeWithSameSimpleNameAsCurrentClassIsNotAssignableFromThis() {
        semantics("ImportedTypeNotAssignableFromThis.jmm", true);
    }

    @Test
    public void importedReceiverWithSameSimpleNameAsCurrentClassUsesImportedMethods() {
        semantics("ImportedReceiverUsesImportedMethods.jmm", false);
    }
}
