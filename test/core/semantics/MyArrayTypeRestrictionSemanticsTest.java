package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyArrayTypeRestrictionSemanticsTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/arrayspec/";
    private static final String RESOURCES_LOCATION = "test";

    public MyArrayTypeRestrictionSemanticsTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void onlyIntArraysAndMainStringArgsAreAccepted() {
        semantics("StringArrayMainParamOk.jmm", false);
        semantics("CurrentClassNamedStringMainParamFail.jmm", true);
        semantics("StringArrayMainExtraParamFail.jmm", true);
        semantics("StringArrayMainSecondParamFail.jmm", true);
        semantics("StringArrayFieldFail.jmm", true);
        semantics("StringArrayParamFail.jmm", true);
        semantics("BooleanArrayLocalFail.jmm", true);
        semantics("ImportedObjectArrayFieldFail.jmm", true);
    }
}
