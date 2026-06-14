package org.eman.parser.ast;

import org.eman.parser.AstVisitor;

import java.util.List;

public class FunctionStatement extends Statement {
    private final String name;
    private final List<String> params;
    private final List<Statement> body;

    public FunctionStatement(String name, List<String> params, List<Statement> body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<String> getParams() {
        return params;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visitFunctionStatement(this);
    }
}
