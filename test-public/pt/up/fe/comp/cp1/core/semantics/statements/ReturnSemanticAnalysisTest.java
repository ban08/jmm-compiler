package pt.up.fe.comp.cp1.core.semantics.statements;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class ReturnSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/statements/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ReturnSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void returnStatementExists() {
        setDescription("Test that a return statement exists");
        semantics("ReturnExistsFail.jmm", true);
        semantics("ReturnExistsOk.jmm", false);
    }

    @Test
    public void returnStatementEmpty() {
        setDescription("Test an empty return statement in a void method");
        semantics("ReturnEmptyOk.jmm", false);
    }

    @Test
    public void returnStatementDoesNotExist() {
        setDescription("Test a void method without return");
        semantics("ReturnNotExistOk.jmm", false);
    }

    @Test
    public void returnStatementNoExpression() {
        setDescription("Test a return statement with no expression (main() edge case)");
        semantics("ReturnNoExprFail.jmm", true);
        semantics("ReturnNoExprOk.jmm", false);
    }

    @Test
    public void returnStatementSimple() {
        setDescription("Testt that a return statement returns the correct type");
        semantics("ReturnSimpleFail.jmm", true);
        semantics("ReturnSimpleOk.jmm", false);
    }

    @Test
    public void returnStatementExpression() {
        setDescription("Test that a return statement returns the result of an expression with the right type");
        semantics("ReturnExpressionFail.jmm", true);
        semantics("ReturnExpressionOk.jmm", false);
    }

    @Test
    public void returnStatementComplexExpression() {
        setDescription("Test that a return statement returns the result of a complex expression with the right type");
        semantics("ReturnExpressionComplexFail.jmm", true);
        semantics("ReturnExpressionComplexOk.jmm", false);
    }
}
