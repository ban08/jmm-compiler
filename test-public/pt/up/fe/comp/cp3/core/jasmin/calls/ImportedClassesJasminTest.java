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

import examples.Auxi;
import examples.Pair;
import examples.inheritance.A;
import examples.inheritance.B;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;
import pt.up.fe.specs.util.SpecsStrings;

@RunWith(Parameterized.class)
public class ImportedClassesJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/core/jasmin/calls/imported/";


    public ImportedClassesJasminTest(InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void ImportedClassesSimpleCall() {
        var res = toJasmin("ImportedClassesSimpleCall");
        var expected = 230;
        var pair = new Pair(expected, 0);
        var ret = res.invoke("method", Integer.class, pair);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ImportedClassesSimpleCallFromNewInstance() {
        var res = toJasmin("ImportedClassesSimpleCallFromNewInstance");
        var expected = 20;
        var ret = res.invoke("method", Integer.class);
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ImportedClassesSimpleCallVoid() {
        var res = toJasmin("ImportedClassesSimpleCallVoid");
        var expected = "10";
        var auxi = new Auxi();
        var ret = res.invoke("method", auxi);
        var outputRes = ret.outputText().strip();
        assertEquals("method should print to std out: ${expected}", expected, outputRes);
    }


    @Test
    public void ImportedClassesCallWithArg() {
        var res = toJasmin("ImportedClassesCallWithArg");
        var pair = new Pair(0, 0);
        res.invoke("method", Integer.class, pair);
        var expected = 1;
        assertEquals("method should have update left value with ${expected}", expected, pair.getLeft());
    }


    @Test
    public void ImportedClassesCallWithArgExpectingClass() {
        var res = toJasmin("ImportedClassesCallWithArgExpectingClass");
        var aux = new Auxi();
        var ret = res.invoke("method", Integer.class, aux);
        var expected = 20;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }


    @Test
    public void ImportedClassesCallWithArgExpectingObject() {
        var res = toJasmin("ImportedClassesCallWithArgExpectingObject");
        var instance = res.newInstance();
        var aux = new Auxi();
        var ret = res.invoke(instance, "method", Integer.class, aux);
        var expected = instance.hashCode();
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ImportedClassesCallWithArgExpectingSuperClass() {
        var res = toJasmin("ImportedClassesCallWithArgExpectingSuperClass", Auxi.class);
        var instance = res.newInstance();
        var aux = new Auxi();
        var ret = res.invoke(instance, "method", Integer.class, aux);
        var expected = 20;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ImportedClassesCallSuperMethodInt() {
        var res = toJasmin("ImportedClassesCallSuperMethodInt", Auxi.class);
        var instance = res.newInstance();
        var ret = res.invoke(instance, "method", Integer.class);
        var expected = 20;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ImportedClassesCallSuperMethodWithArg() {
        var res = toJasmin("ImportedClassesCallSuperMethodWithArg", Auxi.class);
        var instance = res.newInstance();
        var ret = res.invoke(instance, "method", Integer.class);
        var expected = 13;
        assertEquals("method should return ${expected}", expected, ret.returnValue());
    }

    @Test
    public void ImportedClassesCallSuperMethodVoid() {
        var res = toJasmin("ImportedClassesCallSuperMethodVoid", Auxi.class);
        var instance = res.newInstance();
        var ret = res.invoke(instance, "method", Integer.class);
        var expected = "10";

        assertEquals("method should output ${expected}", expected, ret.outputText().strip());
    }

    @Test
    public void ImportedClassesCallReturnStoredInSuperType() {
        var res = toJasmin("ImportedClassesCallReturnStoredInSuperType");
        var instance = res.newInstance();
        var b = new B();
        var ret = res.invoke(instance, "method", A.class, b);
        assertEquals("method should return ${expected}", b, ret.returnValue());
    }


    @Test
    public void ImportedClassesSimpleCallExprStmtRetInt() {
        var res = toJasmin("ImportedClassesSimpleCallExprStmtRetInt");
        var pair = new Pair(0, 0);
        res.invoke("method", Integer.class, pair);
        var expected = 200;
        assertEquals("method should have update left value with ${expected}", expected, pair.getLeft());
    }


    @Test
    public void ImportedClassesStaticCallVoid() {
        var res = toJasmin("ImportedClassesStaticCallVoid");
        var expected = "1";
        var ret = res.invoke("method");
        var outputRes = ret.outputText().strip();
        assertEquals("method should print to std out: ${expected}", expected, outputRes);
    }


    @Test
    public void ImportedClassesStaticCallRetInt() {
        var res = toJasmin("ImportedClassesStaticCallRetInt");
        var expected = "123";
        var ret = res.interact("method", Integer.class, expected);
        assertEquals("method should return: ${expected}", Integer.parseInt(expected), ret.returnValue());
    }


    @Test
    public void ImportedClassesStaticCallExprStmtRetInt() {
        var res = toJasmin("ImportedClassesStaticCallExprStmtRetInt");
        var inputText = "100";
        var ret = res.interact("method", inputText); //value will be ignored, but necessary
        var expectedOutput = "Insert number:";
        assertEquals("method should output: ${expected}", expectedOutput, ret.outputText().strip());
    }


    @Test
    public void ImportedClassStaticCallFromMain() {
        var res = toJasmin("ImportedClassStaticCallFromMain");
        var inputText = "John Doe";
        var ret = res.interact("main", inputText, (Object) new String[0]); //value will be ignored, but necessary

        var expectedOutput = """
                Who are you?
                Hello, %s!""".formatted(inputText);
        assertEquals("method should output: ${expected}", SpecsStrings.normalizeFileContents(expectedOutput, true),
                SpecsStrings.normalizeFileContents(ret.outputText(), true));


    }

}
