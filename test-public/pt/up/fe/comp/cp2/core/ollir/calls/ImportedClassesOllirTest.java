package pt.up.fe.comp.cp2.core.ollir.calls;

import examples.Auxi;
import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.LiteralElement;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.inst.utils.InstTypeUtils;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.Type;
import pt.up.fe.comp.test.env.OllirTestEnv;

import java.util.List;

public class ImportedClassesOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/calls/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ImportedClassesOllirTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    public ClassUnit toOllir(String resourceName) {
        var ollirResult = jmmToOllir(resourceName);
        var classUnit = ollirResult.getOllirClass();
        assertNotNull("Ollir class unit should not be null", classUnit);
        assertEquals("Class name not what was expected", resourceName.replace(".jmm", ""), classUnit.getClassName());
        return classUnit;
    }

    private Method toOllir(String resourceName, String methodName) {
        var classUnit = toOllir(resourceName);
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals(methodName)).findFirst();
        assertEquals("Method '" + methodName + "' should exist", true, method.isPresent());
        return method.orElseThrow();
    }


    public boolean assertType(Type actualType, Object expectedType) {
        if (expectedType instanceof BuiltinKind bk) {
            return BuiltinType.is(actualType, bk);
        } else if (expectedType instanceof String classQualifiedName) {
            return InstTypeUtils.classTypeMatches(actualType, classQualifiedName);
        } else {
            fail("Using wrong type of type comparison. Either use a BuiltinKind or a string with the class qualified name");
            return false; // Unreachable, but required for compilation
        }
    }

    private CallInstruction assertCall(Method method, String methodName, boolean isStatic, String callerType, Object returnType, List<? extends Object> args, Object assignmentLhsType, Object assignmentRhsType) {
        var invokeTypeClass = isStatic ? InvokeStaticInstruction.class : InvokeVirtualInstruction.class;
        var calls = assertInstExists(invokeTypeClass, method);
        assertTrue("Method should contain an invokevirtual call", !calls.isEmpty());
        var call = calls.getFirst();
        assertTrue("Method name should be '" + methodName + "'", call.getMethodName() instanceof LiteralElement le && le.getLiteral().equals(methodName));
        Type actualType = call.getCaller().getType();
        assertTrue("Caller should be of type '" + callerType + "', but was " + actualType, assertType(actualType, callerType));

        if (args.isEmpty()) {
            assertEquals("Call should have no arguments", 0, call.getArguments().size());
        } else {
            assertEquals("Call should have " + args.size() + " arguments", args.size(), call.getArguments().size());
            for (int i = 0; i < args.size(); i++) {
                var arg = call.getArguments().get(i);
                var expectedArgType = args.get(i);
                assertTrue("Argument " + (i + 1) + " should be of type '" + expectedArgType + "'", assertType(arg.getType(), expectedArgType));
            }
        }
        assertTrue("Call return type should be " + returnType, assertType(call.getReturnType(), returnType));

        if (assignmentLhsType != null) {
            var assigns = assertInstExists(AssignInstruction.class, method);
            assertTrue("Method should contain an assignment instruction", !assigns.isEmpty());
            var assign = assigns.getLast(); //The expected assignment is the last one
            assertTrue("Assignment lhs type should be " + assignmentLhsType, assertType(assign.getDest().getType(), assignmentLhsType));
            var assignRhs = assignmentRhsType != null ? assignmentRhsType : assignmentLhsType;
            var assignRhsTypeOpt = InstTypeUtils.getType(assign.getRhs());
            assertTrue("Unable to determine type of assignment rhs", assignRhsTypeOpt.isPresent());
            assertTrue("Assignment rhs type should be " + assignRhs, assertType(assignRhsTypeOpt.orElseThrow(), assignRhs));
        }
        return call;
    }

    @Test
    public void ImportedClassesSimpleCall() {
        var method = toOllir("ImportedClassesSimpleCall.jmm", "method");
        var methodName = "getLeft";
        var callerType = "examples.Pair";
        var returnType = BuiltinKind.INT32;
        var argsTypes = List.of();
        var assignmentType = BuiltinKind.INT32;

        assertCall(method, methodName, false, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesSimpleCallVoid() {
        var method = toOllir("ImportedClassesSimpleCallVoid.jmm", "method");
        var methodName = "instanceCallVoid";
        var callerType = "examples.Auxi";
        var returnType = BuiltinKind.VOID;
        var argsTypes = List.of();
        Object assignmentType = null; //no assignment expected

        assertCall(method, methodName, false, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesSimpleCallExprStmtRetInt() {
        var method = toOllir("ImportedClassesSimpleCallExprStmtRetInt.jmm", "method");
        var methodName = "getLeft";
        var callerType = "examples.Pair";
        var returnType = BuiltinKind.INT32;
        var argsTypes = List.of();
        Object assignmentType = null; //no assignment expected

        assertCall(method, methodName, false, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesSimpleCallFromNewInstance() {
        var method = toOllir("ImportedClassesSimpleCallFromNewInstance.jmm", "method");
        var methodName = "instanceCallInt";
        var callerType = "examples.Auxi";
        var returnType = BuiltinKind.INT32;
        var argsTypes = List.of();
        Object assignmentType = BuiltinKind.INT32;

        var news = assertInstExists(NewInstruction.class, method);
        var newInst = news.getFirst();
        assertTrue("New instruction should be of type '" + callerType + "'", InstTypeUtils.classTypeMatches(newInst.getReturnType(), callerType));
        var invoke = assertCall(method, methodName, false, callerType, returnType, argsTypes, assignmentType, assignmentType);
        assertOrder(method, newInst, invoke);
    }

    @Test
    public void ImportedClassesCallWithArg() {
        var method = toOllir("ImportedClassesCallWithArg.jmm", "method");
        var methodName = "setLeft";
        var callerType = "examples.Pair";
        var returnType = BuiltinKind.INT32;
        var argsTypes = List.of(BuiltinKind.INT32);
        Object assignmentType = BuiltinKind.INT32;

        assertCall(method, methodName, false, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesStaticCallRetInt() {
        var method = toOllir("ImportedClassesStaticCallRetInt.jmm", "method");
        var methodName = "read";
        var callerType = "util.io";
        var returnType = BuiltinKind.INT32;
        var argsTypes = List.of();
        Object assignmentType = BuiltinKind.INT32;

        assertCall(method, methodName, true, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesStaticCallVoid() {
        var method = toOllir("ImportedClassesStaticCallVoid.jmm", "method");
        var methodName = "print";
        var callerType = "util.io";
        var returnType = BuiltinKind.VOID;
        var argsTypes = List.of(BuiltinKind.INT32);
        Object assignmentType = null;

        assertCall(method, methodName, true, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesStaticCallExprStmtRetInt() {
        var method = toOllir("ImportedClassesStaticCallExprStmtRetInt.jmm", "method");
        var methodName = "read";
        var callerType = "util.io";
        var returnType = BuiltinKind.INT32;
        var argsTypes = List.of();
        Object assignmentType = null;

        assertCall(method, methodName, true, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesCallWithArgExpectingClass() {
        var method = toOllir("ImportedClassesCallWithArgExpectingClass.jmm", "method");
        var methodName = "instanceExpectingAuxi";
        var callerType = "examples.Auxi";
        var returnType = BuiltinKind.INT32;
        var argsTypes = List.of(callerType);
        Object assignmentType = BuiltinKind.INT32;

        assertCall(method, methodName, false, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesCallWithArgExpectingObject() {
        var method = toOllir("ImportedClassesCallWithArgExpectingObject.jmm", "method");
        var methodName = "instanceExpectingObject";
        var callerType = "examples.Auxi";
        var returnType = BuiltinKind.INT32;
        var argsTypes = List.of(Object.class.getName());
        Object assignmentType = BuiltinKind.INT32;

        assertCall(method, methodName, false, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesCallWithArgExpectingSuperClass() {
        var method = toOllir("ImportedClassesCallWithArgExpectingSuperClass.jmm", "method");
        var methodName = "instanceExpectingAuxi";
        var callerType = "examples.Auxi";
        var returnType = BuiltinKind.INT32;
        var argsTypes = List.of(Auxi.class.getName());
        Object assignmentType = BuiltinKind.INT32;

        assertCall(method, methodName, false, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesCallSuperMethodInt() {
        var method = toOllir("ImportedClassesCallSuperMethodInt.jmm", "method");
        var methodName = "instanceCallInt";
        var callerType = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.ImportedClassesCallSuperMethodInt";
        var returnType = BuiltinKind.INT32;
        var argsTypes = List.of();
        Object assignmentType = BuiltinKind.INT32;

        assertCall(method, methodName, false, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesCallSuperMethodVoid() {
        var method = toOllir("ImportedClassesCallSuperMethodVoid.jmm", "method");
        var methodName = "instanceCallVoid";
        var callerType = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.ImportedClassesCallSuperMethodVoid";
        var returnType = BuiltinKind.VOID;
        var argsTypes = List.of();
        Object assignmentType = null;

        assertCall(method, methodName, false, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesCallSuperMethodWithArg() {
        var method = toOllir("ImportedClassesCallSuperMethodWithArg.jmm", "method");
        var methodName = "instanceCallIntWithArgs";
        var callerType = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.ImportedClassesCallSuperMethodWithArg";
        var returnType = BuiltinKind.INT32;
        var argsTypes = List.of(BuiltinKind.INT32, BuiltinKind.INT32);
        Object assignmentType = BuiltinKind.INT32;

        assertCall(method, methodName, false, callerType, returnType, argsTypes, assignmentType, assignmentType);
    }

    @Test
    public void ImportedClassesCallReturnStoredInSuperType() {
        var method = toOllir("ImportedClassesCallReturnStoredInSuperType.jmm", "method");
        var methodName = "self";
        var callerType = "examples.inheritance.B";
        var returnType = callerType;
        var argsTypes = List.of();
        Object assignmentType = "examples.inheritance.A";
        Object assignmentTypeRhs = callerType;

        assertCall(method, methodName, false, callerType, returnType, argsTypes, assignmentType, assignmentTypeRhs);
    }
}

