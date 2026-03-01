package pt.up.fe.comp2026.parser;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ListTokenSource;
import pt.up.fe.comp.jmm.ast.antlr.AntlrParser;
import pt.up.fe.comp.jmm.ast.Kind;
import pt.up.fe.comp.jmm.lexer.JmmLexerResult;
import pt.up.fe.comp.jmm.parser.JmmParser;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.JavammParser;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

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

public class JmmParserImpl implements JmmParser {

//    @Override
//    public JmmLexer createLexer() {
//        return new JmmLexerImpl();
//    }

    @Override
    public String getDefaultRule() {
        return "program";
    }

    @Override
    public JmmParserResult parse(JmmLexerResult lexerResult, String startingRule, Map<String, String> config) {
        try {
            // Create a token source from the previously lexed tokens
            var tokenSource = new ListTokenSource(lexerResult.tokens());

            // Wrap lexer around a token stream
            var tokens = new CommonTokenStream(tokenSource);

            // Transforms tokens into a parse tree
            var parser = new JavammParser(tokens);

            // Convert ANTLR CST to JmmNode AST
            Function<String, Kind> stringToKind = s -> JmmKind.fromString(s).orElseThrow(() -> new RuntimeException("Could not convert kind: " + s));
            var anltrParser = new AntlrParser(stringToKind, config);
            var r = anltrParser.parse(parser, startingRule);

            var allReports = new ArrayList<Report>();
            allReports.addAll(lexerResult.reports());
            allReports.addAll(r.reports());

            var result = new JmmParserResult(r.rootNode(), allReports, r.config());

//            if (result.rootNode() != null) {
//                System.out.println("AST:\n" + r.rootNode().toTree());
//            }

            return result;
        } catch (Exception e) {
            // There was an uncaught exception during parsing, create an error JmmParserResult without root node
            return JmmParserResult.newError(Report.newError(Stage.SYNTATIC, -1, -1, "Exception during parsing", e), config);
        }
    }
}
