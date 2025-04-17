package org.upl.parser.grammar;

import org.upl.lexer.Token;
import org.upl.lexer.TokenType;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;

public class Grammar {
    public static final Terminal epsilon = new Terminal("ε");
    public static final Terminal eof = new Terminal(new Token(TokenType.EOF, "$"));

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
            }
        }
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
        for (Production production : productionList) res.append(String.format("%s\n", production));


        return res.toString();
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 1) {
            throw new FileNotFoundException("Missing CFG File!");
        }

        String filePath = args[0];
        Reader reader = new InputStreamReader(new FileInputStream(filePath));

        Grammar grammarParser = new Grammar(reader);

        System.out.println(grammarParser);
    }
}
