package org.eman.parser.ast;

import org.eman.parser.AstVisitor;

public class PrintStatement extends Statement {
    private final Expression expression;

    public PrintStatement(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitPrintStatement(this);
    }
}
