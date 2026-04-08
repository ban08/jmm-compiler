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


//    public void compileBasic(ClassUnit classUnit) {
//        // Test name of the class and super
//        assertEquals("Class name not what was expected", "CompileBasic", classUnit.getClassName());
//        assertEquals("Super class name not what was expected", "Quicksort", classUnit.getSuperClass());
//
//        // Test method 1
//        Method method1 = classUnit.getMethods().stream()
//                .filter(method -> method.getMethodName().equals("method1"))
//                .findFirst()
//                .orElse(null);
//
//        assertNotNull("Could not find method1", method1);
//
//        var retInst1 = method1.getInstructions().stream()
//                .filter(inst -> inst instanceof ReturnInstruction)
//                .findFirst();
//        assertTrue("Could not find a return instruction in method1", retInst1.isPresent());
//
//        // Test method 2
//        Method method2 = classUnit.getMethods().stream()
//                .filter(method -> method.getMethodName().equals("method2"))
//                .findFirst()
//                .orElse(null);
//
//        assertNotNull("Could not find method2'", method2);
//
//        var retInst2 = method2.getInstructions().stream()
//                .filter(inst -> inst instanceof ReturnInstruction)
//                .findFirst();
//        assertTrue("Could not find a return instruction in method2", retInst2.isPresent());
//    }
//
//    public void compileBasicWithFields(OllirResult ollirResult) {
//
//        ClassUnit classUnit = ollirResult.getOllirClass();
//
//        // Test name of the class and super
//        assertEquals("Class name not what was expected", "CompileBasic", classUnit.getClassName());
//        assertEquals("Super class name not what was expected", "Quicksort", classUnit.getSuperClass());
//
//        // Test fields
//        assertEquals("Class should have two fields", 2, classUnit.getNumFields());
//        var fieldNames = new HashSet<>(Arrays.asList("intField", "boolField"));
//
//        //assertThat(fieldNames, hasItem(classUnit.getField(1).getFieldName()));
//        assertThat("Class should contain field boolField", fieldNames, hasItem(classUnit.getField(1).getFieldName()));
//        //assertTrue("Class should contain field intField", classUnit.getFields().stream().anyMatch(f -> f.getFieldName().equals("intField")));
//
//        // Test method 1
//        Method method1 = getMethod(ollirResult, "method1");
//        assertNotNull("Could not find method1", method1);
//
//        var method1GetField = getInstructions(GetFieldInstruction.class, method1);
//        assertTrue("Expected 1 getfield instruction in method1, found " + method1GetField.size(), method1GetField.size() == 1);
//
//
//        // Test method 2
//        var method2 = getMethod(ollirResult, "method2");
//        assertNotNull("Could not find method2'", method2);
//
//        var method2GetField = getInstructions(GetFieldInstruction.class, method2);
//        assertTrue("Expected 0 getfield instruction in method2, found " + method2GetField.size(), method2GetField.isEmpty());
//
//        var method2PutField = getInstructions(PutFieldInstruction.class, method2);
//        assertTrue("Expected 0 putfield instruction in method2, found " + method2PutField.size(), method2PutField.isEmpty());
//
//        // Test method 3
//        var method3 = getMethod(ollirResult, "method3");
//        assertNotNull("Could not find method3'", method3);
//
//        var method3PutField = getInstructions(PutFieldInstruction.class, method3);
//        assertTrue("Expected 1 putfield instruction in method3, found " + method3PutField.size(), method3PutField.size() == 1);
//    }
//
//    public void compileArithmetic(ClassUnit classUnit) {
//        // Test name of the class
//        assertEquals("Class name not what was expected", "CompileArithmetic", classUnit.getClassName());
//
//        // Test foo
//        var methodName = "foo";
//        Method methodFoo = classUnit.getMethods().stream()
//                .filter(method -> method.getMethodName().equals(methodName))
//                .findFirst()
//                .orElse(null);
//
//        assertNotNull("Could not find method " + methodName, methodFoo);
//
//        var binOpInst = methodFoo.getInstructions().stream()
//                .filter(inst -> inst instanceof AssignInstruction)
//                .map(instr -> (AssignInstruction) instr)
//                .filter(assign -> assign.getRhs() instanceof BinaryOpInstruction)
//                .findFirst();
//
//        assertTrue("Could not find a binary op instruction in method " + methodName, binOpInst.isPresent());
//
//        var retInst = methodFoo.getInstructions().stream()
//                .filter(inst -> inst instanceof ReturnInstruction)
//                .findFirst();
//        assertTrue("Could not find a return instruction in method " + methodName, retInst.isPresent());
//    }
//
//    public void compileMethodInvocation(ClassUnit classUnit) {
//        // Test name of the class
//        assertEquals("Class name not what was expected", "CompileMethodInvocation", classUnit.getClassName());
//
//        // Test foo
//        var methodName = "foo";
//        Method methodFoo = classUnit.getMethods().stream()
//                .filter(method -> method.getMethodName().equals(methodName))
//                .findFirst()
//                .orElse(null);
//
//        assertNotNull("Could not find method " + methodName, methodFoo);
//
//        var callInst = methodFoo.getInstructions().stream()
//                .filter(inst -> inst instanceof CallInstruction)
//                .map(CallInstruction.class::cast)
//                .findFirst();
//        assertTrue("Could not find a call instruction in method " + methodName, callInst.isPresent());
//
//        assertEquals("Invocation type not what was expected", InvokeStaticInstruction.class,
//                callInst.get().getClass());
//    }
//
//    public void compileAssignment(ClassUnit classUnit) {
//        // Test name of the class
//        assertEquals("Class name not what was expected", "CompileAssignment", classUnit.getClassName());
//
//        // Test foo
//        var methodName = "foo";
//        Method methodFoo = classUnit.getMethods().stream()
//                .filter(method -> method.getMethodName().equals(methodName))
//                .findFirst()
//                .orElse(null);
//
//        assertNotNull("Could not find method " + methodName, methodFoo);
//
//        var assignInst = methodFoo.getInstructions().stream()
//                .filter(inst -> inst instanceof AssignInstruction)
//                .map(AssignInstruction.class::cast)
//                .findFirst();
//        assertTrue("Could not find an assign instruction in method " + methodName, assignInst.isPresent());
//
//        assertEquals("Assignment does not have the expected type", BuiltinKind.INT32, toBuiltinKind(assignInst.get().getTypeOfAssign()));
//    }
//
//
//    @Test
//    public void basicClass() {
//        var result = jmmToOllir("basic/BasicClass.jmm");
//
//        compileBasic(result.getOllirClass());
//    }
//
//    @Test
//    public void basicClassWithFields() {
//        var result = jmmToOllir("basic/BasicClassWithFields.jmm");
////        System.out.println(result.getOllirCode());
//
//        compileBasicWithFields(result);
//    }
//
//    @Test
//    public void basicAssignment() {
//        var result = jmmToOllir("basic/BasicAssignment.jmm");
//
//        compileAssignment(result.getOllirClass());
//    }
//
//    @Test
//    public void basicMethodInvocation() {
//        var result = jmmToOllir("basic/BasicMethodInvocation.jmm");
//
//        compileMethodInvocation(result.getOllirClass());
//    }
//
//
//    /*checks if method declaration is correct (array)*/
//    @Test
//    public void basicMethodDeclarationArray() {
//        var result = jmmToOllir("basic/BasicMethodsArray.jmm");
//
//        var method = getMethod(result, "func4");
//
//        //assertEquals("Method return type", "int[]", toString(method.getReturnType()), result);
//        assertTrue("Method return type should be array of int",
//                method.getReturnType() instanceof ArrayType at
//                        && at.getElementType() instanceof BuiltinType bt
//                        && bt.getKind() == BuiltinKind.INT32);
//
////        assertEquals("Method return type", "int[]", toString());
//    }
//
//    @Test
//    public void arithmeticSimpleAdd() {
//        var ollirResult = jmmToOllir("arithmetic/Arithmetic_add.jmm");
//
//        compileArithmetic(ollirResult.getOllirClass());
//    }
//
//    @Test
//    public void arithmeticSimpleAnd() {
//        var ollirResult = jmmToOllir("arithmetic/Arithmetic_and.jmm");
//        var method = getMethod(ollirResult, "main");
//        var numBranches = getInstructions(CondBranchInstruction.class, method).size();
//
//        assertTrue("Expected at least 2 branches, found " + numBranches, numBranches >= 2);
//    }
//
//    @Test
//    public void arithmeticSimpleLess() {
//        var ollirResult = jmmToOllir("arithmetic/Arithmetic_less.jmm");
//
//        var method = getMethod(ollirResult, "main");
//
//        assertHasOperation(OperationType.LTH, method);
//
//    }
//
//    @Test
//    public void controlFlowIfSimpleSingleGoTo() {
//
//        var result = jmmToOllir("control_flow/SimpleIfElseStat.jmm");
//
//        var method = getMethod(result, "func");
//
//        var branches = assertInstExists(CondBranchInstruction.class, method);
//        assertEquals("Number of branches", 1, branches.size());
//
//        var gotos = assertInstExists(GotoInstruction.class, method);
//        assertTrue("Has at least 1 goto", !gotos.isEmpty());
//    }
//
//    @Test
//    public void controlFlowIfSwitch() {
//
//        var result = jmmToOllir("control_flow/SwitchStat.jmm");
//
//        var method = getMethod(result, "func");
//
//        var branches = assertInstExists(CondBranchInstruction.class, method);
//        assertEquals("Number of branches", 6, branches.size());
//
//        var gotos = assertInstExists(GotoInstruction.class, method);
//        assertTrue("Has at least 6 gotos", gotos.size() >= 6);
//    }
//
//    @Test
//    public void controlFlowWhileSimple() {
//
//        var result = jmmToOllir("control_flow/SimpleWhileStat.jmm");
//
//        var method = getMethod(result, "func");
//
//        var branches = assertInstExists(CondBranchInstruction.class, method);
//
//        assertTrue("Number of branches between 1 and 2", !branches.isEmpty() && branches.size() < 3);
//    }
//
//
//    /*checks if an array is correctly initialized*/
//    @Test
//    public void arraysInitArray() {
//        var result = jmmToOllir("arrays/ArrayInit.jmm");
//
//        var method = getMethod(result, "main");
//
//        var calls = assertInstExists(CallInstruction.class, method);
//
//        assertEquals("Number of calls", 3, calls.size());
//
//        // Get new
//        var newCalls = calls.stream().filter(call -> call instanceof NewInstruction)
//                .toList();
//
//        assertEquals("Number of 'new' calls", 1, newCalls.size());
//
//        // Get length
//        var lengthCalls = calls.stream().filter(call -> call instanceof ArrayLengthInstruction)
//                .toList();
//
//        assertEquals("Number of 'arraylenght' calls", 1, lengthCalls.size());
//    }
//
//    /*checks if the access to the elements of array is correct*/
//    @Test
//    public void arraysAccessArray() {
//        var result = jmmToOllir("arrays/ArrayAccess.jmm");
//
//        var method = getMethod(result, "foo");
//
//        var assigns = assertInstExists(AssignInstruction.class, method);
//        var numArrayStores = assigns.stream().filter(assign -> assign.getDest() instanceof ArrayOperand).count();
//        assertEquals("Number of array stores", 5L, numArrayStores);
//
//        var numArrayReads = assigns.stream()
//                .flatMap(assign -> getElements(assign.getRhs()).stream())
//                .filter(element -> element instanceof ArrayOperand).count();
//        assertEquals("Number of array reads", 5L, numArrayReads);
//    }
//
//    /*checks multiple expressions as indexes to access the elements of an array*/
//    @Test
//    public void arraysLoadComplexArrayAccess() {
//        // Just parse
//        var result = jmmToOllir("arrays/ComplexArrayAccess.jmm");
//
//        var method = getMethod(result, "main");
//
//        var assigns = assertInstExists(AssignInstruction.class, method);
//        var numArrayStores = assigns.stream().filter(assign -> assign.getDest() instanceof ArrayOperand).count();
//        assertEquals("Number of array stores", 5L, numArrayStores);
//
//        var numArrayReads = assigns.stream()
//                .flatMap(assign -> getElements(assign.getRhs()).stream())
//                .filter(element -> element instanceof ArrayOperand).count();
//        assertEquals("Number of array reads", 6L, numArrayReads);
//    }

}
