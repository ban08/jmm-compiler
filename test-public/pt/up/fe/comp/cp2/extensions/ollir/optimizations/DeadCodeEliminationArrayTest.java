package pt.up.fe.comp.cp2.extensions.ollir.optimizations;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.test.env.OllirTestEnv;
import pt.up.fe.comp2026.ConfigOptions;
import pt.up.fe.comp2026.optimization.OptimizationPhaseSelector;

import java.util.Map;

import static pt.up.fe.comp2026.optimization.OptimizationPhaseSelector.Phase.AST_TRANSFORMATION;


public class DeadCodeEliminationArrayTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/extensions/ollir/optimizations/";
    private static final String RESOURCES_LOCATION = "test-public";

    //Just need to check one of them, if one is AST use AST
    //otherwise just use the other (if AST returns AST, otherwise, both are OLLIR)

    public DeadCodeEliminationArrayTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    public OllirResult toOllir(String resourceName, boolean optimize) {
        return toOllir(resourceName, optimize ? Map.of(ConfigOptions.getOptimize(), "true") : Map.of());
    }

    //Despite only having one option for optimization, this allows the specification of different flags for different optimization phases in the future, if needed
    public OllirResult toOllir(String resourceName, Map<String, String> config) {
        setConfig(config);
        OllirResult ollirResult;

        if (OptimizationPhaseSelector.DEAD_CODE_ELIM == AST_TRANSFORMATION) {
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


    public ClassUnit optimize(String resourceName, boolean shouldChange) {
        var original = toOllir(resourceName, false);
        var optimized = toOllir(resourceName, true);

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


    public Method optimize(String resourceName, String methodName, boolean shouldChange) {
        var optimized = optimize(resourceName, shouldChange);
        var methods = optimized.getMethods().stream().filter(m -> m.getMethodName().equals(methodName)).toList();
        assertEquals("Should contain a single method named " + methodName, 1, methods.size());
        return methods.getFirst();
    }

    public Method optimizeMightNotChange(String resourceName, String methodName) {

        var optimized = toOllir(resourceName, true).getOllirClass();
        var methods = optimized.getMethods().stream().filter(m -> m.getMethodName().equals(methodName)).toList();
        assertEquals("Should contain a single method named " + methodName, 1, methods.size());
        return methods.getFirst();
    }


    /**
     * Test used as reference to check if project implements Dead code elimination
     */
    public void baseTest() {
        var method = optimize("DeadCodeEliminationSimple", "foo", true);
        assertIdRefCount("res", method, 0);
    }

    @Test
    public void testDeadCodeEliminationSimpleArray() {
        var method = optimize("DeadCodeEliminationSimpleArray", "foo", true);
        assertIdRefCount("res", method, 0);
    }

    @Test
    public void testDeadCodeEliminationSimpleArrayInParam() {
        baseTest();
        optimize("DeadCodeEliminationSimpleArrayInParam", "foo", false);
    }


}

