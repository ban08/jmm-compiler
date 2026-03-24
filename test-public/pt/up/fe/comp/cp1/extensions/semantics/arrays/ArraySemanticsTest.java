package pt.up.fe.comp.cp1.extensions.semantics.arrays;

import org.junit.Test;


/**
 * Test variable lookup.
 */
public class ArraySemanticsTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/extensions/semantics/arrays/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ArraySemanticsTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void assignNewToArray() {
        setDescription("Test that 'new type[]' expressions are assigned to arrays");
        semantics("AssignNewToArrayFail.jmm", true);
        semantics("AssignNewToArrayOk.jmm", false);
    }

    @Test
    public void arrayHasNewInit() {
        setDescription("Test that arrays are initialized with a 'new type[]' expression");
        semantics("ArrayHasNewInitFail1.jmm", true);
        semantics("ArrayHasNewInitFail2.jmm", true);
        semantics("ArrayHasNewInitOk.jmm", false);
    }

    @Test
    public void arrayReturn() {
        setDescription("Test that arrays are int[] only (other primitives and Object types are not allowed in J--)");
        semantics("ArrayReturnFail.jmm", true);
        semantics("ArrayReturnOk.jmm", false);
    }
}
