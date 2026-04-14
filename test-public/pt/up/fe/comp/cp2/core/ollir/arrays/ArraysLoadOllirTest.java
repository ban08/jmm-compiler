package pt.up.fe.comp.cp2.core.ollir.arrays;

import org.junit.Test;
import org.specs.comp.ollir.ArrayOperand;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.AssignInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;


public class ArraysLoadOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/arrays/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArraysLoadOllirTest() {
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
    public void testArrayLoad() {
        var method = toOllir("ArrayLoad.jmm", "method");

        var assigns = assertInstExists(AssignInstruction.class, method);
        var reads = assigns.stream()
                .flatMap(assign -> getElements(assign.getRhs()).stream())
                .filter(element -> element instanceof ArrayOperand)
                .count();

        assertTrue("Method should contain at least one array read", reads >= 1);
    }

    @Test
    public void testArrayStore() {
        var method = toOllir("ArrayStore.jmm", "method");

        var assigns = assertInstExists(AssignInstruction.class, method);
        var stores = assigns.stream().filter(assign -> assign.getDest() instanceof ArrayOperand).count();

        assertTrue("Method should contain at least one array store", stores >= 1);
    }
}

