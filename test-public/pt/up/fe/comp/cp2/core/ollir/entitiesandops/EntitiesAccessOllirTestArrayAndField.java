package pt.up.fe.comp.cp2.core.ollir.entitiesandops;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.GetFieldInstruction;
import org.specs.comp.ollir.inst.ReturnInstruction;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.test.env.OllirTestEnv;
import pt.up.fe.specs.util.treenode.NodeInsertUtils;

public class EntitiesAccessOllirTestArrayAndField extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/entitiesandops/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public EntitiesAccessOllirTestArrayAndField() {
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
    public void testAccessField() {
        var method = toOllir("AccessField.jmm", "method");

        ((ClassUnit) method.getParent()).getFields().stream().filter(f -> f.getFieldName().equals("a")).findFirst()
                .orElseThrow(() -> new AssertionError("Field 'a' should exist in the class"));

        assertInstExists(GetFieldInstruction.class, method);
        var retInst = assertInstExists(ReturnInstruction.class, method);
        assertTrue("Return must be i32", BuiltinType.is(retInst.getFirst().getReturnType(), BuiltinKind.INT32));

    }

}