package pt.up.fe.comp.cp2.core.ollir.calls;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.LiteralElement;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.inst.utils.InstTypeUtils;
import pt.up.fe.comp.test.env.OllirTestEnv;

import java.util.Date;
import java.util.List;

public class InstantiationOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/calls/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public InstantiationOllirTest() {
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

    public Method toOllir(String resourceName, String methodName, String classQualifiedName) {
        var method = toOllir(resourceName, methodName);
        checkNewInstance(method, classQualifiedName);
        return method;
    }


    private void checkNewInstance(Method method, String classQualifiedName) {

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
    }


    @Test
    public void testInstantiateSelf() {
        toOllir("InstantiateSelf.jmm", "method", "pt.up.fe.comp.cp2.core.ollir.calls.jmm.InstantiateSelf");
    }

    @Test
    public void testInstantiateImportedClass() {
        toOllir("InstantiateImportedClass.jmm", "method", Date.class.getName());
    }

    @Test
    public void testInstantiateJavaLangClass() {
        toOllir("InstantiateJavaLangClass.jmm", "method", String.class.getName());
    }

    @Test
    public void testInstantiateSelfStoreInSuperType() {
        var method = toOllir("InstantiateSelfStoreInSuperType.jmm", "method", "pt.up.fe.comp.cp2.core.ollir.calls.jmm.InstantiateSelfStoreInSuperType");
        var assigns = assertInstExists(AssignInstruction.class, method);
        var assign = assigns.getLast();//The last assign should be the one storing the new instance in the expected variable


        var lhsType = assign.getDest().getType();
        assertTrue("Assign lhs type should be Object, but was " + lhsType, InstTypeUtils.classTypeMatches(lhsType, Object.class.getName()));
        var rhsType = InstTypeUtils.getType(assign.getRhs());
        assertTrue("Assign rhs type should be the class type", rhsType.isPresent()
                && InstTypeUtils.classTypeMatches(rhsType.orElseThrow(), "pt.up.fe.comp.cp2.core.ollir.calls.jmm.InstantiateSelfStoreInSuperType"));
    }
}

