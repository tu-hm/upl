package org.upl.object.statement;

public class If {
    public final Conditional condition;
    public final StatementList thenBranch;
    public final StatementList elseBranch;

    public If(Conditional condition, StatementList thenBranch, StatementList elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
}
