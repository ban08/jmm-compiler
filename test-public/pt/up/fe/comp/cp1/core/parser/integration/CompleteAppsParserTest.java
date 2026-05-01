package pt.up.fe.comp.cp1.core.parser.integration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.specs.util.SpecsIo;

public class CompleteAppsParserTest extends JmmTestEnv {


    public CompleteAppsParserTest() {
        super("pt/up/fe/comp/cp1/core/parser/integration/apps/", "test-public");
    }


    @Test
    public void testWhileAndIfSeqScalar() {
        parseResource("WhileAndIfSeqScalar.jmm");
    }

    @Test
    public void testWhileAndIfChainedScalar() {
        parseResource("WhileAndIfChainedScalar.jmm");
    }
}
