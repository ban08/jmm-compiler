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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class ArrayStoreJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/arrays/store/";


    public ArrayStoreJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }


    @Test
    public void ArrayStore() {
        var res = toJasmin("ArrayStore");
        var input = new int[]{3, 4, 5, 67, 8};

        res.invoke("method", (Object) input);
        var expected = 30;
        assertEquals("method should have changed first array position with value ${expected}", expected, input[0]);

    }


    @Test
    public void ArrayAccessSimple() {
        var res = toJasmin("ArrayAccessSimple");
        var arr = new int[]{3, 4, 5, 67, 8};
        var newValues = new int[]{4, 3, 2, 1, 0};
        var ret = res.invoke("method", Integer.class, arr, newValues);
        var expected = newValues[1];
        assertEquals("method should have returned ${expected}", expected, ret.returnValue());
        assertEquals("method should have updated pos 0 of array with value ${expected}", expected, arr[0]);
    }


    @Test
    public void ArrayStorePosValue() {
        var res = toJasmin("ArrayStorePosValue");
        var arr = new int[]{3, 4, 5, 67, 8};
        var newValues = new int[]{4, 3, 2, 1, 0};
        var method = res.getJavaMethod("method");

        for (int n = 0; n < newValues.length; n++) {
            var expected = newValues[n];
            res.invoke(method, arr, n, expected);
            assertEquals("method should have changed array in position " + n + " with value ${expected}", expected, arr[n]);
        }
    }

    @Test
    public void ArrayReferenceAssignment() {
        var res = toJasmin("ArrayReferenceAssignment");
        var arr = new int[]{3, 4, 5, 67, 8, 20};
        var expected = new int[]{20, 8, 67, 5, 4, 3};
        var method = res.getJavaMethod("method");

        for (int n = 0; n < arr.length / 2; n++) {
            int m = arr.length - n - 1;
            res.invoke(method, int[].class, arr, n, m);
            assertEquals("method should have switched pos " + n + " to position " + m, expected[n], arr[n]);
            assertEquals("method should have switched pos " + m + " to position " + n, expected[m], arr[m]);
        }
    }


    @Test
    public void ArrayAccessComplex() {
        var res = toJasmin("ArrayAccessComplex");
        var arr = new int[]{3, 4, 2, 67, 8, 342, 346, 756, 89, 234};
        var expectedRet = new int[]{3, 5, 5, 346, 89};
        var expectedSet = new int[]{3, 4, 5, 5, 5, 5, 5, 756, 89, 234};
        var method = res.getJavaMethod("method");
        for (int n = 0; n < arr.length / 2; n++) {
            var ret = res.invoke(method, Integer.class, arr, n);
            assertEquals("method should have returned ${expected}", expectedRet[n], ret.returnValue());
        }
        for (int n = 0; n < arr.length; n++) {
            assertEquals("array in position " + n + " should now be ${expected}", expectedSet[n], arr[n]);
        }

    }

    @Test
    public void ArrayAccessNested() {
        var res = toJasmin("ArrayAccessNested");
        var arr = new int[]{3, 4, 5, 67, 8};
        var newValues = new int[]{4, 3, 2, 1, 0};
        var ret = res.invoke("method", Integer.class, arr, newValues);
        var expectedReturn = arr[0];
        var expectedAssigned = 5;

        assertEquals("method should have returned ${expected}", expectedReturn, ret.returnValue());
        assertEquals("method should have updated pos " + newValues[0] + " of array with value ${expected}", expectedAssigned, arr[newValues[0]]);
    }


}
