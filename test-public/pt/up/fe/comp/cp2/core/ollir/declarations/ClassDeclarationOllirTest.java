package pt.up.fe.comp.cp2.core.ollir.declarations;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.type.*;
import pt.up.fe.comp.test.env.OllirTestEnv;

import static org.hamcrest.CoreMatchers.hasItem;

public class ClassDeclarationOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/declarations/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ClassDeclarationOllirTest() {
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
    public void testBasicEmptyClass() {
        var classUnit = toOllir("BasicClass.jmm");
        assertTrue("Default constructor must exist", classUnit.getMethods().stream().anyMatch(Method::isConstructMethod));
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
}
