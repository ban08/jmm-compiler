package pt.up.fe.comp.cp2.extensions.ollir.declarations;

import org.junit.Test;
import org.specs.comp.ollir.AccessModifier;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.test.env.OllirTestEnv;

import java.util.List;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class MethodVisibilityOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = MethodVisibilityOllirTest.class.getPackageName().replace(".", "/")
            + "/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public MethodVisibilityOllirTest() {
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
    public void testPrivateMethodDeclaration() {
        setDescription("Private instance method declaration");
        var classUnit = toOllir("PrivateMethodDeclaration.jmm");
        var method = classUnit.getMethods().stream().filter(m -> !m.isConstructMethod()).findFirst();
        assertTrue("Method should be present", method.isPresent());
        assertEquals("Method should be ${expected}", AccessModifier.PRIVATE, method.orElseThrow().getMethodAccessModifier());
    }

    @Test
    public void testProtectedMethodDeclaration() {
        setDescription("Protected instance method declaration");
        var classUnit = toOllir("ProtectedMethodDeclaration.jmm");
        var method = classUnit.getMethods().stream().filter(m -> !m.isConstructMethod()).findFirst();
        assertTrue("Method should be present", method.isPresent());
        assertEquals("Method should be ${expected}", AccessModifier.PROTECTED, method.orElseThrow().getMethodAccessModifier());
    }

    @Test
    public void testPrivateStaticMethodDeclaration() {
        setDescription("Private static method declaration");
        var classUnit = toOllir("PrivateStaticMethodDeclaration.jmm");
        var method = classUnit.getMethods().stream().filter(m -> !m.isConstructMethod()).findFirst();
        assertTrue("Method should be present", method.isPresent());
        assertEquals("Method should be ${expected}", AccessModifier.PRIVATE, method.orElseThrow().getMethodAccessModifier());
        assertTrue("Method should be static", method.orElseThrow().isStaticMethod());
    }

    @Test
    public void testProtectedStaticMethodDeclaration() {
        setDescription("Protected instance method declaration");
        var classUnit = toOllir("ProtectedStaticMethodDeclaration.jmm");
        var method = classUnit.getMethods().stream().filter(m -> !m.isConstructMethod()).findFirst();
        assertTrue("Method should be present", method.isPresent());
        assertEquals("Method should be ${expected}", AccessModifier.PROTECTED, method.orElseThrow().getMethodAccessModifier());
        assertTrue("Method should be static", method.orElseThrow().isStaticMethod());

    }

    @Test
    public void testClassWithMixedMethodVisibilities() {
        setDescription("Parse a class with methods of varying visibilities");
        var classUnit = toOllir("MixedMethodVisibilities.jmm");
        var methods = classUnit.getMethods().stream().filter(m -> !m.isConstructMethod()).toList();
        assertEquals("Methods should contain only ${expected} methods", 8, methods.size());
        for (var modifier : AccessModifier.values()) {
            //test has 2 of each
            assertEquals("Should have ${expected} method with modifier " + modifier, 2L, methods.stream().filter(m -> m.getMethodAccessModifier() == modifier).count());
        }
        assertEquals("Should have ${expected} static methods", 4L, methods.stream().filter(Method::isStaticMethod).count());
    }
}
