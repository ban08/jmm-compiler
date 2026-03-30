package pt.up.fe.comp.cp1.extensions.semantics.arrays;

import org.junit.Test;


public class ArrayInitializerListsSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/extensions/semantics/arrays/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArrayInitializerListsSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void initListInArrayType() {
        setDescription("Test that an init lists are applied only for arrays");
        semantics("InitListInArrayTypeFail.jmm", true);
        semantics("InitListInArrayTypeOk.jmm", false);
    }

    @Test
    public void initListAllElementsType() {
        setDescription("Test when all elements of a list are not of the correct type");
        semantics("InitListAllElementsFail.jmm", true);
        semantics("InitListAllElementsOk.jmm", false);
    }

    @Test
    public void initListSomeElementsType() {
        setDescription("Test when some elements of a list are not of the correct type");
        semantics("InitListSomeElementsFail.jmm", true);
        semantics("InitListSomeElementsOk.jmm", false);
    }

    @Test
    public void initListIsEmpty() {
        setDescription("Test when an init list is empty");
        semantics("InitListIsEmptyFail.jmm", true);
        semantics("InitListIsEmptyOk.jmm", false);
    }
}
