package org.upl.parser.grammar;

import org.upl.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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

    public boolean nullable(Set<NonTerminal> nullNonTerminal) {
        for (Symbol symbol : right) {
            if (symbol instanceof NonTerminal && nullNonTerminal.contains(symbol)) {
                continue;
            }
            if (symbol.equals(Grammar.epsilon)) {
                continue;
            }

            return false;
        }

        return true;
    }

    public static List<List<Production>> groupProductListBySymbol(
                List<NonTerminal> nonTerminalList,
                List<Production> productionList,
                boolean left
            ) {
        List<List<Production>> result = new ArrayList<>();
        for (int i = 0; i < nonTerminalList.size(); i++) {
            result.add(new ArrayList<>());
        }

        List<Production> remain = new ArrayList<>();
        for (Production production : productionList) {
            Symbol groupBy = left ? production.left : production.right.getFirst();
            if (groupBy instanceof NonTerminal && nonTerminalList.contains(groupBy)) {
                result.get(nonTerminalList.indexOf(groupBy)).add(production);
            } else {
                remain.add(production);
            }
        }
        result.add(remain);

        return result;
    }

    public static int lcp(Production l, Production r) {
        if (!l.left.equals(r.left)) {
            return 0;
        }

        int lcp = 0;
        for (int i = 0; i < Math.min(l.right.size(), r.right.size()); i++) {
            if (l.right.get(i).equals(r.right.get(i)) &&
                    (!l.right.get(i).equals(Grammar.epsilon))) {
                lcp++;
            }
        }
        return lcp;
    }
}