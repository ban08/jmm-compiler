package pt.up.fe.comp.cp2.core.ollir.declarations;

import examples.Quicksort;
import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.LiteralElement;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.inst.InvokeSpecialInstruction;
import org.specs.comp.ollir.type.*;
import pt.up.fe.comp.test.env.OllirTestEnv;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;

public class ClassDeclarationOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/declarations/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ClassDeclarationOllirTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }


    public void toOllir(String resourceName, String superName, String superQualifiedName) {
        var ollirResult = jmmToOllir(resourceName);
        var classUnit = ollirResult.getOllirClass();
        var expectedSuperNames = List.of(superName, superQualifiedName);
        assertNotNull("Ollir class unit should not be null", classUnit);
        assertEquals("Class name not what was expected", resourceName.replace(".jmm", ""), classUnit.getClassName());

        assertTrue("Super class name not what was expected", expectedSuperNames.contains(classUnit.getSuperClass()));

        var constructorList = classUnit.getMethods().stream().filter(Method::isConstructMethod).toList();
        assertTrue("Default constructor must exist", !constructorList.isEmpty());
        var constructorNoArgs = constructorList.stream().filter(c -> c.getParams().isEmpty()).toList();
        assertEquals("There should be only one default constructor", 1, constructorNoArgs.size());
        var constructor = constructorList.getFirst();
        var constructorName = constructor.getMethodName();
        assertTrue("Constructor name must be \"<init>\" or no name used, but it was " + constructorName, constructorName.equals("<init>") || constructorName.isBlank());
        var invokeSpecials = assertInstExists(InvokeSpecialInstruction.class, constructor);
        assertEquals("Invoke special to super must be one", 1, invokeSpecials.size());
        var invokeSuper = invokeSpecials.getFirst();
        var invokeOperands = invokeSuper.getOperands();
        assertTrue("InvokeSpecial first operand should be \"this\"", invokeOperands.getFirst() instanceof Operand op && op.getName().equals("this"));
        assertTrue("InvokeSpecial operand 'this' should be annotated with super class type", invokeOperands.getFirst().getType() instanceof ClassType ct && expectedSuperNames.contains(ct.getName()));

        assertTrue("InvokeSpecial method name should be a literal string", invokeSuper.getMethodName().isLiteral());
        var methodName = (LiteralElement) invokeSuper.getMethodName();
        assertEquals("InvokeSpecial method name should be \"<init>\"", "<init>", methodName.getLiteral());
//        var superClass = invokeSuper.getSuperClass();
//        assertTrue("InvokeSpecial super init call should specify the super class name",
//                superClass.isPresent() && expectedSuperNames.contains(superClass.get()));

    }

    @Test
    public void testBasicEmptyClass() {
        toOllir("BasicClass.jmm", Object.class.getSimpleName(), Object.class.getName());

    }

    @Test
    public void testBasicWithSuper() {
        toOllir("BasicClassWithSuper.jmm", "Quicksort", "examples.Quicksort");
    }


    @Test
    public void testBasicWithExplicitSuperObject() {
        toOllir("BasicClassWithExplicitSuperObject.jmm", Object.class.getSimpleName(), Object.class.getName());
    }
}
