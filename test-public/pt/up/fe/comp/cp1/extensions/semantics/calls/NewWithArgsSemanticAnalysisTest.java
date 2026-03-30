package pt.up.fe.comp.cp1.extensions.semantics.calls;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class NewWithArgsSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/statements/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public NewWithArgsSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void testNewWithTwoArguments() {
        setDescription("Test new instance with two arguments pass the semantic analysis.");
        //Pair only has a constructor, which accepts two int arguments
        //Should fail on new without arguments
        semanticsFromSnippet("""
                package x;
                import examples.Pair;
                class Main {
                    void foo(){
                        Pair p;
                        p = new Pair();
                    }
                }""", true);
        //Should fail with wrong types of arguments
        semanticsFromSnippet("""
                package x;
                import examples.Pair;
                class Main {
                    void foo(){
                        Pair p;
                        p = new Pair(10, true);
                    }
                }""", true);
        semanticsFromSnippet("""
                package x;
                import examples.Pair;
                class Main {
                    void foo(){
                        Pair p;
                        p = new Pair(1, 2);
                    }
                }""", false);
    }
}
