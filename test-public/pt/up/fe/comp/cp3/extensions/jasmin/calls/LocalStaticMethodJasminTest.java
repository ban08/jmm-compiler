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

import examples.Auxi;
import examples.foo.bar.A;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

import java.util.List;

@RunWith(Parameterized.class)
public class LocalStaticMethodJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/calls/localstatic";

    public LocalStaticMethodJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void LocalStaticMethodCallVoid() {
        var res = toJasmin("LocalStaticMethodCallVoid");
        var tests = List.of(2, 5, -3, 0, 5, 8, -1, 2);
        for (var test : tests) {
            var expected = A.bar(test > 0);
            var ret = res.invoke("method", String.class, test);
            assertEquals("Message should be: ${expected}", expected, ret.returnValue());
        }
    }

    @Test
    public void LocalStaticMethodCallRetInt() {
        var res = toJasmin("LocalStaticMethodCallRetInt");
        var tests = List.of(2, 5, -3, 0, 5, 8, -1, 2);
        for (var test : tests) {
            var expected = -test;
            var ret = res.interact("method", Integer.class, "" + test);
            assertEquals("Call should return: ${expected}", expected, ret.returnValue());
        }
    }

    @Test
    public void LocalStaticMethodCallExprStmtRetInt() {
        var res = toJasmin("LocalStaticMethodCallExprStmtRetInt");
        var tests = List.of(2, 5, -3, 0, 5, 8, -1, 2);
        for (var test : tests) {
            var expected = 2 - test;
            var ret = res.interact("method", Integer.class, "" + test);
            assertEquals("Call should output to console: ${expected}", expected, ret.outputText().trim());
        }
    }


}