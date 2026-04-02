package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyFieldAccessSemanticsTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/fieldaccess/";
    private static final String RESOURCES_LOCATION = "test";

    public MyFieldAccessSemanticsTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void currentClassFieldReceivers() {
        semantics("CurrentClassFieldReceiverOk.jmm", false);
        semantics("CurrentClassFieldReceiverFail.jmm", true);
    }

    @Test
    public void importedFieldReceivers() {
        semantics("ImportedFieldReceiverOk.jmm", false);
        semantics("ImportedFieldReceiverFail.jmm", true);
    }

    @Test
    public void inheritedImportedFields() {
        semantics("ImportedInheritedFieldReceiverOk.jmm", false);
        semantics("InheritedFieldFromImportedSuperOk.jmm", false);
    }

    @Test
    public void lengthNamedFieldsRemainAccessible() {
        semantics("LengthNamedFieldOk.jmm", false);
    }
}
