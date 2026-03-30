package pt.up.fe.comp.cp1.extensions.semantics.arrays;

import org.junit.Test;


public class ArrayInitSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/extensions/semantics/arrays/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArrayInitSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void assignNewToArray() {
        setDescription("Test that 'new type[]' expressions are assigned to arrays");
        semantics("AssignNewToArrayFail.jmm", true);
        semantics("AssignNewToArrayOk.jmm", false);
    }

    @Test
    public void arrayHasNewInit() {
        setDescription("Test that arrays are initialized with a 'new type[]' expression");
        semantics("ArrayHasNewInitFail1.jmm", true);
        semantics("ArrayHasNewInitFail2.jmm", true);
        semantics("ArrayHasNewInitOk.jmm", false);
    }

    @Test
    public void arrayReturn() {
        setDescription("Test returning arrays");
        semantics("ArrayReturnFail.jmm", true);
        semantics("ArrayReturnOk.jmm", false);
    }

    @Test
    public void arraySizeIsIntConstant() {
        setDescription("Test that size is a integer constant in 'new type[size]'");
        semantics("SizeIsIntConstantFail.jmm", true);
        semantics("SizeIsIntConstantOk.jmm", false);
    }

    @Test
    public void arraySizeIsIntVariable() {
        setDescription("Test that size is a integer variable in 'new type[size]'");
        semantics("SizeIsIntVariableFail.jmm", true);
        semantics("SizeIsIntVariableOk.jmm", false);
    }

    @Test
    public void arraySizeIsIntExpression() {
        setDescription("Test that size is an expression that evaluates to an integer in 'new type[size]'");
        semantics("SizeIsIntExpressionFail.jmm", true);
        semantics("SizeIsIntExpressionOk.jmm", false);
    }
}
