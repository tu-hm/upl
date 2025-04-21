package org.upl.graph;

import org.upl.Main;
import org.upl.Pair;
import org.upl.lexer.Token;
import org.upl.object.statement.StatementList;
import org.upl.parser.grammar.Grammar;
import org.upl.parser.grammar.NonTerminal;
import org.upl.parser.grammar.Symbol;
import org.upl.parser.grammar.Terminal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ASTGraph {
    public List<Symbol> graphNode;
    public List<List<Integer>> adj;
    public int root;
    private StatementList statementList;

    private int space = 2;
    private int tin = 0;
    private final List<Integer> depth;
    private final List<List<Boolean>> range;
    private final List<Integer> timeIn;
    private final List<Integer> eulerTour;
    private final List<StringBuilder> result;

    public ASTGraph(List<Symbol> graphNode, List<List<Integer>> adj, int root) {
        this.graphNode = graphNode;
        this.adj = adj;
        this.root = root;
        range = new ArrayList<>();
        timeIn = new ArrayList<>();
        depth = new ArrayList<>();
        eulerTour = new ArrayList<>();
        result = new ArrayList<>();
        for (int i = 0; i < graphNode.size(); i++) {
            timeIn.add(0);
            range.add(new ArrayList<>());
            depth.add(0);
            result.add(new StringBuilder());
        }
    }

    private void DFS(int node) {
        for (int i = 0; i < depth.get(node) * space; i++) {
            result.get(tin).append(" ");
        }
        timeIn.set(node, tin);
        eulerTour.add(node);
        Symbol symbol = graphNode.get(node);

        result.get(tin).append(symbol);
        if ((symbol instanceof Terminal) && (!symbol.equals(Grammar.epsilon))) {
            Token token = (Token) symbol.object;
            result.get(tin).append(String.format(
                    " Token: %s at line %d, column %d",
                    token.getLexeme(), token.getLine(), token.getColumn()
            ));
        }

        tin++;
        for (int x : adj.get(node).reversed()) {
            depth.set(x, depth.get(node) + 1);
            DFS(x);
        }


    }

    public void drawSyntaxTree() {
        DFS(root);

        for (int line = 0; line < eulerTour.size(); line++) {
            int node = eulerTour.get(line);
            int maxRange = line;
            for (int i : adj.get(node)) {
                maxRange = Math.max(maxRange, timeIn.get(i));
            }

            for (int nline = line + 1; nline <= maxRange; nline++) {
                result.get(nline).setCharAt(space * depth.get(node), '│');
            }

            for (int i : adj.get(node)) {
                if (timeIn.get(i) == maxRange) {
                    result.get(maxRange).setCharAt(space * depth.get(node), '└');
                } else {
                    result.get(timeIn.get(i)).setCharAt(space * depth.get(node), '├');
                }
            }

        }

        for (int line = 0; line < eulerTour.size(); line++) {
            System.out.println(result.get(line));
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
