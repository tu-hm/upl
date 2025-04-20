package org.upl.object.statement;

import org.upl.object.expression.Binary;

public class Conditional extends Statement {
    public final Binary condition;
    public Conditional(Binary condition) {
        this.condition = condition;
    }
}
