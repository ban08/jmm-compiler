package pt.up.fe.comp.cp1.extensions.semantics.statements;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class DoWhileSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {


    public DoWhileSemanticAnalysisTest() {
        super("", "");
    }

    @Test
    public void doWhileSimpleBoolean() throws Exception {
        setDescription("Test that a while-loop uses a simple boolean variable");
        semanticsFromSnippet("""
                package x;
                class DoWhileExpressionFail {
                    int something(int value) {
                        int res;
                        res = 0;
                        do{
                            res = res + 5;
                        } while (value);
                        return res;
                    }
                }""", true);
        semanticsFromSnippet("""
                package x;
                class DoWhileExpressionOk {
                    int something(boolean value) {
                        int res;
                        res = 0;
                        do{
                            res = res + 5;
                            value = false;
                        } while (value);
                        return res;
                    }
                }""", false);
    }

    @Test
    public void dowhileExpession() {
        setDescription("Test that a while-loop uses a boolean expression");
        semanticsFromSnippet("""
                package x;
                class DoWhileExpressionFail {
                    int something(int value) {
                        int res;
                        res = 0;
                        do{
                            res = res + 5;
                        } while (value - res);
                        return res;
                    }
                }""", true);
        semanticsFromSnippet("""
                package x;
                class DoWhileExpressionOk {
                    int something(int value) {
                        int res;
                        res = 0;
                        do{
                            res = res + 5;
                        } while (res < value);
                        return res;
                    }
                }""", false);
    }
}
