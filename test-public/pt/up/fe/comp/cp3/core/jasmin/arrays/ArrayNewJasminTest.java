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
public class ArrayNewJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/arrays/newinst/";


    public ArrayNewJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void ArrayInitLiteral() {
        var res = toJasmin("ArrayInitLiteral");
        var ret = res.invoke("init", int[].class);
        var expected = 5;
        assertEquals("method should return an array of size ${expected}", expected, ret.returnValue().length);
    }

    @Test
    public void ArrayInitVariable() {
        var res = toJasmin("ArrayInitVariable");
        var expected = 123;
        var ret = res.invoke("init", int[].class, expected);
        assertEquals("method should return an array of size ${expected}", expected, ret.returnValue().length);
    }

    @Test
    public void ArrayInitExpression() {
        var res = toJasmin("ArrayInitExpression");
        var input = 123;
        var expected = input * 2 + 1;
        var ret = res.invoke("init", int[].class, input);
        assertEquals("method should return an array of size ${expected}", expected, ret.returnValue().length);
    }

    @Test
    public void ArrayInitWithCall() {
        var res = toJasmin("ArrayInitWithCall");
        var input = 123;
        var expected = input * 2;
        var ret = res.invoke("init", int[].class, input);
        assertEquals("method should return an array of size ${expected}", expected, ret.returnValue().length);
        input = -2;
        expected = 1;
        ret = res.invoke("init", int[].class, input);
        assertEquals("method should return an array of size ${expected}", expected, ret.returnValue().length);
    }

    @Test
    public void ArrayInitAsCallArg() {
        var res = toJasmin("ArrayInitAsCallArg");
        var input = 123;
        var expected = true;
        var ret = res.invoke("sort", Boolean.class, new Quicksort(), input);
        assertEquals("method should return an array of size ${expected}", expected, ret.returnValue());

    }

}
