package org.upl.parser.grammar;

import org.upl.lexer.Token;
import org.upl.lexer.TokenType;

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

    public static TokenType toTokenType(Terminal terminal) {
        if (terminal.equals(Grammar.eof)) {
            return TokenType.EOF;
        }
        return TokenType.valueOf(terminal.value);
    }
}