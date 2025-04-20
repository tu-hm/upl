package org.upl.object.statement;

import org.upl.object.expression.Expression;
import org.upl.object.expression.Variable;

public class Assignment extends Statement {
    public final Variable variable;
    public final Expression expression;

    public Assignment(Variable variable, Expression expression) {
        this.variable = variable;
        this.expression = expression;
    }
}
