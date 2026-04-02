package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyStringIdentifierParserTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/parser/stringid/";
    private static final String RESOURCES_LOCATION = "test";

    public MyStringIdentifierParserTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void stringCanAppearInImportPaths() {
        parseResource("StringInImportPath.jmm");
    }

    @Test
    public void stringCanBeUsedAsAnIdentifier() {
        parseResource("StringIdentifierField.jmm");
    }

    @Test
    public void lengthCanBeUsedAsAnIdentifier() {
        parseResource("LengthIdentifierLocal.jmm");
    }
}
