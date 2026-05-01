package pt.up.fe.comp.cp1.core.semantics.integration;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class CompleteAppsSemanticsTestArrayAndField extends JmmTestEnv {


    public CompleteAppsSemanticsTestArrayAndField() {
        super("pt/up/fe/comp/cp1/core/semantics/integration/apps/", "test-public");
    }

    @Test
    public void testScientificApp() {
        semantics("FindMaximum.jmm", false);
    }

    @Test
    public void testSortingApp2() {
        semantics("Lazysort.jmm", false);
    }

    @Test
    public void testSortingApp1() {
        semantics("QuickSort.jmm", false);
    }

    @Test
    public void testGameApp1() {
        semantics("TicTacToe.jmm", false);
    }


    @Test
    public void testWhileAndIfSeq() {
        semantics("WhileAndIfSeq.jmm", false);
    }

    @Test
    public void testWhileAndIfChained() {
        semantics("WhileAndIfChained.jmm", false);
    }
}
