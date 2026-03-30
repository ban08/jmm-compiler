package pt.up.fe.comp.cp1.extensions.semantics;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class LocalStaticMethodSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/statements/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public LocalStaticMethodSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void testCallLocalStaticMethod() {
        setDescription("Test calling a local static method");
        semanticsFromSnippet("""
                package x;
                class N{
                    public static int zero(){
                        return 0;
                    }
                    public static void main(String[] args) {
                        int x;
                        x = N.zero();
                    }
                }""", false);
    }

    @Test
    public void testCallLocalStaticMethodFromThis() {
        setDescription("Test calling a local static method");
        semanticsFromSnippet("""
                package x;
                class N{
                    public static int zero(){
                        return 0;
                    }
                    public void bar() {
                        int x;
                        x = this.zero();
                    }
                }""", false);
    }
}
