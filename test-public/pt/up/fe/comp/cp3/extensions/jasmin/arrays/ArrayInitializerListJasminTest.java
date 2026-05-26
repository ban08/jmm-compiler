package pt.up.fe.comp.cp3.extensions.jasmin.arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.up.fe.comp.cp3.BaseJasminTestEnv;

@RunWith(Parameterized.class)
public class ArrayInitializerListJasminTest extends BaseJasminTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp3/extensions/jasmin/arrays/init/";


    public ArrayInitializerListJasminTest(BaseJasminTestEnv.InputSource inputSource) {
        super(inputSource, BASE_PATH);
    }

    @Test
    public void ArrayInitList() {
        var res = toJasmin("ArrayInitList");
        var ret = res.invoke("method", int[].class);
        var expected_size = 3;
        assertEquals("method should return an array of size ${expected_size}", expected_size, ret.returnValue().length);
    }

    @Test
    public void ArrayInitEmpty() {
        var res = toJasmin("ArrayInitEmpty");
        var ret = res.invoke("method", int[].class);
        var expected_size = 0;
        assertEquals("method should return an array of size ${expected_size}", expected_size, ret.returnValue().length);
    }
}
