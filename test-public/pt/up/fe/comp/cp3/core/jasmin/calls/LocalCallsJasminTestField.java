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

package pt.up.fe.comp.cp3.core.jasmin.calls;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class LocalCallsJasminTestField extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/calls/local/";


    public LocalCallsJasminTestField(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }


    @Test
    public void LocalCallExprStmtVoidStoreField() {
        var res = toJasmin("LocalCallExprStmtVoidStoreField");
        var instance = res.newInstance();
        res.invoke(instance, "method");
        var fieldValue = res.getFieldValue(instance, "field", Integer.class);
        var expected = 1;
        assertEquals("Method call on expression statement should update field value", expected, fieldValue);
    }

    @Test
    public void LocalCallExprStmtIntStoreField() {
        var res = toJasmin("LocalCallExprStmtIntStoreField");
        var instance = res.newInstance();
        res.invoke(instance, "method");
        var fieldValue = res.getFieldValue(instance, "field", Integer.class);
        var expected = 1;
        assertEquals("Method call on expression statement should update field value", expected, fieldValue);
    }


    @Test
    public void LocalCallGetField() {
        var res = toJasmin("LocalCallGetField");
        var instance = res.newInstance();
        var expected = -200;
        res.setFieldValue(instance, "field", expected);
        var ret = res.invoke(instance, "method", Integer.class);
        assertEquals("Method call should return ${expected}", expected, ret.returnValue());
    }


}
