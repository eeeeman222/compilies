package org.eman;

import org.eman.compiler.CompilerPipeline;
import org.eman.lexer.Token;
import org.eman.lexer.TokenType;
import org.eman.parser.ast.*;
import org.eman.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompilerRequirementsTest {

    @Test
    void lab1_lexer_produces_token_array() {
        List<Token> tokens = CompilerPipeline.lex("var x = 42; print \"hi\";");

        assertFalse(tokens.isEmpty());
        assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).getType());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.VAR));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.NUMBER && "42".equals(t.getValue())));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == TokenType.STRING && "hi".equals(t.getValue())));
    }

    @Test
    void lab1_lexer_maximal_munch_for_operators() {
        List<Token> tokens = CompilerPipeline.lex("a == b && c != d");

        assertEquals(TokenType.ID, tokens.get(0).getType());
        assertEquals(TokenType.EQEQ, tokens.get(1).getType());
        assertEquals(TokenType.AND, tokens.get(3).getType());
        assertEquals(TokenType.NEQ, tokens.get(5).getType());
    }

    @Test
    void lab2_parser_builds_ast_with_operator_precedence() {
        List<Statement> ast = CompilerPipeline.parse("print 2 + 3 * 4;");

        BinaryExpression expr = (BinaryExpression) ((PrintStatement) ast.get(0)).getExpression();
        assertEquals(TokenType.PLUS, expr.getOperator());
        assertInstanceOf(NumberExpression.class, expr.getLeft());
        assertInstanceOf(BinaryExpression.class, expr.getRight());
    }

    @Test
    void lab2_parser_supports_if_while_blocks() {
        String source = """
                var x = 0;
                if (x == 0) { x = 1; } else { x = 2; }
                while (x < 3) { x = x + 1; }
                """;
        List<Statement> ast = CompilerPipeline.parse(source);
        assertEquals(3, ast.size());
        assertInstanceOf(IfStatement.class, ast.get(1));
        assertInstanceOf(WhileStatement.class, ast.get(2));
    }

    @Test
    void lab3_semantic_warns_about_unused_and_uninitialized() {
        SemanticAnalyzer analyzer = CompilerPipeline.analyze(CompilerPipeline.parse("""
                var unused;
                print known;
                """));

        assertTrue(analyzer.getErrors().stream().anyMatch(m -> m.contains("never used")));
        assertTrue(analyzer.getErrors().stream().anyMatch(m -> m.contains("never initialized")));
        assertTrue(analyzer.getErrors().stream().anyMatch(m -> m.contains("not defined")));
    }

    @Test
    void lab3_semantic_reports_type_mismatch() {
        SemanticAnalyzer analyzer = CompilerPipeline.analyze(CompilerPipeline.parse("""
                var x = "text";
                print x - 1;
                """));

        assertTrue(analyzer.getErrors().stream().anyMatch(m -> m.contains("Type mismatch")));
    }

    @Test
    void lab4_interpreter_evaluates_math_loops_and_print() {
        String output = CompilerPipeline.run("""
                var i = 0;
                var sum = 0;
                while (i < 3) {
                    sum = sum + i;
                    i = i + 1;
                }
                print sum;
                """);
        assertEquals("3", output);
    }

    @Test
    void lab4_interpreter_lazy_logical_operators() {
        assertDoesNotThrow(() -> CompilerPipeline.run("""
                var flag = 0;
                var ok = flag && (10 / 0);
                print ok;
                """));
    }

    @Test
    void lab5_functions_support_recursion_and_return() {
        String output = CompilerPipeline.run("""
                fun fib(n) {
                    if (n <= 1) {
                        return n;
                    }
                    return fib(n - 1) + fib(n - 2);
                }
                print fib(6);
                """);
        assertEquals("8", output);
    }

    @Test
    void lab6_optimizer_folds_constants() {
        List<Statement> optimized = CompilerPipeline.optimize(CompilerPipeline.parse("print 2 + 3;"));
        NumberExpression folded = CompilerPipeline.requireNumber(
                CompilerPipeline.firstPrintExpression(optimized));
        assertEquals(5.0, folded.getValue());
    }

    @Test
    void lab6_optimizer_folds_string_concatenation() {
        List<Statement> optimized = CompilerPipeline.optimize(CompilerPipeline.parse("print \"a\" + \"b\";"));
        Expression expr = CompilerPipeline.firstPrintExpression(optimized);
        assertInstanceOf(StringExpression.class, expr);
        assertEquals("ab", ((StringExpression) expr).getValue());
    }

    @Test
    void lab6_optimizer_eliminates_dead_code() {
        List<Statement> optimized = CompilerPipeline.optimize(CompilerPipeline.parse("""
                if (0) { print 1; } else print 2;
                while (0) { print 3; }
                """));
        assertEquals(1, optimized.size());
        assertInstanceOf(PrintStatement.class, optimized.get(0));
        NumberExpression value = CompilerPipeline.requireNumber(
                ((PrintStatement) optimized.get(0)).getExpression());
        assertEquals(2.0, value.getValue());
    }

    @Test
    void lab7_arrays_support_index_read_and_write() {
        String output = CompilerPipeline.run("""
                var arr = [10, 20, 30];
                arr[1] = 99;
                print arr[0];
                print arr[1];
                print arr[2];
                """);
        assertEquals("10\n99\n30", output);
    }

    @Test
    void full_pipeline_demo_factorial_and_sort() {
        String output = CompilerPipeline.run("""
                fun factorial(n) {
                    if (n <= 1) { return 1; }
                    return n * factorial(n - 1);
                }
                print factorial(5);

                var arr = [5, 3, 8, 1, 2];
                var n = 5;
                var i = 0;
                while (i < n - 1) {
                    var j = 0;
                    while (j < n - i - 1) {
                        if (arr[j] > arr[j + 1]) {
                            var temp = arr[j];
                            arr[j] = arr[j + 1];
                            arr[j + 1] = temp;
                        }
                        j = j + 1;
                    }
                    i = i + 1;
                }
                print arr;
                """);
        assertTrue(output.startsWith("120"));
        assertTrue(output.contains("[1, 2, 3, 5, 8]"));
    }
}
