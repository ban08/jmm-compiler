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

package pt.up.fe.comp.cp3.extensions.jasmin.declarations;

import examples.Quicksort;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class FieldInitializerJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/declarations/fieldinit";

    public FieldInitializerJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void FieldInitializerIntLiteral() {
        var res = toJasmin("FieldInitializerIntLiteral");
        var instance = res.newInstance();
        var expected = 123;
        var fieldValue = res.getFieldValue(instance, "a", Integer.class);
        assertEquals("Field should be initialized with ${expected}", expected, fieldValue);
    }

    @Test
    public void FieldInitializerBooleanLiteral() {
        var res = toJasmin("FieldInitializerBooleanLiteral");
        var instance = res.newInstance();
        var expected = false;
        var fieldValue = res.getFieldValue(instance, "b", Boolean.class);
        assertEquals("Field should be initialized with ${expected}", expected, fieldValue);
    }

    @Test
    public void FieldInitializerExpression() {
        var res = toJasmin("FieldInitializerExpression");
        var instance = res.newInstance();
        var expected = 1 + 2 * 3;
        var fieldValue = res.getFieldValue(instance, "c", Integer.class);
        assertEquals("Field should be initialized with and expression that results into ${expected}", expected, fieldValue);
    }

    @Test
    public void FieldInitializerNewObject() {
        var res = toJasmin("FieldInitializerNewObject");
        var instance = res.newInstance();
        var fieldValue = res.getFieldValue(instance, "a", Object.class);
        assertTrue("Field should be initialized with an instance of Quicksort", fieldValue instanceof Quicksort);
    }


    @Test
    public void FieldInitializerMethodInvocation() {
        var res = toJasmin("FieldInitializerMethodInvocation");
        var instance = res.newInstance();
        var expected = -10;
        var fieldValue = res.getFieldValue(instance, "a", Integer.class);
        assertEquals("Field should be initialized with return value of an invocation, results into ${expected}", expected, fieldValue);
    }

}
