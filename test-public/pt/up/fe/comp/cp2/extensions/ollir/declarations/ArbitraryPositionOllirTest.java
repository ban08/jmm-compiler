package pt.up.fe.comp.cp2.extensions.ollir.declarations;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.test.env.OllirTestEnv;

import java.util.List;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;

public class ArbitraryPositionOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = ArbitraryPositionOllirTest.class.getPackageName().replace(".", "/")
            + "/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArbitraryPositionOllirTest() {
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
    public void testMethodThenField() {
        setDescription("Method followed by field inside a class");
        var classUnit = toOllir("MethodThenField.jmm");
        assertEquals("Class should have one field", 1, classUnit.getNumFields());
        var field = classUnit.getFields().getFirst();
        assertEquals("Field name should be '${expected}'", "a", field.getFieldName());
        assertTrue("Field intField should be of type int", BuiltinType.is(field.getFieldType(), BuiltinKind.INT32));
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("foo")).findFirst();
        assertTrue("Method 'foo' should be present", method.isPresent());
        assertTrue("Return type of method 'foo' should be void", BuiltinType.is(method.orElseThrow().getReturnType(), BuiltinKind.VOID));
    }

    @Test
    public void testFieldMethodField() {
        setDescription("Field, followed by method, followed by field inside a class");
        var classUnit = toOllir("FieldMethodField.jmm");
        assertEquals("Class should have ${expected} fields", 2, classUnit.getNumFields());
        var fields = classUnit.getFields();
        for (var name : List.of("a", "b")) {
            assertTrue("One field must be named " + name, fields.stream().anyMatch(f -> f.getFieldName().equals(name)));
        }
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("foo")).findFirst();
        assertTrue("Method 'foo' should be present", method.isPresent());
        assertTrue("Return type of method 'foo' should be void", BuiltinType.is(method.orElseThrow().getReturnType(), BuiltinKind.VOID));
    }


    @Test
    public void testMethodsWithParametersAndFields() {
        setDescription("Methods with parameters appearing alongside fields");
        var classUnit = toOllir("MethodsWithParametersAndFields.jmm");
        assertEquals("Class should have ${expected} field(s)", 1, classUnit.getNumFields());

        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("add")).findFirst();
        assertTrue("Method 'add' should be present", method.isPresent());
        assertTrue("Return type of method 'add' should be void", BuiltinType.is(method.orElseThrow().getReturnType(), BuiltinKind.VOID));
        assertEquals("Method 'add' should have {$expected} parameter(s)", 1, method.orElseThrow().getParams().size());
        method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("reset")).findFirst();
        assertTrue("Method 'reset' should be present", method.isPresent());
        assertTrue("Return type of method 'reset' should be void", BuiltinType.is(method.orElseThrow().getReturnType(), BuiltinKind.VOID));
        assertEquals("Method 'reset' should have {$expected} parameter(s)", 0, method.orElseThrow().getParams().size());
    }


    @Test
    public void testInterleavedFieldsAndMethods() {
        setDescription("Interleaved fields and methods in arbitrary order should generated correct Ollir");
        var classUnit = toOllir("InterleavedFieldsAndMethods.jmm");
        assertEquals("Class should have ${expected} field(s)", 3, classUnit.getNumFields());
        assertEquals("Class should have ${expected} method(s)", 2L, classUnit.getMethods().stream().filter(m -> !m.isConstructMethod()).count());
    }
}
