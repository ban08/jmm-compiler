package pt.up.fe.comp.cp2.extensions.ollir.calls;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.LiteralElement;
import org.specs.comp.ollir.inst.InvokeStaticInstruction;
import org.specs.comp.ollir.inst.InvokeVirtualInstruction;
import org.specs.comp.ollir.inst.utils.InstTypeUtils;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class LocalStaticMethodOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/extensions/ollir/calls/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public LocalStaticMethodOllirTest() {
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
    public void testLocalStaticMethodCallVoid() {
        var classUnit = toOllir("LocalStaticMethodCallVoid.jmm");

        var className = classUnit.getClassFullyQualifiedName();
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeStaticInstruction.class, method);
        assertTrue("Method should contain an invokestatic call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'printNumber'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("printNumber"));
        assertTrue("Caller should be of type '" + classUnit.getClassName() + "'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have 1 argument", 1, call.getArguments().size());
        assertTrue("Call return type should be void", BuiltinType.is(call.getReturnType(), BuiltinKind.VOID));
    }

    @Test
    public void testLocalStaticMethodCallRetInt() {
        var classUnit = toOllir("LocalStaticMethodCallRetInt.jmm");

        var className = classUnit.getClassFullyQualifiedName();
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeStaticInstruction.class, method);
        assertTrue("Method should contain an invokestatic call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'readNumber'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("readNumber"));
        assertTrue("Caller should be of type '" + classUnit.getClassName() + "'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have no argument", 0, call.getArguments().size());
        assertTrue("Call return type should be i32", BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void LocalStaticMethodCallExprStmtRetInt() {
        var classUnit = toOllir("LocalStaticMethodCallExprStmtRetInt.jmm");

        var className = classUnit.getClassFullyQualifiedName();
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeStaticInstruction.class, method);
        assertTrue("Method should contain an invokestatic call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'readNumber'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("readNumber"));
        assertTrue("Caller should be of type '" + classUnit.getClassName() + "'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have no argument", 0, call.getArguments().size());
        assertTrue("Call return type should be i32", BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

}

