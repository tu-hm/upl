package org.upl.parser.grammar.lr0;

import org.upl.lexer.Token;
import org.upl.lexer.TokenType;
import org.upl.parser.grammar.Grammar;
import org.upl.parser.grammar.NonTerminal;
import org.upl.parser.grammar.Symbol;
import org.upl.parser.grammar.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SLRParsingTable {
    private final Grammar augmentedGrammar;
    private final CanonicalCollection c;
    public final List<Map<TokenType, Action>> action;
    public final List<Map<NonTerminal, Integer>> go;

    public final int initState;

    /**
     * @param augmentedGrammar an augmented grammar
     */
    public SLRParsingTable(Grammar augmentedGrammar) {
        this.augmentedGrammar = augmentedGrammar;
        // rule 1
        this.c = new CanonicalCollection(this.augmentedGrammar);
        int n = c.itemSetList.size();

        // rule 5
        initState = c.itemSetList.stream()
                .filter(itemSet -> itemSet.items.stream().
                        anyMatch(item -> item.production.left.equals(augmentedGrammar.start)))
                .map(c.itemSetList::indexOf)
                .findFirst().orElse(-1);

        if (initState == -1) {
            throw new RuntimeException(String.format("??? init is %d\n", initState));
        }
        action = new ArrayList<>(n);
        go = new ArrayList<>(n);
        for (int i = 0; i < n; ++ i) {
            action.add(new TreeMap<>());
            go.add(new TreeMap<>());
        }
        constructTable();
    }

    private void constructTable() {
        int n = c.itemSetList.size();

        for (int i = 0; i < n; ++ i) {
            ItemSet itemSet = c.itemSetList.get(i);
            for (Item item : itemSet.items) {
                if (!item.isDotAtEnd()) {
                    Symbol symbol = item.getSymbolAfterDot();
                    if (symbol instanceof Terminal terminal) {
                        // rule 2.a
                        int j = c.itemSetList.indexOf(itemSet.go(terminal));
                        if (terminal.equals(Grammar.epsilon)) {
                            continue;
                        }
                        action.get(i).put(Terminal.toTokenType(terminal), new Action(ActionType.SHIFT, j));
                    } else {
                        // rule 3
                        NonTerminal nonTerminal = (NonTerminal) symbol;
                        int j = c.itemSetList.indexOf(itemSet.go(nonTerminal));
                        go.get(i).put(nonTerminal, j);
                    }
                } else {
                    if (!item.production.left.equals(augmentedGrammar.start)) {
                        // rule 2.b
                        int j = augmentedGrammar.productionList.indexOf(item.production);
                        Action act = new Action(ActionType.REDUCE, j);
                        for (Terminal terminal : augmentedGrammar.follow.get(item.production.left)) {
                            TokenType tokenType = TokenType.EPSILON;
                            if (!terminal.equals(Grammar.epsilon)) {
                                tokenType = Terminal.toTokenType(terminal);
                            }

                            if (action.get(i).containsKey(tokenType)) {
                                Action act2 = action.get(i).get(tokenType);
                                if (act2.actionType() == ActionType.SHIFT) {
                                    System.err.printf("shift/reduce conflict in production %s on terminal %s\n", augmentedGrammar.productionList.get(j), terminal);
                                } else if (act2.actionType() == ActionType.REDUCE) {
                                    System.err.printf("reduce/reduce conflict in production %s on terminal %s\n", augmentedGrammar.productionList.get(j), terminal);
                                }
                            }
                            action.get(i).put(tokenType, act);
                        }
                    } else {
                        // rule 2.c
                        action.get(i).put(TokenType.EOF, new Action(ActionType.ACCEPT, 0));
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("Grammar:\n");
        res.append(augmentedGrammar);
        res.append("\nCanonical Collection:\n");
        res.append(c.toString());
        res.append("\n");

        for (int i = 0; i < action.size(); i++) {
            res.append(String.format("Current state %d:\n", i));
            for (int j = 0; j < augmentedGrammar.terminalList.size(); j++) {
                Terminal terminal = augmentedGrammar.terminalList.get(j);

                TokenType tokenType = TokenType.EOF;
                if (!terminal.equals(Grammar.eof)) {
                    tokenType = TokenType.valueOf(terminal.value);
                }
                if (action.get(i).containsKey(tokenType)) {
                    res.append(
                            String.format("Terminal: %s, Action: %s\n",
                                    terminal,
                                    action.get(i).get(tokenType))
                    );
                }
            }
            for (NonTerminal nonTerminal : go.get(i).keySet()) {
                res.append(
                        String.format("NonTerminal: %s, Goto: %s\n",
                                nonTerminal,
                                go.get(i).get(nonTerminal))
                );
            }
        }
        return res.toString();
    }
}
