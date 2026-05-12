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

import examples.Quicksort;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class AssignmentsJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/entitiesandops/assignments/";

    public AssignmentsJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }


    @Test
    public void AssignParam() {
        var res = toJasmin("AssignParam");
        var oldValue = 2;
        var retValue = 0;
        var ret = res.invoke("method", Integer.class, oldValue);

        assertEquals("method should return ${expected}", retValue, ret.returnValue());
    }

    @Test
    public void AssignLocalVariable() {
        var res = toJasmin("AssignLocalVariable");
        var retValue = 0;
        var ret = res.invoke("method", Integer.class);

        assertEquals("method should return ${expected}", retValue, ret.returnValue());
    }

    @Test
    public void AssignUsingThis() {
        var res = toJasmin("AssignUsingThis");
        var instance = res.newInstance();
        var ret = res.invoke(instance, "method", res.clazz());

        assertEquals("method should return ${expected}", instance, ret.returnValue());
    }

    @Test
    public void AssignInSuperType() {
        var res = toJasmin("AssignInSuperType", Quicksort.class);
        var instance = res.newInstance();
        var ret = res.invoke(instance, "method", Quicksort.class);

        assertEquals("method should return ${expected}", instance, ret.returnValue());
    }
}
