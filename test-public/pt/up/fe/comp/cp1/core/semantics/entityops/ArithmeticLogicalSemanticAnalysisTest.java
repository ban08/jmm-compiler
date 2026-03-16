package pt.up.fe.comp.cp1.core.semantics.entityops;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class ArithmeticLogicalSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/entityops/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArithmeticLogicalSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void arithmeticOperators() {
        setDescription("Test that all arithmetic operators are only applied over integers");
        semantics("ArithmeticOperatorsFail.jmm", true);
        semantics("ArithmeticOperatorsOk.jmm", false);
    }

//    @Test
//    public void zeroDivision() {
//        setDescription("Test division in the presence of a constant 0");
//        semantics("ZeroDivisionFail.jmm", true);
//        semantics("ZeroDivisionOk.jmm", false);
//    }

    @Test
    public void logicalOperators() {
        setDescription("Test that all logical operators are only applied over booleans");
        semantics("LogicalOperatorsFail.jmm", true);
        semantics("LogicalOperatorsOk.jmm", false);
    }

    @Test
    public void allOperators() {
        setDescription("Test a mix-and-match of arithmetic and logical operators in complex expressions");
        semantics("AllOperatorsFail.jmm", true);
        semantics("AllOperatorsOk.jmm", false);
    }
}
