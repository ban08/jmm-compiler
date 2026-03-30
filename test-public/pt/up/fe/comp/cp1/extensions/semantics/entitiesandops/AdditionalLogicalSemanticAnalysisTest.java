package pt.up.fe.comp.cp1.extensions.semantics.entitiesandops;

import org.junit.Test;

public class AdditionalLogicalSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {

    public AdditionalLogicalSemanticAnalysisTest() {
        super("", "");
    }

    public void testSimpleOperation(String operator) {
        setDescription("Test use of " + operator);
        semanticsFromSnippet("""
                package x;
                class ExtraOperation {
                    boolean op(Object lhs, int rhs) {
                        return lhs {op} rhs;
                    }
                }""".replace("{op}", operator), true);
        semanticsFromSnippet("""
                package x;
                class ExtraOperation {
                    boolean op(int lhs, boolean rhs) {
                        return lhs {op} rhs;
                    }
                }""".replace("{op}", operator), true);
        semanticsFromSnippet("""
                package x;
                class ExtraOperation {
                    boolean op(int lhs, int rhs) {
                        return lhs {op} rhs;
                    }
                }""".replace("{op}", operator), false);
    }

    public void testComplexExpression(String operator) {
        setDescription("Test use of " + operator + " in more complex expressions");
        semanticsFromSnippet("""
                package x;
                class ExtraOperation {
                    boolean gt(int value, int other) {
                        return (value + 1)/2 {op} (other*2)/2;
                    }
                }""".replace("{op}", operator), false);
    }


    @Test
    public void testGTExpression() {
        testSimpleOperation(">");
    }

    @Test
    public void testComplexGTExpression() {
        testComplexExpression(">");
    }

    @Test
    public void testGEExpression() {
        testSimpleOperation(">=");
    }

    @Test
    public void testComplexGEExpression() {
        testComplexExpression(">=");
    }

    @Test
    public void testLEExpression() {
        testSimpleOperation("<=");
    }

    @Test
    public void testComplexLEExpression() {
        testComplexExpression("<=");
    }

    @Test
    public void testEqExpression() {
        testSimpleOperation("==");
    }

    @Test
    public void testComplexEqExpression() {
        testComplexExpression("==");
    }

    @Test
    public void testNEExpression() {
        testSimpleOperation("!=");
    }

    @Test
    public void testComplexNEExpression() {
        testComplexExpression("!=");
    }

    @Test
    public void testSimpleOr() {
        setDescription("Test use of ||");
        semanticsFromSnippet("""
                package x;
                class ExtraOperation {
                    boolean or(Object lhs, boolean rhs) {
                        return lhs || rhs;
                    }
                }""", true);
        semanticsFromSnippet("""
                package x;
                class ExtraOperation {
                    boolean or(boolean lhs, int rhs) {
                        return lhs || rhs;
                    }
                }""", true);
        semanticsFromSnippet("""
                package x;
                class ExtraOperation {
                    boolean or(boolean lhs, boolean rhs) {
                        return lhs || rhs;
                    }
                }""", false);
    }

    @Test
    public void testComplexOr() {
        setDescription("Test use of || in more complex expressions");
        semanticsFromSnippet("""
                package x;
                class ExtraOperation {
                    boolean or(int value, int other) {
                        return (value + 1)/2 < other || (other*2)/2 < value;
                    }
                }""", false);
    }
}