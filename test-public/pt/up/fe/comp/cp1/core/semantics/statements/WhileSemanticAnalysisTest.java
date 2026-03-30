package pt.up.fe.comp.cp1.core.semantics.statements;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class WhileSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/statements/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public WhileSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void whileSimpleBoolean() {
        setDescription("Test that a while-loop uses a simple boolean variable");
        semantics("WhileSimpleBooleanFail.jmm", true);
        semantics("WhileSimpleBooleanOk.jmm", false);
    }

    @Test
    public void whileExpression() {
        setDescription("Test that a while-loop uses a boolean expression");
        semantics("WhileExpressionFail.jmm", true);
        semantics("WhileExpressionOk.jmm", false);
    }
}
