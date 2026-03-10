package pt.up.fe.comp.cp1.core.semantics.declarations;

import org.junit.Test;

/**
 * Test variable lookup.
 */
public class MethodDeclSymbolTableErrorTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/declarations/symboltable/";
    private static final String RESOURCES_LOCATION = "test-public";

    public MethodDeclSymbolTableErrorTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void DuplicateParameterName() {
        setDescription("Test that parameter field names are not added to the symbol table");
        symbolTable("DuplicateParameterName.jmm", true);
        symbolTable("DuplicateParameterNameOk.jmm", false);
    }


    @Test
    public void DuplicateLocalVariableName() {
        setDescription("Test that duplicate local variable names are not added to the symbol table");
        symbolTable("DuplicateLocalVariableName.jmm", true);
        symbolTable("DuplicateLocalVariableNameOk.jmm", false);
    }

    @Test
    public void ParameterWithSameNameAsLocal() {
        setDescription("Test that a parameter with the same name as a local variable is not added to the symbol table");
        symbolTable("ParameterWithSameNameAsLocal.jmm", true);
        symbolTable("ParameterWithSameNameAsLocalOk.jmm", false);
    }
}
