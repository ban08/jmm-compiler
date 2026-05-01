package pt.up.fe.comp.cp2.core.ollir.integration;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.ReturnInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class CompleteAppsOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/integration/apps/";
    private static final String RESOURCES_LOCATION = "test-public";

    public CompleteAppsOllirTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    public ClassUnit toOllir(String resourceName) {
        var ollirResult = jmmToOllir(resourceName);
        var classUnit = ollirResult.getOllirClass();
        assertNotNull("Ollir class unit should not be null", classUnit);
        assertEquals("Class name not what was expected", resourceName.replace(".jmm", ""), classUnit.getClassName());
        return classUnit;
    }

    @Test
    public void testFindMaximum() {
        toOllir("FindMaximum.jmm");
    }

    @Test
    public void testHelloWorld() {
        toOllir("HelloWorld.jmm");
    }

    @Test
    public void testLazysort() {
        toOllir("Lazysort.jmm");
    }

    @Test
    public void testLife() {
        toOllir("Life.jmm");
    }

    @Test
    public void testMonteCarloPi() {
        toOllir("MonteCarloPi.jmm");
    }

    @Test
    public void testQuickSort() {
        toOllir("QuickSort.jmm");
    }

    @Test
    public void testSimple() {
        toOllir("Simple.jmm");
    }

    @Test
    public void testTicTacToe() {
        toOllir("TicTacToe.jmm");
    }

    @Test
    public void testTuring() {
        toOllir("Turing.jmm");
    }

    @Test
    public void testWhileAndIfChained() {
        toOllir("WhileAndIfChained.jmm");
    }

    @Test
    public void testWhileAndIfSeq() {
        toOllir("WhileAndIfSeq.jmm");
    }

}

