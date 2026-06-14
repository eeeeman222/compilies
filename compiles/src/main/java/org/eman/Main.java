package org.eman;

import org.eman.compiler.CompilerPipeline;
import org.eman.lexer.Token;
import org.eman.parser.ast.Statement;
import org.eman.semantic.SemanticAnalyzer;
import org.eman.util.AstPrinter;
import org.eman.util.RandomProgramGenerator;

import java.util.List;

public class Main {
    
    private static final String DEMO_PROGRAM = """
            fun factorial(n) {
                if (n <= 1) { return 1; }
                return n * factorial(n - 1);
            }

            print "factorial(5) =";
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

            print "sorted:";
            print arr;
            """;

    public static void main(String[] args) {
        String source = resolveSource(args);

        System.out.println("=== Исходный код ===");
        System.out.println(source);

        List<Token> tokens = CompilerPipeline.lex(source);
        System.out.println("\n=== Лаб. 1: Lexer ===");
        System.out.println("Токенов: " + tokens.size());

        List<Statement> ast = CompilerPipeline.parse(tokens);
        System.out.println("\n=== Лаб. 2: Parser ===");
        System.out.println("Инструкций верхнего уровня: " + ast.size());
        new AstPrinter().print(ast);

        List<Statement> optimized = CompilerPipeline.optimize(ast);
        System.out.println("\n=== Лаб. 6: Optimizer ===");
        System.out.println("После оптимизации: " + optimized.size() + " инструкций");

        SemanticAnalyzer analyzer = CompilerPipeline.analyze(optimized);
        System.out.println("\n=== Лаб. 3: Semantic Analyzer ===");
        List<String> messages = analyzer.getErrors();
        if (messages.isEmpty()) {
            System.out.println("Ошибок и предупреждений нет.");
        } else {
            for (String message : messages) {
                System.out.println("  - " + message);
            }
        }

        if (CompilerPipeline.hasErrors(analyzer)) {
            System.out.println("\nИсполнение пропущено из-за семантических ошибок.");
            return;
        }

        System.out.println("\n=== Лаб. 4–7: Interpreter ===");
        CompilerPipeline.interpret(optimized);
    }

    private static String resolveSource(String[] args) {
        if (args.length == 0) {
            return DEMO_PROGRAM;
        }
        if ("random".equals(args[0])) {
            return new RandomProgramGenerator().generate(8);
        }
        return args[0];
    }
}
