package pt.up.fe.comp.cp1.core.semantics.declarations;

import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.Visibility;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;

import java.util.List;


/**
 * Test variable lookup.
 */
public class MethodDeclSymbolTableTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/declarations/symboltable/";
    private static final String RESOURCES_LOCATION = "test-public";

    public MethodDeclSymbolTableTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }


    @Test
    public void testPublicMethod() {
        var semantics = symbolTableFromSnippet("""
                package x;
                class A{ public void foo() {} }""", false);
        var st = semantics.getSymbolTable();
        var methods = st.getMethods("foo");
        super.assertEquals("Number of methods should be 1", 1, methods.size());
        var method = methods.getFirst();
        super.assertEquals("Method should be public", Visibility.PUBLIC, method.visibility());
    }

    @Test
    public void testPackageProtectedMethod() {
        var semantics = symbolTableFromSnippet("""
                package x;
                class A{ void foo() {} }""", false);
        var st = semantics.getSymbolTable();
        var methods = st.getMethods("foo");
        super.assertEquals("Number of methods should be 1", 1, methods.size());
        var method = methods.getFirst();
        super.assertEquals("Method should be package protected", Visibility.PACKAGE_PROTECTED, method.visibility());
    }

    @Test
    public void Methods() {
        var semantics = symbolTable("MethodsAndFields.jmm", false);
        var st = semantics.getSymbolTable();
        var methods = st.getMethods();
        super.assertTrue("Expected to have at least 4 methods.", methods.size() >= 4);
        var expectedMethods = List.of("main", "getField1", "getField2", "getField3");
        methods = methods.stream().filter(m -> expectedMethods.contains(m.name())).toList();
        var checkInt = 0;
        var checkBool = 0;
        var checkObj = 0;
        var checkVoid = 0;
        var classObj = JmmClassType.ofInstance(qualifiedNameFor("MethodsAndFields"), false);
        for (var m : methods) {
            var ret = m.returnType();//st.getReturnType(m);
            var numParameters = m.parameters().size();//st.getParameters(m).size();

            super.assertTrue("Method " + m.name() + " has unexpected return type " + ret,
                    ret.equals(classObj) || ret.equals(JmmPrimitiveType.BOOLEAN) || ret.equals(JmmPrimitiveType.INT) || ret.equals(JmmPrimitiveType.VOID) || ret instanceof JmmArrayType);
            var message = "Method " + m.name() + " should have ${expected} parameters, but has ${actual}.";
            if (ret.equals(classObj)) {
                checkObj++;
                super.assertEquals(message, 3, numParameters);
            } else if (ret.equals(JmmPrimitiveType.BOOLEAN)) {
                checkBool++;
                super.assertEquals(message, 0, numParameters);
            } else if (ret.equals(JmmPrimitiveType.INT)) {
                checkInt++;
                super.assertEquals(message, 0, numParameters);
            } else if (ret == JmmPrimitiveType.VOID) {
                checkVoid++;
                super.assertEquals(message, 1, numParameters); //main method
            }
        }

        super.assertEquals("Method with return type int", 1, checkInt);
        super.assertEquals("Method with return type boolean", 1, checkBool);
        super.assertEquals("Method with return type object", 1, checkObj);
        super.assertEquals("Method with return type void", 1, checkVoid);

    }

    @Test
    public void MethodReturnsInt() {
        var semantics = symbolTable("MethodsAndFields.jmm", false);
        var st = semantics.getSymbolTable();
        var getField1Opt = st.getMethod(new Signature("getField1", List.of()));
        super.assertTrue("getField1 method should exist", getField1Opt.isPresent());
        var getFieldM = getField1Opt.orElseThrow();
        super.assertEquals("Return of getField1 should be int", JmmPrimitiveType.INT, getFieldM.returnType());
    }

    @Test
    public void Parameters() {
        var semantics = symbolTable("Parameters.jmm", false);
        var st = semantics.getSymbolTable();
        var methods = st.getMethods("all");
        super.assertEquals("Number of methods should be 1", 1, methods.size());
        var method = methods.getFirst();
        var signature = method.signature();
        super.assertEquals("Parameters length should be ${expected}", 3, signature.parameters().size());

        List<JmmType> expectedParamTypes = List.of(
                JmmPrimitiveType.INT,
                JmmPrimitiveType.BOOLEAN,
                JmmClassType.ofInstance(qualifiedNameFor("Parameters"), false)
        );

        for (int i = 0; i < expectedParamTypes.size(); i++) {
            var expectedType = expectedParamTypes.get(i);
            var actualType = signature.parameters().get(i);
            super.assertEquals("Parameter " + i + " type should be ${expected}", expectedType, actualType);
        }

    }

    @Test
    public void LocalVars() {
        var semantics = symbolTable("LocalVars.jmm", false);
        var st = semantics.getSymbolTable();
        var methods = st.getMethods("sum");
        super.assertEquals("Should have one sum method", 1, methods.size());
        var sum = methods.getFirst();
        super.assertTrue("sum should have local variable 'local'", sum.getLocalVariable("local").isPresent());
    }

    @Test
    public void LocalVarWithSameNameAsField() {
        var semantics = symbolTable("LocalVarWithSameNameAsField.jmm", false);
        var st = semantics.getSymbolTable();
        super.assertTrue("Field a exists", st.getField("a").isPresent());
        var methods = st.getMethods("foo");
        super.assertEquals("Should have one foo method", 1, methods.size());
        var foo = methods.getFirst();
        super.assertTrue("Local variable a exists", foo.getLocalVariable("a").isPresent());
    }

}
