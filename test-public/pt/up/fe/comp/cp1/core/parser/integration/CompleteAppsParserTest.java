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
        super("pt/up/fe/comp/cp1/core/parser/integration/apps/", "test");
    }


    @Test
    public void testScientificApp() {
        parseResource("FindMaximum.jmm");
    }

    @Test
    public void testSortingApp2() {
        parseResource("Lazysort.jmm");
    }

    @Test
    public void testSortingApp1() {
        parseResource("QuickSort.jmm");
    }

    @Test
    public void testGameApp1() {
        parseResource("TicTacToe.jmm");
    }

    @Test
    public void testWhileAndIfSeq() {
        parseResource("WhileAndIfSeq.jmm");
    }

    @Test
    public void testWhileAndIfChained() {
        parseResource("WhileAndIfChained.jmm");
    }
}
