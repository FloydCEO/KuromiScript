// src/lexer/TokenType.java
package lexer;

public enum TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    LEFT_BRACKET, RIGHT_BRACKET,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, COLON,
    SLASH, STAR, PERCENT,

    // One or two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, OR, NOT,
    TRUE, FALSE, NULL,
    IF, ELSE, WHILE, FOR, IN,
    GAME, LET, FN, RETURN,
    LOAD, DRAW, SHOW, PLAY, PRINT,

    EOF
}