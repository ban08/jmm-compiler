package pt.up.fe.comp.cp2.core.ollir.entitiesandops;

import org.junit.Test;
import org.specs.comp.ollir.ArrayOperand;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.OperationType;
import org.specs.comp.ollir.inst.*;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class ArithmeticOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/entitiesandops/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArithmeticOllirTest() {
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
    public void testAddLiterals() {
        var method = toOllir("AddLiterals.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
    }

    @Test
    public void testAddLiteralWithParam() {
        var method = toOllir("AddLiteralWithParam.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
    }

    @Test
    public void testMultipleAdds() {
        var method = toOllir("MultipleAdds.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 10);
    }

    @Test
    public void testSubLiteralWithParam() {
        var method = toOllir("SubLiteralWithParam.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1);
    }

    @Test
    public void testMultipleAddSubs() {
        var method = toOllir("MultipleAddSubs.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 5);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 5);
    }

    @Test
    public void testAddSubOrder() {
        var method = toOllir("AddSubOrder.jmm", "method");

        var add = assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1).getFirst();
        var sub = assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1).getFirst();
        assertOrder(method, add, sub);
    }

    @Test
    public void testMultLiteralWithParam() {
        var method = toOllir("MultLiteralWithParam.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.MUL, 1);
    }


    @Test
    public void testMultipleMultDivs() {
        var method = toOllir("MultipleMultDivs.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.MUL, 5);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.DIV, 5);
    }

    @Test
    public void testAddMulOrder() {
        var method = toOllir("AddMulOrder.jmm", "method");

        var add = assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1).getFirst();
        var mul = assertAtLeast(method, BinaryOpInstruction.class, OperationType.MUL, 1).getFirst();
        assertOrder(method, mul, add);
    }

    @Test
    public void testMultipleArithmeticLevels() {
        var method = toOllir("MultipleArithmeticLevels.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 3);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.DIV, 3);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.MUL, 3);
    }


    @Test
    public void testDivLiteralWithParam() {
        var method = toOllir("DivLiteralWithParam.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.DIV, 1);
    }


    @Test
    public void testSimpleParenthesis() {
        var method = toOllir("SimpleParenthesis.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.MUL, 1);
    }

    @Test
    public void testMultipleArithmeticWithParenthesis() {
        var method = toOllir("MultipleArithmeticWithParenthesis.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 3);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.DIV, 3);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.MUL, 3);
    }

    @Test
    public void testNestedParenthesis() {
        var method = toOllir("NestedParenthesis.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 2);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.MUL, 1);
    }

    @Test
    public void testComplexNestedParenthesis() {
        var method = toOllir("ComplexNestedParenthesis.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 2);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.DIV, 2);
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.MUL, 2);
    }
}

