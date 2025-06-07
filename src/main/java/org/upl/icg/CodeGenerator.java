package org.upl.icg;

import org.upl.Main;
import org.upl.graph.ASTGraph;
import org.upl.lexer.Token;
import org.upl.parser.grammar.NonTerminal;
import org.upl.parser.grammar.Symbol;

import java.util.Arrays;
import java.util.List;

public class CodeGenerator {
    private final SymbolTable symbolTable;
    private final Tracker tracker;
    private final ASTGraph astGraph;
    private final Node code;

    public CodeGenerator(ASTGraph _astGraph) {
        symbolTable = new SymbolTable();
        tracker = new Tracker();
        astGraph = _astGraph;
        code = genCode(astGraph.root);
    }

    private boolean checkCodePass(Symbol s) {
        List<String> ls = Arrays.asList(
                "Program",
                "Program-",
                "Program1",
                "BracketStatementLists2",
                "StatementLists3",
                "Statement",
                "BracketStatementLists",
                "StatementLists",
                "IfStatement0",
                "ElseStatement"
        );

        return ls.contains(s.value);
    }

    private Node processCodePass(int node) {
        Node result = new Node();
        boolean first = true;
        for (int x : astGraph.adj.get(node).reversed()) {
            if (astGraph.graphNode.get(x) instanceof NonTerminal) {
                if (!first) {
                    result.code += "\n";
                }
                first = false;
                result.code += genCode(x);

            }
        }
        return result;
    }

    private Node processAssignmentStatement(int node) {
        List<Integer> adj = astGraph.adj.get(node);
        assert (adj.size() == 3);

        Node exp = genCode(adj.get(0));

        Token id = (Token) astGraph.graphNode.get(adj.get(2)).object;
        if (symbolTable.hasSymbol(id.getLexeme())) {
            return new Node(
                    tracker.nextAssignmentLabel()
                            + "\n" + exp.code
                            + "\n" + String.format(
                            "%s = $-1", id.getLexeme()
                    )
            );
        } else {
            Main.compileError(id, String.format(
                    "%s has not been declared", id.getLexeme()
            ));
            return null;
        }
    }

    private Node processDoWhileStatement(int node) {
        List<Integer> adj = astGraph.adj.get(node);
        assert (adj.size() == 6);
        Node conditionalStatement = genCode(adj.get(1));
        Node statementLists = genCode(adj.get(4));

        Node res = new Node();
        String doWhileLabel = tracker.nextDoWhileLabel();
        String conditionalLabel = tracker.nextConditionalLabel();
        res.code = doWhileLabel
                + "\n" + statementLists
                + "\n" + doWhileLabel + " " + conditionalLabel
                + "\n" + conditionalStatement
                + "\n" + String.format("if $-1 goto %s", doWhileLabel);

        return res;
    }

    private Node processForStatement(int node) {
        List<Integer> adj = astGraph.adj.get(node);
        assert (adj.size() == 8);
        Node declarationStatement = genCode(adj.get(5));
        Node conditionalStatement = genCode(adj.get(4));
        Node assignmentStatement = genCode(adj.get(2));
        Node statementLists = genCode(adj.get(0));

        Node res = new Node();
        String forLabel = tracker.nextForLabel();
        String declarationLabel = tracker.nextDeclarationLabel();
        String assignmentLabel = tracker.nextAssignmentLabel();
        String conditionalLabel = tracker.nextConditionalLabel();
        res.code = forLabel
                + "\n" + forLabel + " " + declarationLabel
                + "\n" + declarationStatement.code
                + "\n" + forLabel + " " + conditionalLabel
                + "\n" + conditionalStatement
                + "\n" + String.format("if $-1 goto %s.next", forLabel)
                + "\n" + statementLists
                + "\n" + forLabel + " " + assignmentLabel
                + "\n" + assignmentStatement
                + "\n" + String.format("goto %s", forLabel)
                + "\n" + String.format("%s.next", forLabel);
        return res;
    }

    private Node processPrintStatement(int node) {
        List<Integer> adj = astGraph.adj.get(node);
        assert (adj.size() == 5);
        Node expression = genCode(adj.get(2));
        String expressionLabel = tracker.nextExpressionLabel();
        return new Node(
            expressionLabel
                + "\n" + expression.code
                + "\n" + "print $-1"
        );
    }

    private Node processDeclarationStatement(int node) {
        List<Integer> adj = astGraph.adj.get(node);

        assert (adj.size() == 3);

        Node initDeclarator = genCode(adj.get(0));
        String type = processType(adj.get(2));
        Token id = (Token) astGraph.graphNode.get(adj.get(1)).object;
        if (symbolTable.hasSymbol(id.getLexeme())) {
            Main.compileError(id, String.format(
                    "Redeclaration of %s", id.getLexeme()
            ));
        }

        symbolTable.addSymbol(id.getLexeme(), type.equals("int") ? 4 : 1);

        if (initDeclarator.code.isEmpty()) {
            initDeclarator.code = type.equals("int") ? "0" : "false";
        }

        return new Node(
                tracker.nextDeclarationLabel()
                + "\n" + initDeclarator.code
                + "\n" + String.format("%s = $-1", id.getLexeme())
        );
    }

    private Node processInitDeclarator(int node) {
        List<Integer> adj = astGraph.adj.get(node);
        if (adj.size() == 1) {
            return new Node();
        } else {
            return genCode(adj.get(1));
        }
    }

    private Node processConditionalStatement(int node) {
        List<Integer> adj = astGraph.adj.get(node);
        assert (adj.size() == 3);

        Node leftOp  = genCode(adj.get(0));
        Node rightOp = genCode(adj.get(2));
        String comparisonOperator = processComparisonOperator(adj.get(1));
        String conditionalLabel = tracker.nextConditionalLabel();

        return new Node(
                conditionalLabel
                + "\n" + conditionalLabel + " " + tracker.nextExpressionLabel()
                + "\n" + leftOp
                + "\n" + conditionalLabel + " " + tracker.nextExpressionLabel()
                + "\n" + rightOp
                + "\n" + String.format(
                        "$-%d %s $-1",
                        rightOp.code.lines().count() + 2,
                        comparisonOperator
                )
        );
    }

    private Node processIfStatement(int node) {
        List<Integer> adj = astGraph.adj.get(node).reversed();

        Node conditionalStatement = genCode(adj.get(2));
        Node statementLists       = genCode(adj.get(4));
        Node elseStatement        = adj.size() == 6 ? genCode(adj.get(5)) : new Node();
        String ifLabel = tracker.nextIfLabel();

        return new Node(
                ifLabel
                + "\n" + ifLabel + " " + tracker.nextConditionalLabel()
                + "\n" + conditionalStatement
                + "\n" + String.format("if not $-1 goto %s.false", ifLabel)
                + "\n" + String.format("%s.true", ifLabel)
                + "\n" + statementLists.code
                + "\n" + String.format("goto %s.next", ifLabel)
                + "\n" + String.format("%s.false", ifLabel)
                + "\n" + elseStatement.code
                + "\n" + String.format("%s.next", ifLabel)
        );
    }

    private Node processExpression(int node) {
        List<Integer> adj = astGraph.adj.get(node).reversed();

        Node term = genCode(adj.get(0));
        if (adj.size() == 1) {
            return term;
        }

        Node expression1 = genCode(adj.get(1));
        return new Node(
                term.code
                + "\n" + expression1
                + "\n" + String.format(
                        "$-%d + $-1", expression1.code.lines().count() + 1
                )
        );
    }

    private Node processExpression1(int node) {
        List<Integer> adj = astGraph.adj.get(node).reversed();

        Node term = genCode(adj.get(1));
        if (adj.size() == 2) {
            return term;
        }

        Node expression1 = genCode(adj.get(2));
        return new Node(
                term.code
                        + "\n" + expression1
                        + "\n" + String.format(
                        "$-%d + $-1", expression1.code.lines().count() + 1
                )
        );
    }

    private Node processTerm(int node) {
        List<Integer> adj = astGraph.adj.get(node).reversed();

        Node factor = genCode(adj.get(0));
        if (adj.size() == 1) {
            return factor;
        }

        Node term1 = genCode(adj.get(1));
        return new Node(
                factor.code
                        + "\n" + term1.code
                        + "\n" + String.format(
                        "$-%d * $-1", term1.code.lines().count() + 1
                )
        );
    }

    private Node processTerm1(int node) {
        List<Integer> adj = astGraph.adj.get(node).reversed();

        Node factor = genCode(adj.get(1));
        if (adj.size() == 2) {
            return factor;
        }

        Node term1 = genCode(adj.get(2));
        return new Node(
                factor.code
                    + "\n" + factor.code
                    + "\n" + term1.code
                    + "\n" + String.format(
                            "$-%d * $-1", term1.code.lines().count() + 1
                )
        );
    }

    private Node processFactor(int node) {
        List<Integer> adj = astGraph.adj.get(node).reversed();
        if (adj.size() == 3) {
            return processExpression(adj.get(1));
        } else {
            Symbol symbol = astGraph.graphNode.get(adj.getFirst());
            if (symbol.value.equals("Literal")) {
                return processLiteral(adj.getFirst());
            } else {
                Token id = (Token) symbol.object;
                return new Node(id.getLexeme());
            }
        }
    }

    private String processType(int node) {
        List<Integer> adj = astGraph.adj.get(node).reversed();
        Token type = (Token) astGraph.graphNode.get(adj.getFirst()).object;
        return type.getLexeme();
    }

    private Node processLiteral(int node) {
        List<Integer> adj = astGraph.adj.get(node).reversed();
        Token type = (Token) astGraph.graphNode.get(adj.getFirst()).object;
        return new Node(type.getLexeme());
    }

    private String processComparisonOperator(int node) {
        List<Integer> adj = astGraph.adj.get(node).reversed();
        Token type = (Token) astGraph.graphNode.get(adj.get(0)).object;
        return type.getLexeme();
    }

    private Node genCode(int node) {
        Symbol cur = astGraph.graphNode.get(node);
        if (checkCodePass(cur)) {
            return processCodePass(node);
        }

        return switch (cur.value) {
            case "AssignmentStatement"  -> processAssignmentStatement(node);
            case "DoWhileStatement"     -> processDoWhileStatement(node);
            case "ForStatement"         -> processForStatement(node);
            case "PrintStatement"       -> processPrintStatement(node);
            case "DeclarationStatement" -> processDeclarationStatement(node);
            case "InitDeclarator"       -> processInitDeclarator(node);
            case "ConditionalStatement" -> processConditionalStatement(node);
            case "IfStatement"          -> processIfStatement(node);
            case "Expression"           -> processExpression(node);
            case "Expression'"          -> processExpression1(node);
            case "Term"                 -> processTerm(node);
            case "Term'"                -> processTerm1(node);
            case "Factor"               -> processFactor(node);
            default -> throw new IllegalArgumentException("Unknown statement type: " + cur.value);
        };
    }

    @Override
    public String toString() {
        return symbolTable.toString() + "\n" + code.toString();
    }
}
