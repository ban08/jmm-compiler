package pt.up.fe.comp.cp2.extensions.ollir.calls;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.LiteralElement;
import org.specs.comp.ollir.inst.CondBranchInstruction;
import org.specs.comp.ollir.inst.InvokeVirtualInstruction;
import org.specs.comp.ollir.inst.NewInstruction;
import org.specs.comp.ollir.inst.utils.InstTypeUtils;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.test.env.OllirTestEnv;

import java.util.List;

public class ImplicitThisOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/extensions/ollir/calls/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ImplicitThisOllirTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    public ClassUnit toOllir(String resourceName) {
        var ollirResult = jmmToOllir(resourceName);
        var classUnit = ollirResult.getOllirClass();
        assertNotNull("Ollir class unit should not be null", classUnit);
        assertEquals("Class name not what was expected", resourceName.replace(".jmm", ""), classUnit.getClassName());
        return classUnit;
    }

    @Test
    public void testImplicitThis() {
        var classUnit = toOllir("VirtualCallImplicitThis.jmm");

        var className = classUnit.getClassFullyQualifiedName();
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'callee'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("callee"));
        assertTrue("Caller should be of type '" + classUnit.getClassName() + "'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have 1 argument", 1, call.getArguments().size());
        assertTrue("Call return type should be i32", BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testImplicitThisChain() {
        var classUnit = toOllir("VirtualCallImplicitThisChain.jmm");

        var className = classUnit.getClassFullyQualifiedName();
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertAtLeast(method, InvokeVirtualInstruction.class, 2);
        var callArg = calls.getFirst();
        assertTrue("First method invocation should be 'callee2'", callArg.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("callee2"));
        assertTrue("Caller should be of type '" + classUnit.getClassName() + "'", InstTypeUtils.classTypeMatches(callArg.getCaller().getType(), className));
        assertEquals("Call should have 0 arguments", 0, callArg.getArguments().size());
        assertTrue("Call return type should be i32", BuiltinType.is(callArg.getReturnType(), BuiltinKind.INT32));

        var callOuter = calls.get(1);
        assertTrue("Second method invocation should be 'callee'", callOuter.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("callee"));
        assertTrue("Caller should be of type '" + classUnit.getClassName() + "'", InstTypeUtils.classTypeMatches(callOuter.getCaller().getType(), className));
        assertEquals("Call should have 1 argument", 1, callOuter.getArguments().size());
        assertTrue("Call return type should be i32", BuiltinType.is(callOuter.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testImplicitThisOnSuper() {
        var classUnit = toOllir("VirtualCallImplicitThisOnSuper.jmm");

        var className = classUnit.getClassFullyQualifiedName();
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'instanceCallIntWithArgs'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("instanceCallIntWithArgs"));
        assertTrue("Caller should be of type '" + classUnit.getClassName() + "'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have 2 argument", 2, call.getArguments().size());
        assertTrue("Call return type should be i32", BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testVirtualCallImplicitThisOnCondition() {
        var classUnit = toOllir("VirtualCallImplicitThisOnCondition.jmm");

        var className = classUnit.getClassFullyQualifiedName();
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'isEmpty'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("isEmpty"));
        assertTrue("Caller should be of type '" + classUnit.getClassName() + "'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have no argument", 0, call.getArguments().size());
        assertTrue("Call return type should be boolean", BuiltinType.is(call.getReturnType(), BuiltinKind.BOOLEAN));
    }

}

