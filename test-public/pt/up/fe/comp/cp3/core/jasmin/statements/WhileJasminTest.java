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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class WhileJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/statements/whileloop/";


    public WhileJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void WhileSimple() {
        var res = toJasmin("WhileSimple");
        var ret = res.invoke("method", Integer.class, 10);
        assertEquals("method should return ${expected}", 20, ret.returnValue());
    }

    @Test
    public void WhileNested() {
        var res = toJasmin("WhileNested");
        var ret = res.invoke("method", Integer.class, 10);
        assertEquals("method should return ${expected}", 90, ret.returnValue());
    }

    @Test
    public void WhileComplexCondition() {
        var res = toJasmin("WhileComplexCondition");
        var ret = res.invoke("method", Integer.class, 10, true);
        assertEquals("method should return ${expected}", 20, ret.returnValue());
        ret = res.invoke("method", Integer.class, 10, false);
        assertEquals("method should return ${expected}", 0, ret.returnValue());
    }

    @Test
    public void WhileIf() {
        var res = toJasmin("WhileIf");
        var ret = res.invoke("method", Integer.class, 2);
        assertEquals("method should return ${expected}", 0, ret.returnValue());
        ret = res.invoke("method", Integer.class, 5);
        assertEquals("method should return ${expected}", 6, ret.returnValue());
        ret = res.invoke("method", Integer.class, 10);
        assertEquals("method should return ${expected}", 20, ret.returnValue());
    }

}
