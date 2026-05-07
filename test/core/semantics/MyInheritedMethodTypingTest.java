package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyInheritedMethodTypingTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/inheritedmethods/";
    private static final String RESOURCES_LOCATION = "test";

    public MyInheritedMethodTypingTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void grandparentInheritedMethodKeepsReturnType() {
        semantics("GrandparentMethodReturnOk.jmm", false);
        semantics("GrandparentMethodAssignMismatch.jmm", true);
    }

    @Test
    public void importedReceiverCanCallInheritedMethod() {
        semantics("ImportedReceiverInheritedMethodOk.jmm", false);
    }
}
