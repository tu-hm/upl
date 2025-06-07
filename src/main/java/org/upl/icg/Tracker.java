package org.upl.icg;

public class Tracker {
    int assignmentStatement;
    int doWhileStatement;
    int forStatement;
    int printStatement;
    int declarationStatement;
    int conditionalStatement;
    int ifStatement;
    int expression;

    public String nextAssignmentLabel() {
        assignmentStatement++;
        return "#assignment statement " + assignmentStatement;
    }

    public String nextDoWhileLabel() {
        doWhileStatement++;
        return "#do while statement " + doWhileStatement;
    }

    public String nextForLabel() {
        forStatement++;
        return "#for statement " + forStatement;
    }

    public String nextPrintLabel() {
        printStatement++;
        return "#print statement " + printStatement;
    }

    public String nextDeclarationLabel() {
        declarationStatement++;
        return "#declaration statement " + declarationStatement;
    }

    public String nextConditionalLabel() {
        conditionalStatement++;
        return "#conditional statement " + conditionalStatement;
    }

    public String nextIfLabel() {
        ifStatement++;
        return "#if statement " + ifStatement;
    }

    public String nextExpressionLabel() {
        expression++;
        return "#expression " + expression;
    }
}