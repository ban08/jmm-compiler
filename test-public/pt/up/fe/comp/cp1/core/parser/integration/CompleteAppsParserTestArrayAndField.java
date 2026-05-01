package pt.up.fe.comp.cp1.core.parser.integration;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class CompleteAppsParserTestArrayAndField extends JmmTestEnv {


    public CompleteAppsParserTestArrayAndField() {
        super("pt/up/fe/comp/cp1/core/parser/integration/apps/", "test-public");
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
