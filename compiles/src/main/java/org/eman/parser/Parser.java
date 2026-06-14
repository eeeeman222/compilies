package org.eman.parser;

import org.eman.parser.ast.*;
import org.eman.lexer.Token;
import org.eman.lexer.TokenType;

import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int position;

    public Parser(List<Token> tokens) {
        this.tokens = new ArrayList<>(tokens);
        this.position = 0;
    }

    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(parseDeclaration());
        }
        return statements;
    }

    private Statement parseDeclaration() {
        if (match(TokenType.VAR)) return parseVarDeclaration();
        if (match(TokenType.FUN)) return parseFunctionDeclaration();
        return parseStatement();
    }

    private Statement parseFunctionDeclaration() {
        Token name = consume(TokenType.ID, "Ожидается имя функции.");
        consume(TokenType.LPAREN, "Ожидается '(' после имени функции.");

        List<String> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                params.add(consume(TokenType.ID, "Ожидается имя параметра.").getValue());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RPAREN, "Ожидается ')' после параметров.");
        consume(TokenType.LBRACE, "Ожидается '{' перед телом функции.");

        List<Statement> body = parseBlock();
        return new FunctionStatement(name.getValue(), params, body);
    }

    private Statement parseStatement() {
        if (match(TokenType.IF)) return parseIfStatement();
        if (match(TokenType.WHILE)) return parseWhileStatement();
        if (match(TokenType.PRINT)) return parsePrintStatement();
        if (match(TokenType.RETURN)) return parseReturnStatement();
        if (match(TokenType.LBRACE)) return new BlockStatement(parseBlock());

        return parseExpressionStatement();
    }

    private Statement parseReturnStatement() {
        Expression value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = parseExpression();
        }
        consume(TokenType.SEMICOLON, "Ожидается ';' после return.");
        return new ReturnStatement(value);
    }

    private Statement parseVarDeclaration() {
        Token name = consume(TokenType.ID, "Ожидается имя переменной.");
        Expression initializer = null;

        if (match(TokenType.EQ)) {
            initializer = parseExpression();
        }

        consume(TokenType.SEMICOLON, "Ожидается ';' после объявления переменной.");
        return new VarStatement(name.getValue(), initializer);
    }

    private Statement parseIfStatement() {
        consume(TokenType.LPAREN, "Ожидается '(' после 'if'.");
        Expression condition = parseExpression();
        consume(TokenType.RPAREN, "Ожидается ')' после условия 'if'.");

        Statement thenBranch = parseStatement();
        Statement elseBranch = null;

        if (match(TokenType.ELSE)) {
            elseBranch = parseStatement();
        }

        return new IfStatement(condition, thenBranch, elseBranch);
    }

    private Statement parseWhileStatement() {
        consume(TokenType.LPAREN, "Ожидается '(' после 'while'.");
        Expression condition = parseExpression();
        consume(TokenType.RPAREN, "Ожидается ')' после условия 'while'.");

        Statement body = parseStatement();
        return new WhileStatement(condition, body);
    }

    private Statement parsePrintStatement() {
        Expression value = parseExpression();
        consume(TokenType.SEMICOLON, "Ожидается ';' после значения.");
        return new PrintStatement(value);
    }

    private Statement parseExpressionStatement() {
        Expression expr = parseExpression();
        consume(TokenType.SEMICOLON, "Ожидается ';' после выражения.");
        return new ExpressionStatement(expr);
    }

    private List<Statement> parseBlock() {
        List<Statement> statements = new ArrayList<>();

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(parseDeclaration());
        }

        consume(TokenType.RBRACE, "Ожидается '}' после блока.");
        return statements;
    }

    private Expression parseExpression() {
        return parseAssignment();
    }

    private Expression parseAssignment() {
        Expression expr = parseLogicalOr();

        if (match(TokenType.EQ)) {
            Token equals = previous();
            Expression value = parseAssignment();

            if (expr instanceof VariableExpression varExpr) {
                return new AssignExpression(varExpr.getName(), value);
            }

            if (expr instanceof IndexExpression indexExpr) {
                return new IndexAssignExpression(indexExpr.getArray(), indexExpr.getIndex(), value);
            }

            throw parserError(equals.getLine(), "Недопустимая цель для присваивания.");
        }

        return expr;
    }

    private Expression parseLogicalOr() {
        Expression expr = parseLogicalAnd();

        while (match(TokenType.OR)) {
            TokenType op = previous().getType();
            Expression right = parseLogicalAnd();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseLogicalAnd() {
        Expression expr = parseEquality();

        while (match(TokenType.AND)) {
            TokenType op = previous().getType();
            Expression right = parseEquality();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseEquality() {
        Expression expr = parseComparison();

        while (match(TokenType.EQEQ, TokenType.NEQ)) {
            TokenType op = previous().getType();
            Expression right = parseComparison();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseComparison() {
        Expression expr = parseTerm();

        while (match(TokenType.LT, TokenType.LTEQ, TokenType.GT, TokenType.GTEQ)) {
            TokenType op = previous().getType();
            Expression right = parseTerm();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseTerm() {
        Expression expr = parseFactor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            TokenType op = previous().getType();
            Expression right = parseFactor();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseFactor() {
        Expression expr = parseUnary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            TokenType op = previous().getType();
            Expression right = parseUnary();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseUnary() {
        if (match(TokenType.EXCL, TokenType.MINUS)) {
            TokenType op = previous().getType();
            Expression right = parseUnary();
            return new UnaryExpression(op, right);
        }

        return parsePostfix();
    }

    private Expression parsePostfix() {
        Expression expr = parsePrimary();

        while (match(TokenType.LBRACKET)) {
            Expression index = parseExpression();
            consume(TokenType.RBRACKET, "Ожидается ']' после индекса.");
            expr = new IndexExpression(expr, index);
        }

        return expr;
    }

    private Expression parsePrimary() {
        if (match(TokenType.NUMBER)) {
            return new NumberExpression(Double.parseDouble(previous().getValue()));
        }

        if (match(TokenType.STRING)) {
            return new StringExpression(previous().getValue());
        }

        if (match(TokenType.ID)) {
            String name = previous().getValue();
            if (match(TokenType.LPAREN)) {
                return finishCall(name);
            }
            return new VariableExpression(name);
        }

        if (match(TokenType.LBRACKET)) {
            return finishArrayLiteral();
        }

        if (match(TokenType.LPAREN)) {
            Expression expr = parseExpression();
            consume(TokenType.RPAREN, "Ожидается ')' после выражения.");
            return expr;
        }

        Token token = peek();
        throw parserError(token.getLine(), "Ожидается выражение.");
    }

    private Expression finishCall(String name) {
        List<Expression> arguments = new ArrayList<>();

        if (!check(TokenType.RPAREN)) {
            do {
                arguments.add(parseExpression());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RPAREN, "Ожидается ')' после аргументов.");
        return new CallExpression(name, arguments);
    }

    private Expression finishArrayLiteral() {
        List<Expression> elements = new ArrayList<>();

        if (!check(TokenType.RBRACKET)) {
            do {
                elements.add(parseExpression());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RBRACKET, "Ожидается ']' после элементов массива.");
        return new ArrayExpression(elements);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) position++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(position);
    }

    private Token previous() {
        return tokens.get(position - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        Token token = peek();
        throw parserError(token.getLine(), message);
    }

    private RuntimeException parserError(int line, String message) {
        return new RuntimeException(String.format("[Parser Error] Line %d: %s", line, message));
    }
}
