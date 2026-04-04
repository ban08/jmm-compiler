package core.lexer;

import org.antlr.v4.runtime.Token;
import org.junit.Test;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.lexer.JmmLexerImpl;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MyLexerTest {

    @Test
    public void invalidCharactersProduceLexicalErrorsWithPosition() {
        var result = new JmmLexerImpl().lex("""
                package p;
                class A {
                    public int foo() {
                        return @;
                    }
                }
                """, Collections.emptyMap());

        assertTrue("Expected the lexer to reject unsupported characters", result.hasErrors());

        var report = result.reports().getFirst();
        assertEquals("Expected a lexical-stage report", Stage.LEXICAL, report.getStage());
        assertTrue("Expected the lexer error to point to the offending line", report.getLine() >= 4);
        assertTrue("Expected the lexer error to point to the offending column", report.getColumn() >= 1);
    }

    @Test
    public void commentsAreSkippedWithoutChangingTheTokenStream() {
        var result = new JmmLexerImpl().lex("""
                package p; /* keywords and operators inside comments should be ignored: if else + == */
                import util.io;
                class A {
                    public int foo() {
                        return 1 /* hidden operator */ +
                                // hidden trailing expression
                                2;
                    }
                }
                """, Collections.emptyMap());

        assertFalse("Expected comment-heavy input to lex successfully", result.hasErrors());

        var tokenTexts = result.tokens().stream()
                .map(Token::getText)
                .filter(text -> !"<EOF>".equals(text))
                .toList();

        assertEquals(List.of(
                "package", "p", ";",
                "import", "util", ".", "io", ";",
                "class", "A", "{",
                "public", "int", "foo", "(", ")", "{",
                "return", "1", "+", "2", ";",
                "}", "}"
        ), tokenTexts);
    }
}
