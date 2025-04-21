package org.upl.parser.manual;

import org.upl.Main;
import org.upl.graph.ASTGraph;
import org.upl.lexer.Token;
import org.upl.parser.Parser;
import org.upl.parser.grammar.*;
import org.upl.parser.grammar.lr0.Action;
import org.upl.parser.grammar.lr0.ActionType;
import org.upl.parser.grammar.lr0.SLRParsingTable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class BottomUp extends Parser {
    private final SLRParsingTable table;
    public BottomUp(Reader reader) {
        grammar = new Grammar(reader).getAugmentedGrammar();
        grammar.init();
        grammar.removeEpsilonRule();
        table = new SLRParsingTable(grammar);

        System.out.println(table);
    }

    @Override
    public ASTGraph parse(List<Token> tokens) {
        Stack<Integer> states = new Stack<>();
        Stack<Integer> symbols = new Stack<>();
        List<Symbol> graphNode = new ArrayList<>();
        List<List<Integer>> adj = new ArrayList<>();
        states.add(table.initState);
        int currentToken = 0;
        while (true) {
            for (int x : states) {
                System.out.printf("%d ", x);
            }
            System.out.println();

            for (int x : symbols) {
                System.out.printf("%d %s ", x, graphNode.get(x));
            }
            System.out.println();

            if (states.empty()) {
                break;
            } else if (currentToken == tokens.size()) {
                break;
            }

            Token input = tokens.get(currentToken);
            int state = states.peek();
            Action action = table.action.get(state).get(input.getType());
            if (action == null) {
                Main.compileError(input, "Unexpected token");
                if (state == table.initState) {
                    currentToken++;
                } else {
                    states.pop();
                }
                continue;
            }

            if (action.actionType() == ActionType.SHIFT) {
                System.out.printf("shift %s\n", input.getLexeme());

                int j = action.number();
                states.add(j);
                symbols.add(graphNode.size());
                graphNode.add(new Terminal(input));
                adj.add(new ArrayList<>());
                currentToken++;
            } else if (action.actionType() == ActionType.REDUCE) {
                int j = action.number();
                Production production = grammar.productionList.get(j);
                List<Integer> nw = new ArrayList<>();
                for (int k = 0; k < production.right.size(); k++) {
                    nw.add(symbols.pop());
                    states.pop();
                }
                symbols.add(graphNode.size());
                graphNode.add(production.left);
                System.out.printf("reduce by %s\n", production);
                adj.add(nw);

                int t = states.peek();
                states.push(table.go.get(t).get(production.left));
            } else if (action.actionType() == ActionType.ACCEPT) {
                break;
            }
        }

        return new ASTGraph(graphNode, adj, adj.size() - 1);
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 1) {
            throw new FileNotFoundException("Missing CFG File!");
        }

        String filePath = args[0];
        Reader reader = new InputStreamReader(new FileInputStream(filePath));

        BottomUp bottomUp = new BottomUp(reader);

    }
}
