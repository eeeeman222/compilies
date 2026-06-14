package org.eman.parser.ast;

import org.eman.parser.AstVisitor;

public class VarStatement extends Statement {
    private final String name;
    private final Expression initializer; 

    public VarStatement(String name, Expression initializer) {
        this.name = name;
        this.initializer = initializer;
    }

    public String getName() {
        return name;
    }

    public Expression getInitializer() {
        return initializer;
    }

    public boolean hasInitializer() {
        return initializer != null;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitVarStatement(this);
    }
}
