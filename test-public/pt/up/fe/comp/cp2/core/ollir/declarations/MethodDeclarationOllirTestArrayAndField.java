package pt.up.fe.comp.cp2.core.ollir.declarations;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class MethodDeclarationOllirTestArrayAndField extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/declarations/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public MethodDeclarationOllirTestArrayAndField() {
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
    public void testClassWithFieldsAndMethods() {
        var classUnit = toOllir("ClassWithFieldsAndMethods.jmm");
        assertEquals("Class should have two fields", 2, classUnit.getNumFields());
        assertEquals("Class should have two methods", 2L, classUnit.getMethods().stream().filter(m -> !m.isConstructMethod()).count());
        assertTrue("Class should contain field intField", classUnit.getFields().stream().anyMatch(f -> f.getFieldName().equals("intField")));
        assertTrue("Class should contain field boolField", classUnit.getFields().stream().anyMatch(f -> f.getFieldName().equals("boolField")));
        assertTrue("Class should contain method method1", classUnit.getMethods().stream().anyMatch(m -> m.getMethodName().equals("method1")));
        assertTrue("Class should contain method method2", classUnit.getMethods().stream().anyMatch(m -> m.getMethodName().equals("method2")));
    }

}
