package pt.up.fe.comp.cp1.core.parser.integration;

import org.junit.Test;
import pt.up.fe.comp.test.env.JmmTestEnv;

import static pt.up.fe.comp.cp1.core.parser.RulesNames.METHOD;
import static pt.up.fe.comp.cp1.core.parser.RulesNames.STATEMENT;

public class FeaturesCombinedParserTest extends JmmTestEnv {
//    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/integration/apps";
//    private static final String RESOURCES_LOCATION = "test-public";


    public FeaturesCombinedParserTest() {
        super("", "");
    }

    @Test
    public void testIfWithComplexCondition() {
        setDescription("Parse an if statement with a complex condition");
        parseSnippet("if(a && b && c) {} else {}", STATEMENT);
    }

    @Test
    public void testMethodWithReturnStatementWithExpression() {
        setDescription("Parse a method that contains a return statement with an expression in the body");
        parseSnippet("int foo() { return a + b; }", METHOD);
    }
}
