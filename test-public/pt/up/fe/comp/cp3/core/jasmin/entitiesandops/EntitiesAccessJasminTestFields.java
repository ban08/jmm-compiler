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

package pt.up.fe.comp.cp3.core.jasmin.entitiesandops;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)

public class EntitiesAccessJasminTestFields extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/entitiesandops/entities/";

    public EntitiesAccessJasminTestFields(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void AccessField() {
        var res = toJasmin("AccessField");
        var instance = res.newInstance();
        int value = -20;
        res.setFieldValue(instance, "a", value);
        var ret2 = res.invoke(instance, "get", Integer.class);
        assertEquals("method get should return ${expected}", value, ret2.returnValue());
    }

    @Test
    public void AccessFieldShadowing() {
        var res = toJasmin("AccessFieldShadowing");
        var instance = res.newInstance();
        int value = -20;
        int shadowed = 5;
        res.setFieldValue(instance, "a", value);
        var ret2 = res.invoke(instance, "get", Integer.class, shadowed);
        assertEquals("method get should return ${expected}", shadowed, ret2.returnValue());
    }

}
