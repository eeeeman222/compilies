package org.eman.parser.ast;

import org.eman.parser.AstVisitor;

public class VariableExpression extends Expression {
    private final String name;

    public VariableExpression(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitVariableExpression(this);
    }
}
