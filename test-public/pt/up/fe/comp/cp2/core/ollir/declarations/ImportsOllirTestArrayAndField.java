package pt.up.fe.comp.cp2.core.ollir.declarations;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.type.ClassKind;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class ImportsOllirTestArrayAndField extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/declarations/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ImportsOllirTestArrayAndField() {
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
    public void testFieldTypeIsExplicitlyImported() {
        var classUnit = toOllir("FieldTypeIsExplicitlyImported.jmm");
        var field = classUnit.getFields().stream().filter(f -> f.getFieldName().equals("o")).findFirst();
        assertTrue("Field 'o' should be present", field.isPresent());
        var fieldType = field.orElseThrow().getFieldType();
        assertTrue("Field type is a class", ClassType.is(fieldType, ClassKind.OBJECTREF));
        assertEquals("Field type name should be ${expected}", "Quicksort", ((ClassType) fieldType).getName());
    }

    @Test
    public void testFieldTypeIsImplicitlyImported() {
        var classUnit = toOllir("FieldTypeIsImplicitlyImported.jmm");
        var field = classUnit.getFields().stream().filter(f -> f.getFieldName().equals("o")).findFirst();
        assertTrue("Field 'o' should be present", field.isPresent());
        var fieldType = field.orElseThrow().getFieldType();
        assertTrue("Field type is a class", ClassType.is(fieldType, ClassKind.OBJECTREF));
        assertEquals("Field type name should be ${expected}", "Object", ((ClassType) fieldType).getName());
    }
}
