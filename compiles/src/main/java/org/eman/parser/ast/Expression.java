package org.eman.parser.ast;

import org.eman.parser.AstVisitor;

public abstract class Expression {
    public abstract <T> T accept(AstVisitor<T> visitor);
}
