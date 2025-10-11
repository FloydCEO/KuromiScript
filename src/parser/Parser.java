package parser;

import lexer.Token;
import lexer.TokenType;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<ASTNode.Stmt> parse() {
        List<ASTNode.Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            ASTNode.Stmt stmt = statement();
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        return statements;
    }

    private ASTNode.Stmt statement() {
        try {
            if (match(TokenType.GAME)) return gameStatement();
            if (match(TokenType.LET)) return letStatement();
            if (match(TokenType.FN)) return functionStatement();
            if (match(TokenType.RETURN)) return returnStatement();
            if (match(TokenType.IF)) return ifStatement();
            if (match(TokenType.WHILE)) return whileStatement();
            if (match(TokenType.FOR)) return forStatement();
            if (match(TokenType.LOAD)) return loadStatement();
            if (match(TokenType.DRAW)) return drawStatement();
            if (match(TokenType.SHOW)) return showStatement();
            if (match(TokenType.PLAY)) return playStatement();
            if (match(TokenType.PRINT)) return printStatement();
            if (match(TokenType.LEFT_BRACE)) return new ASTNode.Block(block());

            if (check(TokenType.IDENTIFIER)) {
                Token name = peek();
                if (peekNext().type == TokenType.EQUAL) {
                    advance();
                    advance();
                    ASTNode.Expr value = expression();
                    consumeStatementEnd();
                    return new ASTNode.Assignment(name, value);
                }
            }

            return expressionStatement();
        } catch (Exception e) {
            synchronize();
            return null;
        }
    }

    private ASTNode.Stmt gameStatement() {
        int width = (int) ((Number) consume(TokenType.NUMBER, "Expect width").literal).doubleValue();
        int height = (int) ((Number) consume(TokenType.NUMBER, "Expect height").literal).doubleValue();
        consume(TokenType.LEFT_BRACE, "Expect '{'");
        List<ASTNode.Stmt> body = block();
        return new ASTNode.GameStart(width, height, body);
    }

    private ASTNode.Stmt letStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name");
        consume(TokenType.EQUAL, "Expect '='");
        ASTNode.Expr initializer = expression();
        consumeStatementEnd();
        return new ASTNode.Let(name, initializer);
    }

    private ASTNode.Stmt functionStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Expect function name");
        consume(TokenType.LEFT_PAREN, "Expect '('");
        List<Token> params = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                params.add(consume(TokenType.IDENTIFIER, "Expect parameter name"));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')'");
        consume(TokenType.LEFT_BRACE, "Expect '{'");
        List<ASTNode.Stmt> body = block();
        return new ASTNode.Function(name, params, body);
    }

    private ASTNode.Stmt returnStatement() {
        ASTNode.Expr value = null;
        if (!check(TokenType.SEMICOLON) && !check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            value = expression();
        }
        consumeStatementEnd();
        return new ASTNode.Return(value);
    }

    private ASTNode.Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '('");
        ASTNode.Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')'");
        consume(TokenType.LEFT_BRACE, "Expect '{'");
        List<ASTNode.Stmt> thenBranch = block();
        List<ASTNode.Stmt> elseBranch = new ArrayList<>();
        if (match(TokenType.ELSE)) {
            consume(TokenType.LEFT_BRACE, "Expect '{'");
            elseBranch = block();
        }
        return new ASTNode.If(condition, thenBranch, elseBranch);
    }

    private ASTNode.Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '('");
        ASTNode.Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')'");
        consume(TokenType.LEFT_BRACE, "Expect '{'");
        List<ASTNode.Stmt> body = block();
        return new ASTNode.While(condition, body);
    }

    private ASTNode.Stmt forStatement() {
        Token variable = consume(TokenType.IDENTIFIER, "Expect variable name");
        consume(TokenType.IN, "Expect 'in'");
        ASTNode.Expr iterable = expression();
        consume(TokenType.LEFT_BRACE, "Expect '{'");
        List<ASTNode.Stmt> body = block();
        return new ASTNode.For(variable, iterable, body);
    }

    private ASTNode.Stmt loadStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name");
        consume(TokenType.COLON, "Expect ':'");
        String path = (String) consume(TokenType.STRING, "Expect file path").literal;
        consumeStatementEnd();
        return new ASTNode.Load(name, path);
    }

    private ASTNode.Stmt drawStatement() {
        Token typeToken = consume(TokenType.IDENTIFIER, "Expect draw type");
        String type = typeToken.lexeme.toLowerCase();

        // FIX: Changed from List<Expr> to List<ASTNode.Expr>
        List<ASTNode.Expr> args = new ArrayList<>();
        String color = "black";

        if ("rect".equals(type)) {
            args.add(expression());
            args.add(expression());
            args.add(expression());
            args.add(expression());
            color = (String) consume(TokenType.STRING, "Expect color").literal;
        } else if ("circle".equals(type)) {
            args.add(expression());
            args.add(expression());
            args.add(expression());
            color = (String) consume(TokenType.STRING, "Expect color").literal;
        } else if ("image".equals(type)) {
            Token imgName = consume(TokenType.IDENTIFIER, "Expect image name");
            args.add(new ASTNode.Variable(imgName));
            args.add(expression());
            args.add(expression());
        } else if ("line".equals(type)) {
            args.add(expression());
            args.add(expression());
            args.add(expression());
            args.add(expression());
            color = (String) consume(TokenType.STRING, "Expect color").literal;
        }

        consumeStatementEnd();
        return new ASTNode.Draw(type, args, color);
    }

    private ASTNode.Stmt showStatement() {
        String text = (String) consume(TokenType.STRING, "Expect text").literal;
        ASTNode.Expr x = expression();
        ASTNode.Expr y = expression();
        String alignment = "left";
        if (check(TokenType.IDENTIFIER)) {
            alignment = advance().lexeme;
        }
        consumeStatementEnd();
        return new ASTNode.Show(text, x, y, alignment);
    }

    private ASTNode.Stmt playStatement() {
        String path = (String) consume(TokenType.STRING, "Expect file path").literal;
        consumeStatementEnd();
        return new ASTNode.Play(path);
    }

    private ASTNode.Stmt printStatement() {
        ASTNode.Expr expr = expression();
        consumeStatementEnd();
        return new ASTNode.Print(expr);
    }

    private ASTNode.Stmt expressionStatement() {
        ASTNode.Expr expr = expression();
        consumeStatementEnd();
        return new ASTNode.ExpressionStmt(expr);
    }

    private List<ASTNode.Stmt> block() {
        List<ASTNode.Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            ASTNode.Stmt stmt = statement();
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}'");
        return statements;
    }

    private ASTNode.Expr expression() {
        return logicalOr();
    }

    private ASTNode.Expr logicalOr() {
        ASTNode.Expr expr = logicalAnd();
        while (match(TokenType.OR)) {
            Token operator = previous();
            ASTNode.Expr right = logicalAnd();
            expr = new ASTNode.Binary(expr, operator, right);
        }
        return expr;
    }

    private ASTNode.Expr logicalAnd() {
        ASTNode.Expr expr = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            ASTNode.Expr right = equality();
            expr = new ASTNode.Binary(expr, operator, right);
        }
        return expr;
    }

    private ASTNode.Expr equality() {
        ASTNode.Expr expr = comparison();
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            ASTNode.Expr right = comparison();
            expr = new ASTNode.Binary(expr, operator, right);
        }
        return expr;
    }

    private ASTNode.Expr comparison() {
        ASTNode.Expr expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            ASTNode.Expr right = term();
            expr = new ASTNode.Binary(expr, operator, right);
        }
        return expr;
    }

    private ASTNode.Expr term() {
        ASTNode.Expr expr = factor();
        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            ASTNode.Expr right = factor();
            expr = new ASTNode.Binary(expr, operator, right);
        }
        return expr;
    }

    private ASTNode.Expr factor() {
        ASTNode.Expr expr = unary();
        while (match(TokenType.SLASH, TokenType.STAR, TokenType.PERCENT)) {
            Token operator = previous();
            ASTNode.Expr right = unary();
            expr = new ASTNode.Binary(expr, operator, right);
        }
        return expr;
    }

    private ASTNode.Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS, TokenType.NOT)) {
            Token operator = previous();
            ASTNode.Expr right = unary();
            return new ASTNode.Unary(operator, right);
        }
        return postfix();
    }

    private ASTNode.Expr postfix() {
        ASTNode.Expr expr = primary();
        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                List<ASTNode.Expr> arguments = new ArrayList<>();
                if (!check(TokenType.RIGHT_PAREN)) {
                    do {
                        arguments.add(expression());
                    } while (match(TokenType.COMMA));
                }
                consume(TokenType.RIGHT_PAREN, "Expect ')'");
                expr = new ASTNode.Call(expr, arguments);
            } else if (match(TokenType.LEFT_BRACKET)) {
                ASTNode.Expr index = expression();
                consume(TokenType.RIGHT_BRACKET, "Expect ']'");
                expr = new ASTNode.Index(expr, index);
            } else {
                break;
            }
        }
        return expr;
    }

    private ASTNode.Expr primary() {
        if (match(TokenType.TRUE)) return new ASTNode.Literal(true);
        if (match(TokenType.FALSE)) return new ASTNode.Literal(false);
        if (match(TokenType.NULL)) return new ASTNode.Literal(null);
        if (match(TokenType.NUMBER)) return new ASTNode.Literal(previous().literal);
        if (match(TokenType.STRING)) return new ASTNode.Literal(previous().literal);
        if (match(TokenType.IDENTIFIER)) return new ASTNode.Variable(previous());
        if (match(TokenType.LEFT_BRACKET)) {
            List<ASTNode.Expr> elements = new ArrayList<>();
            if (!check(TokenType.RIGHT_BRACKET)) {
                do {
                    elements.add(expression());
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RIGHT_BRACKET, "Expect ']'");
            return new ASTNode.ArrayLiteral(elements);
        }
        if (match(TokenType.LEFT_PAREN)) {
            ASTNode.Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')'");
            return expr;
        }
        throw error("Expected expression");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekNext() {
        if (current + 1 >= tokens.size()) return tokens.get(tokens.size() - 1);
        return tokens.get(current + 1);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(message);
    }

    private void consumeStatementEnd() {
        if (check(TokenType.SEMICOLON)) advance();
    }

    private RuntimeException error(String message) {
        Token token = peek();
        System.err.println("[Line " + token.line + "] Error at '" + token.lexeme + "': " + message);
        return new RuntimeException(message);
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;
            switch (peek().type) {
                case GAME:
                case LET:
                case FN:
                case RETURN:
                case IF:
                case WHILE:
                case FOR:
                case LOAD:
                case DRAW:
                case SHOW:
                case PLAY:
                case PRINT:
                    return;
            }
            advance();
        }
    }
}