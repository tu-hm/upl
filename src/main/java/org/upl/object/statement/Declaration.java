package org.upl.object.statement;

import org.upl.object.expression.Expression;
import org.upl.object.expression.Variable;

public class Declaration extends Statement {
    public final Variable variable;
    public final Expression initializer;

    public Declaration(Variable variable, Expression initializer) {
        this.variable = variable;
        this.initializer = initializer;
    }
}
