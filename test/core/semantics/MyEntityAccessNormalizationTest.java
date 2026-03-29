package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class MyEntityAccessNormalizationTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/extensions/";
    private static final String RESOURCES_LOCATION = "test";

    public MyEntityAccessNormalizationTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void implicitInstanceCallsAreNormalizedToThisReceiver() {
        var semanticsResult = semantics("ImplicitThisCallOk.jmm", false);
        var root = semanticsResult.getRootNode();

        assertTrue("Implicit-this calls should be normalized away",
                root.getDescendants(JmmKind.IMPLICIT_THIS_CALL_EXPR).isEmpty());

        var methodCalls = root.getDescendants(JmmKind.METHOD_CALL_EXPR);
        assertEquals("Expected exactly one explicit method call after normalization", 1, methodCalls.size());

        var methodCall = methodCalls.get(0);
        assertEquals("Expected the normalized call to keep the original method name", "sum",
                methodCall.get(JmmAttributes.METHOD_CALL_EXPR.NAME));
        assertTrue("Instance implicit calls should normalize to an explicit 'this' receiver",
                JmmKind.THIS_EXPR.check(methodCall.getChild(0)));
    }

    @Test
    public void implicitStaticCallsAreNormalizedToClassReceiver() {
        var semanticsResult = semantics("ImplicitThisStaticOk.jmm", false);
        var root = semanticsResult.getRootNode();

        assertTrue("Implicit-this calls should be normalized away",
                root.getDescendants(JmmKind.IMPLICIT_THIS_CALL_EXPR).isEmpty());

        var methodCalls = root.getDescendants(JmmKind.METHOD_CALL_EXPR);
        assertEquals("Expected exactly one explicit method call after normalization", 1, methodCalls.size());

        var methodCall = methodCalls.get(0);
        assertTrue("Static implicit calls should normalize to a class-name receiver",
                JmmKind.VAR_REF_EXPR.check(methodCall.getChild(0)));
        assertEquals("Expected the receiver to be the current class name", "ImplicitThisStaticOk",
                methodCall.getChild(0).get(JmmAttributes.VAR_REF_EXPR.NAME));
    }
}
