package org.eman.parser.ast;

import org.eman.parser.AstVisitor;

public class NumberExpression extends Expression {
    private final double value;

    public NumberExpression(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitNumberExpression(this);
    }
}
