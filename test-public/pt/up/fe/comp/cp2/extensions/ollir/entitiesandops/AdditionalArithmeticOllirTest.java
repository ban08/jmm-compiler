package pt.up.fe.comp.cp2.extensions.ollir.entitiesandops;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.OperationType;
import org.specs.comp.ollir.inst.*;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class AdditionalArithmeticOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/extensions/ollir/entitiesandops/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public AdditionalArithmeticOllirTest() {
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
    public void testRemainder() {
        var method = toOllir("Remainder.jmm", "method");

        assertAtLeast(method, BinaryOpInstruction.class, OperationType.REM, 1);
    }

    @Test
    public void RemMulOrder() {
        var method = toOllir("RemMulOrder.jmm", "method");

        var mul = assertAtLeast(method, BinaryOpInstruction.class, OperationType.MUL, 1).getFirst();
        var rem = assertAtLeast(method, BinaryOpInstruction.class, OperationType.REM, 1).getFirst();
        assertOrder(method, mul, rem);
    }

    @Test
    public void RemSubOrder() {
        var method = toOllir("RemSubOrder.jmm", "method");

        var sub = assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1).getFirst();
        var rem = assertAtLeast(method, BinaryOpInstruction.class, OperationType.REM, 1).getFirst();
        assertOrder(method, rem, sub);
    }

    @Test
    public void testNegativeLiteral() {
        var method = toOllir("NegativeLiteral.jmm", "method");
        //-1 => 0-1
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1);
    }

    @Test
    public void testNegativeVar() {
        var method = toOllir("NegativeVar.jmm", "method");
        //-a => 0-a
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1);
    }

    @Test
    public void testNegativeCall() {
        var method = toOllir("NegativeCall.jmm", "method");
        //-this.foo() => t= invoke; 0- t;
        var invokeL = assertAtLeast(method, InvokeVirtualInstruction.class, 1);
        var binOpL = assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1);
        assertOrder(method, invokeL.getFirst(), binOpL.getFirst());
    }

    @Test
    public void testNegativeParenthesis() {
        var method = toOllir("NegativeParenthesis.jmm", "method");
        //-(b+1)=> t = b + 1; 0-t;
        var sum = assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
        var sub = assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1);
        assertOrder(method, sum.getFirst(), sub.getFirst());
    }


    @Test
    public void testPositiveLiteral() {
        var method = toOllir("PositiveLiteral.jmm", "method");
        //+1 => 1 OR 0+1
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 0);
    }

    @Test
    public void testPositiveVar() {
        var method = toOllir("PositiveVar.jmm", "method");
        //+a => a OR 0+a
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 0);
    }

    @Test
    public void testPositiveCall() {
        var method = toOllir("PositiveCall.jmm", "method");
        //this.foo() => t= invoke; 0 + t; OR t
        var invokeL = assertAtLeast(method, InvokeVirtualInstruction.class, 1);
        var binOpL = assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 0);
        if (!binOpL.isEmpty())
            assertOrder(method, invokeL.getFirst(), binOpL.getFirst());
    }

    @Test
    public void testPositiveParenthesis() {
        var method = toOllir("PositiveParenthesis.jmm", "method");
        //+(b-1)=> t = b - 1; 0+t; OR t
        var sum = assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 0);
        var sub = assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1);
        if (!sum.isEmpty())
            assertOrder(method, sub.getFirst(), sum.getFirst());
    }

    @Test
    public void testIncrementSimple() {
        var method = toOllir("IncrementSimple.jmm", "method");
        //++a; => a = a + 1; or t= a +1; a = t;
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
    }

    @Test
    public void testIncrementOnReturn() {
        var method = toOllir("IncrementOnReturn.jmm", "method");
        //++a; => a = a + 1; or t= a +1; a = t;
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
    }

    @Test
    public void testIncrementOnParenthesis() {
        var method = toOllir("IncrementOnParenthesis.jmm", "method");
        //++a; => a = a + 1; or t= a +1; a = t;
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
    }

    @Test
    public void testIncrementOnCall() {
        var method = toOllir("IncrementOnCall.jmm", "method");
        //++a; => a = a + 1; or t= a +1; a = t;
        var adds = assertAtLeast(method, BinaryOpInstruction.class, OperationType.ADD, 1);
        var invokes = assertAtLeast(method, InvokeVirtualInstruction.class, 1);
        assertOrder(method, adds.getFirst(), invokes.getFirst());
    }


    @Test
    public void testDecrementSimple() {
        var method = toOllir("DecrementSimple.jmm", "method");
        //++a; => a = a + 1; or t= a +1; a = t;
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1);
    }

    @Test
    public void testDecrementOnReturn() {
        var method = toOllir("DecrementOnReturn.jmm", "method");
        //++a; => a = a + 1; or t= a +1; a = t;
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1);
    }

    @Test
    public void testDecrementOnParenthesis() {
        var method = toOllir("DecrementOnParenthesis.jmm", "method");
        //++a; => a = a + 1; or t= a +1; a = t;
        assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1);
    }

    @Test
    public void testDecrementOnCall() {
        var method = toOllir("DecrementOnCall.jmm", "method");
        //++a; => a = a + 1; or t= a +1; a = t;
        var adds = assertAtLeast(method, BinaryOpInstruction.class, OperationType.SUB, 1);
        var invokes = assertAtLeast(method, InvokeVirtualInstruction.class, 1);
        assertOrder(method, adds.getFirst(), invokes.getFirst());
    }

}

