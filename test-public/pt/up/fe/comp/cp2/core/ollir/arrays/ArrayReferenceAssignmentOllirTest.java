package pt.up.fe.comp.cp2.core.ollir.arrays;

import org.junit.Test;
import org.specs.comp.ollir.ArrayOperand;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.inst.AssignInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;

import static org.junit.Assert.assertFalse;


public class ArrayReferenceAssignmentOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/arrays/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArrayReferenceAssignmentOllirTest() {
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
    public void testArrayReferenceAssignment() {
        var method = toOllir("ArrayReferenceAssignment.jmm", "method");

        var assigns = assertInstExists(AssignInstruction.class, method);

        var refAssignOpt = assigns.stream()
                .filter(assign -> assign.getDest() instanceof Operand &&
                        ((Operand) assign.getDest()).getName().equals("b"))
                .findFirst();

        assertTrue("Method should contain an assignment to variable 'b'", refAssignOpt.isPresent());
        var refAssign = refAssignOpt.get();

        // LHS ('b') must be a standard Operand, NOT an ArrayOperand (which would imply b[...] = ...)
        assertFalse("Left hand side should be a standard reference Operand, not an ArrayOperand",
                refAssign.getDest() instanceof ArrayOperand);

        // RHS ('a') must also be a standard Operand, NOT an ArrayOperand (which would imply ... = a[...])
        var rhsElements = getElements(refAssign.getRhs());
        boolean isRhsSimpleOperand = rhsElements.stream().anyMatch(e ->
                e instanceof Operand &&
                        !(e instanceof ArrayOperand) &&
                        ((Operand) e).getName().equals("a")
        );

        assertTrue("Right hand side should contain the standard reference Operand 'a', not an ArrayOperand", isRhsSimpleOperand);
    }
}