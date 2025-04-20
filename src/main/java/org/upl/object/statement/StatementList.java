package org.upl.object.statement;

import java.util.List;

public class StatementList extends Statement {
    public final List<Statement> statements;
    public StatementList(List<Statement> statements) {
        this.statements = statements;
    }
}
