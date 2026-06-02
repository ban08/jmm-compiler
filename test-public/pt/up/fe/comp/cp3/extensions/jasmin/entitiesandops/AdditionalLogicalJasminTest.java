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

package pt.up.fe.comp.cp3.extensions.jasmin.entitiesandops;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

import java.util.List;

@RunWith(Parameterized.class)
public class AdditionalLogicalJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/entitiesandops/addlogic";

    public AdditionalLogicalJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void GreaterThanLiterals() {
        var res = toJasmin("GreaterThanLiterals");

        var ret = res.invoke("method", Boolean.class);
        assertEquals(1 + " > " + 2 + " ? ${actual} (should be ${expected})", false, ret.returnValue());

    }

    @Test
    public void GreaterThanLiteralAndParam() {
        var res = toJasmin("GreaterThanLiteralAndParam");

        var tests = List.of(List.of(1, false), List.of(2, false), List.of(-1, true), List.of(-2, true));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Boolean.class, a);
            assertEquals(1 + " > " + a + " ? ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void GreaterThanComplexCalc() {
        var res = toJasmin("GreaterThanComplexCalc");

        var tests = List.of(List.of(1, false), List.of(2, false), List.of(-1, false), List.of(-2, true));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Boolean.class, a);
            assertEquals(a + " / 2 > (" + a + "/2 * 3 + 1) ? ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void NotAndGreaterThan() {
        var res = toJasmin("NotAndGreaterThan");

        var tests = List.of(List.of(1, 2, true), List.of(2, 1, false), List.of(4, 4, true), List.of(-2, -2, true));
        for (var test : tests) {
            var a = test.getFirst();
            var b = test.get(1);
            var expected = test.getLast();
            var ret = res.invoke("method", Boolean.class, a, b);
            assertEquals(a + " <= " + b + " ? ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void Equals() {
        var res = toJasmin("Equals");

        var tests = List.of(List.of(1, 2, false), List.of(2, 1, false), List.of(4, 4, true), List.of(-2, -2, true));
        for (var test : tests) {
            var a = test.getFirst();
            var b = test.get(1);
            var expected = test.getLast();
            var ret = res.invoke("method", Boolean.class, a, b);
            assertEquals(a + " == " + b + " ? ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void NotEquals() {
        var res = toJasmin("NotEquals");

        var tests = List.of(List.of(1, 2, true), List.of(2, 1, true), List.of(4, 4, false), List.of(-2, -2, false));
        for (var test : tests) {
            var a = test.getFirst();
            var b = test.get(1);
            var expected = test.getLast();
            var ret = res.invoke("method", Boolean.class, a, b);
            assertEquals(a + " == " + b + " ? ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void LessOrEquals() {
        var res = toJasmin("LessOrEquals");

        var tests = List.of(List.of(1, 2, true), List.of(2, 1, false), List.of(4, 4, true), List.of(-2, -2, true));
        for (var test : tests) {
            var a = test.getFirst();
            var b = test.get(1);
            var expected = test.getLast();
            var ret = res.invoke("method", Boolean.class, a, b);
            assertEquals(a + " <= " + b + " ? ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void GreaterOrEquals() {
        var res = toJasmin("GreaterOrEquals");

        var tests = List.of(List.of(1, 2, false), List.of(2, 1, true), List.of(4, 4, true), List.of(-2, -2, true));
        for (var test : tests) {
            var a = test.getFirst();
            var b = test.get(1);
            var expected = test.getLast();
            var ret = res.invoke("method", Boolean.class, a, b);
            assertEquals(a + " <= " + b + " ? ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void OrLiterals() {
        var res = toJasmin("OrLiterals");
        var ret = res.invoke("method", Boolean.class);
        assertEquals("false || true ? ${actual} (should be ${expected})", true, ret.returnValue());
    }

    @Test
    public void OrParams() {
        var res = toJasmin("OrParams");

        var tests = List.of(List.of(true, true, true), List.of(true, false, true), List.of(false, true, true), List.of(false, false, false));
        for (var test : tests) {
            var a = test.getFirst();
            var b = test.get(1);
            var expected = test.getLast();
            var ret = res.invoke("method", Boolean.class, a, b);
            assertEquals(a + " || " + b + " ? ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void OrNested() {
        var res = toJasmin("OrNested");
        for (int i = 0; i < Math.pow(2, 5); i++) {
            boolean a = (i & 0b10000) != 0;
            boolean b = (i & 0b01000) != 0;
            boolean c = (i & 0b00100) != 0;
            boolean d = (i & 0b00010) != 0;
            boolean e = (i & 0b00001) != 0;
            boolean expected = a || b || c || d || e;
            var ret = res.invoke("method", Boolean.class, a, b, c, d, e);
            assertEquals(a + " || " + b + " || " + c + " || " + d + " || " + e + " ? ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void OrLessThanNot() {
        var res = toJasmin("OrLessThanNot");

        var tests = List.of(
                List.of(true, 0, 0, true, false, true),
                List.of(false, 2, 4, true, false, true),
                List.of(false, 4, 2, false, false, true),
                List.of(false, 4, 2, true, true, true),
                List.of(false, 4, 2, true, false, false)
        );
        for (var test : tests) {
            var a = test.getFirst();
            var b = test.get(1);
            var c = test.get(2);
            var d = test.get(3);
            var e = test.get(4);
            var expected = test.getLast();
            var ret = res.invoke("method", Boolean.class, a, b, c, d, e);
            assertEquals("Complex expression with args {" + a + "," + b + "," + c + "," + d + "," + e + "} must be ${expected}", expected, ret.returnValue());
        }
    }

    @Test
    public void MultipleLogicals() {
        var res = toJasmin("MultipleLogicals");

        var tests = List.of(
                List.of(5, 3, 0, 0, 0, true),
                List.of(5, 10, 4, 8, 0, true),
                List.of(5, 10, 5, 6, 0, true),
                List.of(5, 10, 20, 5, 1, true),
                List.of(5, 10, 6, 5, 1, true),
                List.of(5, 10, 20, 2, 0, false),
                List.of(5, 10, 20, 5, 0, false)
        );
        for (var test : tests) {
            var a = test.getFirst();
            var b = test.get(1);
            var c = test.get(2);
            var d = test.get(3);
            var e = test.get(4);
            var expected = test.getLast();
            var ret = res.invoke("method", Boolean.class, a, b, c, d, e);
            assertEquals("Complex expression with args {" + a + "," + b + "," + c + "," + d + "," + e + "} must be ${expected}", expected, ret.returnValue());
        }
    }

}