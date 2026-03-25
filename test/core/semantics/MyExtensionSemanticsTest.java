package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyExtensionSemanticsTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/extensions/";
    private static final String RESOURCES_LOCATION = "test";

    public MyExtensionSemanticsTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void implicitThisCalls() {
        semantics("ImplicitThisCallOk.jmm", false);
        semantics("ImplicitThisCallFail.jmm", true);
    }

    @Test
    public void staticImplicitThisCalls() {
        semantics("ImplicitThisStaticOk.jmm", false);
        semantics("ImplicitThisStaticFail.jmm", true);
    }

    @Test
    public void loopConditions() {
        semantics("LoopConditionsOk.jmm", false);
        semantics("DoWhileConditionFail.jmm", true);
        semantics("ForConditionFail.jmm", true);
    }

    @Test
    public void forHeaderAssignments() {
        semantics("ForHeaderAssignOk.jmm", false);
        semantics("ForHeaderAssignFail.jmm", true);
    }
}
