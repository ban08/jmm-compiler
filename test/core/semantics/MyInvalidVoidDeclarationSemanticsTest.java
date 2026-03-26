package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyInvalidVoidDeclarationSemanticsTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/voiddecl/";
    private static final String RESOURCES_LOCATION = "test";

    public MyInvalidVoidDeclarationSemanticsTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void fieldCannotHaveVoidType() {
        semantics("FieldCannotHaveVoidType.jmm", true);
    }

    @Test
    public void localCannotHaveVoidType() {
        semantics("LocalCannotHaveVoidType.jmm", true);
    }

    @Test
    public void parameterCannotHaveVoidType() {
        semantics("ParameterCannotHaveVoidType.jmm", true);
    }

    @Test
    public void voidReturnTypeRemainsAllowed() {
        semantics("VoidReturnTypeAllowed.jmm", false);
    }
}
