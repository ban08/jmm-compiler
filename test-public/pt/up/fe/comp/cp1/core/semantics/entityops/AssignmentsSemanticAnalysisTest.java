package pt.up.fe.comp.cp1.core.semantics.entityops;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class AssignmentsSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/entityops/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public AssignmentsSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void assignExpressionToVar() {
        setDescription("Test assignment of a complex arithmetic expression to a variable");
        semantics("AssignExpressionToVarFail.jmm", true);
        semantics("AssignExpressionToVarOk.jmm", false);
    }

    @Test
    public void assignMethodReturnVal() {
        setDescription("Test assignment of a method return value to a variable");
        semantics("AssignMethodReturnValFail.jmm", true);
        semantics("AssignMethodReturnValOk.jmm", false);
    }

    @Test
    public void assignExpressionWithMethodCalls() {
        setDescription("Test assignment of a complex arithmetic expression with method calls to a variable");
        semantics("AssignExpressionWithMethodCallsFail.jmm", true);
        semantics("AssignExpressionWithMethodCallsOk.jmm", false);
    }

    @Test
    public void assignThisToOwnType() {
        setDescription("Test assignment of 'this' to obj of the same type");
        semantics("AssignThisToOwnTypeFail.jmm", true);
        semantics("AssignThisToOwnTypeOk.jmm", false);
    }

    @Test
    public void assignThisToParentType() {
        setDescription("Test assignment of 'this' to obj of parent type");
        semantics("AssignThisToParentTypeFail.jmm", true);
        semantics("AssignThisToParentTypeOk.jmm", false);
    }

    @Test
    public void assignObjToParentClass() {
        setDescription("Test assignment of objects to a reference of type of a parent class");
        semantics("AssignObjToParentClassFail.jmm", true);
        semantics("AssignObjToParentClassOk.jmm", false);
    }

    @Test
    public void assignInitToParentClass() {
        setDescription("Test assignment of an init to a reference of type of a parent class");
        semantics("AssignInitToParentClassFail.jmm", true);
        semantics("AssignInitToParentClassOk.jmm", false);
    }
}
