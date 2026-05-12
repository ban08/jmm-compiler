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

public class EntitiesAccessJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/entitiesandops/entities/";

    public EntitiesAccessJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void AccessParam() {
        var res = toJasmin("AccessParam");
        var ret = res.invoke("method", Integer.class, 10);
        assertEquals("method should return ${expected}", 10, ret.returnValue());
    }


    @Test
    public void AccessThis() {
        var res = toJasmin("AccessThis");
        var instance = res.newInstance();
        var ret = res.invoke(instance, "method", res.clazz());
        assertEquals("method should return instance ${expected}", instance, ret.returnValue());
    }

    @Test
    public void AccessLocalVariable() {
        var res = toJasmin("AccessLocalVariable");
        var ret = res.invoke("method", Integer.class);
        assertEquals("method should return ${expected}", 1, ret.returnValue());
    }


    @Test
    public void AccessImplicitClass() {
        var res = toJasmin("AccessImplicitClass");
        String qsInstance = "Hello World";
        var ret = res.invoke("method", String.class, qsInstance);
        assertEquals("method should return ${expected}", qsInstance, ret.returnValue());
    }


    @Test
    public void AccessImportedClass() {
        var res = toJasmin("AccessImportedClass");
        Quicksort qsInstance = new Quicksort();
        var ret = res.invoke("method", Quicksort.class, qsInstance);
        assertEquals("method should return ${expected}", qsInstance, ret.returnValue());
    }
}
