package pt.up.fe.comp.cp2.extensions.ollir.entitiesandops;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.OperationType;
import org.specs.comp.ollir.inst.BinaryOpInstruction;
import org.specs.comp.ollir.inst.InvokeVirtualInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class AdditionalArithmeticArrayOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/extensions/ollir/entitiesandops/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public AdditionalArithmeticArrayOllirTest() {
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
    public void testIncrementOnArray() {
        var method = toOllir("IncrementOnArray.jmm", "method");
        //++a; => a = a + 1; or t= a +1; a = t;
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
    }

}

