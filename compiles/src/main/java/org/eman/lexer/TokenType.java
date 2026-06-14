package org.eman.lexer;

public enum TokenType {
    NUMBER,
    ID,
    STRING,
    VAR,
    PRINT,
    FUN,
    RETURN,

    IF, ELSE,
    WHILE,

    PLUS, MINUS, STAR, SLASH,
    EQ, EQEQ, EXCL, NEQ,
    LT, GT, LTEQ, GTEQ,
    AND, OR,

    LPAREN, RPAREN,
    LBRACE, RBRACE,
    LBRACKET, RBRACKET,
    COMMA,
    SEMICOLON,

    EOF
}
