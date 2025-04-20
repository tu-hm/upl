package org.upl.object.expression;

import org.upl.lexer.Token;

public class Variable extends Expression {
    public final Token type;
    public final Token identifier;
    public Variable (Token type, Token identifier) {
        this.type = type;
        this.identifier = identifier;
    }
}
