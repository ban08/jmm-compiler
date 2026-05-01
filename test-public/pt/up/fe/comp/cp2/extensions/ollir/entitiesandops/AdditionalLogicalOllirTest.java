package pt.up.fe.comp.cp2.extensions.ollir.entitiesandops;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.OperationType;
import org.specs.comp.ollir.inst.BinaryOpInstruction;
import org.specs.comp.ollir.inst.CondBranchInstruction;
import org.specs.comp.ollir.inst.InvokeVirtualInstruction;
import org.specs.comp.ollir.inst.UnaryOpInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class AdditionalLogicalOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/extensions/ollir/entitiesandops/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public AdditionalLogicalOllirTest() {
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
    public void testGreaterThanLiterals() {
        var method = toOllir("GreaterThanLiterals.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.GTH, 1);
    }

    @Test
    public void testGreaterThanLiteralAndParam() {
        var method = toOllir("GreaterThanLiteralAndParam.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.GTH, 1);
    }

    @Test
    public void testGreaterThanComplexCalc() {
        var method = toOllir("GreaterThanComplexCalc.jmm", "method");

        var lth = assertAtLeast(method, BinaryOpInstruction.class, OperationType.GTH, 1).getFirst();
        var adds = assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
        var divs = assertAtLeast(method, BinaryOpInstruction.class, OperationType.DIV, 2);
        var mults = assertAtLeast(method, BinaryOpInstruction.class, OperationType.MUL, 1);
        assertOrder(method, adds.getFirst(), lth);
        assertOrder(method, divs.getFirst(), lth);
        assertOrder(method, mults.getFirst(), lth);
    }


    @Test
    public void testNotAndGreaterThan() {
        var method = toOllir("NotAndGreaterThan.jmm", "method");
        var lth = assertAtLeast(method, BinaryOpInstruction.class, OperationType.GTH, 1);
        var not = assertAtLeast(method, UnaryOpInstruction.class, OperationType.LOGICAL_NOT, 1);
        assertOrder(method, lth.getFirst(), not.getFirst());
    }

    @Test
    public void testGreaterOrEquals() {
        var method = toOllir("GreaterOrEquals.jmm", "method");
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.GTE, 1);
    }

    @Test
    public void testEquals() {
        var method = toOllir("Equals.jmm", "method");
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.EQ, 1);
    }


    @Test
    public void testNotEquals() {
        var method = toOllir("NotEquals.jmm", "method");
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.NEQ, 1);
    }

    @Test
    public void testLessOrEquals() {
        var method = toOllir("LessOrEquals.jmm", "method");
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.LTE, 1);
    }


    @Test
    public void testOrLiterals() {
        var method = toOllir("OrLiterals.jmm", "method");
        assertInstExists(CondBranchInstruction.class, method);
    }

    @Test
    public void testOrParams() {
        var method = toOllir("OrParams.jmm", "method");
        assertInstExists(CondBranchInstruction.class, method);
    }

    @Test
    public void testOrNested() {
        var method = toOllir("OrNested.jmm", "method");
        assertAtLeast(method, CondBranchInstruction.class, 4);
    }

    @Test
    public void testOrLessThanNot() {
        var method = toOllir("OrLessThanNot.jmm", "method");
        assertAtLeast(method, CondBranchInstruction.class, 3);
    }


    @Test
    public void testMultipleLogicals() {
        var method = toOllir("MultipleLogicals.jmm", "method");
        assertAtLeast(method, CondBranchInstruction.class, 4);
    }
}

