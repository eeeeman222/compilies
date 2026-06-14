package org.eman.lexer;

public class Token {
    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public TokenType type;
    public String value;
    public int position;
    public int line;
    public int column;

    public Token(TokenType type, String value, int position, int line, int column) {
        this.type = type;
        this.value = value;
        this.position = position;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return "Token(Type: " + type + ", Value: " + value + "} at " + position;
    }

}
