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

import examples.Quicksort;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class IfWithoutElseJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/statements/ifnoelse";

    public IfWithoutElseJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void IfSimple() {
        var res = toJasmin("IfSimple");
        var ret = res.invoke("method", Integer.class, true);
        assertEquals("If with true should return ${expected}", 1, ret.returnValue());
        ret = res.invoke("method", Integer.class, false);
        assertEquals("If with true should return ${expected}", 0, ret.returnValue());
    }


    @Test
    public void IfNested() {
        var res = toJasmin("IfNested");
        var ret = res.invoke("method", Integer.class, true, false);
        assertEquals("If with true,false should return ${expected}", 1, ret.returnValue());
        ret = res.invoke("method", Integer.class, true, true);
        assertEquals("If with true,true should return ${expected}", 2, ret.returnValue());
        ret = res.invoke("method", Integer.class, false, true);
        assertEquals("If with false,<true|false> should return ${expected}", 0, ret.returnValue());
    }

}