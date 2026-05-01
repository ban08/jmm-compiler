package pt.up.fe.comp.cp1.extensions.semantics.calls;

import org.junit.Test;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.EXPRESSION;


/**
 * Test variable lookup.
 */
public class ImplicitThisSemanticAnalysisTest extends pt.up.fe.comp.test.env.JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/statements/semanticanalysis/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ImplicitThisSemanticAnalysisTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }

    @Test
    public void testSimpleImplicitThis() {
        setDescription("Test that method calls without an explicit object pass semantics.");
        semanticsFromSnippet("""
                package x;
                class Implicit{
                    public void bar(){
                        fxx();
                    }
                    public void foo(){
                    }
                }
                """, true);
        semanticsFromSnippet("""
                package x;
                class Implicit{
                    public void bar(){
                        foo();
                    }
                    public void foo(){
                    }
                }
                """, false);
    }

    @Test
    public void testSimpleImplicitThisWithArguments() {
        setDescription("Test that method calls with arguments but without an explicit object pass semantics.");
        semanticsFromSnippet("""
                package x;
                class Implicit{
                    public void bar(int a){
                        foo(true, 1, a);
                    }
                    public void foo(int a, boolean b, int c){
                    }
                }
                """, true);
        semanticsFromSnippet("""
                package x;
                class Implicit{
                    public void bar(int a){
                        foo(1, true, a);
                    }
                    public void foo(int a, boolean b, int c){
                    }
                }
                """, false);
    }

    @Test
    public void testSimpleImplicitThisWithMethodCallArgument() {
        setDescription("Test that method calls with a method call as an argument and without an explicit object pass semantics.");
        semanticsFromSnippet("""
                package x;
                class Implicit{
                    public void bar(){
                        foo(xaa());
                    }
                    public void foo(int a){
                    }
                
                    public int xaa(){
                        return 1;
                    }
                }
                """, false);
    }


}
