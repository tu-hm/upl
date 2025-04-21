package org.upl.parser;

import org.upl.graph.ASTGraph;
import org.upl.lexer.Token;
import org.upl.parser.grammar.Grammar;

import java.util.List;

public abstract class Parser {
    public Grammar grammar;

    public abstract ASTGraph parse(List<Token> tokens);
}
