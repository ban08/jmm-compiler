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

package pt.up.fe.comp.cp3.core.jasmin.declarations;

import examples.Quicksort;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)

public class MethodDeclarationJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/declarations/methoddecl/";

    public MethodDeclarationJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void VoidMethod() {
        var res = toJasmin("ClassWithVoidMethod");
        res.invoke("method");
    }


    @Test
    public void MethodReturnInt() {
        var res = toJasmin("MethodWithReturnType");
        res.invoke("method", Integer.class);
    }


    @Test
    public void MethodWithSingleParam() {
        var res = toJasmin("MethodWithSingleParam");
        res.invoke("method", Integer.class, 0);
    }

    @Test
    public void MethodWithMultipleParam() {
        var res = toJasmin("MethodWithMultipleParam");
        res.invoke("method", Integer.class, 0, 1, false);
    }


    @Test
    public void MethodParameterIsExplicitlyImported() {
        var res = toJasmin("MethodParameterIsExplicitlyImported");
        res.invoke("make", new Quicksort());
    }


    @Test
    public void MethodReturnTypeIsImplicitlyImportedImported() {
        var res = toJasmin("MethodReturnTypeIsImplicitlyImported");
        var retType = Object.class;
        var result = res.invoke("make", retType);
        assertEquals("Return should be of type ${expected}", retType, result.returnValue().getClass());
    }


    @Test
    public void ClassWithTwoMethods() {
        var res = toJasmin("ClassWithTwoMethods");
        var result = res.invoke("method1", Integer.class); // public int method1()
        assertEquals("method1 should return ${expected}", 0, result.returnValue());
        var result2 = res.invoke("method2", Boolean.class, 2, true); // public boolean method2(int intParam1, boolean boolParam1)
        assertEquals("method2 should return ${expected}", true, result2.returnValue());
    }

}
