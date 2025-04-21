package org.upl.parser.grammar;

import org.upl.graph.Graph;
import org.upl.lexer.Token;
import org.upl.lexer.TokenType;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;

import static java.lang.Math.min;
import static org.upl.parser.grammar.Production.groupProductListBySymbol;

public class Grammar {
    public static List<Production> allCurrentNonTerminalAiProduction(
            List<Production> productionList,
            NonTerminal nonTerminal) {
        List<Production> res = new ArrayList<>();
        for (Production production : productionList) {
            if (production.left.equals(nonTerminal)) {
                List<Symbol> right = new ArrayList<>(production.right);
                res.add(new Production(production.left, right));
            }
        }
        return res;
    }

    public static final Terminal epsilon = new Terminal("ε");
    public static final Terminal eof = new Terminal("$");
    public static final String dot = ".";

    public NonTerminal start;
    public final List<NonTerminal> nonTerminalList;
    public final List<Terminal> terminalList;
    public final List<Production> productionList;
    public final Map<NonTerminal, Set<Terminal>> first = new HashMap<>();
    public final Map<NonTerminal, Set<Terminal>> follow = new HashMap<>();

    public Grammar(Reader reader) {
        this.nonTerminalList = new ArrayList<>();
        this.terminalList = new ArrayList<>();
        this.productionList = new ArrayList<>();

        String content = readInput(reader);

        String[] sections = content.split("###\n");
        for (String section : sections) {
            if (section.startsWith("TERMINAL:")) {
                processTerminal(section);
            } else if (section.startsWith("NON-TERMINAL:")) {
                processNonTerminal(section);
            } else if (section.startsWith("PRODUCTION_RULE:")) {
                processProductionRule(section);
            } else if (section.startsWith("START:")) {
                start = new NonTerminal(section.split(":")[1].trim());
            }
        }
    }

    public Grammar(NonTerminal start, List<NonTerminal> nonTerminalList,
                   List<Terminal> terminalList, List<Production> productionList) {
        this.start = start;
        this.nonTerminalList = nonTerminalList;
        this.terminalList = terminalList;
        this.productionList = productionList;
    }

    public void init() {
        removeEpsilonRule();
        eliminateLeftRecursion();
        removeLeftFactoring();
        initializeFirstAndFollow();
        calculateFirst();
        calculateFollow();
    }

    public Grammar getAugmentedGrammar() {
        NonTerminal newStart = new NonTerminal(String.format("%s-", start));

        List<NonTerminal> newNonTerminalList = new ArrayList<>();
        newNonTerminalList.add(newStart);
        newNonTerminalList.addAll(nonTerminalList);

        List<Terminal> newTerminalList = new ArrayList<>(terminalList);

        List<Production> newProductionList = new ArrayList<>();
        newProductionList.add(new Production(newStart, start));
        newProductionList.addAll(productionList);

        return new Grammar(newStart, newNonTerminalList, newTerminalList, newProductionList);
    }

    private String readInput(Reader reader) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n"); // Append the line to the content
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return content.toString();
    }

    private void processNonTerminal(String input) {
        input = input.replaceFirst("NON-TERMINAL:", "");
        String[] tokens = input.split(",");

        for (String token : tokens) {
            String trimmedToken = (token.trim());
            nonTerminalList.add(new NonTerminal(trimmedToken));
        }
    }

    private void processTerminal(String input) {
        input = input.replaceFirst("TERMINAL:", "");
        String[] tokens = input.split(",");

        for (String token : tokens) {
            String trimmedToken = (token.trim());

            if (trimmedToken.equals("EPSILON")) {
                terminalList.add(epsilon);
            } else if (trimmedToken.equals("EOF")) {
                terminalList.add(eof);
            } else if (!trimmedToken.isEmpty()) {
                terminalList.add(
                        new Terminal(new Token(TokenType.valueOf(trimmedToken), trimmedToken))
                );
            }
        }
    }

    private Symbol convert(String tokenName) {
        if (tokenName.equals("EPSILON")) {
            return epsilon;
        }

        if (tokenName.equals("EOF")) {
            return eof;
        }

        for (NonTerminal symbol : nonTerminalList) {
            if (symbol.toString().equals(tokenName)) {
                return symbol;
            }
        }

        for (Terminal symbol : terminalList) {
            if (symbol.toString().equals(tokenName)) {
                return symbol;
            }
        }

        return null;
    }

    private void processProductionRule(String input) {
        input = input.replaceFirst("PRODUCTION_RULE:", "");
        input = input.replaceAll("\n", " ");
        String[] rules = input.split(";");

        for (String rule : rules) {
            if (!rule.contains("->")) {
                continue;
            }

            String[] component = rule.split("->");

            Symbol left = convert(component[0].trim());
            if (!(left instanceof NonTerminal)) {
                throw new InvalidParameterException("");
            }

            String[] rights = component[1].split("\\|");
            for (String rightString : rights) {
                String[] rhs = rightString.trim().split(" ");
                List<Symbol> right = new ArrayList<>();
                for (String rightSymbol : rhs) {
                    String trimmedRightSymbol = rightSymbol.trim();
                    right.add(convert(trimmedRightSymbol));
                }

                productionList.add(new Production((NonTerminal) left, right));
            }
        }
    }

    private Set <NonTerminal> findNullable() {
        Set <NonTerminal> nullable = new HashSet<>();

        for (int iteration = 0; iteration < nonTerminalList.size(); iteration++) {
            boolean terminate = true;

            for (Production production : productionList) {
                if (production.nullable(nullable)) {
                    nullable.add(production.left);
                    terminate = false;
                }
            }

            if (terminate) {
                break;
            }
        }
        return nullable;
    }

    public void removeEpsilonRule() {
        terminalList.remove(epsilon);
        Set <NonTerminal> nullable = findNullable();
        List<Production> newProductionRule = new ArrayList<>();

        for (Production production : productionList) {
            NonTerminal left = production.left;
            List<Symbol> right = production.right;

            int countNullable = 0;
            for (Symbol symbol : right) {
                if (symbol instanceof NonTerminal && nullable.contains(symbol)) {
                    countNullable++;
                }
            }

            for (int mask = 0; mask < 1 << countNullable; mask++) {
                List<Symbol> newRight = new ArrayList<>();

                int currentNullableCount = 0;
                for (Symbol symbol : right) {

                    if (symbol instanceof NonTerminal && nullable.contains(symbol)) {
                        if ((mask >> currentNullableCount) % 2 == 1) {
                            newRight.add(symbol);
                        }

                        currentNullableCount++;
                    } else if (!symbol.equals(epsilon)) {
                        newRight.add(symbol);
                    }
                }

                if (newRight.isEmpty()) {
                    continue;
                }

                newProductionRule.add(
                        new Production(left, newRight)
                );
            }
        }

        productionList.clear();
        productionList.addAll(newProductionRule);
    }

    public void eliminateLeftRecursion() {
        List<List<Production>> productionGroup = groupProductListBySymbol(
                nonTerminalList,
                productionList,
                true
        );

        productionList.clear();
        List<NonTerminal> processedSymbols = new ArrayList<>();
        List<List<Production>> accepted = new ArrayList<>();
        for (int i = 0; i + 1 < productionGroup.size(); i++) {
            processedSymbols.add(nonTerminalList.get(i));
            List<List<Production>> groupOfI = groupProductListBySymbol(
                    processedSymbols,
                    productionGroup.get(i),
                    false
            );

            List<Production> nonRecursionGeneratedByI = new ArrayList<>(groupOfI.getLast());
            for (int j = 0; j < i; j++) {
                for (Production production : groupOfI.get(j)) {
                    production.right.removeFirst();
                    for (Production goodJ : accepted.get(j)) {
                        List<Symbol> newRight = new ArrayList<>(goodJ.right);
                        newRight.addAll(production.right);
                        nonRecursionGeneratedByI.add(new Production(
                                production.left,
                                newRight
                        ));
                    }
                }
            }

            if (!groupOfI.get(i).isEmpty()) {
                NonTerminal newSymbol = new NonTerminal(nonTerminalList.get(i).value + "'");
                for (Production recursion : groupOfI.get(i)) {
                    recursion.right.removeFirst();
                    recursion.right.add(newSymbol);
                    productionList.add(
                            new Production(
                                    newSymbol,
                                    recursion.right
                            )
                    );
                }
                productionList.add(
                        new Production(
                                newSymbol,
                                epsilon
                        )
                );
                nonTerminalList.add(newSymbol);

                for (Production nonRecursion : nonRecursionGeneratedByI) {
                    nonRecursion.right.add(newSymbol);
                }
            }
            accepted.add(nonRecursionGeneratedByI);
            productionList.addAll(nonRecursionGeneratedByI);
        }
    }

    public void removeLeftFactoring() {
        int count = 0;
        while (true) {
            int mx = 0, mi = 0;
            for (int i = 0; i < productionList.size(); i++) {
                for (int j = i + 1; j < productionList.size(); j++) {
                    int cur = Production.lcp(productionList.get(i), productionList.get(j));

                    if (mx < cur) {
                        mx = cur;
                        mi = i;
                    }
                }
            }
            if (mx == 0) {
                break;
            }

            List<Production> remain = new ArrayList<>();
            Production best = productionList.get(mi);
            NonTerminal newSymbol = new NonTerminal(best.left.value + count);
            for (Production production : productionList) {
                if (Production.lcp(best, production) >= mx) {
                    List<Symbol> temp = new ArrayList<>(
                            production.right.subList(mx, production.right.size()));
                    if (temp.isEmpty()) {
                        temp.add(Grammar.epsilon);
                    }
                    remain.add(new Production(newSymbol, temp));
                } else {
                    remain.add(production);
                }
            }


            List<Symbol> temp = new ArrayList<>(best.right.subList(0, mx));
            temp.add(newSymbol);
            remain.add(new Production(best.left, temp));
            nonTerminalList.add(newSymbol);
            productionList.clear();
            productionList.addAll(remain);
            count++;
        }
    }

    public void initializeFirstAndFollow() {
        for (NonTerminal symbol : nonTerminalList) {
            first.put(symbol, new HashSet<>());
            follow.put(symbol, new HashSet<>());
        }
    }

    public boolean nullable(Symbol symbol) {
        if (symbol instanceof Terminal) {
            return symbol.equals(epsilon);
        } else if (symbol instanceof NonTerminal) {
            return first.get(symbol).contains(epsilon);
        }
        return false;
    }

    public void calculateFirst() {
        Graph graph = new Graph(nonTerminalList.size());
        for (Production production : productionList) {
            Symbol rightFirst = production.right.getFirst();
            if (rightFirst instanceof NonTerminal) {
                graph.addDirectionalEdge(
                        nonTerminalList.indexOf(production.left),
                        nonTerminalList.indexOf(rightFirst)
                );
            }
        }

        if (graph.hasCycle()) {
            throw new InvalidParameterException("Grammar graph still has cycle!");
        }

        List<List<Production>> grouped = Production.groupProductListBySymbol(
            nonTerminalList,
            productionList,
            true
        );
        for (int i : graph.getReversedTopo()) {
            NonTerminal current = nonTerminalList.get(i);
            for (Production production : grouped.get(i)) {
                for (Symbol symbol : production.right) {
                    if (symbol instanceof Terminal) {
                        first.get(current).add((Terminal) symbol);
                    } else if (symbol instanceof NonTerminal) {
                        first.get(current).addAll(first.get(symbol));
                    }
                    if (!nullable(symbol)) {
                        break;
                    }
                }
            }
        }
    }

    public void processFollow(Production production) {
        for (int i = 0; i + 1 < production.right.size(); i++) {
            Symbol B = production.right.get(i);
            Symbol beta = production.right.get(i + 1);
            if (!(B instanceof NonTerminal)) {
                continue;
            }

            if (beta instanceof Terminal) {
                follow.get(B).add((Terminal) beta);
            } else if (beta instanceof NonTerminal) {
                follow.get(B).addAll(first.get(beta));
            }
        }
    }

    public void calculateFollow() {
        follow.get(start).add(eof);
        Graph graph = new Graph(nonTerminalList.size());

        for (Production production : productionList) {
            for (Symbol symbol : production.right.reversed()) {
                if (symbol instanceof NonTerminal) {
                    graph.addDirectionalEdge(
                            nonTerminalList.indexOf(symbol),
                            nonTerminalList.indexOf(production.left)
                    );
                }
                if (!nullable(symbol)) {
                    break;
                }
            }
            processFollow(production);
        }

        for (int step = 0; step < graph.getV(); step++) {
            for (int i = 0; i < graph.getV(); i++) {
                for (int v : graph.getE().get(i)) {
                    follow.get(nonTerminalList.get(i))
                            .addAll(follow.get(nonTerminalList.get(v)));
                }
            }
        }

    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();

        res.append("Terminals:");
        for (Terminal terminal : terminalList) res.append(String.format(" %s", terminal));
        res.append("\n");
        res.append("Non-terminals:");
        for (NonTerminal nonTerminal : nonTerminalList) res.append(String.format(" %s", nonTerminal));
        res.append("\n");
        res.append("Productions Rules:\n");
        for (int i = 0; i < productionList.size(); i++) {
            res.append(String.format("%d, %s\n", i + 1, productionList.get(i)));
        }
        res.append("FIRST:\n");
        first.forEach(((symbol, terminals) -> {
            res.append(String.format("first(%s): ", symbol));
            for (Terminal terminal : terminals) {
                res.append(String.format("%s ", terminal));
            }
            res.append("\n");
        }));

        res.append("FOLLOW:\n");
        follow.forEach(((nonTerminal, terminals) -> {
            res.append(String.format("follow(%s): ", nonTerminal));
            for (Terminal terminal : terminals) {
                res.append(String.format("%s ", terminal));
            }
            res.append("\n");
        }));
        res.append("\n");

        return res.toString();
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 1) {
            throw new FileNotFoundException("Missing CFG File!");
        }

        String filePath = args[0];
        Reader reader = new InputStreamReader(new FileInputStream(filePath));

        Grammar grammarParser = new Grammar(reader);
        grammarParser.removeEpsilonRule();
        grammarParser.eliminateLeftRecursion();
        grammarParser.removeLeftFactoring();
        grammarParser.initializeFirstAndFollow();
        grammarParser.calculateFirst();
        grammarParser.calculateFollow();

        System.out.println(grammarParser);
    }
}
