package pt.up.fe.comp.cp2.core.ollir.arrays;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.ArrayLengthInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;


public class ArrayLengthOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/arrays/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArrayLengthOllirTest() {
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
    public void testLocalLength() {
        var method = toOllir("ArrayLength.jmm", "getLocalLength");

        var lengthInsts = assertInstExists(ArrayLengthInstruction.class, method);
        assertTrue("Method should contain at least one arraylength instruction", !lengthInsts.isEmpty());
    }

    @Test
    public void testParamLength() {
        var method = toOllir("ArrayLength.jmm", "getParamLength");

        var lengthInsts = assertInstExists(ArrayLengthInstruction.class, method);
        assertTrue("Method should contain at least one arraylength instruction", !lengthInsts.isEmpty());
    }

    @Test
    public void testMethodReturnLength() {
        var method = toOllir("ArrayLength.jmm", "getMethodReturnLength");

        var lengthInsts = assertInstExists(ArrayLengthInstruction.class, method);
        assertTrue("Method should contain at least one arraylength instruction", !lengthInsts.isEmpty());
    }
}