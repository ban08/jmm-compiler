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

package pt.up.fe.comp.cp3.extensions.jasmin.entitiesandops;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

import java.util.List;

@RunWith(Parameterized.class)
public class AdditionalArithmeticJasminTestArrayField extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/entitiesandops/addarith";

    public AdditionalArithmeticJasminTestArrayField(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void IncrementOnArray() {
        var res = toJasmin("IncrementOnArray");
        var tests = List.of(List.of(2, 3), List.of(-3, -2), List.of(5, 6), List.of(-1, 0));
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            var array = new int[]{a};
            res.invoke("method", (Object) array);
            assertEquals("++ a[0] == ${actual} (should be ${expected})", expected, array[0]);
        }
    }

    @Test
    public void IncrementOnField() {
        var res = toJasmin("IncrementOnField");
        var tests = List.of(List.of(2, 3), List.of(-3, -2), List.of(5, 6), List.of(-1, 0));
        var instance = res.newInstance();
        var method = res.getJavaMethod("method");
        for (var test : tests) {
            var a = test.getFirst();
            var expected = test.getLast();
            res.setFieldValue(instance, "a", a);
            res.invoke(instance, method);
            var actual = res.getFieldValue(instance, "a", Integer.class);
            assertEquals("++ fieldA; == ${actual} (should be ${expected})", expected, actual);
        }
    }

}