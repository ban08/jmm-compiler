package core.parser;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;

public class MyArrayTypeAstParserTest extends JmmTestEnv {

    public MyArrayTypeAstParserTest() {
        super("", "");
    }

    @Test
    public void multidimensionalArrayTypesAreRepresentedCompositionally() {
        var result = parseSnippet("int[][] foo() {}", METHOD);
        var methodDecl = result.rootNode();

        var returnType = methodDecl.getObject(JmmAttributes.METHOD_DECL.RETURN_TYPE.getKey(), pt.up.fe.comp.jmm.ast.JmmNode.class);
        assertTrue("Top-level return type should be an array type", JmmKind.ARRAY_TYPE.check(returnType));

        var nestedArray = returnType.getObject(JmmAttributes.ARRAY_TYPE.ELEMENT_TYPE.getKey(), pt.up.fe.comp.jmm.ast.JmmNode.class);
        assertTrue("Second level should also be an array type", JmmKind.ARRAY_TYPE.check(nestedArray));

        var baseType = nestedArray.getObject(JmmAttributes.ARRAY_TYPE.ELEMENT_TYPE.getKey(), pt.up.fe.comp.jmm.ast.JmmNode.class);
        assertTrue("Innermost type should be a simple type", JmmKind.SIMPLE_TYPE.check(baseType));
        assertEquals("Innermost simple type should keep the primitive name", "int",
                baseType.get(JmmAttributes.SIMPLE_TYPE.NAME));
    }
}
