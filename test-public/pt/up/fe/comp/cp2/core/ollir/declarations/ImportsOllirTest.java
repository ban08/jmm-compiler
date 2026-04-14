package pt.up.fe.comp.cp2.core.ollir.declarations;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.type.ClassKind;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class ImportsOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/declarations/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ImportsOllirTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    public ClassUnit toOllir(String resourceName) {
        var ollirResult = jmmToOllir(resourceName);
        var classUnit = ollirResult.getOllirClass();
        assertNotNull("Ollir class unit should not be null", classUnit);
        assertEquals("Class name not what was expected", resourceName.replace(".jmm", ""), classUnit.getClassName());
        return classUnit;
    }

    @Test
    public void testBasicWithImports() {
        var ollirResult = jmmToOllir("BasicClassWithImports.jmm");
        var classUnit = ollirResult.getOllirClass();
        assertNotNull("Ollir class unit should not be null", classUnit);
        assertEquals("Class name not what was expected", "BasicClassWithImports", classUnit.getClassName());
        assertEquals("Imports should exist", 2, classUnit.getImports().size());
    }

    @Test
    public void testBasicWithSuper() {
        var classUnit = toOllir("BasicClassWithSuper.jmm");
        assertEquals("Super class name not what was expected", "Quicksort", classUnit.getSuperClass());
    }


    @Test
    public void testBasicWithExplicitSuperObject() {
        var classUnit = toOllir("BasicClassWithExplicitSuperObject.jmm");
        assertEquals("Super class name not what was expected", "Object", classUnit.getSuperClass());
    }

    @Test
    public void testMethodParameterIsExplicitlyImported() {
        var classUnit = toOllir("MethodParameterIsExplicitlyImported.jmm");
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("make")).findFirst();
        assertTrue("Method 'make' should be present", method.isPresent());
        var paramType = method.orElseThrow().getParam(0).getType();
        assertTrue("Method parameter is a class", ClassType.is(paramType, ClassKind.OBJECTREF));
        assertEquals("Method parameter type name should be ${expected}", "Quicksort", ((ClassType) paramType).getName());
    }

    @Test
    public void testMethodReturnTypeIsImplicitlyImported() {
        var classUnit = toOllir("MethodReturnTypeIsImplicitlyImported.jmm");
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("make")).findFirst();
        assertTrue("Method 'make' should be present", method.isPresent());
        var returnType = method.orElseThrow().getReturnType();
        assertTrue("Method return type is a class", ClassType.is(returnType, ClassKind.OBJECTREF));
        assertEquals("Method return type name should be ${expected}", "Object", ((ClassType) returnType).getName());
    }
}
