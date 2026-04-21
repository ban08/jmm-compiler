package pt.up.fe.comp.cp2.core.ollir.declarations;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class MethodDeclarationOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/declarations/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public MethodDeclarationOllirTest() {
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
    public void testSingleVoidMethod() {
        var classUnit = toOllir("ClassWithVoidMethod.jmm");
        assertEquals("Class should have one method", 1L, classUnit.getMethods().stream().filter(m -> !m.isConstructMethod()).count());
        assertTrue("Class should contain method 'method'", classUnit.getMethods().stream().anyMatch(m -> m.getMethodName().equals("method")));
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst().orElseThrow();
        assertTrue("Method return type should be void", BuiltinType.is(method.getReturnType(), BuiltinKind.VOID));
    }

    @Test
    public void testMethodWithReturnType() {
        var classUnit = toOllir("MethodWithReturnType.jmm");
        assertEquals("Class should have one method", 1L, classUnit.getMethods().stream().filter(m -> !m.isConstructMethod()).count());
        assertTrue("Class should contain method 'method'", classUnit.getMethods().stream().anyMatch(m -> m.getMethodName().equals("method")));
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst().orElseThrow();
        assertTrue("Method return type should be int", BuiltinType.is(method.getReturnType(), BuiltinKind.INT32));
    }

    @Test
    public void testClassWithTwoMethods() {
        var classUnit = toOllir("ClassWithTwoMethods.jmm");
        assertEquals("Class should have two methods", 2L, classUnit.getMethods().stream().filter(m -> !m.isConstructMethod()).count());
        assertTrue("Class should contain method method1", classUnit.getMethods().stream().anyMatch(m -> m.getMethodName().equals("method1")));
        assertTrue("Class should contain method method2", classUnit.getMethods().stream().anyMatch(m -> m.getMethodName().equals("method2")));
    }

    @Test
    public void testClassWithMainMethod() {
        var classUnit = toOllir("ClassWithMainMethod.jmm");
        assertTrue("Class should contain method main", classUnit.getMethods().stream().anyMatch(m -> m.getMethodName().equals("main")));
    }

    @Test
    public void testMethodWithSingleParam() {
        var classUnit = toOllir("MethodWithSingleParam.jmm");
        assertEquals("Class should have one method", 1L, classUnit.getMethods().stream().filter(m -> !m.isConstructMethod()).count());
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Class should contain method 'method'", method.isPresent());
        assertEquals("Method should have one parameter", 1, method.orElseThrow().getParams().size());
        var param = method.get().getParams().getFirst();
        assertTrue("Method parameter should be int", BuiltinType.is(param.getType(), BuiltinKind.INT32));
    }

    @Test
    public void testMethodWithMultipleParam() {
        var classUnit = toOllir("MethodWithMultipleParam.jmm");
        assertEquals("Class should have one method", 1L, classUnit.getMethods().stream().filter(m -> !m.isConstructMethod()).count());
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertTrue("Class should contain method 'method'", method.isPresent());
        assertEquals("Method should have ${expected} parameters", 3, method.orElseThrow().getParams().size());
        var param1 = method.get().getParams().get(0);
        var param2 = method.get().getParams().get(1);
        var param3 = method.get().getParams().get(2);
        assertTrue("First method parameter should be int", BuiltinType.is(param2.getType(), BuiltinKind.INT32));
        assertTrue("Second method parameter should be int", BuiltinType.is(param2.getType(), BuiltinKind.INT32));
        assertTrue("Third method parameter should be boolean", BuiltinType.is(param3.getType(), BuiltinKind.BOOLEAN));
    }
}
