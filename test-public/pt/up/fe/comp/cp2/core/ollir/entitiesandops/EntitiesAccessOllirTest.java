package pt.up.fe.comp.cp2.core.ollir.entitiesandops;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.inst.GetFieldInstruction;
import org.specs.comp.ollir.inst.ReturnInstruction;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class EntitiesAccessOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/entitiesandops/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public EntitiesAccessOllirTest() {
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
    public void testAccessLocalVariables() {
        var method = toOllir("AccessLocalVariable.jmm", "method");

        var retInst = assertInstExists(ReturnInstruction.class, method);
        assertTrue("Return must be i32", BuiltinType.is(retInst.getFirst().getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testAccessParam() {
        var method = toOllir("AccessParam.jmm", "method");

        assertEquals("Method should have one parameter", 1, method.getParams().size());
        var retInst = assertInstExists(ReturnInstruction.class, method);
        assertTrue("Return must be i32", BuiltinType.is(retInst.getFirst().getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testAccessThis() {
        var method = toOllir("AccessThis.jmm", "method");


        var retInst = assertInstExists(ReturnInstruction.class, method);
        var retType = retInst.getFirst().getReturnType();
        assertTrue("Return must be AccessThis", retType instanceof ClassType ct && ct.getName().endsWith("AccessThis")); //accept fully qualified class names as well
    }

    @Test
    public void testAccessImplicitClass() {
        var method = toOllir("AccessImplicitClass.jmm", "method");

        var retInst = assertInstExists(ReturnInstruction.class, method);
        var retType = retInst.getFirst().getReturnType();
        assertTrue("Return must be String", retType instanceof ClassType ct && ct.getName().endsWith("String")); //accept fully qualified class names as well
    }

    @Test
    public void testAccessImportedClass() {
        var method = toOllir("AccessImportedClass.jmm", "method");

        var retInst = assertInstExists(ReturnInstruction.class, method);
        var retType = retInst.getFirst().getReturnType();
        assertTrue("Return must be Quicksort", retType instanceof ClassType ct && ct.getName().endsWith("Quicksort")); //accept fully qualified class names as well
    }
}