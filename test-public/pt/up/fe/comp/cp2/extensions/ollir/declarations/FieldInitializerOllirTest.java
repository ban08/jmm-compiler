package pt.up.fe.comp.cp2.extensions.ollir.declarations;

import org.junit.Test;
import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.InvokeVirtualInstruction;
import org.specs.comp.ollir.inst.NewInstruction;
import org.specs.comp.ollir.inst.PutFieldInstruction;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.test.env.OllirTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.CLASS;

public class FieldInitializerOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = FieldInitializerOllirTest.class.getPackageName().replace(".", "/")
            + "/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public FieldInitializerOllirTest() {
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
    public void testFieldInitializerIntLiteral() {
        setDescription("Field initializer with integer literal");
        var classUnit = toOllir("FieldInitializerIntLiteral.jmm");
        var opt = classUnit.getMethods().stream().filter(Method::isConstructMethod).findFirst();
        assertTrue("Constructor should be present", opt.isPresent());
        var constructor = opt.orElseThrow();

        var insts = assertInstExists(PutFieldInstruction.class, constructor);
        assertTrue("Expect to find a single " + PutFieldInstruction.class.getSimpleName() + " instruction", insts.size() == 1);
        var putField = insts.getFirst();
        assertEquals("PutField should be for field 'a'", "a", putField.getField().getName());
        assertTrue("PutField should be with an integer value", BuiltinType.is(putField.getValue().getType(), BuiltinKind.INT32));
    }

    @Test
    public void testFieldInitializerBooleanLiteral() {
        setDescription("Field initializer with boolean literal");
        var classUnit = toOllir("FieldInitializerBooleanLiteral.jmm");
        var opt = classUnit.getMethods().stream().filter(Method::isConstructMethod).findFirst();
        assertTrue("Constructor should be present", opt.isPresent());
        var constructor = opt.orElseThrow();
        var insts = assertInstExists(PutFieldInstruction.class, constructor);
        assertTrue("Expect to find a single " + PutFieldInstruction.class.getSimpleName() + " instruction", insts.size() == 1);
        var putField = insts.getFirst();
        assertEquals("PutField should be for field 'b'", "b", putField.getField().getName());
        assertTrue("PutField should be with an integer value", BuiltinType.is(putField.getValue().getType(), BuiltinKind.BOOLEAN));
    }

    @Test
    public void testFieldInitializerExpression() {
        setDescription("Field initializer with simple expression");
        var classUnit = toOllir("FieldInitializerExpression.jmm");
        var opt = classUnit.getMethods().stream().filter(Method::isConstructMethod).findFirst();
        assertTrue("Constructor should be present", opt.isPresent());
        var constructor = opt.orElseThrow();
        var insts = assertInstExists(PutFieldInstruction.class, constructor);
        assertTrue("Expect to find a single " + PutFieldInstruction.class.getSimpleName() + " instruction", insts.size() == 1);
        var putField = insts.getFirst();
        assertEquals("PutField should be for field 'c'", "c", putField.getField().getName());
        assertTrue("PutField should be with an integer value", BuiltinType.is(putField.getValue().getType(), BuiltinKind.INT32));
    }

    @Test
    public void testFieldInitializerMethodInvocation() {
        setDescription("Field initializer using a method invocation (with method defined)");
        var classUnit = toOllir("FieldInitializerMethodInvocation.jmm");
        var opt = classUnit.getMethods().stream().filter(Method::isConstructMethod).findFirst();
        assertTrue("Constructor should be present", opt.isPresent());
        var constructor = opt.orElseThrow();


        var assignOpt = constructor.getInstructions().stream().filter(i -> i instanceof AssignInstruction)
                .map(i -> (AssignInstruction) i).findFirst();

        assertTrue("Assign should be present", assignOpt.isPresent());
        var assign = assignOpt.orElseThrow();

        assertTrue("InvokeVirtual should be present", assign.getRhs() instanceof InvokeVirtualInstruction);
        var invokeVirtual = (InvokeVirtualInstruction) assign.getRhs();
        Element element = invokeVirtual.getMethodName().toElement();
        assertTrue("Method name should be a literal element", element.isLiteral());
        assertEquals("InvokeVirtual should be for method 'init'", "init", ((LiteralElement) element).getLiteral());

        var putFieldOpt = constructor.getInstructions().stream().filter(i -> i instanceof PutFieldInstruction).map(i -> (PutFieldInstruction) i).findFirst();
        assertTrue("PutField should be present", putFieldOpt.isPresent());
        var putField = putFieldOpt.orElseThrow();
        assertEquals("PutField should be for field 'a'", "a", putField.getField().getName());
        assertTrue("PutField should be with an integer value", BuiltinType.is(putField.getValue().getType(), BuiltinKind.INT32));
    }


    @Test
    public void testFieldInitializerNewObject() {
        setDescription("Field initializer using new object should parse");
        var classUnit = toOllir("FieldInitializerNewObject.jmm");
        var opt = classUnit.getMethods().stream().filter(Method::isConstructMethod).findFirst();
        assertTrue("Constructor should be present", opt.isPresent());
        var constructor = opt.orElseThrow();


        var assignOpt = constructor.getInstructions().stream().filter(i -> i instanceof AssignInstruction)
                .map(i -> (AssignInstruction) i).findFirst();

        assertTrue("Assign should be present", assignOpt.isPresent());
        var assign = assignOpt.orElseThrow();

        assertTrue("InvokeVirtual should be present", assign.getRhs() instanceof NewInstruction);
        var newInstruction = (NewInstruction) assign.getRhs();
        assertTrue("New instruction should return an instance of Quicksort",
                newInstruction.getReturnType() instanceof ClassType ct && ct.getName().equals("Quicksort"));


        var putFieldOpt = constructor.getInstructions().stream().filter(i -> i instanceof PutFieldInstruction).map(i -> (PutFieldInstruction) i).findFirst();
        assertTrue("PutField should be present", putFieldOpt.isPresent());
        var putField = putFieldOpt.orElseThrow();
        assertEquals("PutField should be for field 'a'", "a", putField.getField().getName());
        assertTrue("PutField should be with an instance of Quicksort value",
                putField.getValue().getType() instanceof ClassType ct && ct.getName().equals("Quicksort"));
    }

}
