package pt.up.fe.comp.cp2.core.ollir.declarations;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class ClassDeclarationFieldOllirTestArrayAndField extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/declarations/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ClassDeclarationFieldOllirTestArrayAndField() {
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
    public void testClassWithIntField() {
        var classUnit = toOllir("ClassWithIntField.jmm");
        assertEquals("Class should have one field", 1, classUnit.getNumFields());
        var field = classUnit.getFields().getFirst();
        assertEquals("Field name should be '${expected}'", "field", field.getFieldName());
        assertTrue("Field intField should be of type int", BuiltinType.is(field.getFieldType(), BuiltinKind.INT32));
    }

    @Test
    public void testClassWithStringField() {
        var classUnit = toOllir("ClassWithStringField.jmm");
        assertEquals("Class should have one field", 1, classUnit.getNumFields());
        var field = classUnit.getFields().getFirst();
        assertEquals("Field name should be '${expected}'", "field", field.getFieldName());
        var fieldType = field.getFieldType();
        assertTrue("Field intField should be of type int", fieldType instanceof ClassType ct && ct.getName().equals(String.class.getSimpleName()));
    }

    @Test
    public void testClassWithIntArrayField() {
        var classUnit = toOllir("ClassWithIntArrayField.jmm");
        assertEquals("Class should have one field", 1, classUnit.getNumFields());
        var field = classUnit.getFields().getFirst();
        assertEquals("Field name should be '${expected}'", "field", field.getFieldName());
        var fieldType = field.getFieldType();
        assertTrue("Field intField should be of type int", fieldType instanceof ArrayType at && BuiltinType.is(at.getElementType(), BuiltinKind.INT32));
    }


    @Test
    public void testClassWithTwoFields() {
        var classUnit = toOllir("ClassWithTwoFields.jmm");
        assertEquals("Class should have two fields", 2, classUnit.getNumFields());
        var intField = classUnit.getFields().stream().filter(f -> f.getFieldName().equals("intField")).findFirst();
        assertTrue("Class should contain field intField", intField.isPresent());
        assertTrue("Field intField should be of type int", BuiltinType.is(intField.orElseThrow().getFieldType(), BuiltinKind.INT32));
        var boolField = classUnit.getFields().stream().filter(f -> f.getFieldName().equals("boolField")).findFirst();
        assertTrue("Class should contain field boolField", boolField.isPresent());
        assertTrue("Field boolField should be of type boolean", BuiltinType.is(boolField.orElseThrow().getFieldType(), BuiltinKind.BOOLEAN));
    }

}
