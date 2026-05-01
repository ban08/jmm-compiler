package pt.up.fe.comp.cp1.extensions.semantics.calls;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class ImplicitThisSemanticAnalysisTestArrayAndField extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/statements/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ImplicitThisSemanticAnalysisTestArrayAndField() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void testImplicitThisGoingToSuper() {
        setDescription("Test method calls with an implicit this object but pertains to super.");
        semanticsFromSnippet("""
                package x;
                import examples.Quicksort;
                class Implicit extends Quicksort{
                    public void sortAndPrint(int[] L){
                        quicksort(L);
                        printL(L);
                    }
                }
                """, false);
    }
}
