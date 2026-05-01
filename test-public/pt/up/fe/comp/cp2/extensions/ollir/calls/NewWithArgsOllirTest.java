package pt.up.fe.comp.cp2.extensions.ollir.calls;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.LiteralElement;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.CallInstruction;
import org.specs.comp.ollir.inst.InvokeSpecialInstruction;
import org.specs.comp.ollir.inst.NewInstruction;
import org.specs.comp.ollir.inst.utils.InstTypeUtils;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.Type;
import pt.up.fe.comp.test.env.OllirTestEnv;

import java.util.Date;
import java.util.List;

public class NewWithArgsOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/extensions/ollir/calls/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public NewWithArgsOllirTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    public ClassUnit toOllir(String resourceName) {
        var ollirResult = jmmToOllir(resourceName);
        var classUnit = ollirResult.getOllirClass();
        assertNotNull("Ollir class unit should not be null", classUnit);
        assertEquals("Class name not what was expected", resourceName.replace(".jmm", ""), classUnit.getClassName());
        return classUnit;
    }

    private Method toOllir(String resourceName, String methodName) {
        var classUnit = toOllir(resourceName);
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals(methodName)).findFirst();
        assertEquals("Method '" + methodName + "' should exist", true, method.isPresent());
        return method.orElseThrow();
    }

    public Method toOllir(String resourceName, String methodName, String classQualifiedName, List<? extends Object> arguments) {
        var method = toOllir(resourceName, methodName);
        checkNewInstance(method, classQualifiedName, arguments);
        return method;
    }

    public boolean assertType(Type actualType, Object expectedType) {
        if (expectedType instanceof BuiltinKind bk) {
            return BuiltinType.is(actualType, bk);
        } else if (expectedType instanceof String classQualifiedName) {
            return InstTypeUtils.classTypeMatches(actualType, classQualifiedName);
        } else {
            fail("Using wrong type of type comparison. Either use a BuiltinKind or a string with the class qualified name");
            return false; // Unreachable, but required for compilation
        }
    }

    private InvokeSpecialInstruction checkNewInstance(Method method, String classQualifiedName,
                                                      List<? extends Object> arguments) {

        var calls = assertInstExists(CallInstruction.class, method);
        var news = calls.stream().filter(call -> call instanceof NewInstruction).map(NewInstruction.class::cast).toList();
        assertTrue("Method should contain a new instruction", !news.isEmpty());
        var invokeSpecials = calls.stream().filter(call -> call instanceof InvokeSpecialInstruction).map(InvokeSpecialInstruction.class::cast).toList();
        assertTrue("Method should contain an invokespecial", !invokeSpecials.isEmpty());
        var newInst = news.getFirst();
        var simpleName = classQualifiedName.contains(".") ? classQualifiedName.substring(classQualifiedName.lastIndexOf(".") + 1) : classQualifiedName;
        var thisType = List.of(simpleName, classQualifiedName);
        assertTrue("new instance can use name of class as argument",
                newInst.getArguments().isEmpty()
                        ||
                        (newInst.getArguments().getFirst() instanceof Operand op
                                && thisType.contains(op.getName()))
        );
        assertTrue("new instance 'return type' should be the class type",
                InstTypeUtils.classTypeMatches(newInst.getReturnType(), classQualifiedName));

        var invokeSpecial = invokeSpecials.getFirst();
        assertTrue("Method should contain an invokespecial call to init", invokeSpecial.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("<init>"));
        assertTrue("invokespecial first argument should be of the class type",
                InstTypeUtils.classTypeMatches(invokeSpecial.getCaller().getType(), classQualifiedName));

        assertOrder(method, newInst, invokeSpecial);

        assertEquals("Call should have " + arguments.size() + " arguments", arguments.size(), invokeSpecial.getArguments().size());
        for (int i = 0; i < arguments.size(); i++) {
            var arg = invokeSpecial.getArguments().get(i);
            var expectedArgType = arguments.get(i);
            assertTrue("Argument " + (i + 1) + " should be of type '" + expectedArgType + "'", assertType(arg.getType(), expectedArgType));
        }

        return invokeSpecial;
    }


    @Test
    public void testInstantiateWithArgs() {
        var argumentsTypes = List.of(BuiltinKind.INT32, BuiltinKind.INT32, BuiltinKind.INT32);
        toOllir("InstantiateWithArgs.jmm",
                "method",
                Date.class.getName(),
                argumentsTypes);
    }

    @Test
    public void testInstantiateJavaLangWithArgs() {
        var argumentsTypes = List.of(String.class.getName());
        toOllir("InstantiateJavaLangWithArgs.jmm",
                "method",
                String.class.getName(),
                argumentsTypes);
    }

}

