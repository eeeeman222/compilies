package org.eman.parser.ast;

import org.eman.parser.AstVisitor;

public class IfStatement extends Statement {
    private final Expression condition;
    private final Statement thenBranch; 
    private final Statement elseBranch; 

    public IfStatement(Expression condition, Statement thenBranch) {
        this(condition, thenBranch, null);
    }

    public IfStatement(Expression condition, Statement thenBranch, Statement elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getThenBranch() {
        return thenBranch;
    }

    public Statement getElseBranch() {
        return elseBranch;
    }

    public boolean hasElseBranch() {
        return elseBranch != null;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitIfStatement(this);
    }
}
