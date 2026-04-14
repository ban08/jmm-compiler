package pt.up.fe.comp.cp2.core.ollir.entitiesandops;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.GetFieldInstruction;
import org.specs.comp.ollir.inst.PutFieldInstruction;
import org.specs.comp.ollir.inst.utils.InstTypeUtils;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class AssignmentsOllirTestArrayAndField extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/entitiesandops/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public AssignmentsOllirTestArrayAndField() {
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
    public void testAssignField() {
        var method = toOllir("AssignField.jmm", "method");

        var putField = assertInstExists(PutFieldInstruction.class, method);
        var assignToFieldOpt = putField.stream().filter(pf -> pf.getField().getName().equals("a")).findFirst();
        assertTrue("There should be an assignment to field 'a'", assignToFieldOpt.isPresent());
        var assignToField = assignToFieldOpt.orElseThrow();

        assertTrue("Field a must be i32", BuiltinType.is(assignToField.getField().getType(), BuiltinKind.INT32));
        assertTrue("RHS of assignment to 'field' must be a i32", BuiltinType.is(assignToField.getValue().getType(), BuiltinKind.INT32));
    }

    @Test
    public void testAssignFromField() {
        var method = toOllir("AssignFromField.jmm", "method");


        var getfields = assertInstExists(GetFieldInstruction.class, method);
        var getFieldOpt = getfields.stream().filter(a -> a.getField().getName().equals("b")).findFirst();
        assertTrue("There should be a getfield to field 'b'", getFieldOpt.isPresent());
        assertTrue("Accessed field b must be i32", getFieldOpt.orElseThrow().getField().getType() instanceof BuiltinType bt && BuiltinType.is(bt, BuiltinKind.INT32));

        var assign = assertInstExists(AssignInstruction.class, method);
        var assignFromFieldOpt = assign.stream().filter(a -> a.getDest() instanceof Operand op && op.getName().equals("a")).findFirst();
        assertTrue("There should be an assignment to variable 'a'", assignFromFieldOpt.isPresent());
        var rhsTypeOpt = InstTypeUtils.getType(assignFromFieldOpt.orElseThrow());
        assertTrue("Should be able to determine type of rhs assignment to 'a'", rhsTypeOpt.isPresent());
        assertTrue("RHS of assignment to 'a' must be a i32", BuiltinType.is(rhsTypeOpt.orElseThrow(), BuiltinKind.INT32));
    }


}

