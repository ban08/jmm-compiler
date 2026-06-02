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
public class AdditionalArithmeticJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/entitiesandops/addarith";

    public AdditionalArithmeticJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void Remainder() {
        var res = toJasmin("Remainder");

        var tests = List.of(List.of(1, 2, 1), List.of(2, 1, 0), List.of(4, 2, 0), List.of(5, 2, 1), List.of(9, 5, 4));
        for (var test : tests) {
            var a = test.getFirst();
            var b = test.get(1);
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a, b);
            assertEquals(a + " % " + b + " = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void RemMulOrder() {
        var res = toJasmin("RemMulOrder");

        var tests = List.of(List.of(2, 1), List.of(3, 0), List.of(4, 2));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("2 * " + a + " % 3 = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void RemSubOrder() {
        var res = toJasmin("RemSubOrder");

        var tests = List.of(List.of(2, -1), List.of(3, 1), List.of(5, -1), List.of(4, 0));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("1 - " + a + " % 3 = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void PositiveLiteral() {
        var res = toJasmin("PositiveLiteral");
        var expected = 1;
        var ret = res.invoke("method", Integer.class);
        assertEquals("Method should return ${expected})", expected, ret.returnValue());
    }

    @Test
    public void PositiveVar() {
        var res = toJasmin("PositiveVar");
        var tests = List.of(List.of(2, 2), List.of(-3, -3), List.of(5, 5), List.of(-1, -1));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("+ " + a + " = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void PositiveParenthesis() {
        var res = toJasmin("PositiveParenthesis");
        var tests = List.of(List.of(2, 1), List.of(-3, -4), List.of(5, 4), List.of(-1, -2));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("+ (" + a + "-1) = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void PositiveCall() {
        var res = toJasmin("PositiveCall");
        var tests = List.of(List.of(2, 2), List.of(-3, -3), List.of(5, 5), List.of(-1, -1));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("+ foo(" + a + ") = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void NegativeLiteral() {
        var res = toJasmin("NegativeLiteral");
        var expected = -1;
        var ret = res.invoke("method", Integer.class);
        assertEquals("Method should return ${expected})", expected, ret.returnValue());
    }

    @Test
    public void NegativeVar() {
        var res = toJasmin("NegativeVar");
        var tests = List.of(List.of(2, -2), List.of(-3, 3), List.of(5, -5), List.of(-1, 1));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("- " + a + " = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void NegativeParenthesis() {
        var res = toJasmin("NegativeParenthesis");
        var tests = List.of(List.of(2, -3), List.of(-3, 2), List.of(5, -6), List.of(-1, 0));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("- (" + a + "-1) = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void NegativeCall() {
        var res = toJasmin("NegativeCall");
        var tests = List.of(List.of(2, -2), List.of(-3, 3), List.of(5, -5), List.of(-1, 1));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("- foo(" + a + ") = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void IncrementSimple() {
        var res = toJasmin("IncrementSimple");
        var tests = List.of(List.of(2, 3), List.of(-3, -2), List.of(5, 6), List.of(-1, 0));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("++ " + a + " = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void IncrementOnReturn() {
        var res = toJasmin("IncrementOnReturn");
        var tests = List.of(List.of(2, 3), List.of(-3, -2), List.of(5, 6), List.of(-1, 0));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("return ++ " + a + "; returns ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void IncrementOnParenthesis() {
        var res = toJasmin("IncrementOnParenthesis");
        var tests = List.of(List.of(2, 3), List.of(-3, -2), List.of(5, 6), List.of(-1, 0));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("++ (" + a + ") = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }


    @Test
    public void IncrementOnCall() {
        var res = toJasmin("IncrementOnCall");
        var tests = List.of(List.of(2, 4), List.of(-3, -1), List.of(5, 7), List.of(-1, 1));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("foo(++ " + a + ") = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void DecrementSimple() {
        var res = toJasmin("DecrementSimple");
        var tests = List.of(List.of(2, 1), List.of(-3, -4), List.of(5, 4), List.of(-1, -2));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("-- " + a + " = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }


    @Test
    public void DecrementOnReturn() {
        var res = toJasmin("DecrementOnReturn");
        var tests = List.of(List.of(2, 1), List.of(-3, -4), List.of(5, 4), List.of(-1, -2));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("return -- " + a + "; returns ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void DecrementOnParenthesis() {
        var res = toJasmin("DecrementOnParenthesis");
        var tests = List.of(List.of(2, 1), List.of(-3, -4), List.of(5, 4), List.of(-1, -2));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("-- (" + a + ") = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

    @Test
    public void DecrementOnCall() {
        var res = toJasmin("DecrementOnCall");
        var tests = List.of(List.of(2, 0), List.of(-3, -5), List.of(5, 3), List.of(-1, -3));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("foo(-- " + a + ") = ${actual} (should be ${expected})", expected, ret.returnValue());
        }
    }

}