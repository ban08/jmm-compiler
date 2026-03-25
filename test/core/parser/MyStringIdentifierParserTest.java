package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyStringIdentifierParserTest extends JmmTestEnv {

    public MyStringIdentifierParserTest() {
        super("", "");
    }

    @Test
    public void stringCanAppearInImportPaths() {
        parseSnippet("""
                package parser.stringid;
                import java.lang.String;
                class Demo {}
                """);
    }

    @Test
    public void stringCanBeUsedAsAnIdentifier() {
        parseSnippet("""
                package parser.stringid;
                class Demo {
                    int String;
                }
                """);
    }
}
