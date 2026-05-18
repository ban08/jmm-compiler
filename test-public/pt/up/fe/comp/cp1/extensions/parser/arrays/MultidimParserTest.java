// java
package pt.up.fe.comp.cp1.extensions.parser.arrays;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.*;

public class MultidimParserTest extends JmmTestEnv {


    public MultidimParserTest() {
        super("", "");
    }

    @Test
    public void testIntTwoDimArrayLocalVarType() {
        setDescription("Parse int[ ][ ] as a type for local variable declaration");
        parseSnippet("void foo (){int[][] a;}", METHOD);
    }

    @Test
    public void testIntTwoDimArrayTypeMethodReturn() {
        setDescription("Parse int[ ][ ] as a return type for a method declaration");
        parseSnippet("int[][] foo (){return a;}", METHOD);
    }

    @Test
    public void testIntTwoDimArrayTypeField() {
        setDescription("Parse int[ ][ ] as a type for a field declaration");
        parseSnippet("class Test { int[][] a; }", CLASS);
    }

    @Test
    public void testIntTwoDimArrayTypeMethodParam() {
        setDescription("Parse int[ ][ ] as a type for a method parameter");
        parseSnippet("void foo (int[][] a) {}", METHOD);
    }

    @Test
    public void test5DimArrayType() {
        setDescription("Parse int[ ][ ][ ][ ][ ] as a type for a local variable declaration");
        parseSnippet("void foo (){int[][][][][] a;}", METHOD);
    }

    @Test
    public void testNewIntTwoDimFixedSizes() {
        setDescription("Parse new int[2][3] multidimensional array creation");
        parseSnippet("void foo(){ int[][] a; a = new int[2][3];}", METHOD);
    }

    @Test
    public void testNewIntTwoDimWithExprSize() {
        setDescription("Parse new int[i+1][j] where the first dimension is an expression");
        parseSnippet("void foo(){ int[][] a; a = new int[i + 1][j];}", METHOD);
    }

    @Test
    public void testNewInt5DimWithExprSize() {
        setDescription("Parse new int[i][j][k][l][m] where all dimensions are expressions");
        parseSnippet("void foo(){ int[][][][][] a; a = new int[i][j][k][l][m];}", METHOD);
    }

    @Test
    public void testMultidimArrayReadSimple() {
        setDescription("Parse read access to a multidimensional array element (a[1][2])");
        parseSnippet("void foo(){ int[][] a; i = a[1][2];}", METHOD);
    }

    @Test
    public void testMultidimArrayReadWithExprIndices() {
        setDescription("Parse read access with expression indices (a[i+1][j*2])");
        parseSnippet("void foo(){ int[][] a; i = a[i + 1][j * 2];}", METHOD);
    }

    @Test
    public void testMultidimArrayReadWithNestedIndexExpr() {
        setDescription("Parse read access where an index is itself an array access (a[b[2]][c])");
        parseSnippet("void foo(){ int[][] a; i = a[b[2]][c];}", METHOD);
    }

    @Test
    public void test5DimArrayReadWithExprIndices() {
        setDescription("Parse read access to a 5-dimensional array with expression indices (a[i][j][k][l][m])");
        parseSnippet("void foo(){ int[][][][][] a; i = a[i][j][k][l][m];}", METHOD);
    }

    @Test
    public void testMultidimArrayWriteSimple() {
        setDescription("Parse write (assignment) to a multidimensional array element (a[1][2] = 5)");
        parseSnippet("void foo(){ a[1][2] = 5;}", METHOD);
    }

    @Test
    public void testMultidimArrayWriteWithExpr() {
        setDescription("Parse write to multidimensional array where value is an expression (a[i][j] = b + 1)");
        parseSnippet("void foo(){ a[i][j] = b + 1;}", METHOD);
    }

    @Test
    public void testAssignmentUsingMultidimRead() {
        setDescription("Parse assignment where the right-hand side is a multidimensional read (x = a[0][1])");
        parseSnippet("void foo(){ int[][][][][] a; x = a[0][1];}", METHOD);
    }

    @Test
    public void test5DimArrayWriteWithExpr() {
        setDescription("Parse write to a 5-dimensional array where value is an expression (a[i][j][k][l][m] = b + 1)");
        parseSnippet("void foo(){ a[i][j][k][l][m] = b + 1;}", METHOD);
    }
}
