package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class FullyQualifiedTypeIdentityTest extends JmmTestEnv {

    public FullyQualifiedTypeIdentityTest() {
        super("", "");
    }

    @Test
    public void importedTypeWithSameSimpleNameAsCurrentClassIsNotAssignableFromThis() {
        var parserResult = parseSnippet("""
                package p;
                import java.util.Date;
                class Date {
                    void foo() {
                        Date value;
                        value = this;
                    }
                }
                """);

        var symbolTable = symbolTable(parserResult, false);
        semantics(symbolTable, true);
    }

    @Test
    public void importedReceiverWithSameSimpleNameAsCurrentClassUsesImportedMethods() {
        var parserResult = parseSnippet("""
                package p;
                import java.util.Date;
                class Date {
                    void foo() {
                        new Date().after(new Date());
                    }
                }
                """);

        var symbolTable = symbolTable(parserResult, false);
        semantics(symbolTable, false);
    }
}
