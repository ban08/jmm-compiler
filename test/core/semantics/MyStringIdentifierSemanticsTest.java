package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyStringIdentifierSemanticsTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/semantics/stringid/";
    private static final String RESOURCES_LOCATION = "test";

    public MyStringIdentifierSemanticsTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void explicitStringImportsRemainValid() {
        semantics("ExplicitStringImportOk.jmm", false);
    }

    @Test
    public void lengthCanBeUsedAsALocalIdentifier() {
        semantics("LengthIdentifierLocalOk.jmm", false);
    }
}
