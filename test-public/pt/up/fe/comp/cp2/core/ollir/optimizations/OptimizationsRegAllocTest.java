package pt.up.fe.comp.cp2.core.ollir.optimizations;

import org.junit.Test;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.test.env.OllirTestEnv;
import pt.up.fe.comp2026.optimization.OptimizationPhaseSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static pt.up.fe.comp2026.optimization.OptimizationPhaseSelector.Phase.*;


public class OptimizationsRegAllocTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/optimizations/";
    private static final String RESOURCES_LOCATION = "test-public";

    public OptimizationsRegAllocTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    public ClassUnit toOllir(String resourceName) {
        OllirResult ollirResult;

        if (OptimizationPhaseSelector.REG_ALLOC == AST_TRANSFORMATION) {
            ollirResult = jmmToOllir("jmm/" + resourceName + ".jmm", true);
        } else {
            ollirResult = loadOllir("ollir/" + resourceName + ".ollir");
            setComparingOllir(ollirResult.getOllirCode(), "Original Ollir code", "Register Allocation", ollirResult.getOllirClass());
            ollirResult = optimize(ollirResult);
        }
        var classUnit = ollirResult.getOllirClass();
        assertNotNull("Ollir class unit should not be null", classUnit);
        assertEquals("Class name not what was expected", resourceName, classUnit.getClassName());
        return classUnit;
    }

    public ClassUnit apply(String resourceName, String methodName, boolean isStatic, int numParams, int rValueForLocals, int maxRegistersForLocals, Map<String, List<String>> interferences) {

        int expected_registers = isStatic ? 0 : 1 + numParams + maxRegistersForLocals;
        setDescription("Register allocation should use at most " + expected_registers + " registers with -r=" + rValueForLocals);

        //with specific r value, this + num_params must be considered
        var rValue = rValueForLocals <= 0 ? rValueForLocals : (isStatic ? 0 : 1) + numParams + rValueForLocals;
        var config = Map.of(
                "registerAllocation", String.valueOf(rValue)
        );
        this.setConfig(config);
        var classUnit = toOllir(resourceName);

        var methodOpt = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals(methodName)).findFirst();
        assertEquals("Method '" + methodName + "' should exist", true, methodOpt.isPresent());
        var method = methodOpt.orElseThrow();
        var varTable = method.getVarTable();
        int max_used = varTable.values().stream().map(Descriptor::getVirtualReg).max(Integer::compareTo).orElse(0);
        int actual_num_registers_used = max_used + 1;
        List<Element> params = method.getParams();
        switch (rValueForLocals) {
            case -1: //can use as many registers as needed
                break;
            case 0: //should use minimum possible
                assertTrue("With -r=0 Should use at most " + expected_registers + " register(s), but used " + actual_num_registers_used, actual_num_registers_used <= expected_registers);
                break;
            default: //should use at most num_registers
                assertTrue("Should use at most " + expected_registers + " register(s), but used " + actual_num_registers_used, actual_num_registers_used <= expected_registers);
        }

        var firstRegValue = isStatic ? 0 : 1;
        var paramNames = new ArrayList<String>();
        for (int i = 0; i < numParams; i++) {
            var param = params.get(i);
            if (param instanceof Operand op) {
                var paramReg = varTable.get(op.getName()).getVirtualReg();
                assertEquals("Parameter '" + op.getName() + "' (reg: ${actual}) should be assigned to register ${expected}", firstRegValue + i, paramReg);
                paramNames.add(op.getName());
            } else {
                fail("Parameter '" + param + "' should be an operand");
            }
        }
        firstRegValue += numParams;
        for (var entry : varTable.entrySet()) {
            var varName = entry.getKey();
            if (paramNames.contains(varName) || varName.equalsIgnoreCase("this")) {
                continue; //already checked parameters
            }
            var varReg = entry.getValue().getVirtualReg();
            assertTrue("Variable '" + varName + "' (reg: " + varReg + ") should be assigned to a register number greater or equal to " + firstRegValue, varReg >= firstRegValue);
        }

        if (interferences != null && !interferences.isEmpty()) {
            for (var entry : interferences.entrySet()) {
                var varName = entry.getKey();
                var varReg = varTable.get(varName).getVirtualReg();
                var interferencesList = entry.getValue();
                for (var interfName : interferencesList) {
                    var interfReg = varTable.get(interfName).getVirtualReg();
                    assertNotEquals("Variables '" + varName + "' (reg: ${expected}) and '" + interfReg + "' (reg: ${actual}) should have different registers.", varReg, interfReg);
                }
            }
        }
        return classUnit;
    }

    @Test
    public void testRegAllocSlidesLivAn2() {
        var resourceName = "RegAllocSlidesLivAn2";
        var methodName = "method";
        var isStatic = false;
        var num_params = 0;
        var rValueForLocals = 2;
        var max_registers_allowed = rValueForLocals; //to allow some gap, add to this value
        var interferences = Map.of(
                "a", List.of("b")
        );
        apply(resourceName, methodName, isStatic, num_params, rValueForLocals, max_registers_allowed, interferences);
    }

    @Test
    public void testRegAllocNoChange2() {
        //first force use of register allocation with first test so it is guaranteed that code for reg alloc was created
        testRegAllocSlidesLivAn2();
        var resourceName = "RegAllocNoChange2";
        var methodName = "method";
        var isStatic = false;
        var num_params = 0;
        var rValueForLocals = 2;
        var max_registers_allowed = rValueForLocals; //to allow some gap, add to this value
        var interferences = Map.of(
                "a", List.of("b")
        );
        apply(resourceName, methodName, isStatic, num_params, rValueForLocals, max_registers_allowed, interferences);
    }

    @Test
    public void testRegAllocSlidesLivAn2_with_0() {
        var resourceName = "RegAllocSlidesLivAn2";
        var methodName = "method";
        var isStatic = false;
        var num_params = 0;
        var rValueForLocals = 0;
        var max_registers_allowed = 2; //to allow some gap, add to this value
        var interferences = Map.of(
                "a", List.of("b")
        );
        apply(resourceName, methodName, isStatic, num_params, rValueForLocals, max_registers_allowed, interferences);
    }

    @Test
    public void testRegAllocSlidesLivAn2_with_m1() {
        var resourceName = "RegAllocSlidesLivAn2";
        var methodName = "method";
        var isStatic = false;
        var num_params = 0;
        var rValueForLocals = -1;
        var max_registers_allowed = 2; //to allow some gap, add to this value
        var interferences = Map.of(
                "a", List.of("b")
        );
        apply(resourceName, methodName, isStatic, num_params, rValueForLocals, max_registers_allowed, interferences);
    }

    @Test
    public void testRegAllocMixedVars1() {
        var resourceName = "RegAllocMixedVars1";
        var methodName = "method";
        var isStatic = false;
        var num_params = 0;
        var rValueForLocals = 1;
        var max_registers_allowed = rValueForLocals; //to allow some gap, add to this value
        Map<String, List<String>> interferences = Map.of();
        apply(resourceName, methodName, isStatic, num_params, rValueForLocals, max_registers_allowed, interferences);
    }

    @Test
    public void testRegAllocParamAndVars1() {
        var resourceName = "RegAllocParamAndVars1";
        var methodName = "method";
        var isStatic = false;
        var num_params = 1;
        var rValueForLocals = 1;
        var max_registers_allowed = rValueForLocals; //to allow some gap, add to this value
        Map<String, List<String>> interferences = Map.of();
        apply(resourceName, methodName, isStatic, num_params, rValueForLocals, max_registers_allowed, interferences);
    }

    @Test
    public void testRegAllocMixedVars2() {
        var resourceName = "RegAllocMixedVars2";
        var methodName = "method";
        var isStatic = false;
        var num_params = 0;
        var rValueForLocals = 2;
        var max_registers_allowed = rValueForLocals; //to allow some gap, add to this value
        var interferences = Map.of(
                "a", List.of("b"),
                "b", List.of("c")
        );
        apply(resourceName, methodName, isStatic, num_params, rValueForLocals, max_registers_allowed, interferences);
    }

    @Test
    public void testRegAllocMixedVars3() {
        var resourceName = "RegAllocMixedVars3";
        var methodName = "method";
        var isStatic = false;
        var num_params = 0;
        var rValueForLocals = 3;
        var max_registers_allowed = rValueForLocals; //to allow some gap, add to this value
        var interferences = Map.of(
                "a", List.of("b", "c"),
                "b", List.of("d")
        );
        apply(resourceName, methodName, isStatic, num_params, rValueForLocals, max_registers_allowed, interferences);
    }

    @Test
    public void testRegAllocIf4() {
        var resourceName = "RegAllocIf4";
        var methodName = "method";
        var isStatic = false;
        var num_params = 4;
        var rValueForLocals = 4;
        var max_registers_allowed = rValueForLocals + 1; //to allow temp for if
        var interferences = Map.of(
                "a", List.of("d", "f", "e"),
                "b", List.of("c", "e", "f"),
                "c", List.of("e", "f"),
                "d", List.of("e"),
                "e", List.of("f")
        );
        apply(resourceName, methodName, isStatic, num_params, rValueForLocals, max_registers_allowed, interferences);
    }

    @Test
    public void testRegAllocIf3() {
        var resourceName = "RegAllocIf3";
        var methodName = "method";
        var isStatic = false;
        var num_params = 4;
        var rValueForLocals = 3;
        var max_registers_allowed = rValueForLocals + 1; //to allow temp for if
        var interferences = Map.of(
                "a", List.of("d", "f", "e"),
                "b", List.of("c"),
                "d", List.of("e"),
                "e", List.of("f")
        );
        apply(resourceName, methodName, isStatic, num_params, rValueForLocals, max_registers_allowed, interferences);
    }

    @Test
    public void testRegAllocIf2() {
        var resourceName = "RegAllocIf2";
        var methodName = "method";
        var isStatic = false;
        var num_params = 4;
        var rValueForLocals = 2;
        var max_registers_allowed = rValueForLocals + 1; //to allow temp for if
        var interferences = Map.of(
                "a", List.of("d"),
                "b", List.of("c")
        );
        apply(resourceName, methodName, isStatic, num_params, rValueForLocals, max_registers_allowed, interferences);
    }
}

