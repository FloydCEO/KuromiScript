// src/lexer/Lexer.java
package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("Kuromi", TokenType.KUROMI);
        keywords.put("<Start>", TokenType.START);
        keywords.put("<Close>", TokenType.CLOSE);
        keywords.put("<Game Start>", TokenType.START);
        keywords.put("<Close Game>", TokenType.CLOSE);
        keywords.put("LoadImage", TokenType.LOAD_IMAGE);
        keywords.put("DisplayImage", TokenType.DISPLAY_IMAGE);
        keywords.put("ShowText", TokenType.SHOW_TEXT);
        keywords.put("PlaySound", TokenType.PLAY_SOUND);
        keywords.put("DrawRectangle", TokenType.DRAW_RECTANGLE);
        keywords.put("DrawCircle", TokenType.DRAW_CIRCLE);
        keywords.put("DrawLine", TokenType.DRAW_LINE);
        keywords.put("DrawTriangle", TokenType.DRAW_TRIANGLE);
        keywords.put("Print", TokenType.PRINT);
        keywords.put("Let", TokenType.LET);
        keywords.put("Repeat", TokenType.REPEAT);
        keywords.put("Times", TokenType.TIMES);
        keywords.put("Wait", TokenType.WAIT);
        keywords.put("If", TokenType.IF);
        keywords.put("Then", TokenType.THEN);
        keywords.put("Else", TokenType.ELSE);
        keywords.put("centered", TokenType.CENTERED);
        keywords.put("left", TokenType.LEFT);
        keywords.put("right", TokenType.RIGHT);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).type == TokenType.IDENTIFIER) {
                    addToken(TokenType.CALL);
                } else {
                    addToken(TokenType.LEFT_PAREN);
                }
                break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case ':': addToken(TokenType.COLON); break;
            case '=': addToken(TokenType.EQUAL); break;
            case '"': string(); break;
            case ',': addToken(TokenType.COMMA); break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '/':
                if (peek() == '/') {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (peek() == '*') {
                    advance();
                    while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
                        if (peek() == '\n') line++;
                        advance();
                    }
                    if (!isAtEnd()) advance();
                    if (!isAtEnd()) advance();
                } else {
                    error("Unexpected character.");
                }
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else if (c == '<') {
                    identifier();
                } else {
                    error("Unexpected character: " + c);
                }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek()) || peek() == '<' || peek() == '>' || peek() == ' ') advance();
        String text = source.substring(start, current).replace(" ", "");
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            error("Unterminated string.");
            return;
        }
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void error(String message) {
        System.err.println("[line " + line + "] Error: " + message);
    }
}