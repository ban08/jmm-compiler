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

package pt.up.fe.comp.cp3.core.jasmin.entitiesandops;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class ArithmeticJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/entitiesandops/arithmetic/";


    public ArithmeticJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void AddLiterals() {
        var res = toJasmin("AddLiterals");
        var expected = 3;
        var ret = res.invoke("method", Integer.class);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void AddLiteralWithParam() {
        var res = toJasmin("AddLiteralWithParam");
        var param = -5;
        var expected = -4;
        var ret = res.invoke("method", Integer.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void AddSubOrder() {
        var res = toJasmin("AddSubOrder");
        var param = -5;
        var expected = -7;
        var ret = res.invoke("method", Integer.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void MultipleAdds() {
        var res = toJasmin("MultipleAdds");
        var param = 1;
        var expected = 56;
        var ret = res.invoke("method", Integer.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void MultipleAddSubs() {
        var res = toJasmin("MultipleAddSubs");
        var param = 1;
        var expected = 2;
        var ret = res.invoke("method", Integer.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void SubLiteralWithParam() {
        var res = toJasmin("SubLiteralWithParam");
        var param = -5;
        var expected = 6;
        var ret = res.invoke("method", Integer.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void MultLiteralWithParam() {
        var res = toJasmin("MultLiteralWithParam");
        var param = -5;
        var expected = -5;
        var ret = res.invoke("method", Integer.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void DivLiteralWithParam() {
        var res = toJasmin("DivLiteralWithParam");
        var param = -5;
        var expected = -2;
        var ret = res.invoke("method", Integer.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void AddMulOrder() {
        var res = toJasmin("AddMulOrder");
        var param = 2;
        var expected = 7;
        var ret = res.invoke("method", Integer.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void MultipleMultDivs() {
        var res = toJasmin("MultipleMultDivs");
        var param = 10;
        var expected = 207;
        var ret = res.invoke("method", Integer.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void MultipleArithmeticLevels() {
        var res = toJasmin("MultipleArithmeticLevels");
        var param = 5;
        var expected = 24;
        var ret = res.invoke("method", Integer.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void SimpleParenthesis() {
        var res = toJasmin("SimpleParenthesis");
        var expected = -3;
        var ret = res.invoke("method", Integer.class);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void NestedParenthesis() {
        var res = toJasmin("NestedParenthesis");
        var expected = 13;
        var ret = res.invoke("method", Integer.class);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ComplexNestedParenthesis() {
        var res = toJasmin("ComplexNestedParenthesis");
        var param = 5;
        var expected = 1;
        var ret = res.invoke("method", Integer.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void MultipleArithmeticWithParenthesis() {
        var res = toJasmin("MultipleArithmeticWithParenthesis");
        var param = 5;
        var expected = -168;
        var ret = res.invoke("method", Integer.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }
}
