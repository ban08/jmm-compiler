// java
package pt.up.fe.comp.cp1.extensions.semantics.declarations;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class ArbitraryPositionSemanticsTest extends JmmTestEnv {


    private static final String BASE_PATH = "pt/up/fe/comp/cp1/extensions/semantics/declarations/symboltable/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArbitraryPositionSemanticsTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }


    @Test
    public void testFieldAfterMethod() {
        setDescription("Test that a field declared after methods are correctly accessed");
        symbolTableFromSnippet("""
                package x;
                class FieldAfterMethod {
                    void method() {
                        int a;
                        a = field;
                    }
                    int field;
                }""", false);
    }

    @Test
    public void testMethodFieldFieldMethod() {
        setDescription("Test that fields and methods declared in arbitrary order are correctly accessed");
        symbolTableFromSnippet("""
                package x;
                class MethodFieldFieldMethod {
                     boolean method1() {
                         return field2;
                     }
                
                     int field1;
                     boolean field2;
                
                     int method2() {
                         return field1;
                     }
                }""", false);
    }

}
