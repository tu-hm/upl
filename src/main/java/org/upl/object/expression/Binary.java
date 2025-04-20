package org.upl.object.expression;

import org.upl.lexer.Token;

public class Binary extends Expression {
    public final Expression left;
    public final Token operator;
    public final Expression right;

    public Binary(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
}