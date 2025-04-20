package org.upl.object.statement;

public class DoWhile extends Statement {
    public final StatementList body;
    public final Conditional condition;

    public DoWhile(StatementList body, Conditional condition) {
        this.body = body;
        this.condition = condition;
    }
}
