package pt.up.fe.comp.cp2.core.ollir.entitiesandops;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.OperationType;
import org.specs.comp.ollir.inst.*;
import pt.up.fe.comp.test.env.OllirTestEnv;


public class LogicalOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/entitiesandops/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public LogicalOllirTest() {
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
    public void testSimpleUnaryNot() {
        var method = toOllir("SimpleUnaryNot.jmm", "method");

        assertAtLeast(method, UnaryOpInstruction.class, OperationType.LOGICAL_NOT, 1);
    }

    @Test
    public void testLessThanLiterals() {
        var method = toOllir("LessThanLiterals.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.LTH, 1);
    }

    @Test
    public void testLessThanLiteralAndParam() {
        var method = toOllir("LessThanLiteralAndParam.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.LTH, 1);
    }

    @Test
    public void testLessThanComplexCalc() {
        var method = toOllir("LessThanComplexCalc.jmm", "method");

        var lth = assertAtLeast(method, BinaryOpInstruction.class, OperationType.LTH, 1).getFirst();
        var adds = assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
        var divs = assertAtLeast(method, BinaryOpInstruction.class, OperationType.DIV, 2);
        var mults = assertAtLeast(method, BinaryOpInstruction.class, OperationType.MUL, 1);
        assertOrder(method, adds.getFirst(), lth);
        assertOrder(method, divs.getFirst(), lth);
        assertOrder(method, mults.getFirst(), lth);
    }


    @Test
    public void testNotAndLessThan() {
        var method = toOllir("NotAndLessThan.jmm", "method");
        var lth = assertAtLeast(method, BinaryOpInstruction.class, OperationType.LTH, 1);
        var not = assertAtLeast(method, UnaryOpInstruction.class, OperationType.LOGICAL_NOT, 1);
        assertOrder(method, lth.getFirst(), not.getFirst());
    }

    @Test
    public void testAndLiterals() {
        var method = toOllir("AndLiterals.jmm", "method");
        assertInstExists(CondBranchInstruction.class, method);
    }

    @Test
    public void testAndParams() {
        var method = toOllir("AndParams.jmm", "method");
        assertInstExists(CondBranchInstruction.class, method);
    }

    @Test
    public void testAndNested() {
        var method = toOllir("AndNested.jmm", "method");
        assertAtLeast(method, CondBranchInstruction.class, 4);
    }

    @Test
    public void testAndLessThanNot() {
        var method = toOllir("AndLessThanNot.jmm", "method");
        assertAtLeast(method, CondBranchInstruction.class, 3);
    }
}

