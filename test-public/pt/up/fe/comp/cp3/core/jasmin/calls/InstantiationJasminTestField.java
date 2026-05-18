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

import examples.Quicksort;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class InstantiationJasminTestField extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/calls/instantiation/";


    public InstantiationJasminTestField(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void InstantiateAndStoreInField() {
        var res = toJasmin("InstantiateAndStoreInField");
        var instance = res.newInstance();
        res.invoke(instance, "method"); //or res.clazz()
        var fieldValue = res.getFieldValue(instance, "other", res.clazz());
        assertNotNull("Field should not be null", fieldValue);
        assert fieldValue != null;
        assertEquals("Field should be an instance of ${expected}", res.clazz(), fieldValue.getClass());
    }

}
