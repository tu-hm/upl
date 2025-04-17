package org.upl.parser.grammar;

import org.upl.lexer.Token;

public class Terminal extends Symbol {
    public final Token token;
    public Terminal(Token token) {
        super(token.getType().name(), token);
        this.token = token;
    }

    public Terminal(String name) {
        super(name);
        this.token = null;
    }
}