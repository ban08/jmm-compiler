package pt.up.fe.comp.cp2.extensions.ollir.optimizations;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.test.env.OllirTestEnv;
import pt.up.fe.comp2026.ConfigOptions;
import pt.up.fe.comp2026.optimization.OptimizationPhaseSelector.Phase;

import java.util.Map;

import static pt.up.fe.comp2026.optimization.OptimizationPhaseSelector.CONST_FOLD;
import static pt.up.fe.comp2026.optimization.OptimizationPhaseSelector.CONST_PROP;
import static pt.up.fe.comp2026.optimization.OptimizationPhaseSelector.Phase.AST_TRANSFORMATION;


public class ConstPropAndFoldingTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/extensions/ollir/optimizations/";
    private static final String RESOURCES_LOCATION = "test-public";

    //Just need to check one of them, if one is AST use AST
    //otherwise just use the other (if AST returns AST, otherwise, both are OLLIR)
    private static final Phase earliest = CONST_PROP == AST_TRANSFORMATION ? CONST_PROP : CONST_FOLD;

    public ConstPropAndFoldingTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    public OllirResult toOllir(String resourceName, Phase testedOptimization, boolean optimize) {
        return toOllir(resourceName, optimize ? Map.of(ConfigOptions.getOptimize(), "true") : Map.of(),
                testedOptimization);
    }

    //Despite only having one option for optimization, this allows the specification of different flags for different optimization phases in the future, if needed
    public OllirResult toOllir(String resourceName, Map<String, String> config, Phase testedOptimization) {
        setConfig(config);
        OllirResult ollirResult;

        if (testedOptimization == AST_TRANSFORMATION) {
            ollirResult = jmmToOllir("jmm/" + resourceName + ".jmm", true);
        } else {
            ollirResult = loadOllir("ollir/" + resourceName + ".ollir");
            setComparingOllir(ollirResult.getOllirCode(), "Original Ollir code", "Register Allocation", ollirResult.getOllirClass());
            ollirResult = optimize(ollirResult);
        }
        var classUnit = ollirResult.getOllirClass();
        assertNotNull("Ollir class unit should not be null", classUnit);
        assertEquals("Class name not what was expected", resourceName, classUnit.getClassName());
        return ollirResult;
    }


    public ClassUnit optimize(String resourceName, Phase testedOptimization, boolean shouldChange) {
        var original = toOllir(resourceName, testedOptimization, false);
        var optimized = toOllir(resourceName, testedOptimization, true);

        var originalCode = original.getOllirClass().code(); //.getOllirCode();
        var optimizedCode = optimized.getOllirClass().code(); //.getOllirCode();
        if (shouldChange) {
            assertNotEquals("Expected code to change with -o flag",
                    originalCode, optimizedCode);
        } else {
            assertEquals("Expected code to not change with -o flag",
                    originalCode, optimizedCode);
        }
        return optimized.getOllirClass();
    }


    public Method optimize(String resourceName, String methodName, Phase testedOptimization, boolean shouldChange) {
        var optimized = optimize(resourceName, testedOptimization, shouldChange);
        var methods = optimized.getMethods().stream().filter(m -> m.getMethodName().equals(methodName)).toList();
        assertEquals("Should contain a single method named " + methodName, 1, methods.size());
        return methods.getFirst();
    }


    @Test
    public void testSimplePropagation() {
        var testedOpt = CONST_PROP;
        var method = optimize("PropSimple", "foo", testedOpt, true);
        assertLiteralReturn("1", method);
        assertIdRefAtMost("a", method, 1);

    }


    @Test
    public void testPropWithIf() {
        var testedOpt = CONST_PROP;
        var method = optimize("PropWithIf", "foo", testedOpt, true);
        assertLiteralAtLeast("3", method, 2); // dead-code can remove assignment
        assertLiteralCount("0", method, 2);
        assertIdRefAtMost("a", method, 1);
        assertIdRefAtMost("b", method, 1);
        assertIdRefAtMost("res", method, 4);
        assertIdRefCount("i", method, 3);
    }

    @Test
    public void testPropWithCall() {
        var testedOpt = CONST_PROP;
        var method = optimize("PropWithCall", "foo", testedOpt, true);
        assertLiteralAtMost("3", method, 2);
        assertIdRefAtMost("a", method, 1);
        assertIdRefAtLeast("res", method, 2);
        assertIdRefCount("i", method, 2);
    }

    @Test
    public void testFoldSimple() {
        var testedOpt = CONST_FOLD;
        var method = optimize("FoldSimple", "foo", testedOpt, true);
        assertFindLiteral("30", method);

    }

    @Test
    public void constFoldSequence() {

        var testedOpt = CONST_FOLD;
        var method = optimize("FoldSequence", "foo", testedOpt, true);
        assertFindLiteral("14", method);
        assertIdRefAtMost("a", method, 1);
    }

    @Test
    public void constPropAnFoldSimple() {

        var testedOpt = earliest;
        var method = optimize("PropAndFoldingSimple", "foo", testedOpt, true);
        assertFindLiteral("15", method);
        assertIdRefAtMost("a", method, 1);
        assertIdRefAtMost("b", method, 1);
    }

    @Test
    public void constPropAnFoldRequiresIteration() {

        var testedOpt = earliest;
        var method = optimize("PropAndFoldingRequiresIteration", "foo", testedOpt, true);
        assertLiteralCount("15", method, 1, 3);
        assertIdRefAtMost("a", method, 1);
        assertIdRefAtMost("b", method, 1);
        assertIdRefAtMost("c", method, 1);
    }

    @Test
    public void constPropAnFoldSequence() {

        var testedOpt = earliest;
        var method = optimize("PropAndFoldingSequence", "foo", testedOpt, true);
        assertFindLiteral("150", method);
        assertIdRefAtMost("a", method, 1);
        assertIdRefAtMost("b", method, 1);
        assertIdRefAtMost("c", method, 1);
        assertIdRefAtMost("d", method, 1);
        assertIdRefAtMost("e", method, 1);
    }

    @Test
    public void testNoOptimization() {

        var testedOpt = earliest;
        optimize("NoOptimization", "foo", testedOpt, false);
    }

}

