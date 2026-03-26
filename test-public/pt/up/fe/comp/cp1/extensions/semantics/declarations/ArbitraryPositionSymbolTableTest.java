// java
package pt.up.fe.comp.cp1.extensions.semantics.declarations;

import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.test.env.JmmTestEnv;

import java.util.List;

public class ArbitraryPositionSymbolTableTest extends JmmTestEnv {


    private static final String BASE_PATH = "pt/up/fe/comp/cp1/extensions/semantics/declarations/symboltable/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArbitraryPositionSymbolTableTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }


    @Test
    public void testFieldAfterMethod() {
        setDescription("Test that a field declared after methods are correctly added to the symbol table");
        var semantics = symbolTableFromSnippet("""
                package x;
                class FieldAfterMethod {
                    public void method() {}
                    int field;
                }""", false);
        var fields = semantics.getSymbolTable().getFields();
        super.assertEquals("Number of fields should be ${expected}, but was ${actual}", 1, fields.size());
        var field = fields.getFirst();
        super.assertEquals("Field name should be ${expected}, but was ${actual}", "field", field.name());
        var methods = semantics.getSymbolTable().getMethods("method");
        super.assertEquals("Number of methods should be ${expected}, but was ${actual}", 1, methods.size());
        var method = methods.getFirst();
        super.assertEquals("Method name should be ${expected}, but was ${actual}", "method", method.name());
    }

    @Test
    public void testMethodFieldFieldMethod() {
        setDescription("Test that fields and methods declared in arbitrary order are correctly added to the symbol table");
        var semantics = symbolTableFromSnippet("""
                package x;
                class MethodFieldFieldMethod {
                    public void method1() {}
                    int field1;
                    boolean field2;
                    public void method2() {}
                }
                """, false);
        var fields = semantics.getSymbolTable().getFields();
        super.assertEquals("Number of fields should be ${expected}, but was ${actual}", 2, fields.size());
        var field1 = fields.getFirst();
        var field2 = fields.get(1);
        super.assertEquals("First field name should be ${expected}, but was ${actual}", "field1", field1.name());
        super.assertEquals("Second field name should be ${expected}, but was ${actual}", "field2", field2.name());
        var expectedMethods = List.of("method1", "method2");
        var methods = semantics.getSymbolTable().getMethods().stream().filter(m -> expectedMethods.contains(m.name())).toList();
        super.assertEquals("Number of methods should be ${expected}, but was ${actual}", 2, methods.size());
        var method1 = methods.getFirst();
        var method2 = methods.get(1);
        super.assertEquals("First method name should be ${expected}, but was ${actual}", "method1", method1.name());
        super.assertEquals("Second method name should be ${expected}, but was ${actual}", "method2", method2.name());
    }

    @Test
    public void testMethodMethodFieldField() {
        setDescription("Test that fields declared after methods are correctly added to the symbol table, even if there are multiple methods before them");
        var semantics = symbolTableFromSnippet("""
                package x;
                class MethodMethodFieldField {
                    public void method1() {}
                    public void method2() {}
                    int field1;
                    boolean field2;
                }
                """, false);
        var fields = semantics.getSymbolTable().getFields();
        super.assertEquals("Number of fields should be ${expected}, but was ${actual}", 2, fields.size());
        var field1 = fields.getFirst();
        var field2 = fields.get(1);
        super.assertEquals("First field name should be ${expected}, but was ${actual}", "field1", field1.name());
        super.assertEquals("Second field name should be ${expected}, but was ${actual}", "field2", field2.name());
        var expectedMethods = List.of("method1", "method2");
        var methods = semantics.getSymbolTable().getMethods().stream().filter(m -> expectedMethods.contains(m.name())).toList();
        super.assertEquals("Number of methods should be ${expected}, but was ${actual}", 2, methods.size());
        var method1 = methods.getFirst();
        var method2 = methods.get(1);
        super.assertEquals("First method name should be ${expected}, but was ${actual}", "method1", method1.name());
        super.assertEquals("Second method name should be ${expected}, but was ${actual}", "method2", method2.name());
    }
}
