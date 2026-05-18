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

@RunWith(Parameterized.class)
public class LocalCallsJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/calls/local/";


    public LocalCallsJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void LocalCallOnThis() {
        var res = toJasmin("LocalCallOnThis");
        var ret = res.invoke("method", Integer.class);
        var expected = 1;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LocalSimpleCallOverVar() {
        var res = toJasmin("LocalSimpleCallOverVar");
        var instance = res.newInstance();
        var ret = res.invoke("method", Integer.class, instance); //here, instance is an argument
        var expected = 0;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LocalCallWithArg() {
        var res = toJasmin("LocalCallWithArg");
        var ret = res.invoke("method", Integer.class);
        var expected = 2;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LocalCallWithMultipleArg() {
        var res = toJasmin("LocalCallWithMultipleArg");
        var ret = res.invoke("method", Integer.class);
        var expected = 3;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LocalCallExprStmtVoid() {
        var res = toJasmin("LocalCallExprStmtVoid");
        res.invoke("method"); //nothing else to check, empty callee method...
    }

    @Test
    public void LocalCallExprStmtInt() {
        var res = toJasmin("LocalCallExprStmtInt");
        res.invoke("method"); //nothing else to check, return is not used...
    }


    @Test
    public void LocalCallsComplexArithmetic() {
        var res = toJasmin("LocalCallsComplexArithmetic");
        var ret = res.invoke("method", Integer.class);
        var expected = 49;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LocalCallsComplexLogical() {
        var res = toJasmin("LocalCallsComplexLogical");
        var ret = res.invoke("method", Boolean.class);
        var expected = true;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LocalCallOverNewInstance() {
        var res = toJasmin("LocalCallOverNewInstance");
        var ret = res.invoke("method", Boolean.class);
        var expected = true;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LocalCallsNewInstanceArg() {
        var res = toJasmin("LocalCallsNewInstanceArg");
        var instance = res.newInstance();
        var ret = res.invoke(instance, "method", Object.class);
        assertNotEquals("method should not return 'this' instance ${expected}", instance, ret.returnValue());
    }

    @Test
    public void LocalCallOverNewInstanceParenthesis() {
        var res = toJasmin("LocalCallOverNewInstanceParenthesis");
        var ret = res.invoke("method", Boolean.class);
        var expected = false;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LocalCallChainedCall() {
        var res = toJasmin("LocalCallChainedCall");
        var ret = res.invoke("method", Integer.class);
        var expected = -2;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void LocalCallComplexTarget() {
        var res = toJasmin("LocalCallComplexTarget");
        var ret = res.invoke("method", Integer.class);
        var expected = 100;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

}
