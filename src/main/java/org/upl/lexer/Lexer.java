package org.upl.lexer;

import org.upl.Main;

import java.util.*;

import static java.lang.Character.isDigit;
import static org.upl.lexer.TokenType.*;

public class Lexer implements ILexer {
    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("begin", BEGIN), Map.entry("end", END),
            Map.entry("if", IF), Map.entry("else", ELSE),
            Map.entry("for", FOR), Map.entry("while", WHILE),
            Map.entry("do", DO), Map.entry("print", PRINT),
            Map.entry("int", INT), Map.entry("bool", BOOL),
            Map.entry("true", TRUE), Map.entry("false", FALSE)
    );

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0, current = 0, line = 1, currentStartOfLine = 0;


    public Lexer(String source) {
        this.source = source;
    }

    private int column() {
        return current - currentStartOfLine;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char peek(int ahead) {
        int index = current + ahead;
        return index < source.length() ? source.charAt(index) : '\0';
    }

    private char peek() {
        return peek(0);
    }

    private char consume() {
        char c = source.charAt(current++);
        if (c == '\n') {
            line++;
            currentStartOfLine = current;
        }
        return c;
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private void addToken(TokenType type, Object value) {
        tokens.add(new Token(
                type,
                source.substring(start, current),
                value,
                line,
                column()
                )
        );
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void scanToken() {
        char c = consume();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case '+': addToken(PLUS); break;
            case '*': addToken(MULTIPLY); break;
            case '/': handleSlash(); break;
            case ';': addToken(SEMICOLON); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case ' ':
            case '\r':
            case '\t':
            case '\n':
                break; // Ignore whitespace
            default:
                if (isDigit(c)) {
                    number();
                } else if (Character.isLetter(c) || c == '_') {
                    identifier();
                } else {
                    Main.error(line, column(), "Unexpected character: '" + c + "'");
                }
        }
    }

    private void handleSlash() {
        if (match('/')) { // Single-line comment
            while (!isAtEnd() && peek() != '\n') consume();
        } else if (match('*')) { // Multi-line comment
            while (!isAtEnd() && !(peek() == '*' && peek(1) == '/')) consume();
            if (!isAtEnd()) {
                consume(); // Consume '*'
                consume(); // Consume '/'
            }
        } else {
            Main.error(line, column(),"Unexpected /");
        }
    }

    private void number() {
        while (isDigit(peek())) consume();
        addToken(NUMBER, Integer.parseInt(source.substring(start, current)));
    }

    private boolean isLetter(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    private void identifier() {
        while (isLetter(peek())) consume();
        while (isDigit(peek())) consume();
        String text = source.substring(start, current);
        TokenType type = KEYWORDS.get(text);
        if (type == null) type = ID;
        if (type == TRUE || type == FALSE) {
            addToken(type, Boolean.parseBoolean(source.substring(start, current)));
            return;
        }
        addToken(type);
    }

    @Override
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "EOF", null, line, column()));
        return tokens;
    }
}
