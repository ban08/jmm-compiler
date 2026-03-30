package pt.up.fe.comp.cp1.extensions.semantics.arrays;

import org.junit.Test;


public class ArrayAccessSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/extensions/semantics/arrays/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArrayAccessSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void arrayAccessOverArray() {
        setDescription("Test that an array access is only done over an array");
        semantics("ArrayAccessOverArrayFail.jmm", true);
        semantics("ArrayAccessOverArrayOk.jmm", false);
    }

    @Test
    public void loadFromArrayConstantIndex() {
        setDescription("Test loading an array element using a constant as the index");
        semantics("ArrayConstantLoadFail.jmm", true);
        semantics("ArrayConstantLoadOk.jmm", false);
    }

    @Test
    public void loadFromArrayVariableIndex() {
        setDescription("Test loading an array element using a variable as the index");
        semantics("ArrayVariableLoadFail.jmm", true);
        semantics("ArrayVariableLoadOk.jmm", false);
    }

    @Test
    public void loadFromArrayExpressionIndex() {
        setDescription("Test loading an array element using an expression as the index");
        semantics("ArrayExpressionLoadFail.jmm", true);
        semantics("ArrayExpressionLoadOk.jmm", false);
    }

    @Test
    public void storeToArrayConstantIndex() {
        setDescription("Test storing an array element using a constant as the index");
        semantics("ArrayConstantStoreFail.jmm", true);
        semantics("ArrayConstantStoreOk.jmm", false);
    }

    @Test
    public void storeToArrayVariableIndex() {
        setDescription("Test storing an array element using a variable as the index");
        semantics("ArrayVariableStoreFail.jmm", true);
        semantics("ArrayVariableStoreOk.jmm", false);
    }

    @Test
    public void storeToArrayExpressionIndex() {
        setDescription("Test storing an array element using an expression as the index");
        semantics("ArrayExpressionStoreFail.jmm", true);
        semantics("ArrayExpressionStoreOk.jmm", false);
    }

    @Test
    public void arrayLoadAssignment() {
        setDescription("Test assigning an array element to a variable with the right data type");
        semantics("ArrayLoadAssignmentFail.jmm", true);
        semantics("ArrayLoadAssignmentOk.jmm", false);
    }

    @Test
    public void arrayStoreAssignment() {
        setDescription("Test assigning a variable to an array element with the right data type");
        semantics("ArrayStoreAssignmentFail.jmm", true);
        semantics("ArrayStoreAssignmentOk.jmm", false);
    }

    @Test
    public void arrayLoadToArrayStoreAssignment() {
        setDescription("Test storing to an array position the value loaded from an array position");
        semantics("ArrayPosToArrayPosAssignmentFail.jmm", true);
        semantics("ArrayPosToArrayPosAssignmentOk.jmm", false);
    }

    @Test
    public void arrayToArrayAssignment() {
        setDescription("Tests assigning an array to another array");
        semantics("ArrayToArrayAssignmentFail1.jmm", true);
        semantics("ArrayToArrayAssignmentFail2.jmm", true);
        semantics("ArrayToArrayAssignmentOk.jmm", false);
    }

    @Test
    public void arrayLengthAccess() {
        setDescription("Tests accessing the length of an array");
        semantics("ArrayLengthAccessFail.jmm", true);
        semantics("ArrayLengthAccessOk.jmm", false);
    }

    @Test
    public void noArraysInArithExpressions() {
        setDescription("Test no arrays (not array accesses!) are used in arithmetic expressions");
        semantics("NoArraysInArithExpressionsFail.jmm", true);
        semantics("NoArraysInArithExpressionsOk.jmm", false);
    }
}
