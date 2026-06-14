package org.eman.compiler;

import org.eman.lexer.Lexer;
import org.eman.lexer.Token;
import org.eman.optimizer.AstOptimizer;
import org.eman.parser.Parser;
import org.eman.parser.ast.Expression;
import org.eman.parser.ast.NumberExpression;
import org.eman.parser.ast.PrintStatement;
import org.eman.parser.ast.Statement;
import org.eman.runtime.Interpreter;
import org.eman.semantic.SemanticAnalyzer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CompilerPipeline {

    private CompilerPipeline() {
    }

    public static List<Token> lex(String source) {
        return new Lexer(source).tokenize();
    }

    public static List<Statement> parse(String source) {
        return new Parser(lex(source)).parse();
    }

    public static List<Statement> parse(List<Token> tokens) {
        return new Parser(tokens).parse();
    }

    public static SemanticAnalyzer analyze(List<Statement> ast) {
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(ast);
        return analyzer;
    }

    public static List<Statement> optimize(List<Statement> ast) {
        return new AstOptimizer().optimize(ast);
    }

    public static void interpret(List<Statement> ast) {
        new Interpreter().interpret(ast);
    }

    public static String run(String source) {
        List<Statement> ast = parse(source);
        SemanticAnalyzer analyzer = analyze(ast);

        if (hasErrors(analyzer)) {
            throw new RuntimeException("Semantic errors: " + analyzer.getErrors());
        }

        List<Statement> optimized = optimize(ast);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            interpret(optimized);
        } finally {
            System.setOut(original);
        }

        return buffer.toString(StandardCharsets.UTF_8).trim();
    }

    public static boolean hasErrors(SemanticAnalyzer analyzer) {
        return analyzer.getErrors().stream().anyMatch(error -> !error.startsWith("Warning:"));
    }

    public static Expression firstPrintExpression(List<Statement> ast) {
        for (Statement statement : ast) {
            if (statement instanceof PrintStatement print) {
                return print.getExpression();
            }
        }
        throw new IllegalStateException("No print statement found");
    }

    public static NumberExpression requireNumber(Expression expression) {
        if (expression instanceof NumberExpression number) {
            return number;
        }
        throw new IllegalStateException("Expected NumberExpression, got " + expression.getClass().getSimpleName());
    }
}
