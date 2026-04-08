package pt.up.fe.comp.cp2.core.ollir.statements;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.ReturnInstruction;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class CompoundStatementOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/statements/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public CompoundStatementOllirTest() {
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
    public void testEmptyCompound() {
        var classUnit = toOllir("CompoundEmpty.jmm");
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst().orElseThrow();

        assertInstExists(ReturnInstruction.class, method);
    }

    @Test
    public void testNestedCompound() {
        var classUnit = toOllir("CompoundNested.jmm");
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst().orElseThrow();

        var assigns = assertInstExists(AssignInstruction.class, method);
        assertTrue("Expected at least two assignments in method", assigns.size() >= 2);
        assertInstExists(ReturnInstruction.class, method);
    }
}

