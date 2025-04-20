package org.upl.parser.manual;

import org.upl.Main;
import org.upl.graph.ASTGraph;
import org.upl.lexer.Token;
import org.upl.lexer.TokenType;
import org.upl.parser.grammar.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.InvalidParameterException;
import java.util.*;

public class TopDownParser {
    public final Grammar grammar;
    public final Map<NonTerminal, Map <TokenType, Production>> parsingTable;
    public TopDownParser(Reader reader) {
        grammar = new Grammar(reader);
        grammar.removeEpsilonRule();
        grammar.eliminateLeftRecursion();
        grammar.removeLeftFactoring();
        grammar.initializeFirstAndFollow();
        grammar.calculateFirst();
        grammar.calculateFollow();

        parsingTable = new HashMap<>();
        for (NonTerminal nonTerminal : grammar.nonTerminalList) {
            parsingTable.put(nonTerminal, new HashMap<>());
        }
        createParsingTable();
    }

    private void addEntriesToParsingTable(NonTerminal A, Terminal a, Production p) {
        if (a.equals(Grammar.epsilon)) {
            return;
        }
        TokenType tokenType = Terminal.toTokenType(a);
        if (parsingTable.get(A).containsKey(tokenType)) {
            throw new InvalidParameterException("Grammar is not LR(1)!");
        }

        parsingTable.get(A).put(tokenType, p);
    }

    private void createParsingTable() {
        for (Production production : grammar.productionList) {
            for (Symbol alpha : production.right) {
                if (alpha instanceof Terminal) {
                    addEntriesToParsingTable(production.left, (Terminal) alpha, production);
                } else if (alpha instanceof NonTerminal) {
                    for (Terminal a : grammar.first.get(alpha)) {
                        addEntriesToParsingTable(production.left, a, production);
                    }
                }
                if (grammar.nullable(alpha)) {
                    if (alpha.equals(production.right.getLast())) {
                        for (Terminal b : grammar.follow.get(production.left)) {
                            addEntriesToParsingTable(production.left, b, production);
                        }
                    }
                } else {
                    break;
                }
            }
        }
    }

    private Production move(NonTerminal A, TokenType token) {
        return parsingTable.get(A).get(token);
    }

    public ASTGraph parse(List<Token> tokens) {
        Stack<Integer> stack = new Stack<>();
        stack.add(0);
        List<Symbol> graphNode = new ArrayList<>();
        List<List<Integer>> adj = new ArrayList<>();
        graphNode.add(grammar.start);
        adj.add(new ArrayList<>());

        int currentToken = 0;
        while (!stack.empty() && currentToken < tokens.size()) {
            int node = stack.pop();
            Symbol now = graphNode.get(node);
            Token input = tokens.get(currentToken);
            if (now instanceof Terminal) {
                if (now.equals(Grammar.epsilon)) {
                    continue;
                }
                if (Terminal.toTokenType((Terminal) now)
                        == input.getType()) {
                    currentToken++;
                    graphNode.get(node).setObject(input);
                } else {
                    Main.compileError(input, String.format("Expected: %s", now));
                }
            } else if (now instanceof NonTerminal) {
                Production production = move((NonTerminal) now, input.getType());
                if (production != null) {
                    for (Symbol symbol : production.right.reversed()) {
                        adj.get(node).add(graphNode.size());
                        stack.add(graphNode.size());
                        graphNode.add(symbol);
                        adj.add(new ArrayList<>());
                    }
                } else {
                    if (now.value.startsWith("StatementLists")
                            || now.value.startsWith("Program")) {
                        Main.compileError(input, ("Unexpected character"));
                        currentToken++;
                        stack.add(node);
                    }
                }
            }
        }

        return new ASTGraph(graphNode, adj, 0);
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 1) {
            throw new FileNotFoundException("Missing CFG File!");
        }

        String filePath = args[0];
        Reader reader = new InputStreamReader(new FileInputStream(filePath));

        TopDownParser topDownParser = new TopDownParser(reader);

    }
}
