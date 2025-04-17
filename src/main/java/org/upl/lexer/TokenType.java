package org.upl.lexer;

public enum TokenType {
    EOF,

    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    SEMICOLON,

    BEGIN, THEN, END,
    DO, WHILE, FOR, PRINT, IF, ELSE,

    INT, BOOL, ID,

    PLUS, MULTIPLY,
    EQUAL,
    EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    NUMBER, TRUE, FALSE,
}
