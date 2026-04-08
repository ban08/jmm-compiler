package pt.up.fe.comp.cp2.core.ollir.statements;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.inst.CondBranchInstruction;
import org.specs.comp.ollir.inst.ReturnInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class IfStatementOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/statements/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public IfStatementOllirTest() {
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
    public void testSimpleIfElse() {
        var classUnit = toOllir("IfSimpleElse.jmm");
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst().orElseThrow();

        assertInstExists(CondBranchInstruction.class, method);
        assertInstExists(ReturnInstruction.class, method);
    }

    @Test
    public void testNestedIfElse() {
        var classUnit = toOllir("IfNested.jmm");
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst().orElseThrow();

        assertInstExists(CondBranchInstruction.class, method);
        assertInstExists(ReturnInstruction.class, method);
    }
}

