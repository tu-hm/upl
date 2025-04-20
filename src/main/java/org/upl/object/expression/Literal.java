package org.upl.object.expression;

import org.upl.lexer.Token;

public class Literal extends Expression {
    public final Token value;

    public Literal (Token value) {
        this.value = value;
    }
}
