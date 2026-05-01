package pt.up.fe.comp.cp2.extensions.ollir.statements;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.inst.CondBranchInstruction;
import org.specs.comp.ollir.inst.ReturnInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class IfWithoutElseOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/extensions/ollir/statements/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public IfWithoutElseOllirTest() {
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
    public void testSimpleIf() {
        var classUnit = toOllir("IfSimple.jmm");
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst().orElseThrow();

        var cond = assertInstExists(CondBranchInstruction.class, method);
        var ret = assertInstExists(ReturnInstruction.class, method);
        assertOrder(method, cond.getFirst(), ret.getFirst());
    }

    @Test
    public void testNestedIf() {
        var classUnit = toOllir("IfNested.jmm");
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst().orElseThrow();

        var conds = assertAtLeast(method, CondBranchInstruction.class, 2);
        var ret = assertInstExists(ReturnInstruction.class, method);
        assertOrder(method, conds.getFirst(), conds.get(1), ret.getFirst());
    }
}

