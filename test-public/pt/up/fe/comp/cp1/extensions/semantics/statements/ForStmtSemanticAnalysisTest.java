package pt.up.fe.comp.cp1.extensions.semantics.statements;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class ForStmtSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {

    public ForStmtSemanticAnalysisTest() {
        super("", "");
    }

    @Test
    public void forSimpleBoolean() {
        setDescription("Test that a for-loop uses a simple boolean variable");
        semanticsFromSnippet("""
                package x;
                class ForExpressionFail {
                    int something(int value) {
                        int res;
                        for(res = 0; value; res = res+5){
                        }
                        return res;
                    }
                }""", true);

        semanticsFromSnippet("""
                package x;
                class ForExpressionOk {
                    int something(boolean value) {
                        int res;
                        for(res = 0; value; res = res+5){
                            value = false;
                        }
                        return res;
                    }
                }""", false);
    }

    @Test
    public void forBooleanExpression() {
        setDescription("Test that a for-loop uses boolean expression");
        semanticsFromSnippet("""
                package x;
                class ForExpressionFail {
                    int something(int value) {
                        int res;
                        for(res = 0; value-res; res = res+5){
                        }
                        return res;
                    }
                }""", true);
        semanticsFromSnippet("""
                package x;
                class ForExpressionOk {
                    int something(int value) {
                        int res;
                        for(res = 0; res < value; res = res+5){
                        }
                        return res;
                    }
                }""", false);
    }


    @Test
    public void forWrongAssigment() {
        setDescription("Test that a for-loop uses boolean expression");
        semanticsFromSnippet("""
                package x;
                class ForExpressionFail {
                    int something(boolean value) {
                        int res;
                        for(res = value; 0 < res; res = res-5){
                        }
                        return res;
                    }
                }""", true);
        semanticsFromSnippet("""
                package x;
                class ForExpressionFail {
                    int something(boolean value) {
                        int res;
                        for(res = value; 0 < res; res = res-value){
                        }
                        return res;
                    }
                }""", true);

        semanticsFromSnippet("""
                package x;
                class ForExpressionOk {
                    int something(int value) {
                        int res;
                        for(res = value; 0 < res; res = res-5){
                        }
                        return res;
                    }
                }""", false);
    }
}
