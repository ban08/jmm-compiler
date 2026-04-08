package pt.up.fe.comp.cp1.core.semantics.entityops;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class ArithmeticSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/entityops/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArithmeticSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }


    @Test
    public void arithmeticOperators() {
        setDescription("Test that all arithmetic operators are only applied over integers");
        semantics("ArithmeticOperatorsFail.jmm", true);
        semantics("ArithmeticOperatorsOk.jmm", false);
    }

}
