package pt.up.fe.comp.cp1.core.semantics.statements;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class IfElseSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/statements/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public IfElseSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void ifElseSimpleBoolean() {
        setDescription("Test that an if/else block uses a simple boolean variable");
        semantics("IfElseSimpleBooleanFail.jmm", true);
        semantics("IfElseSimpleBooleanOk.jmm", false);
    }

    @Test
    public void ifElseExpression() {
        setDescription("Test that an if/else block uses a boolean expression");
        semantics("IfElseExpressionFail.jmm", true);
        semantics("IfElseExpressionOk.jmm", false);
    }
}
