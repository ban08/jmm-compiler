package pt.up.fe.comp2026.lexer;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import pt.up.fe.comp.jmm.ast.antlr.JmmErrorListener;
import pt.up.fe.comp.jmm.lexer.JmmLexer;
import pt.up.fe.comp.jmm.lexer.JmmLexerResult;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.JavammLexer;

import java.util.Map;

public class JmmLexerImpl implements JmmLexer {
    @Override
    public JmmLexerResult lex(String jmmCode, Map<String, String> config) {
        // Convert code string into a character stream
        var input = new ANTLRInputStream(jmmCode);

        // Transform characters into tokens using the lexer
        var lex = new JavammLexer(input);

        var errorListener = new JmmErrorListener(Stage.LEXICAL);
        lex.removeErrorListeners();
        lex.addErrorListener(errorListener);

        // Wrap lexer around a token stream
        var tokenStream = new CommonTokenStream(lex);

        // Read all tokens from the stream
        tokenStream.fill();

        if (errorListener.hasErrors()) {
            return new JmmLexerResult(null, errorListener.getReports(), config);
        }

        var tokens = tokenStream.getTokens();
        return new JmmLexerResult(tokens, errorListener.getReports(), config);
    }
}
