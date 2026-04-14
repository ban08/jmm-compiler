package pt.up.fe.comp.cp2.core.ollir.arrays;

import org.junit.Test;
import org.specs.comp.ollir.ArrayOperand;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.BinaryOpInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;


public class ArrayAccessOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/arrays/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArrayAccessOllirTest() {
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
    public void testSimpleAccess() {
        var method = toOllir("ArrayAccessSimple.jmm", "method");

        var assigns = assertInstExists(AssignInstruction.class, method);

        boolean hasArrayAssign = assigns.stream().anyMatch(assign ->
                assign.getDest() instanceof ArrayOperand);

        assertTrue("Method should contain an assignment to an array element", hasArrayAssign);
    }

    @Test
    public void testComplexAccess() {
        var method = toOllir("ArrayAccessComplex.jmm", "method");

        var assigns = assertInstExists(AssignInstruction.class, method);
        var binOps = assertInstExists(BinaryOpInstruction.class, method);

        // Because OLLIR doesn't allow expressions in array indices, the i+2 and i*2
        // must be calculated in separate binary op instructions first.
        assertTrue("Method should calculate the complex indices using BinaryOpInstructions", binOps.size() >= 2);

        int arrayStores = Math.toIntExact(assigns.stream().filter(a -> a.getDest() instanceof ArrayOperand).count());
        assertEquals("Method should contain exactly 1 array store", 1, arrayStores);
    }

    @Test
    public void testNestedAccess() {
        var method = toOllir("ArrayAccessNested.jmm", "method");

        var assigns = assertInstExists(AssignInstruction.class, method);

        // For a[b[0]] = 5, OLLIR must flatten this.
        // It requires an array read (t1 = b[0]) followed by an array store (a[t1] = 5).

        long arrayReads = assigns.stream()
                .flatMap(assign -> getElements(assign.getRhs()).stream())
                .filter(element -> element instanceof ArrayOperand)
                .count();

        long arrayStores = assigns.stream()
                .filter(assign -> assign.getDest() instanceof ArrayOperand)
                .count();

        assertTrue("Nested array access requires at least 1 array read (for the index)", arrayReads >= 1);
        assertTrue("Nested array access requires at least 1 array store", arrayStores >= 1);
    }
}