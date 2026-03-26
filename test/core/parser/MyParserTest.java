// java
package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class MyParserTest extends JmmTestEnv {
    private static final String BASE_PATH = "core/parser/";
    private static final String RESOURCES_LOCATION = "test";


    public MyParserTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void testHelloWorld() {
        parseResource("HelloWorld.jmm");
    }
}
