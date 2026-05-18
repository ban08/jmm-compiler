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

import java.util.Date;
import java.util.List;

@RunWith(Parameterized.class)
public class InstantiationJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/calls/instantiation/";


    public InstantiationJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void InstantiateSelf() {
        var res = toJasmin("InstantiateSelf");
        var ret = res.invoke("method", Object.class); //or res.clazz()
        assertEquals("method should return ${expected}", res.clazz(), ret.returnValue().getClass());
    }

    @Test
    public void InstantiateSelfStoreInSuperType() {
        var res = toJasmin("InstantiateSelfStoreInSuperType");
        var ret = res.invoke("method", Object.class);
        assertEquals("method should return ${expected}", res.clazz(), ret.returnValue().getClass());
    }

    @Test
    public void InstantiateSelfStoreInImportedSuperType() {
        var res = toJasmin("InstantiateSelfStoreInImportedSuperType", Quicksort.class);
        var ret = res.invoke("method", Quicksort.class);
        assertEquals("method should return ${expected}", res.clazz(), ret.returnValue().getClass());
    }

    @Test
    public void InstantiateJavaLangClass() {
        var res = toJasmin("InstantiateJavaLangClass");
        var ret = res.invoke("method", String.class);
        assertEquals("method should return ${expected}", String.class, ret.returnValue().getClass());
    }


    @Test
    public void InstantiateImportedClass() {
        var res = toJasmin("InstantiateImportedClass");
        var ret = res.invoke("method", Date.class);
        assertEquals("method should return ${expected}", Date.class, ret.returnValue().getClass());
    }


}
