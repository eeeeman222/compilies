package org.eman.parser.ast;

import org.eman.parser.AstVisitor;

public class IndexExpression extends Expression {
    private final Expression array;
    private final Expression index;

    public IndexExpression(Expression array, Expression index) {
        this.array = array;
        this.index = index;
    }

    public Expression getArray() {
        return array;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIndexExpression(this);
    }
}
