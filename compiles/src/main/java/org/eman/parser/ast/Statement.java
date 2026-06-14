package org.eman.parser.ast;

import org.eman.parser.AstVisitor;

public abstract class Statement {
    public abstract <T> T accept(AstVisitor<T> visitor);
}
