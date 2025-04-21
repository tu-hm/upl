package org.upl.graph;

import org.upl.Main;
import org.upl.Pair;
import org.upl.lexer.Token;
import org.upl.object.statement.StatementList;
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

    public ASTGraph(List<Symbol> graphNode, List<List<Integer>> adj, int root) {
        this.graphNode = graphNode;
        this.adj = adj;
        this.root = root;
    }

    private void DFS(int node, int depth) {
        for (int i = 0; i < depth; i++) {
            if (i == depth - 2) {
                System.out.print("┕");
            } else {
                System.out.print(i % 2 == 0 ? "|" : " ");
            }
        }
        System.out.print(graphNode.get(node));
        if (graphNode.get(node) instanceof Terminal) {
            assert (graphNode.get(node).object instanceof Token);
            Token current = (Token) graphNode.get(node).object;
            System.out.printf(" %s at line: %d, column: %d",
                    current.getLexeme(),
                    current.getLine(),
                    current.getColumn());
        }
        System.out.println();
        for (int x : adj.get(node)) {
            DFS(x, depth + 2);
        }
    }

    public void drawSyntaxTree() {
        DFS(root, 0);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}