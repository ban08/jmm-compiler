package core.lexer;

import org.junit.Test;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.lexer.JmmLexerImpl;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
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
}
