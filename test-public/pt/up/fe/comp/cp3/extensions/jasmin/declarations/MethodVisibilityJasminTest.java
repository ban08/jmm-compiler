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
import pt.up.fe.comp.jmm.analysis.table.Visibility;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;

@RunWith(Parameterized.class)
public class MethodVisibilityJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/declarations/methodvis";

    public MethodVisibilityJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    public void testMethod(Method method, boolean isStatic, Visibility vis) {
        var statText = isStatic ? "static" : "non-static";
        assertTrue("Method " + method.getName() + "should be " + statText, Modifier.isStatic(method.getModifiers()) == isStatic);
        String visText;
        Function<Integer, Boolean> isVis = switch (vis) {
            case PRIVATE -> {
                visText = "private";
                yield Modifier::isPrivate;
            }
            case PROTECTED -> {
                visText = "protected";
                yield Modifier::isProtected;
            }
            case PUBLIC -> {
                visText = "public";
                yield Modifier::isPublic;
            }
            default -> {
                visText = "package protected";
                yield i -> !Modifier.isProtected(i) && !Modifier.isPublic(i) && !Modifier.isPrivate(i);
            }
        };
        assertTrue("Method " + method.getName() + " should be " + visText, isVis.apply(method.getModifiers()));

    }


    @Test
    public void PrivateMethodDeclaration() {
        var res = toJasmin("PrivateMethodDeclaration");
        var method = res.getJavaMethod("foo");
        testMethod(method, false, Visibility.PRIVATE);
    }

    @Test
    public void ProtectedMethodDeclaration() {
        var res = toJasmin("ProtectedMethodDeclaration");
        var method = res.getJavaMethod("bar");
        testMethod(method, false, Visibility.PROTECTED);
    }

    @Test
    public void PrivateStaticMethodDeclaration() {
        var res = toJasmin("PrivateStaticMethodDeclaration");
        var method = res.getJavaMethod("helper");
        testMethod(method, true, Visibility.PRIVATE);
    }

    @Test
    public void ProtectedStaticMethodDeclaration() {
        var res = toJasmin("ProtectedStaticMethodDeclaration");
        var method = res.getJavaMethod("compute");
        testMethod(method, true, Visibility.PROTECTED);
    }

    @Test
    public void MixedMethodVisibilities() {
        var res = toJasmin("MixedMethodVisibilities");
        var method = res.getJavaMethod("privateMethod");
        testMethod(method, false, Visibility.PRIVATE);
        method = res.getJavaMethod("protectedMethod");
        testMethod(method, false, Visibility.PROTECTED);
        method = res.getJavaMethod("publicMethod");
        testMethod(method, false, Visibility.PUBLIC);
        method = res.getJavaMethod("packageProtectedMethod");
        testMethod(method, false, Visibility.PACKAGE_PROTECTED);
        method = res.getJavaMethod("privateStaticMethod");
        testMethod(method, true, Visibility.PRIVATE);
        method = res.getJavaMethod("protectedStaticMethod");
        testMethod(method, true, Visibility.PROTECTED);
        method = res.getJavaMethod("publicStaticMethod");
        testMethod(method, true, Visibility.PUBLIC);
        method = res.getJavaMethod("packageProtectedStaticMethod");
        testMethod(method, true, Visibility.PACKAGE_PROTECTED);
    }

}
