package org.eman.parser.ast;

import org.eman.parser.AstVisitor;

import java.util.List;

public class CallExpression extends Expression {
    private final String name;
    private final List<Expression> arguments;

    public CallExpression(String name, List<Expression> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitCallExpression(this);
    }
}
