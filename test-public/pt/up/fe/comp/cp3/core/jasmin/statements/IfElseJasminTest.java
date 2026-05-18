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

import examples.Auxi;
import examples.Pair;
import examples.inheritance.A;
import examples.inheritance.B;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

import java.util.List;

@RunWith(Parameterized.class)
public class IfElseJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/statements/ifelse/";


    public IfElseJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void IfSimpleElse() {
        var res = toJasmin("IfSimpleElse");
        var ret = res.invoke("method", Integer.class, true);
        assertEquals("method should return ${expected}", 1, ret.returnValue());
        ret = res.invoke("method", Integer.class, false);
        assertEquals("method should return ${expected}", 0, ret.returnValue());
    }

    @Test
    public void IfNested() {
        var res = toJasmin("IfNested");
        var ret = res.invoke("method", Integer.class, true, true);
        assertEquals("method should return ${expected}", 2, ret.returnValue());
        ret = res.invoke("method", Integer.class, true, false);
        assertEquals("method should return ${expected}", 1, ret.returnValue());
        ret = res.invoke("method", Integer.class, false, false);
        assertEquals("method should return ${expected}", 0, ret.returnValue());
    }

    @Test
    public void IfWithNot() {
        var res = toJasmin("IfWithNot");
        var ret = res.invoke("method", Integer.class, true);
        assertEquals("method should return ${expected}", 0, ret.returnValue());
        ret = res.invoke("method", Integer.class, false);
        assertEquals("method should return ${expected}", 1, ret.returnValue());
    }

    @Test
    public void IfWithLessThan() {
        var res = toJasmin("IfWithLessThan");
        var ret = res.invoke("method", Integer.class, 7);
        assertEquals("method should return ${expected}", 1, ret.returnValue());
        ret = res.invoke("method", Integer.class, 11);
        assertEquals("method should return ${expected}", 0, ret.returnValue());
    }

    @Test
    public void IfWithAnd() {
        var res = toJasmin("IfWithAnd");
        var ret = res.invoke("method", Integer.class, true, true);
        assertEquals("method should return ${expected}", 1, ret.returnValue());
        ret = res.invoke("method", Integer.class, true, false);
        assertEquals("method should return ${expected}", 0, ret.returnValue());
        ret = res.invoke("method", Integer.class, false, false);
        assertEquals("method should return ${expected}", 0, ret.returnValue());
    }

}
