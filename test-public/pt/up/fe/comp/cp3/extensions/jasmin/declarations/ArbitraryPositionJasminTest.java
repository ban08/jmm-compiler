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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class ArbitraryPositionJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/declarations/arbitrarypos";

    public ArbitraryPositionJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void MethodThenField() {
        var res = toJasmin("MethodThenField");
        var instance = res.newInstance();
        var expected = -123;
        res.setFieldValue(instance, "a", expected);
        var ret = res.invoke(instance, "foo", Integer.class);
        assertEquals("Field after method should nonetheless return its value", expected, ret.returnValue());
    }

    @Test
    public void MethodsWithParametersAndFields() {
        var res = toJasmin("MethodsWithParametersAndFields");
        var instance = res.newInstance();
        res.invoke(instance, "add", 2);
        res.invoke(instance, "add", 5);
        var expected = 7;
        var fieldV = res.getFieldValue(instance, "count", Integer.class);
        assertEquals("After two adds the field should have ${expected}", expected, fieldV);
        res.invoke(instance, "reset");
        fieldV = res.getFieldValue(instance, "count", Integer.class);
        assertEquals("After reset field should have 0", 0, fieldV);
    }


    @Test
    public void FieldMethodField() {
        var res = toJasmin("FieldMethodField");
        var instance = res.newInstance();
        var a = -123;
        res.setFieldValue(instance, "a", a);
        var b = 32;
        res.setFieldValue(instance, "b", b);
        var expected = a + b;

        var ret = res.invoke(instance, "foo", Integer.class);
        assertEquals("Method in middle of fields should nonetheless return their sum", expected, ret.returnValue());
    }

    @Test
    public void InterleavedFieldsAndMethods() {
        var res = toJasmin("InterleavedFieldsAndMethods");
        var instance = res.newInstance();
        var a = 23;
        res.setFieldValue(instance, "a", a);
        var b = 55;
        res.setFieldValue(instance, "b", b);

        var s = "This is a test";
        res.setFieldValue(instance, "s", s);
        var expected = a + b;
        var expectedStr = s;

        var ret = res.invoke(instance, "f2", Integer.class);
        assertEquals("Method in middle of fields should nonetheless return their sum", expected, ret.returnValue());
        var ret2 = res.invoke(instance, "f1", String.class);
        assertEquals("Field after method should nonetheless return its value", expectedStr, ret2.returnValue());
    }


}
