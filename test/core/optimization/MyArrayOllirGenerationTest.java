package core.optimization;

import org.junit.Assert;
import org.junit.Test;
import org.specs.comp.ollir.ArrayOperand;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.NewInstruction;

import static core.optimization.MyOllirTestSupport.assertNoErrors;
import static core.optimization.MyOllirTestSupport.toOllir;

public class MyArrayOllirGenerationTest {

    @Test
    public void lowersArrayInitializerIntoAllocationAndStores() {
        var result = toOllir("""
                package core.optimization;

                class ArrayInit {
                    int[] init() {
                        int[] values;
                        values = new int[] {1, 2, 3};
                        return values;
                    }
                }
                """);

        assertNoErrors(result);

        var method = result.getOllirClass().getMethods().stream()
                .filter(candidate -> candidate.getMethodName().equals("init"))
                .findFirst()
                .orElseThrow();

        long newArrays = method.getInstructions().stream()
                .filter(AssignInstruction.class::isInstance)
                .map(AssignInstruction.class::cast)
                .filter(assign -> assign.getRhs() instanceof NewInstruction)
                .count();

        long arrayStores = method.getInstructions().stream()
                .filter(AssignInstruction.class::isInstance)
                .map(AssignInstruction.class::cast)
                .filter(assign -> assign.getDest() instanceof ArrayOperand)
                .count();

        Assert.assertTrue("Array initializer should allocate a new array", newArrays >= 1);
        Assert.assertEquals("Array initializer should lower each element into a store", 3, arrayStores);
    }

    @Test
    public void lowersMultidimensionalArraysIntoStoresAndReads() {
        var result = toOllir("""
                package core.optimization;

                class MultiArray {
                    int method(int n, int m) {
                        int[][] matrix;
                        int value;
                        matrix = new int[n][m];
                        matrix[0][1] = 7;
                        value = matrix[0][1];
                        return value;
                    }
                }
                """);

        assertNoErrors(result);

        var method = result.getOllirClass().getMethods().stream()
                .filter(candidate -> candidate.getMethodName().equals("method"))
                .findFirst()
                .orElseThrow();

        var assigns = method.getInstructions().stream()
                .filter(AssignInstruction.class::isInstance)
                .map(AssignInstruction.class::cast)
                .toList();

        long arrayStores = assigns.stream()
                .filter(assign -> assign.getDest() instanceof ArrayOperand)
                .count();

        long arrayReads = assigns.stream()
                .flatMap(assign -> assign.getRhs().getChildren().stream())
                .filter(ArrayOperand.class::isInstance)
                .count();

        Assert.assertTrue("Multidimensional array lowering should emit stores into array elements", arrayStores >= 2);
        Assert.assertTrue("Multidimensional array lowering should materialize at least one array read", arrayReads >= 1);
    }
}
