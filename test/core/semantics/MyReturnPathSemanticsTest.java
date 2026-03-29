package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyReturnPathSemanticsTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/returnpaths/";
    private static final String RESOURCES_LOCATION = "test";

    public MyReturnPathSemanticsTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void ifWithoutElseDoesNotGuaranteeReturn() {
        semantics("IfWithoutElseMissingReturn.jmm", true);
    }

    @Test
    public void bothIfBranchesReturningIsAccepted() {
        semantics("IfElseBothBranchesReturn.jmm", false);
    }

    @Test
    public void trailingReturnAfterPartialBranchIsAccepted() {
        semantics("TrailingReturnAfterPartialBranch.jmm", false);
    }

    @Test
    public void onlyOneIfElseBranchReturningStillFails() {
        semantics("IfElseOneBranchMissingReturn.jmm", true);
    }

    @Test
    public void nonTerminatingLoopsSatisfyReturnRequirement() {
        semantics("InfiniteLoopsDoNotNeedExplicitReturn.jmm", false);
    }

    @Test
    public void constantTrueLoopExpressionsSatisfyReturnRequirement() {
        semantics("ConstantTrueLoopExpressionsDoNotNeedExplicitReturn.jmm", false);
    }
}
