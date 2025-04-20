package org.upl.graph;

import org.upl.Main;
import org.upl.Pair;
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
}