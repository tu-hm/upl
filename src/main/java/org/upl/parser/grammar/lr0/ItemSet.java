package org.upl.parser.grammar.lr0;

import org.upl.parser.grammar.Grammar;
import org.upl.parser.grammar.NonTerminal;
import org.upl.parser.grammar.Production;
import org.upl.parser.grammar.Symbol;

import java.util.*;

public class ItemSet {
    public final Grammar grammar;
    public final Set<Item> items;
    public ItemSet(Grammar grammar, Set<Item> itemSet) {
        this.grammar = grammar;
        this.items = itemSet;
    }
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();

        List<Item> itemList = new ArrayList<>(items);
        {
            Map<Production, Integer> order = new TreeMap<>();
            for (int i = 0; i < grammar.productionList.size(); ++ i) {
                order.put(grammar.productionList.get(i), i);
            }

            itemList.sort((lhs, rhs) -> {
                int lhsd = 0;
                if (lhs.dotPos == 0) lhsd = 1;
                int rhsd = 0;
                if (rhs.dotPos == 0) rhsd = 1;
                if (lhsd != rhsd) {
                    return Integer.compare(lhsd, rhsd);
                }

                return Integer.compare(order.get(lhs.production), order.get(rhs.production));
            });
        }
        itemList.forEach((item -> res.append(item).append("\n")));

        return res.toString();
    }
    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof ItemSet itemSet)) {
            // ItemSet of 2 different grammar is automatically false
            // Note that this is not using equals()
            if (grammar != itemSet.grammar) return false;
            return this.items.equals(itemSet.items);
        }
        return false;
    }
    private void addRecursively(Item item) {
        if (items.contains(item)) return;
        items.add(item);
        if (!item.isDotAtEnd()) {
            Symbol symbol = item.getSymbolAfterDot();
            if ((symbol instanceof NonTerminal nonTerminal)) {
                List<Production> bProduction = Grammar.allCurrentNonTerminalAiProduction(grammar.productionList, nonTerminal);
                for (Production production : bProduction) {
                    Item newItem = new Item(production, 0);
                    addRecursively(newItem);
                }
            }
        }
    }
    public ItemSet closure() {
        Set<Item> itemSet = new TreeSet<>();

        ItemSet res = new ItemSet(this.grammar, itemSet);
        for (Item item : this.items) {
            res.addRecursively(item);
        }

        return res;
    }

    public ItemSet go(Symbol symbol) {
        Set<Item> itemSet = new TreeSet<>();
        ItemSet res = new ItemSet(this.grammar, itemSet);

        for (Item item : this.items) {
            if (!item.isDotAtEnd()) {
                if (symbol.equals(item.getSymbolAfterDot())) {
                    res.addRecursively(item.advance());
                }
            }
        }

        return res;
    }
}