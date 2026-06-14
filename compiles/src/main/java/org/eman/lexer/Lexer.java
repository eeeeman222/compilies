package org.eman.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String input;
    private int start;
    private int current;
    private int line = 1;
    private int column = 1;

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("var", TokenType.VAR);
        KEYWORDS.put("print", TokenType.PRINT);
        KEYWORDS.put("fun", TokenType.FUN);
        KEYWORDS.put("return", TokenType.RETURN);
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("while", TokenType.WHILE);
    }

    public Lexer(String input) {
        this.input = input != null ? input : "";
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            start = current;
            scanToken(tokens);
        }

        tokens.add(makeToken(TokenType.EOF, "\0"));
        return tokens;
    }

    private void scanToken(List<Token> tokens) {
        char c = advance();

        switch (c) {
            case ' ', '\r', '\t' -> {  }
            case '\n' -> {
                line++;
                column = 1;
            }
            case '(' -> tokens.add(makeToken(TokenType.LPAREN, "("));
            case ')' -> tokens.add(makeToken(TokenType.RPAREN, ")"));
            case '{' -> tokens.add(makeToken(TokenType.LBRACE, "{"));
            case '}' -> tokens.add(makeToken(TokenType.RBRACE, "}"));
            case '[' -> tokens.add(makeToken(TokenType.LBRACKET, "["));
            case ']' -> tokens.add(makeToken(TokenType.RBRACKET, "]"));
            case ',' -> tokens.add(makeToken(TokenType.COMMA, ","));
            case ';' -> tokens.add(makeToken(TokenType.SEMICOLON, ";"));
            case '+' -> tokens.add(makeToken(TokenType.PLUS, "+"));
            case '*' -> tokens.add(makeToken(TokenType.STAR, "*"));
            case '/' -> tokens.add(makeToken(TokenType.SLASH, "/"));
            case '!' -> {
                if (match('=')) {
                    tokens.add(makeToken(TokenType.NEQ, "!="));
                } else {
                    tokens.add(makeToken(TokenType.EXCL, "!"));
                }
            }
            case '=' -> tokens.add(makeToken(
                    match('=') ? TokenType.EQEQ : TokenType.EQ,
                    stringValue()
            ));
            case '<' -> tokens.add(makeToken(
                    match('=') ? TokenType.LTEQ : TokenType.LT,
                    stringValue()
            ));
            case '>' -> tokens.add(makeToken(
                    match('=') ? TokenType.GTEQ : TokenType.GT,
                    stringValue()
            ));
            case '&' -> {
                if (match('&')) {
                    tokens.add(makeToken(TokenType.AND, "&&"));
                } else {
                    throw error("Unexpected character '&'");
                }
            }
            case '|' -> {
                if (match('|')) {
                    tokens.add(makeToken(TokenType.OR, "||"));
                } else {
                    throw error("Unexpected character '|'");
                }
            }
            case '-' -> tokens.add(makeToken(TokenType.MINUS, "-"));
            case '"' -> string(tokens);
            default -> {
                if (isDigit(c)) {
                    current--;
                    column--;
                    number(tokens);
                } else if (isAlpha(c)) {
                    current--;
                    column--;
                    identifier(tokens);
                } else {
                    throw error("Unexpected character '" + c + "'");
                }
            }
        }
    }

    private void number(List<Token> tokens) {
        while (isDigit(peek())) {
            advance();
        }
        tokens.add(makeToken(TokenType.NUMBER, stringValue()));
    }

    private void identifier(List<Token> tokens) {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = stringValue();
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.ID);
        tokens.add(makeToken(type, text));
    }

    private void string(List<Token> tokens) {
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') {
                line++;
                column = 1;
            }
            advance();
        }

        if (isAtEnd()) {
            throw error("Unterminated string literal");
        }

        advance(); 
        String raw = input.substring(start + 1, current - 1);
        tokens.add(makeToken(TokenType.STRING, raw));
    }

    private Token makeToken(TokenType type, String lexeme) {
        return new Token(type, lexeme, start, line, columnAt(start));
    }

    private int columnAt(int pos) {
        int col = 1;
        for (int i = 0; i < pos && i < input.length(); i++) {
            if (input.charAt(i) == '\n') {
                col = 1;
            } else {
                col++;
            }
        }
        return col;
    }

    private String stringValue() {
        return input.substring(start, current);
    }

    private boolean match(char expected) {
        if (isAtEnd() || input.charAt(current) != expected) {
            return false;
        }
        current++;
        column++;
        return true;
    }

    private char peek() {
        return isAtEnd() ? '\0' : input.charAt(current);
    }

    private char advance() {
        char c = input.charAt(current++);
        if (c != '\n') {
            column++;
        }
        return c;
    }

    private boolean isAtEnd() {
        return current >= input.length();
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private RuntimeException error(String message) {
        return new RuntimeException(String.format(
                "[Lexer Error] %s at Line %d, Column %d",
                message, line, column
        ));
    }
}
