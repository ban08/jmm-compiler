package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyClassIdentifierValueSemanticsTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/classidentifiers/";
    private static final String RESOURCES_LOCATION = "test";

    public MyClassIdentifierValueSemanticsTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void bareClassIdentifiersCannotBeUsedAsValues() {
        semantics("ReturnClassIdentifierFail.jmm", true);
        semantics("AssignClassIdentifierFail.jmm", true);
        semantics("FieldInitializerClassIdentifierFail.jmm", true);
    }
}
