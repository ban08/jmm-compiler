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

package pt.up.fe.comp.cp3.core.jasmin.statements;

import examples.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class ReturnJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/statements/returnstmt/";


    public ReturnJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void ReturnInt() {
        var res = toJasmin("ReturnInt");
        var expected = 7;
        var ret = res.invoke("method", Integer.class);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ReturnVoid() {
        var res = toJasmin("ReturnVoid");
        res.invoke("method");
    }

    @Test
    public void VoidNoReturn() {
        var res = toJasmin("VoidNoReturn");
        res.invoke("method");
    }

    @Test
    public void ReturnThis() {
        var res = toJasmin("ReturnThis");
        var instance = res.newInstance();
        var ret = res.invoke(instance, "method", res.clazz());
        assertEquals("method should return ${expected}", instance, ret.returnValue());
    }

    @Test
    public void ReturnString() {
        var res = toJasmin("ReturnString");
        var expected = "Hello World!";
        var ret = res.invoke("method", String.class, expected);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ReturnPair() {
        var res = toJasmin("ReturnPair");
        var expected = new Pair(0, 10);
        var ret = res.invoke("method", Pair.class, expected);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }
}
