package pt.up.fe.comp.cp1.extensions.semantics.entitiesandops;

import org.junit.Test;

public class AdditionalArithmeticSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {


    public AdditionalArithmeticSemanticAnalysisTest() {
        super("", "");
    }

    @Test
    public void testRemainderWrongOperands() {
        setDescription("Test use of % with wrong operands");
        semanticsFromSnippet("""
                package x;
                class RemainderWrongOperands {
                    int rem(int dividend, Object divisor) {
                        int res;
                        res = dividend % divisor;
                        return res;
                    }
                }""", true);
        semanticsFromSnippet("""
                package x;
                class RemainderWrongOperands {
                    int rem(boolean dividend, int divisor) {
                        int res;
                        res = dividend % divisor;
                        return res;
                    }
                }""", true);
        semanticsFromSnippet("""
                package x;
                class RemainderWrongOperands {
                    int rem(int dividend, int divisor) {
                        int res;
                        res = dividend % divisor;
                        return res;
                    }
                }""", false);

    }

    @Test
    public void testPositiveExpression() {
        setDescription("Test use of positive expressions");
        semanticsFromSnippet("""
                package x;
                class PositiveNumber {
                    int pos(Object value) {
                        return +value;
                    }
                }""", true);
        semanticsFromSnippet("""
                package x;
                class PositiveNumber {
                    int pos(int value) {
                        return +value;
                    }
                }""", false);
    }

    @Test
    public void testNegativeExpression() {
        setDescription("Test use of negative expressions");
        semanticsFromSnippet("""
                package x;
                class NegativeNumber {
                    int neg(Object value) {
                        return -value;
                    }
                }""", true);
        setDescription("Test use of negative expressions");
        semanticsFromSnippet("""
                package x;
                class NegativeNumber {
                    int neg(int value) {
                        return -value;
                    }
                }""", false);
    }

    @Test
    public void testComplexNegativeExpression() {
        setDescription("Test use of negative expressions in more complex expressions");
        semanticsFromSnippet("""
                package x;
                class ComplexNegative {
                    int neg(int value) {
                        return -(value + 1);
                    }
                }""", false);
    }

    @Test
    public void testIncrementExpression() {
        setDescription("Test use of increment expressions");
        semanticsFromSnippet("""
                package x;
                class Increment {
                    int inc(Object value) {
                        return ++value;
                    }
                }""", true);
        semanticsFromSnippet("""
                package x;
                class Increment {
                    int inc(int value) {
                        return ++value;
                    }
                }""", false);
    }

    @Test
    public void testDecrementExpression() {
        setDescription("Test use of decrement expressions");
        semanticsFromSnippet("""
                package x;
                class Decrement {
                    int dec(Object value) {
                        return --value;
                    }
                }""", true);
        semanticsFromSnippet("""
                package x;
                class Decrement {
                    int dec(int value) {
                        return --value;
                    }
                }""", false);
    }

    @Test
    public void testComplexIncrementExpression() {
        setDescription("Test use of increment expressions in more complex expressions");
        semanticsFromSnippet("""
                package x;
                class ComplexInc {
                    int inc(int value) {
                        return 3 * ++value;
                    }
                }""", false);
    }

    @Test
    public void testComplexDecrementExpression() {
        setDescription("Test use of decrement expressions in more complex expressions");
        semanticsFromSnippet("""
                package x;
                class ComplexDec {
                    int dec(int value) {
                        return --value / 5;
                    }
                }""", false);
    }
}