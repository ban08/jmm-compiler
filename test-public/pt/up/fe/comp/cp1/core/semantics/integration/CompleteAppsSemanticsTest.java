package pt.up.fe.comp.cp1.core.semantics.integration;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class CompleteAppsSemanticsTest extends JmmTestEnv {


    public CompleteAppsSemanticsTest() {
        super("pt/up/fe/comp/cp1/core/semantics/integration/apps/", "test-public");
    }


    @Test
    public void testWhileAndIfSeqScalar() {
        semantics("WhileAndIfSeqScalar.jmm", false);
    }

    @Test
    public void testWhileAndIfChainedScalar() {
        semantics("WhileAndIfChainedScalar.jmm", false);
    }

}
