package pt.up.fe.comp.cp2.core.ollir.calls;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.LiteralElement;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.inst.CallInstruction;
import org.specs.comp.ollir.inst.InvokeSpecialInstruction;
import org.specs.comp.ollir.inst.NewInstruction;
import org.specs.comp.ollir.inst.PutFieldInstruction;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.test.env.OllirTestEnv;

import java.util.List;

public class InstantiationFieldOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/calls/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public InstantiationFieldOllirTest() {
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
    public void testInstantiateAndStoreInField() {
        var classUnit = toOllir("InstantiateAndStoreInField.jmm");
        var methodO = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst();
        assertEquals("Method 'method' should exist", true, methodO.isPresent());
        var method = methodO.orElseThrow();
        var calls = assertInstExists(CallInstruction.class, method);
        var news = calls.stream().filter(call -> call instanceof NewInstruction).map(NewInstruction.class::cast).toList();
        assertTrue("Method should contain a new instruction", !news.isEmpty());
        var invokeSpecials = calls.stream().filter(call -> call instanceof InvokeSpecialInstruction).map(InvokeSpecialInstruction.class::cast).toList();
        assertTrue("Method should contain an invokespecial", !invokeSpecials.isEmpty());
        var newInst = news.getFirst();
        var qualifiedName = "pt.up.fe.comp.cp2.core.ollir.calls.jmm.InstantiateAndStoreInField";
        var simpleName = qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
        var thisType = List.of(simpleName, qualifiedName);
        assertTrue("new instance can use name of class as argument",
                newInst.getArguments().isEmpty()
                        ||
                        (newInst.getArguments().getFirst() instanceof Operand op
                                && thisType.contains(op.getName()))
        );
        assertTrue("new instance 'return type' should be the class type",
                newInst.getReturnType() instanceof ClassType ct
                        && thisType.contains(ct.getName()));

        var invokeSpecial = invokeSpecials.getFirst();
        assertTrue("Method should contain an invokespecial call to init", invokeSpecial.getMethodName() instanceof LiteralElement le && le.getLiteral().equals("<init>"));
        assertTrue("invokespecial first argument should be of the class type",
                invokeSpecial.getCaller().getType() instanceof ClassType ct
                        && thisType.contains(ct.getName()));


        var putFields = assertInstExists(PutFieldInstruction.class, method);
        assertTrue("Method should contain a putfield instruction", !putFields.isEmpty());
        var putField = putFields.getFirst();
        assertTrue("field in putfield should be of the class type",
                putField.getField().getType() instanceof ClassType ct
                        && thisType.contains(ct.getName()));
        assertTrue("putfield value should be of the class type",
                putField.getValue().getType() instanceof ClassType ct
                        && thisType.contains(ct.getName()));

        assertOrder(method, List.of(newInst, invokeSpecial, putField));
    }

}

