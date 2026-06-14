package org.eman.parser.ast;

import org.eman.parser.AstVisitor;
import org.eman.lexer.TokenType;

public class UnaryExpression extends Expression {
    private final TokenType operator; 
    private final Expression right;

    public UnaryExpression(TokenType operator, Expression right) {
        this.operator = operator;
        this.right = right;
    }

    public TokenType getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitUnaryExpression(this);
    }
}
