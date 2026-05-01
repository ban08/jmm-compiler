package pt.up.fe.comp.cp2.core.ollir.calls;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.LiteralElement;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.inst.utils.InstTypeUtils;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.test.env.OllirTestEnv;

import java.util.List;

public class LocalCallsOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/calls/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public LocalCallsOllirTest() {
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
    public void testLocalSimpleCallOverVar() {
        var classUnit = toOllir("LocalSimpleCallOverVar.jmm");
        var className = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.LocalSimpleCallOverVar";

        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'callee'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("callee"));
        assertTrue("Caller should be of type 'LocalSimpleCallOverVar'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));


        assertEquals("Call should have no arguments", 0, call.getArguments().size());
        assertTrue("Call return type should be int", BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testLocalCallOnThis() {
        var classUnit = toOllir("LocalCallOnThis.jmm");
        var className = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.LocalCallOnThis";
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'callee'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("callee"));
        assertTrue("Caller should be of type 'LocalCallOnThis'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have no arguments", 0, call.getArguments().size());
        assertTrue("Call return type should be void", BuiltinType.is(call.getReturnType(), BuiltinKind.VOID));
    }

    @Test
    public void testLocalCallWithArg() {
        var classUnit = toOllir("LocalCallWithArg.jmm");
        var className = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.LocalCallWithArg";
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'callee'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("callee"));
        assertTrue("Caller should be of type 'LocalCallWithArg'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have one argument", 1, call.getArguments().size());
        assertTrue("Argument should be int", call.getArguments().getFirst().getType() instanceof BuiltinType bt && bt.getKind() == BuiltinKind.INT32);
        assertTrue("Call return type should be int", BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testLocalCallWithMultipleArg() {
        var classUnit = toOllir("LocalCallWithMultipleArg.jmm");
        var className = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.LocalCallWithMultipleArg";
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'callee'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("callee"));
        assertTrue("Caller should be of type 'LocalCallWithMultipleArg'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have three arguments", 3, call.getArguments().size());
        var types = List.of(BuiltinKind.INT32, BuiltinKind.BOOLEAN, BuiltinKind.INT32);
        for (int i = 0; i < call.getArguments().size(); i++)
            assertTrue("Argument " + (i + 1) + " should be " + types.get(i), call.getArguments().get(i).getType() instanceof BuiltinType bt && bt.getKind() == types.get(i));
        assertTrue("Call return type should be int", BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testLocalCallsComplexArithmetic() {
        var classUnit = toOllir("LocalCallsComplexArithmetic.jmm");
        var className = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.LocalCallsComplexArithmetic";
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'arithmetic'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("arithmetic"));
        assertTrue("Caller should be of type 'LocalCallsComplexArithmetic'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have one argument", 1, call.getArguments().size());
        assertTrue("Argument should be int", call.getArguments().getFirst().getType() instanceof BuiltinType bt && bt.getKind() == BuiltinKind.INT32);
        assertTrue("Call return type should be int", BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testLocalCallsComplexLogical() {
        var classUnit = toOllir("LocalCallsComplexLogical.jmm");
        var className = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.LocalCallsComplexLogical";
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var shortCircuits = assertInstExists(CondBranchInstruction.class, method);
        assertTrue("Method should contain a conditional branch for short-circuit evaluation", !shortCircuits.isEmpty());
        var sc = shortCircuits.getFirst();
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'logical'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("logical"));
        assertTrue("Caller should be of type 'LocalCallsComplexLogical'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have one argument", 1, call.getArguments().size());
        assertTrue("Argument should be boolean", call.getArguments().getFirst().getType() instanceof BuiltinType bt && bt.getKind() == BuiltinKind.BOOLEAN);
        assertTrue("Call return type should be boolean", BuiltinType.is(call.getReturnType(), BuiltinKind.BOOLEAN));

        assertOrder(method, sc, call);
    }

    @Test
    public void testLocalCallOverNewInstance() {
        var classUnit = toOllir("LocalCallOverNewInstance.jmm");
        var className = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.LocalCallOverNewInstance";

        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var news = assertInstExists(NewInstruction.class, method);
        assertTrue("Method should contain a 'new' call", !news.isEmpty());
        var newInst = news.getFirst();
        assertTrue("New instance should be of class LocalCallOverNewInstance", InstTypeUtils.classTypeMatches(newInst.getReturnType(), className));
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'callee'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("callee"));
        assertTrue("Call should be on LocalCallOverNewInstance class instance'",
                InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have no arguments", 0, call.getArguments().size());

        assertTrue("Call return type should be void ", call.getReturnType() instanceof BuiltinType bt
                && BuiltinType.is(bt, BuiltinKind.VOID));

        assertOrder(method, newInst, call);
    }

    @Test
    public void testLocalCallOverNewInstanceParenthesis() {
        var classUnit = toOllir("LocalCallOverNewInstanceParenthesis.jmm");
        var className = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.LocalCallOverNewInstanceParenthesis";

        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var news = assertInstExists(NewInstruction.class, method);
        assertTrue("Method should contain a 'new' call", !news.isEmpty());
        var newInst = news.getFirst();
        assertTrue("New instance should be of class LocalCallOverNewInstanceParenthesis", InstTypeUtils.classTypeMatches(newInst.getReturnType(), className));
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'callee'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("callee"));
        assertTrue("Call should be on LocalCallOverNewInstanceParenthesis class instance'",
                InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have no arguments", 0, call.getArguments().size());

        assertTrue("Call return type should be void ", call.getReturnType() instanceof BuiltinType bt
                && BuiltinType.is(bt, BuiltinKind.VOID));

        assertOrder(method, newInst, call);
    }


    @Test
    public void testLocalCallExprStmtVoid() {
        var classUnit = toOllir("LocalCallExprStmtVoid.jmm");
        var className = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.LocalCallExprStmtVoid";
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'callee'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("callee"));
        assertTrue("Caller should be of type 'LocalCallExprStmtVoid'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));
        assertEquals("Call should have one argument", 1, call.getArguments().size());
        assertTrue("Argument should be int", call.getArguments().getFirst().getType() instanceof BuiltinType bt && bt.getKind() == BuiltinKind.INT32);
        assertTrue("Call return type should be void", BuiltinType.is(call.getReturnType(), BuiltinKind.VOID));
    }

    @Test
    public void testLocalCallExprStmtInt() {
        var classUnit = toOllir("LocalCallExprStmtInt.jmm");
        var className = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.LocalCallExprStmtInt";
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be 'callee'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("callee"));
        assertTrue("Caller should be of type 'LocalCallExprStmtInt'", InstTypeUtils.classTypeMatches(call.getCaller().getType(), className));

        assertEquals("Call should have one argument", 1, call.getArguments().size());
        assertTrue("Argument should be int", call.getArguments().getFirst().getType() instanceof BuiltinType bt && bt.getKind() == BuiltinKind.INT32);
        assertTrue("Call return type should be int", BuiltinType.is(call.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testLocalCallChainedCall() {
        var classUnit = toOllir("LocalCallChainedCall.jmm");
        var className = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.LocalCallChainedCall";
        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var calls = assertInstExists(InvokeVirtualInstruction.class, method);
        assertEquals("Method should contain two invokevirtual calls", 2, calls.size());

        var firstOpt = calls.stream().filter(m -> m.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("thisInstance")).findFirst();
        assertTrue("First call name should be 'thisInstance'", firstOpt.isPresent());
        var first = firstOpt.orElseThrow();


        assertTrue("First call should be of type 'LocalCallChainedCall'", InstTypeUtils.classTypeMatches(first.getCaller().getType(), className));
        assertEquals("First call should have no arguments", 0, first.getArguments().size());
        assertTrue("First call return type should be 'LocalCallChainedCall'", InstTypeUtils.classTypeMatches(first.getReturnType(), className));

        var secondOpt = calls.stream().filter(m -> m.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("foo")).findFirst();
        assertTrue("Second call name should be 'foo'", secondOpt.isPresent());
        var second = secondOpt.orElseThrow();
        assertTrue("Second call should be of type 'LocalCallChainedCall'", InstTypeUtils.classTypeMatches(second.getCaller().getType(), className));
        assertEquals("Second call should have no arguments", 0, second.getArguments().size());
        assertTrue("Second call return type should be 'void'", BuiltinType.is(second.getReturnType(), BuiltinKind.VOID));

        assertOrder(method, first, second);
    }

    @Test
    public void testLocalCallComplexTarget() {
        var classUnit = toOllir("LocalCallComplexTarget.jmm");
        var className = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.LocalCallComplexTarget";

        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Method 'method' should exist", methodOpt.isPresent());
        var method = methodOpt.orElseThrow();


        var news = assertInstExists(NewInstruction.class, method);
        assertTrue("Method should contain a 'new' call", !news.isEmpty());
        var newInst = news.getFirst();
        assertTrue("New instance should be of class LocalCallOverNewInstance", InstTypeUtils.classTypeMatches(newInst.getReturnType(), className));

        var calls = assertInstExists(InvokeVirtualInstruction.class, method);

        assertEquals("Method should contain two invokevirtual calls", 2, calls.size());
        var firstOpt = calls.stream().filter(m -> m.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("thisInstance")).findFirst();
        assertTrue("First call name should be 'thisInstance'", firstOpt.isPresent());
        var first = firstOpt.orElseThrow();
        assertTrue("First call should be of type 'LocalCallComplexTarget'", InstTypeUtils.classTypeMatches(first.getCaller().getType(), className));
        assertEquals("First call should have no arguments", 0, first.getArguments().size());
        assertTrue("First call return type should be 'LocalCallComplexTarget'", InstTypeUtils.classTypeMatches(first.getReturnType(), className));

        var secondOpt = calls.stream().filter(m -> m.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("foo")).findFirst();
        assertTrue("Second call name should be 'foo'", secondOpt.isPresent());
        var second = secondOpt.orElseThrow();
        assertTrue("Second call should be of type 'LocalCallComplexTarget'", InstTypeUtils.classTypeMatches(second.getCaller().getType(), className));
        assertEquals("Second call should have no arguments", 0, second.getArguments().size());
        assertTrue("Second call return type should be 'void'", BuiltinType.is(second.getReturnType(), BuiltinKind.VOID));

        assertOrder(method, newInst, first, second);
    }

}

