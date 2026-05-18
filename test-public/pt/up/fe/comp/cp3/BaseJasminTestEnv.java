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

package pt.up.fe.comp.cp3;

import org.junit.runners.Parameterized;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.test.env.JasminTestEnv;
import pt.up.fe.comp.test.env.executors.CompilationResult;
import pt.up.fe.specs.util.SpecsStrings;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class BaseJasminTestEnv extends JasminTestEnv {
    private Function<String, JasminResult> toJasminFunction;
    private String extension;
    private static final List<String> classPath = List.of("./libs-jmm/compiled");


    public enum InputSource {
        JMM,
        OLLIR
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {

        var inputSources = List.of(
                InputSource.JMM,
                InputSource.OLLIR);

        return inputSources.stream().map(v -> new Object[]{v}).toList();
    }

    protected static final String RESOURCES_LOCATION = "test-public";

    private static String buildBasePath(InputSource source, String basePath) {
        if (!basePath.endsWith("/"))
            basePath += "/";
        return basePath + (source == InputSource.JMM ? "jmm" : "ollir") + "/";
    }

    protected BaseJasminTestEnv(InputSource source, String basePath) {
        super(buildBasePath(source, basePath), RESOURCES_LOCATION);
        this.addClassPaths(classPath);
        switch (source) {
            case JMM:
                toJasminFunction = super::getJasminFromJmm;
                extension = "jmm";
                break;
            case OLLIR:
                toJasminFunction = super::getJasminFromOllir;
                extension = "ollir";
                break;
        }

    }

    public BaseJasminTestEnv() {
        super("", RESOURCES_LOCATION);
    }

    private static final String PUBLIC_CLASS_REGEX = "\\.class\\s+public";
    private static final String CLASS_REF_REGEX = "((\\w+/)+\\w+)";
    private static final String CLASS_NAME_REGEX = PUBLIC_CLASS_REGEX + "\\s+?" + CLASS_REF_REGEX;
    private static final String SUPER_NAME_REGEX = "\\.super\\s+" + CLASS_REF_REGEX;
    private static final String FIELD_REGEX = "\\.field\\s+(public\\s+)?('\\w+'|\\w+)\\s+(.+?)\\s";
    private static final String INVOKE_SUPER_REGEX = "invokespecial\\s+?" + CLASS_REF_REGEX + "/<init>\\(\\)V";


    public CompilationResult toJasmin(String classSimpleName, String superClassQualifiedName) {
        JasminResult result = toJasminFunction.apply(classSimpleName + "." + extension);
        var expectedClassName = super.basePath + classSimpleName; // Assuming the class name matches the resource name
        var jasminCode = result.getJasminCode();
        assertNotNull("Jasmin code should not be null", jasminCode);
        assertTrue("Jasmin code should not be empty", !jasminCode.isBlank());
        var publicClassMatch = SpecsStrings.matches(jasminCode, Pattern.compile(PUBLIC_CLASS_REGEX));
        assertTrue("Expected to find a public class declaration in the Jasmin code", publicClassMatch);
        var classNameMatch = SpecsStrings.matches(jasminCode, Pattern.compile(CLASS_NAME_REGEX));
        assertTrue("Expected to find a class declaration with name in the Jasmin code", classNameMatch);
        var actualClassName = SpecsStrings.getRegexGroup(jasminCode, CLASS_NAME_REGEX, 1);
        assertEquals("Class name in Jasmin code should match ${expected}", expectedClassName, actualClassName);

        superClassQualifiedName = pathOf(superClassQualifiedName);
        var superNameMatch = SpecsStrings.matches(jasminCode, Pattern.compile(SUPER_NAME_REGEX));
        assertTrue("Expected to find a class declaration in the Jasmin code", superNameMatch);
        var actualSuperClassName = SpecsStrings.getRegexGroup(jasminCode, SUPER_NAME_REGEX, 1);
        assertEquals("Class super in Jasmin code should match ${expected}", superClassQualifiedName, actualSuperClassName);

        //find default constructor: assertion done inside
        var constructor = getJasminMethod(result, "<init>");

        var foundInvoke = SpecsStrings.matches(jasminCode, Pattern.compile(INVOKE_SUPER_REGEX));
        assertTrue("Expected to find invokespecial inside constructor", foundInvoke);
        var invokespecialType = SpecsStrings.getRegexGroup(constructor, INVOKE_SUPER_REGEX, 1);
        assertEquals("Expect class of invokespecial to be super " + superClassQualifiedName, invokespecialType, superClassQualifiedName);

        var compilationResult = compile(result);
        var classFile = new File(compilationResult.outputDir(), expectedClassName + ".class");
        assertTrue("Expected to generate java class in " + classFile.getPath(), classFile.exists());
        return compilationResult;
    }


    public CompilationResult toJasmin(String resourceName) {
        return toJasmin(resourceName, Object.class);
    }

    public CompilationResult toJasmin(String resourceName, Class<?> superClass) {
        return toJasmin(resourceName, superClass.getName());
    }


    public String pathOf(String classQualifiedName) {
        return classQualifiedName.replace('.', '/');
    }

    public String typeRefFor(String classQualifiedName) {
        return "L" + pathOf(classQualifiedName) + ";";
    }

    public String typeRefFor(Class<?> clazz) {
        return typeRefFor(clazz.getName());
    }


    public Map<String, String> getFields(String classCode) {
        var fields = new HashMap<String, String>();
        var fieldsStrs = SpecsStrings.getRegexMatches(classCode, FIELD_REGEX);
        fieldsStrs.forEach(field -> fields.put(SpecsStrings.getRegexGroup(field, FIELD_REGEX, 2).replace("'", ""), SpecsStrings.getRegexGroup(field, FIELD_REGEX, 3)));
        return fields;
    }

    public void assertFields(CompilationResult res, Map<String, String> fields) {
        var fieldsInCode = getFields(res.jasmin().getJasminCode());
        for (Map.Entry<String, String> field : fields.entrySet()) {
            assertTrue("Field '" + field.getKey() + "' should exist.", fieldsInCode.containsKey(field.getKey()));
            assertEquals("Type of field '" + field.getKey() + "' should match", field.getValue(), fieldsInCode.get(field.getKey()));
        }
    }

}
