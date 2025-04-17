package org.upl.lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class JFlexLexer implements ILexer {
    private final JFlexScanner scanner;
    public JFlexLexer(Reader reader) {
        scanner = new JFlexScanner(reader);
    }

    @Override
    public List<Token> scanTokens() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            try {
                Token lastToken = scanner.yylex();
                if (lastToken == null) continue;
                tokens.add(lastToken);
                if (lastToken.getType() == TokenType.EOF) {
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return tokens;
    }
}
