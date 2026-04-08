package pt.up.fe.comp.cp2.core.ollir.statements;

import org.junit.Test;
import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.inst.ReturnInstruction;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.test.env.OllirTestEnv;

public class ReturnStatementOllirTest extends OllirTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp2/core/ollir/statements/jmm/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ReturnStatementOllirTest() {
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
    public void testIntReturn() {
        var classUnit = toOllir("ReturnInt.jmm");
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst().orElseThrow();

        assertTrue("Method return type should be int", BuiltinType.is(method.getReturnType(), BuiltinKind.INT32));
        assertInstExists(ReturnInstruction.class, method);
    }

    @Test
    public void testVoidReturn() {
        var classUnit = toOllir("ReturnVoid.jmm");
        var method = classUnit.getMethods().stream().filter(m -> m.getMethodName().equals("method")).findFirst().orElseThrow();

        assertTrue("Method return type should be void", BuiltinType.is(method.getReturnType(), BuiltinKind.VOID));
        assertInstExists(ReturnInstruction.class, method);
    }
}

