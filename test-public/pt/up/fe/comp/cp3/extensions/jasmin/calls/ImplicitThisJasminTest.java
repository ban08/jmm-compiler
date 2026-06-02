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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

import java.util.Date;
import java.util.List;

@RunWith(Parameterized.class)
public class ImplicitThisJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/calls/implthis";

    public ImplicitThisJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void VirtualCallImplicitThis() {
        var res = toJasmin("VirtualCallImplicitThis");
        var tests = List.of(List.of(2, 5), List.of(-3, 0), List.of(5, 8), List.of(-1, 2));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("Implicit this should return ${expected}", expected, ret.returnValue());
        }
    }

    @Test
    public void VirtualCallImplicitThisChain() {
        var res = toJasmin("VirtualCallImplicitThisChain");
        var tests = List.of(List.of(2, 7), List.of(-3, 2), List.of(5, 10), List.of(-1, 4));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("Implicit this chained should return ${expected}", expected, ret.returnValue());
        }
    }

    @Test
    public void VirtualCallImplicitThisOnCondition() {
        var res = toJasmin("VirtualCallImplicitThisOnCondition");
        var tests = List.of(List.of(2, 3), List.of(0, 2), List.of(1, 3), List.of(-1, 3));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var ret = res.invoke("method", Integer.class, a);
            assertEquals("Implicit this inside an if condition should return ${expected}", expected, ret.returnValue());
        }
    }

    @Test
    public void VirtualCallImplicitThisOnSuper() {
        var res = toJasmin("VirtualCallImplicitThisOnSuper", Auxi.class);
        var tests = List.of(List.of(2, 3), List.of(0, 2), List.of(1, 3), List.of(-1, 3));
        for (var test : tests) {
            var b = test.getFirst();
            var c = test.getLast();
            var expected = b + c;
            var ret = res.invoke("method", Integer.class, b, c);
            assertEquals("Implicit this calling a super method should return ${expected}", expected, ret.returnValue());
        }
    }

}