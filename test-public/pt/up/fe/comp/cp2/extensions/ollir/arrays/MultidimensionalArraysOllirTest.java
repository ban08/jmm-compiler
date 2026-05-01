package pt.up.fe.comp.cp2.extensions.ollir.arrays;

import org.junit.Test;
import org.specs.comp.ollir.ArrayOperand;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.CallInstruction;
import org.specs.comp.ollir.inst.NewInstruction;
import org.specs.comp.ollir.type.ArrayType;
import pt.up.fe.comp.test.env.OllirTestEnv;


public class MultidimensionalArraysOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/extensions/ollir/arrays/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public MultidimensionalArraysOllirTest() {
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
    public void testMultidimensionalInit() {
        var classUnit = toOllir("MultiDimInit.jmm");
        var method = classUnit.getMethods().stream()
                .filter(m -> m.getMethodName().equals("method"))
                .findFirst().orElseThrow();

        var calls = assertInstExists(CallInstruction.class, method);
        var newInstOpt = calls.stream()
                .filter(c -> c instanceof NewInstruction && c.getReturnType() instanceof ArrayType)
                .findFirst();

        assertTrue("Method should contain a new array initialization instruction", newInstOpt.isPresent());

        var newInst = (NewInstruction) newInstOpt.get();
        assertEquals("A 2D array initialization 'new int[2][3]' should pass exactly 2 size arguments to 'new'",
                2, newInst.getArguments().size());

        assertTrue("The return type should be an ArrayType",
                newInst.getReturnType() instanceof ArrayType);

        var returnType = (ArrayType) newInst.getReturnType();
        assertTrue("The return type should be an array with 2 dimensions",
                returnType.getNumDimensions() == 2);
    }

    @Test
    public void testMultidimensionalAccess() {
        var classUnit = toOllir("MultiDimAccess.jmm");
        var method = classUnit.getMethods().stream()
                .filter(m -> m.getMethodName().equals("method"))
                .findFirst().orElseThrow();

        var assigns = assertInstExists(AssignInstruction.class, method);

        long arrayReads = assigns.stream()
                .flatMap(assign -> getElements(assign.getRhs()).stream())
                .filter(element -> element instanceof ArrayOperand)
                .count();

        long arrayStores = assigns.stream()
                .filter(assign -> assign.getDest() instanceof ArrayOperand)
                .count();

        // For `a[0][1] = 5`, OLLIR must flatten: `t1 = a[0]; t1[1] = 5;` (1 Read, 1 Store)
        // For `return a[1][2]`, OLLIR must flatten: `t2 = a[1]; t3 = t2[2]; return t3;` (2 Reads)
        // Total expected: At least 3 array reads, at least 1 array store

        assertTrue("Multidimensional access requires flattening, producing at least 3 array reads total",
                arrayReads >= 3);
        assertTrue("Multidimensional access requires at least 1 array store for a[0][1] = 5",
                arrayStores >= 1);
    }
}