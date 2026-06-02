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

package pt.up.fe.comp.cp3.extensions.jasmin.statements;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

import java.util.List;

@RunWith(Parameterized.class)
public class DoWhileLoopJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/statements/doloop";

    public DoWhileLoopJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void DoWhileSimple() {
        var res = toJasmin("DoWhileSimple");

        var tests = List.of(List.of(9, 9), List.of(221, 221), List.of(0, 1), List.of(-1, 1), List.of(5, 5));
        for (var test : tests) {
            var limit = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, limit);
            assertEquals("For Loop with limit " + limit + " should return ${expected}", expected, ret.returnValue());
        }
    }

    @Test
    public void DoWhileWithWhile() {
        var res = toJasmin("DoWhileWithWhile");

        var tests = List.of(List.of(9, 45), List.of(221, 24531), List.of(0, 0), List.of(-1, 0), List.of(5, 15));
        for (var test : tests) {
            var limit = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, limit);
            assertEquals("For Loop with limit " + limit + " should return ${expected}", expected, ret.returnValue());
        }
    }

    @Test
    public void DoWhileNested() {
        var res = toJasmin("DoWhileNested");

        var tests = List.of(List.of(9, 189), List.of(221, 121771), List.of(0, 1), List.of(-1, 1), List.of(5, 55));
        for (var test : tests) {
            var limit = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, limit);
            assertEquals("For Loop with limit " + limit + " should return ${expected}", expected, ret.returnValue());
        }
    }


    @Test
    public void DoWhileAsWhile() {
        var res = toJasmin("DoWhileAsWhile");

        var tests = List.of(List.of(9, 9), List.of(221, 221), List.of(0, 0), List.of(-1, 0), List.of(5, 5));
        for (var test : tests) {
            var limit = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, limit);
            assertEquals("For Loop with limit " + limit + " should return ${expected}", expected, ret.returnValue());
        }
    }
}