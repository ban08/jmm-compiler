/**
 * Copyright 2022 SPeCS.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.comp.cp3.core.jasmin.arrays;

import examples.Quicksort;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class ArrayLoadJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/arrays/load/";


    public ArrayLoadJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void ArrayLengthLocal() {
        var res = toJasmin("ArrayLengthLocal");
        var ret = res.invoke("getLocalLength", Integer.class);
        var expected = 5;
        assertEquals("method should return an array of size ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ArrayLengthParam() {
        var res = toJasmin("ArrayLengthParam");
        var expected = 105;
        var input = new int[expected];
        var ret = res.invoke("getParamLength", Integer.class, (Object) input);
        assertEquals("method should return an array of size ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ArrayLengthCall() {
        var res = toJasmin("ArrayLengthCall");
        var ret = res.invoke("getMethodReturnLength", Integer.class);
        var expected = 10;
        assertEquals("method should return an array of size ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ArrayAsParameter() {
        var res = toJasmin("ArrayAsParameter");
        var input = new int[10];
        var ret = res.invoke("method", int[].class, (Object) input);
        var expected = input;
        assertEquals("method should return the same array ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ArrayPos0Load() {
        var res = toJasmin("ArrayPos0Load");
        var input = new int[]{3, 4, 5, 67, 8};
        var ret = res.invoke("method", Integer.class, (Object) input);
        var expected = input[0];
        assertEquals("method should return the same array ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ArrayPosNLoad() {
        var res = toJasmin("ArrayPosNLoad");
        var input = new int[]{3, 4, 5, 67, 8};
        var method = res.getJavaMethod("method");
        for (int n = 0; n < input.length; n++) {
            var ret = res.invoke(method, Integer.class, input, n);
            var expected = input[n];
            assertEquals("method should return the value in position " + n + ", which is ${expected}", expected, ret.returnValue());
        }
    }

    @Test
    public void ArrayAsArgument() {
        var res = toJasmin("ArrayAsArgument");
        var input = new int[]{3, 4, 5, 67, 8};

        var ret = res.invoke("method", Integer.class, (Object) input);
        var expected = input[0];
        assertEquals("method should return ${expected}", expected, ret.returnValue());

    }

    @Test
    public void ArrayAccessSimple() {
        var res = toJasmin("ArrayAsArgument");
        var input = new int[]{3, 4, 5, 67, 8};

        var ret = res.invoke("method", Integer.class, (Object) input);
        var expected = input[0];
        assertEquals("method should return ${expected}", expected, ret.returnValue());

    }

    @Test
    public void ArrayPosNestedLoad() {
        var res = toJasmin("ArrayPosNestedLoad");
        var arr = new int[]{3, 4, 5, 67, 8};
        var pos = new int[]{4, 3, 2, 1, 0};
        var method = res.getJavaMethod("method");

        for (int n = 0; n < pos.length; n++) {
            var ret = res.invoke(method, Integer.class, arr, pos, n);
            var expected = arr[pos[n]];
            assertEquals("method should return the value in position " + n + ", which is ${expected}", expected, ret.returnValue());
        }

    }

    @Test
    public void ArrayPosCallIndex() {
        var res = toJasmin("ArrayPosCallIndex");
        var arr = new int[]{3, 4, 5, 67, 8};
        var divisor = arr.length;
        var dividends = new int[]{16, 3, 5, 60, 1, 7, 28};
        var method = res.getJavaMethod("method");
        for (int div : dividends) {
            var ret = res.invoke(method, Integer.class, arr, div, divisor);
            var expected = arr[div % divisor];
            assertEquals("method with inputs " + div + "," + divisor + " should return the value in position " + (div % divisor) + ", which is ${expected}", expected, ret.returnValue());
        }

    }


}
