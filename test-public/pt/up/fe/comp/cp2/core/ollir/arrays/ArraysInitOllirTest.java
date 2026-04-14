package pt.up.fe.comp.cp2.core.ollir.arrays;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.BinaryOpInstruction;
import org.specs.comp.ollir.inst.CallInstruction;
import org.specs.comp.ollir.inst.NewInstruction;
import org.specs.comp.ollir.type.ArrayType;
import pt.up.fe.comp.test.env.OllirTestEnv;


public class ArraysInitOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/arrays/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArraysInitOllirTest() {
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
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals(methodName)).findFirst();
        assertEquals("Method '" + methodName + "' should exist", true, method.isPresent());
        return method.orElseThrow();
    }

    @Test
    public void testArrayInitLiteral() {
        var method = toOllir("ArrayInitLiteral.jmm", "init");

        var calls = assertInstExists(CallInstruction.class, method);
        boolean hasNewArray = calls.stream().anyMatch(c ->
                c instanceof NewInstruction && c.getReturnType() instanceof ArrayType);

        assertTrue("Method should contain a new array initialization instruction", hasNewArray);
    }

    @Test
    public void testArrayInitVariable() {
        var method = toOllir("ArrayInitVariable.jmm", "init");

        var calls = assertInstExists(CallInstruction.class, method);
        boolean hasNewArray = calls.stream().anyMatch(c ->
                c instanceof NewInstruction && c.getReturnType() instanceof ArrayType);

        assertTrue("Method should contain a new array initialization instruction", hasNewArray);
    }

    @Test
    public void testArrayInitExpression() {
        var method = toOllir("ArrayInitExpression.jmm", "init");

        var calls = assertInstExists(CallInstruction.class, method);
        boolean hasNewArray = calls.stream().anyMatch(c ->
                c instanceof NewInstruction && c.getReturnType() instanceof ArrayType);

        assertTrue("Method should contain a new array initialization instruction", hasNewArray);

        var binOps = assertInstExists(BinaryOpInstruction.class, method);
        assertTrue("Method should contain at least 2 binary operations for the size calculation", binOps.size() >= 2);
    }
}