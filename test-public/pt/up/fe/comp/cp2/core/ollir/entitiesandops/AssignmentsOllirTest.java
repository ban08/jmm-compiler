package pt.up.fe.comp.cp2.core.ollir.entitiesandops;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.utils.InstTypeUtils;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class AssignmentsOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/entitiesandops/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public AssignmentsOllirTest() {
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
    public void testAssignLocalVariable() {
        var method = toOllir("AssignLocalVariable.jmm", "method");

        var assign = assertInstExists(AssignInstruction.class, method);
        var assignToAOpt = assign.stream().filter(a -> a.getDest() instanceof Operand op && op.getName().equals("a")).findFirst();
        assertTrue("There should be an assignment to variable 'a'", assignToAOpt.isPresent());
        var assignToA = assignToAOpt.orElseThrow();

        assertTrue("a must be i32", assignToA.getDest() instanceof Operand op && BuiltinType.is(op.getType(), BuiltinKind.INT32));
        var rhsTypeOpt = InstTypeUtils.getType(assignToA);
        assertTrue("Should be able to determine type of rhs assignment to 'a'", rhsTypeOpt.isPresent());
        assertTrue("RHS of assignment to 'a' must be a i32", BuiltinType.is(rhsTypeOpt.orElseThrow(), BuiltinKind.INT32));
    }

    @Test
    public void testAssignParam() {
        var method = toOllir("AssignParam.jmm", "method");

        var assign = assertInstExists(AssignInstruction.class, method);
        var assignToAOpt = assign.stream().filter(a -> a.getDest() instanceof Operand op && op.getName().equals("a")).findFirst();
        assertTrue("There should be an assignment to variable 'a'", assignToAOpt.isPresent());
        var assignToA = assignToAOpt.orElseThrow();

        assertTrue("a must be i32", assignToA.getDest() instanceof Operand op && BuiltinType.is(op.getType(), BuiltinKind.INT32));
        var rhsTypeOpt = InstTypeUtils.getType(assignToA);
        assertTrue("Should be able to determine type of rhs assignment to 'a'", rhsTypeOpt.isPresent());
        assertTrue("RHS of assignment to 'a' must be a i32", BuiltinType.is(rhsTypeOpt.orElseThrow(), BuiltinKind.INT32));
    }

    @Test
    public void testAssignUsingThis() {
        var method = toOllir("AssignUsingThis.jmm", "method");

        var assign = assertInstExists(AssignInstruction.class, method);
        var assignToAOpt = assign.stream().filter(a -> a.getDest() instanceof Operand op && op.getName().equals("a")).findFirst();
        assertTrue("There should be an assignment to variable 'a'", assignToAOpt.isPresent());
        var assignToA = assignToAOpt.orElseThrow();

        assertTrue("a must be 'AssignUsingThis' class", assignToA.getDest() instanceof Operand op && op.getType() instanceof ClassType ct && ct.getName().endsWith("AssignUsingThis"));
        var rhsTypeOpt = InstTypeUtils.getType(assignToA);
        assertTrue("Should be able to determine type of rhs assignment to 'a'", rhsTypeOpt.isPresent());
        assertTrue("RHS of assignment to 'a' must be a 'AssignUsingThis'", rhsTypeOpt.orElseThrow() instanceof ClassType ct && ct.getName().endsWith("AssignUsingThis"));
        //TODO- find "THIS operand and check type is correct
    }

    @Test
    public void testAssignInSuperType() {
        var method = toOllir("AssignInSuperType.jmm", "method");

        var assign = assertInstExists(AssignInstruction.class, method);
        var assignToAOpt = assign.stream().filter(a -> a.getDest() instanceof Operand op && op.getName().equals("q")).findFirst();
        assertTrue("There should be an assignment to variable 'q'", assignToAOpt.isPresent());
        var assignToA = assignToAOpt.orElseThrow();

        assertTrue("a must be 'Quicksort' class", assignToA.getDest() instanceof Operand op && op.getType() instanceof ClassType ct && ct.getName().endsWith("Quicksort"));
        var rhsTypeOpt = InstTypeUtils.getType(assignToA.getRhs());
        assertTrue("Should be able to determine type of rhs assignment to 'a'", rhsTypeOpt.isPresent());
        var rhsType = rhsTypeOpt.orElseThrow();
        assertTrue("RHS of assignment to 'a' must be a class type", rhsType instanceof ClassType);
        assert rhsType instanceof ClassType;
        var ct = (ClassType) rhsType;
        assertEquals("RHS of assignment to 'a' must be a 'AssignInSuperType'", "AssignInSuperType", ct.getName());
    }
}

