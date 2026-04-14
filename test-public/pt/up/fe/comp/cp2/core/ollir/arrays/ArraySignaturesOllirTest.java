package pt.up.fe.comp.cp2.core.ollir.arrays;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.CallInstruction;
import org.specs.comp.ollir.inst.InvokeVirtualInstruction;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.test.env.OllirTestEnv;


public class ArraySignaturesOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/arrays/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArraySignaturesOllirTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    public ClassUnit toOllir(String resourceName) {
        var ollirResult = jmmToOllir(resourceName);
        var classUnit = ollirResult.getOllirClass();
        assertNotNull("Ollir class unit should not be null", classUnit);
        assertEquals("Class name not what was expected", resourceName.replace(".jmm", ""), classUnit.getClassName());
        return classUnit;
    }

    public Method toOllir(String resourceName, String methodName) {
        var classUnit = toOllir(resourceName);
        var method = classUnit.getMethods().stream()
                .filter(m -> m.getMethodName().equals(methodName))
                .findFirst();
        assertTrue("Method '" + methodName + "' should exist", method.isPresent());
        return method.orElseThrow();
    }

    @Test
    public void testArrayAsParameter() {
        var method = toOllir("ArrayAsParameter.jmm", "method");

        assertEquals("Method should have exactly 1 parameter", 1, method.getParams().size());
        var paramType = method.getParam(0).getType();

        assertTrue("Parameter type must be an ArrayType", paramType instanceof ArrayType);
        var arrayType = (ArrayType) paramType;
        assertTrue("Inner element of the array must be an INT32",
                BuiltinType.is(arrayType.getElementType(), BuiltinKind.INT32));
    }

    @Test
    public void testArrayAsReturnType() {
        var method = toOllir("ArrayAsReturnType.jmm", "method");

        var returnType = method.getReturnType();

        assertTrue("Return type must be an ArrayType", returnType instanceof ArrayType);
        var arrayType = (ArrayType) returnType;
        assertTrue("Inner element of the returned array must be an INT32",
                BuiltinType.is(arrayType.getElementType(), BuiltinKind.INT32));
    }

    @Test
    public void testArrayAsArgument() {
        var method = toOllir("ArrayAsArgument.jmm", "method");

        var calls = assertInstExists(CallInstruction.class, method);

        var invokeOpt = calls.stream()
                .filter(c -> c instanceof InvokeVirtualInstruction)
                .findFirst();

        assertTrue("Method should contain an InvokeVirtualInstruction", invokeOpt.isPresent());
        var invoke = invokeOpt.get();

        assertEquals("The method call should pass exactly 1 argument", 1, invoke.getArguments().size());
        var argType = invoke.getArguments().get(0).getType();

        assertTrue("The argument passed to the method must be an ArrayType", argType instanceof ArrayType);
        var arrayType = (ArrayType) argType;
        assertTrue("Inner element of the argument array must be an INT32",
                BuiltinType.is(arrayType.getElementType(), BuiltinKind.INT32));
    }
}