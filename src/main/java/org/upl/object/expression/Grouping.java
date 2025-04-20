package org.upl.object.expression;

import org.upl.lexer.Token;

public class Grouping extends Expression {
    public final Expression expression;

    public Grouping (Expression expression, Token leftParenthesis, Token rightParenthesis) {
        this.expression = expression;
    }
}
