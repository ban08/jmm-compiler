package pt.up.fe.comp.cp1.extensions.semantics.arrays;

import org.junit.Test;


public class MultidimensionalArraySemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/extensions/semantics/arrays/semanticanalysis/multidim/";
    private static final String RESOURCES_LOCATION = "test-public";

    public MultidimensionalArraySemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    // tests similar to 1-D core arrays, repurposed and altered for N-D arrays
    @Test
    public void assignNewToMultiArray() {
        setDescription("Test that 'new type[][]...' expressions are assigned to N-D arrays");
        semantics("AssignNewToMultiArrayFail.jmm", true);
        semantics("AssignNewToMultiArrayOk.jmm", false);
    }

    @Test
    public void multiArrayHasNewInit() {
        setDescription("Test that N-D arrays are initialized with a 'new type[][]...' expression");
        semantics("MultiArrayHasNewInitFail1.jmm", true);
        semantics("MultiArrayHasNewInitFail2.jmm", true);
        semantics("MultiArrayHasNewInitOk.jmm", false);
    }

    @Test
    public void multiArrayReturn() {
        setDescription("Test returning an N-D array");
        semantics("MultiArrayReturnFail.jmm", true);
        semantics("MultiArrayReturnOk.jmm", false);
    }

    @Test
    public void arraySizeIsIntConstant() {
        setDescription("Test that size is a integer constant in 'new type[size][size]...'");
        semantics("MultiSizeIsIntConstantFail.jmm", true);
        semantics("MultiSizeIsIntConstantOk.jmm", false);
    }

    @Test
    public void arraySizeIsIntVariable() {
        setDescription("Test that size is a integer variable in 'new type[size][size]...'");
        semantics("MultiSizeIsIntVariableFail.jmm", true);
        semantics("MultiSizeIsIntVariableOk.jmm", false);
    }

    @Test
    public void arraySizeIsIntExpression() {
        setDescription("Test that size is an expression that evaluates to an integer in 'new type[size][size]...'");
        semantics("MultiSizeIsIntExpressionFail.jmm", true);
        semantics("MultiSizeIsIntExpressionOk.jmm", false);
    }

    // new tests
    @Test
    public void multiArrayHasPartialSize() {
        setDescription("Test partial array sizes (only the first dimension is required)");
        semantics("MultiArrayPartialSizeFail1.jmm", true);
        semantics("MultiArrayPartialSizeFail2.jmm", true);
        semantics("MultiArrayPartialSizeOk.jmm", false);
    }

    // new tests
    @Test
    public void multiArrayNoSize() {
        setDescription("Test partial array sizes (only the first dimension is required)");
        semantics("MultiArrayNoSizeFail.jmm", true);
        semantics("MultiArrayPartialSizeOk.jmm", false);
    }

    @Test
    public void multiArrayToMultiArrayAssignmentType() {
        setDescription("Test that assignments from one N-D array to another have the same base datatype");
        semantics("MultiArrayAssignmentTypeFail.jmm", true);
        semantics("MultiArrayAssignmentTypeOk.jmm", false);
    }

    @Test
    public void multiArrayToMultiArrayAssignmentDimensions() {
        setDescription("Test that assignments from one N-D array to another have the same dimensions");
        semantics("MultiArrayAssignmentDimensionsFail.jmm", true);
        semantics("MultiArrayAssignmentDimensionsOk.jmm", false);
    }

    @Test
    public void multiArrayAssignBaseType() {
        setDescription("Test assigning a base type position, e.g. int foo; foo = bar[1][2][3] on a 3D bar array");
        semantics("MultiArrayBaseAssignFail.jmm", true);
        semantics("MultiArrayBaseAssignOk.jmm", false);
    }

    @Test
    public void multiArrayAssignSubArrayType() {
        setDescription("Test assigning a sub array of an array, e.g. int[][] foo; foo = bar[1][2] on a 3D bar array");
        semantics("MultiArraySubArrayAssignFail.jmm", true);
        semantics("MultiArraySubArrayAssignOk.jmm", false);
    }
}
