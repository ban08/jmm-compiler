package core.semantics;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

public class InvalidVoidDeclarationSemanticsTest extends JmmTestEnv {

    public InvalidVoidDeclarationSemanticsTest() {
        super("", "");
    }

    @Test
    public void fieldCannotHaveVoidType() {
        var parserResult = parseSnippet("""
                package p;
                class A {
                    void field;
                    void foo() {}
                }
                """);

        var symbolTable = symbolTable(parserResult, false);
        semantics(symbolTable, true);
    }

    @Test
    public void localCannotHaveVoidType() {
        var parserResult = parseSnippet("""
                package p;
                class A {
                    void foo() {
                        void local;
                    }
                }
                """);

        var symbolTable = symbolTable(parserResult, false);
        semantics(symbolTable, true);
    }

    @Test
    public void parameterCannotHaveVoidType() {
        var parserResult = parseSnippet("""
                package p;
                class A {
                    void foo(void value) {}
                }
                """);

        var symbolTable = symbolTable(parserResult, false);
        semantics(symbolTable, true);
    }

    @Test
    public void voidReturnTypeRemainsAllowed() {
        var parserResult = parseSnippet("""
                package p;
                class A {
                    void foo() {
                        return;
                    }
                }
                """);

        var symbolTable = symbolTable(parserResult, false);
        semantics(symbolTable, false);
    }
}
