package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;

public class MyExpressionPrecedenceParserTest extends JmmTestEnv {

    public MyExpressionPrecedenceParserTest() {
        super("", "");
    }

    @Test
    public void unaryNotWrapsMethodCallsInsteadOfStealingTheirReceiver() {
        var result = parseSnippet("!a.foo()", EXPRESSION);
        var root = result.rootNode();

        assertTrue("Expected unary not to be the root node", JmmKind.NOT_EXPR.check(root));

        var methodCall = root.getChild(0);
        assertTrue("Expected the operand of '!' to be a method call", JmmKind.METHOD_CALL_EXPR.check(methodCall));
        assertEquals("Expected the method name to remain attached to the call", "foo",
                methodCall.get(JmmAttributes.METHOD_CALL_EXPR.NAME));

        var receiver = methodCall.getChild(0);
        assertTrue("Expected the receiver of foo() to remain the identifier 'a'", JmmKind.VAR_REF_EXPR.check(receiver));
        assertEquals("Expected the receiver variable name to be preserved", "a",
                receiver.get(JmmAttributes.VAR_REF_EXPR.NAME));
    }

    @Test
    public void postfixChainsNestFromLeftToRight() {
        var result = parseSnippet("a.foo()[0].bar()", EXPRESSION);
        var root = result.rootNode();

        assertTrue("Expected the outermost postfix operation to be the final method call",
                JmmKind.METHOD_CALL_EXPR.check(root));
        assertEquals("Expected the outermost method call to be bar()", "bar",
                root.get(JmmAttributes.METHOD_CALL_EXPR.NAME));

        var arrayAccess = root.getChild(0);
        assertTrue("Expected bar() to be invoked over the result of an array access",
                JmmKind.ARRAY_ACCESS_EXPR.check(arrayAccess));

        var innerCall = arrayAccess.getChild(0);
        assertTrue("Expected the array access receiver to be the previous method call",
                JmmKind.METHOD_CALL_EXPR.check(innerCall));
        assertEquals("Expected the inner method call to be foo()", "foo",
                innerCall.get(JmmAttributes.METHOD_CALL_EXPR.NAME));

        var originalReceiver = innerCall.getChild(0);
        assertTrue("Expected the original chain receiver to remain 'a'", JmmKind.VAR_REF_EXPR.check(originalReceiver));
        assertEquals("Expected the original receiver variable name to be preserved", "a",
                originalReceiver.get(JmmAttributes.VAR_REF_EXPR.NAME));

        var index = arrayAccess.getChild(1);
        assertTrue("Expected the array index to be the integer literal 0", JmmKind.INTEGER_LITERAL.check(index));
        assertEquals("Expected the array index literal to be preserved", "0",
                index.get(JmmAttributes.INTEGER_LITERAL.VALUE));
    }

    @Test
    public void unaryMinusWrapsWholePostfixChains() {
        var result = parseSnippet("-a[0].length", EXPRESSION);
        var root = result.rootNode();

        assertTrue("Expected unary minus to be the root node", JmmKind.UNARY_EXPR.check(root));
        assertEquals("Expected the unary operator to be '-'", "-",
                root.get(JmmAttributes.UNARY_EXPR.OP));

        var fieldAccess = root.getChild(0);
        assertTrue("Expected the operand of unary minus to be the full '.length' access",
                JmmKind.FIELD_ACCESS_EXPR.check(fieldAccess));
        assertEquals("Expected the accessed field to be length", "length",
                fieldAccess.get(JmmAttributes.FIELD_ACCESS_EXPR.NAME));

        var arrayAccess = fieldAccess.getChild(0);
        assertTrue("Expected length to apply over the full array access expression",
                JmmKind.ARRAY_ACCESS_EXPR.check(arrayAccess));

        var base = arrayAccess.getChild(0);
        assertTrue("Expected the array access receiver to remain 'a'", JmmKind.VAR_REF_EXPR.check(base));
        assertEquals("Expected the base variable name to be preserved", "a",
                base.get(JmmAttributes.VAR_REF_EXPR.NAME));
    }
}
