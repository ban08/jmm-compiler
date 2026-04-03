package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyImportedCallsSemanticsTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/importedcalls/";
    private static final String RESOURCES_LOCATION = "test";

    public MyImportedCallsSemanticsTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void importedStaticReceiverCalls() {
        semantics("ImportedStaticReceiverOk.jmm", false);
        semantics("ImportedStaticReceiverSameSimpleNameOk.jmm", true);
        semantics("ImportedStaticReceiverFail.jmm", true);
    }
}
