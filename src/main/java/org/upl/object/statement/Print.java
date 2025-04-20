package org.upl.object.statement;

import org.upl.object.expression.Expression;

public class Print extends Statement {
    public final Expression expression;
    public Print(Expression expression) {
        this.expression = expression;
    }
}
