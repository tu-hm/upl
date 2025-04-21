package org.upl.parser.grammar.lr0;

import org.upl.parser.grammar.Grammar;
import org.upl.parser.grammar.Production;
import org.upl.parser.grammar.Symbol;

public class Item implements Comparable<Item> {
    public final Production production;
    public final int dotPos;
    public Item(Production production, int dotPos) {
        this.production = production;
        this.dotPos = dotPos;
        if (dotPos < 0 || production.right.size() < dotPos) {
            throw new RuntimeException(String.format("Invalid dotPos (%d) where size is (%d)", dotPos, production.right.size()));
        }
    }
    @Override
    public int compareTo(Item item) {
        int cmp = production.compareTo(item.production);
        if (cmp != 0) return cmp;
        return Integer.compare(dotPos, item.dotPos);
    }
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(production.left).append(" ->");
        for (int i = 0; i < production.right.size(); ++ i) {
            if (i == dotPos) {
                res.append(" ").append(Grammar.dot);
            }
            res.append(" ").append(production.right.get(i));
        }
        if (dotPos == production.right.size()) {
            res.append(" ").append(Grammar.dot);
        }
        return res.toString();
    }
    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof Item item)) {
            if (!this.production.equals(item.production)) return false;
            return this.dotPos == item.dotPos;
        }
        return false;
    }
    public boolean isDotAtEnd() {
        return (dotPos >= production.right.size());
    }
    public Symbol getSymbolAfterDot() {
        if (isDotAtEnd()) throw new RuntimeException("Trying to get symbol after dot (which is at end)");
        return production.right.get(dotPos);
    }
    public Item advance() {
        return new Item(production, dotPos + 1);
    }
}