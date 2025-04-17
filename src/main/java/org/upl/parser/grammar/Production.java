package org.upl.parser.grammar;

import org.upl.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Production implements Comparable<Production> {
    /**
     *  NonTerminalReducer: as its name, this is a reducer
     *  <br>
     *  When a production is reduced (by BottomUpParser), then its code will be called
     */
    public interface NonTerminalValueReducer {
        Object reduce(Object[] symbolValues);
    }

    public static final NonTerminalValueReducer nullReducer = symbolValues -> null;
    public final NonTerminal left;
    public final List<Symbol> right;
    public final NonTerminalValueReducer nonTerminalValueReducer;
    public Production(NonTerminal left, List<Symbol> right) {
        this.left = left;
        this.right = right;
        this.nonTerminalValueReducer = nullReducer;
    }

    public Production(NonTerminal left, Symbol... symbols) {
        this.left = left;
        this.right = new ArrayList<>();
        right.addAll(Arrays.asList(symbols));
        this.nonTerminalValueReducer = nullReducer;
    }

    public Production(NonTerminal left, List<Symbol> right, NonTerminalValueReducer nonTerminalValueReducer) {
        this.left = left;
        this.right = right;
        this.nonTerminalValueReducer = nonTerminalValueReducer;
    }

    @Override
    public int compareTo(Production production) {
        if (!left.equals(production.left)) {
            return left.compareTo(production.left);
        }

        return Utils.compareList(right, production.right);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(left).append(" ->");
        for (Symbol symbol : right) {
            stringBuilder.append(' ').append(symbol);
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Production production)) return false;

        if (!this.left.equals(production.left)) return false;
        if (this.right.size() != production.right.size()) {
            return false;
        }

        for (int i = 0; i < this.right.size(); i++) {
            if (!this.right.get(i).equals(production.right.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     *  Remove ε, if necessary
     */
    public void trim() {
        right.removeIf((symbol -> symbol.equals(Grammar.epsilon)));
        if (right.isEmpty()) {
            right.add(Grammar.epsilon);
        }
    }

    public boolean isEpsilonProduction() {
        return right.size() == 1 && right.get(0).equals(Grammar.epsilon);
    }

    public NonTerminal reduce(Symbol[] symbols) {
        Object[] symbolValues = new Object[symbols.length];
        for (int i = 0; i < symbols.length; ++ i) {
            symbolValues[i] = symbols[i].object;
        }
        return new NonTerminal(left.toString(), nonTerminalValueReducer.reduce(symbolValues));
    }
}