package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.ast.TypeUtils;
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

    @Test
    public void implicitStaticCallsFromInstanceMethodsAreNormalizedToClassReceiver() {
        var semanticsResult = semantics("ImplicitStaticCallFromInstanceOk.jmm", false);
        var root = semanticsResult.getRootNode();

        assertTrue("Implicit-this calls should be normalized away",
                root.getDescendants(JmmKind.IMPLICIT_THIS_CALL_EXPR).isEmpty());

        var methodCalls = root.getDescendants(JmmKind.METHOD_CALL_EXPR);
        assertEquals("Expected exactly one explicit method call after normalization", 1, methodCalls.size());

        var methodCall = methodCalls.get(0);
        assertTrue("Static implicit calls should normalize to a class-name receiver even in instance methods",
                JmmKind.VAR_REF_EXPR.check(methodCall.getChild(0)));
        assertEquals("Expected the receiver to be the current class name", "ImplicitStaticCallFromInstanceOk",
                methodCall.getChild(0).get(JmmAttributes.VAR_REF_EXPR.NAME));
    }

    @Test
    public void explicitThisStaticCallsAreNormalizedToClassReceiver() {
        var semanticsResult = semantics("ExplicitThisStaticCallOk.jmm", false);
        var root = semanticsResult.getRootNode();

        var methodCalls = root.getDescendants(JmmKind.METHOD_CALL_EXPR);
        assertEquals("Expected exactly one explicit method call after normalization", 1, methodCalls.size());

        var methodCall = methodCalls.get(0);
        assertTrue("Explicit static calls on 'this' should normalize to a class-name receiver",
                JmmKind.VAR_REF_EXPR.check(methodCall.getChild(0)));
        assertEquals("Expected the receiver to be the current class name", "ExplicitThisStaticCallOk",
                methodCall.getChild(0).get(JmmAttributes.VAR_REF_EXPR.NAME));
    }

    @Test
    public void normalizedStaticReceiversKeepCurrentClassIdentityWhenImportsShareTheSameSimpleName() {
        var semanticsResult = semantics("ImplicitStaticCallSameSimpleNameAsImportOk.jmm", false);
        var root = semanticsResult.getRootNode();

        var methodCalls = root.getDescendants(JmmKind.METHOD_CALL_EXPR);
        assertEquals("Expected exactly one explicit method call after normalization", 1, methodCalls.size());

        var methodCall = methodCalls.get(0);
        var resolvedCall = TypeUtils.with(semanticsResult.getSymbolTable()).resolveMethodCall(methodCall);

        assertTrue("The normalized static call should still resolve after rewriting the receiver",
                resolvedCall.isPresent());
        assertTrue("The normalized call should still target a static method",
                resolvedCall.get().method().isStatic());
        assertEquals("Expected the normalized receiver to keep the current class fully qualified name",
                "core.semantics.extensions.Date",
                resolvedCall.get().receiverType().asClass().fullyQualifiedName());
        assertTrue("Expected the normalized receiver to keep the current class, not the imported class",
                !resolvedCall.get().receiverType().asClass().isImported());
    }

    @Test
    public void instanceStaticCallsAreNormalizedToClassReceivers() {
        var semanticsResult = semantics("InstanceStaticReceiverNormalizationOk.jmm", false);
        var root = semanticsResult.getRootNode();

        var methodCalls = root.getDescendants(JmmKind.METHOD_CALL_EXPR);
        assertEquals("Expected exactly one explicit method call after normalization", 1, methodCalls.size());

        var methodCall = methodCalls.get(0);
        assertTrue("Static calls through instances should normalize to a class-name receiver",
                JmmKind.VAR_REF_EXPR.check(methodCall.getChild(0)));
        assertEquals("Expected the receiver to be the current class name after normalization",
                "InstanceStaticReceiverNormalizationOk",
                methodCall.getChild(0).get(JmmAttributes.VAR_REF_EXPR.NAME));

        var resolvedCall = TypeUtils.with(semanticsResult.getSymbolTable()).resolveMethodCall(methodCall);
        assertTrue("The normalized static call should still resolve", resolvedCall.isPresent());
        assertTrue("The normalized call should still target a static method", resolvedCall.get().method().isStatic());
        assertTrue("The normalized receiver should be a static class reference",
                resolvedCall.get().receiverType().asClass().staticRef());
    }
}
