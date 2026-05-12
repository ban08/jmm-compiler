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

import java.util.List;

@RunWith(Parameterized.class)
public class LogicalJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/entitiesandops/logical/";


    public LogicalJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void LessThanLiterals() {
        var res = toJasmin("LessThanLiterals");
        var expected = true;
        var ret = res.invoke("method", Boolean.class);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LessThanLiteralAndParam() {
        var res = toJasmin("LessThanLiteralAndParam");
        var param = -5;
        var expected = false;
        var ret = res.invoke("method", Boolean.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LessThanComplexCalc() {
        var res = toJasmin("LessThanComplexCalc");
        var param = -5;
        var expected = true;
        var ret = res.invoke("method", Boolean.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void SimpleUnaryNot() {
        var res = toJasmin("SimpleUnaryNot");
        var param = true;
        var expected = false;
        var ret = res.invoke("method", Boolean.class, param);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void NotAndLessThan() {
        var res = toJasmin("NotAndLessThan");
        var pairs = List.of(List.of(0, 0),
                List.of(0, 2), List.of(2, -2), List.of(-5, -3));
        for (var pair : pairs) {
            var ret = res.invoke("method", Boolean.class, pair.getFirst(), pair.getLast());
            boolean expected = !(pair.getFirst() < pair.getLast());
            assertEquals("!(" + pair.getFirst() + "<" + pair.getLast() + ") should return ${expected}", expected, ret.returnValue());
        }
    }

    @Test
    public void AndLiterals() {
        var res = toJasmin("AndLiterals");
        var expected = true;
        var ret = res.invoke("method", Boolean.class);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void AndParams() {
        var res = toJasmin("AndParams");
        var pairs = List.of(List.of(true, true), List.of(false, false), List.of(true, false), List.of(false, true));
        for (var pair : pairs) {
            var ret = res.invoke("method", Boolean.class, pair.getFirst(), pair.getLast());
            boolean expected = pair.getFirst() && pair.getLast();
            assertEquals(pair.getFirst() + " && " + pair.getLast() + " should return ${expected}", expected, ret.returnValue());
        }
    }

    @Test
    public void AndNested() {
        var res = toJasmin("AndNested");
        for (int i = 0; i < Math.pow(2, 5); i++) {
            boolean a = (i & 0b10000) != 0;
            boolean b = (i & 0b01000) != 0;
            boolean c = (i & 0b00100) != 0;
            boolean d = (i & 0b00010) != 0;
            boolean e = (i & 0b00001) != 0;
            boolean expected = a && b && c && d && e;
            var ret = res.invoke("method", Boolean.class, a, b, c, d, e);
            assertEquals(a + " && " + b + " && " + c + " && " + d + " && " + e + " should return ${expected}", expected, ret.returnValue());
        }
    }

    @Test
    public void AndLessThanNot() {
        var res = toJasmin("AndLessThanNot");
        var b = 2;
        var c = 3;
        for (int i = 0; i < Math.pow(2, 3); i++) {
            boolean a = (i & 0b100) != 0;
            boolean d = (i & 0b010) != 0;
            boolean e = (i & 0b001) != 0;
            boolean expected = a && b < c && (!d && e);
            var ret = res.invoke("method", Boolean.class, a, b, c, d, e);
            assertEquals(a + " && " + b + "<" + c + " && (!" + d + " && " + e + ") should return ${expected}", expected, ret.returnValue());
        }
    }


}
