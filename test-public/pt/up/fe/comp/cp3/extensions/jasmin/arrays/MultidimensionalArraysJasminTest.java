package pt.up.fe.comp.cp3.extensions.jasmin.arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class MultidimensionalArraysJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/arrays/multidim/";


    public MultidimensionalArraysJasminTest(BaseJasminTestEnv.InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void MultiDimInit() {
        var res = toJasmin("MultiDimInit");
        var ret = res.invoke("method", int[][].class);
        var expected_first_dim = 4;
        assertEquals("method should return an array with ${expected_first_dim} arrays inside", expected_first_dim, ret.returnValue().length);
        var expected_second_dim = 2;
        var actual_second_dim = Arrays.stream(ret.returnValue()).findFirst().map(a -> a.length).orElse(0);
        assertEquals("method should return an array with inner arrays of ${expected_second_dim} dimension", expected_second_dim, actual_second_dim);
    }

    @Test
    public void MultiDimAccess() {
        var res = toJasmin("MultiDimAccess");
        int[][][] method_arg = new int[7][6][3];
        method_arg[2][3][0] = 0;
        method_arg[2][3][1] = 1;
        method_arg[2][3][2] = 2;
        var ret = res.invoke("method", int[].class, (Object) method_arg);
        var expected = method_arg[2][3];
        assertEquals("method should return {expected}: the one dimension array inside the three dimension array", expected, ret.returnValue());
    }

    @Test
    public void MultiDimWrite() {
        var res = toJasmin("MultiDimWrite");
        int[][][] method_arg_a = new int[4][5][3];
        int[] method_arg_b = new int[]{10, 11, 12};
        var ret = res.invoke("method", int[][][].class, method_arg_a, method_arg_b);
        int expected_0_1_2 = 13;
        int actual_0_1_2 = ret.returnValue()[0][1][2];
        assertEquals("method should return an array with value '13' on position [0][1][2]", expected_0_1_2, actual_0_1_2);
        int expected_1_2_0 = 10;
        int actual_1_2_0 = ret.returnValue()[1][2][0];
        assertEquals("method should return an array with value '10' on position [1][2][0]", expected_1_2_0, actual_1_2_0);
        int expected_1_2_1 = 11;
        int actual_1_2_1 = ret.returnValue()[1][2][1];
        assertEquals("method should return an array with value '11' on position [1][2][1]", expected_1_2_1, actual_1_2_1);
        int expected_1_2_2 = 12;
        int actual_1_2_2 = ret.returnValue()[1][2][2];
        assertEquals("method should return an array with value '12' on position [1][2][2]", expected_1_2_2, actual_1_2_2);
    }
}
