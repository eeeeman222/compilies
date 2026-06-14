package org.eman.parser.ast;

import org.eman.parser.AstVisitor;

public class AssignExpression extends Expression {
    private final String name; 
    private final Expression value; 

    public AssignExpression(String name, Expression value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitAssignExpression(this);
    }
}
