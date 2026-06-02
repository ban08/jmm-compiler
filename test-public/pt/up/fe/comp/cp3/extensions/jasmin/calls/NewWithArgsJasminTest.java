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

package pt.up.fe.comp.cp3.extensions.jasmin.calls;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

import java.util.Date;
import java.util.List;

@RunWith(Parameterized.class)
public class NewWithArgsJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/calls/newwargs";

    public NewWithArgsJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void InstantiateWithArgs() {
        var res = toJasmin("InstantiateWithArgs");

        var tests = List.of(List.of(1999, 2, 1), List.of(2025, 12, 5), List.of(2026, 12, 31), List.of(999, 9, 9));
        for (var test : tests) {
            var y = test.getFirst();
            var m = test.get(1);
            var d = test.getLast();
            var ret = res.invoke("method", Date.class, y, m, d);
            var expected = new Date(y, m, d);
            assertEquals("Should return date: ${expected}", expected.toString(), ret.returnValue().toString());
        }
    }

    @Test
    public void InstantiateJavaLangWithArgs() {
        var res = toJasmin("InstantiateJavaLangWithArgs");

        var tests = List.of("hello", "world", "how", "are", "you", "?", "", " ", "\n", "\t");
        for (var test : tests) {
            var ret = res.invoke("method", String.class, test);
            assertEquals("Should return a string: ${expected}", test, ret.returnValue());
            assertTrue("Returned string should not be the same instance", test != ret.returnValue());
        }
    }

}