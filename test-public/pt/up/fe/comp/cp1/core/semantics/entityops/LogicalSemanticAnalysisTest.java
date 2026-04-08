package pt.up.fe.comp.cp1.core.semantics.entityops;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class LogicalSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/entityops/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public LogicalSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void logicalOperators() {
        setDescription("Test that all logical operators are only applied over booleans");
        semantics("LogicalOperatorsFail.jmm", true);
        semantics("LogicalOperatorsOk.jmm", false);
    }

}
