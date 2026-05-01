package pt.up.fe.comp.cp2.extensions.ollir.arrays;

import org.junit.Test;
import org.specs.comp.ollir.ArrayOperand;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.CallInstruction;
import org.specs.comp.ollir.inst.NewInstruction;
import org.specs.comp.ollir.type.ArrayType;
import pt.up.fe.comp.test.env.OllirTestEnv;


public class ArrayInitializerListOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/extensions/ollir/arrays/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArrayInitializerListOllirTest() {
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
    public void testArrayInitializerList() {
        var classUnit = toOllir("ArrayInitList.jmm");
        var method = classUnit.getMethods().stream()
                .filter(m -> m.getMethodName().equals("method"))
                .findFirst().orElseThrow();

        var calls = assertInstExists(CallInstruction.class, method);
        var newInstOpt = calls.stream()
                .filter(c -> c instanceof NewInstruction && c.getReturnType() instanceof ArrayType)
                .findFirst();

        assertTrue("Method should contain a new array initialization instruction", newInstOpt.isPresent());

        var newInst = (NewInstruction) newInstOpt.get();
        assertEquals("Array initialized with {10, 20, 30} should have exactly 1 size argument mapped to length 3",
                1, newInst.getArguments().size());

        var assigns = assertInstExists(AssignInstruction.class, method);
        long arrayStores = assigns.stream()
                .filter(a -> a.getDest() instanceof ArrayOperand)
                .count();

        assertTrue("Array initializer with 3 elements must generate at least 3 array store instructions",
                arrayStores >= 3);
    }
}