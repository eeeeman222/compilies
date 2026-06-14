package org.eman.parser.ast;

import org.eman.parser.AstVisitor;

public class IndexAssignExpression extends Expression {
    private final Expression array;
    private final Expression index;
    private final Expression value;

    public IndexAssignExpression(Expression array, Expression index, Expression value) {
        this.array = array;
        this.index = index;
        this.value = value;
    }

    public Expression getArray() {
        return array;
    }

    public Expression getIndex() {
        return index;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIndexAssignExpression(this);
    }
}
