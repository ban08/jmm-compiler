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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

import java.util.Map;

@RunWith(Parameterized.class)

public class ImportsJasminTestField extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/declarations/imports/";

    public ImportsJasminTestField(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void ClassWithObjectField() {
        var res = toJasmin("FieldTypeIsImplicitlyImported");
        assertFields(res, Map.of("o", typeRefFor(Object.class)));
    }

    @Test
    public void ClassWithStringField() {
        var res = toJasmin("ClassWithStringField");
        assertFields(res, Map.of("field", typeRefFor(String.class)));
    }


    @Test
    public void ClassWithFieldImportedClass() {
        var res = toJasmin("FieldTypeIsExplicitlyImported");
        assertFields(res, Map.of("o", typeRefFor("examples.Quicksort")));
    }

}
