package org.upl.parser.grammar.lr0;

import org.upl.parser.grammar.Grammar;
import org.upl.parser.grammar.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class CanonicalCollection {
    public final List<ItemSet> itemSetList = new ArrayList<>();

    /**
     * @param augmentedGrammar an augmented grammar (not augmented one)
     */
    public CanonicalCollection(Grammar augmentedGrammar) {
        {
            ItemSet i0 = new ItemSet(augmentedGrammar, new TreeSet<>());
            i0.items.add(new
                    Item(Grammar.allCurrentNonTerminalAiProduction(augmentedGrammar.productionList, augmentedGrammar.start).getFirst(), 0));
            itemSetList.add(i0.closure());
        }
        List<Symbol> symbols = new ArrayList<>();
        symbols.addAll(augmentedGrammar.nonTerminalList);
        symbols.addAll(augmentedGrammar.terminalList);
        while (true) {
            List<ItemSet> toAdd = new ArrayList<>();

            for (ItemSet i : itemSetList) {
                for (Symbol symbol : symbols) {
                    ItemSet next = i.go(symbol);
                    if (!next.items.isEmpty() && !itemSetList.contains(next)) {
                        toAdd.add(next);
                    }
                }
            }

            if (toAdd.isEmpty()) break;
            itemSetList.addAll(toAdd);
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < itemSetList.size(); ++ i) {
            ItemSet itemSet = itemSetList.get(i);
            res.append(String.format("I%d:\n", i)).append(itemSet.toString()).append("\n");
        }
        return res.toString();
    }
}