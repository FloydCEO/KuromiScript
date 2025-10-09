// src/parser/Parser.java
package parser;

import interpreter.KuromiValue;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<ASTNode.Stmt> parse() {
        List<ASTNode.Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }
        return statements;
    }

    private ASTNode.Stmt statement() {
        if (match(TokenType.KUROMI)) {
            if (match(TokenType.START)) {
                return new ASTNode.StartGame();
            } else if (match(TokenType.CLOSE)) {
                return new ASTNode.CloseGame();
            }
        } else if (match(TokenType.LOAD_IMAGE)) {
            return loadImageStmt();
        } else if (match(TokenType.DISPLAY_IMAGE)) {
            return displayImageStmt();
        } else if (match(TokenType.SHOW_TEXT)) {
            return showTextStmt();
        } else if (match(TokenType.PLAY_SOUND)) {
            return playSoundStmt();
        } else if (match(TokenType.DRAW_RECTANGLE)) {
            return drawRectangleStmt();
        } else if (match(TokenType.DRAW_CIRCLE)) {
            return drawCircleStmt();
        } else if (match(TokenType.DRAW_LINE)) {
            return drawLineStmt();
        } else if (match(TokenType.DRAW_TRIANGLE)) {
            return drawTriangleStmt();
        } else if (match(TokenType.PRINT)) {
            return printStmt();
        } else if (match(TokenType.LET)) {
            return varDecl();
        } else if (match(TokenType.REPEAT)) {
            return repeatStmt();
        } else if (match(TokenType.WAIT)) {
            return waitStmt();
        } else if (match(TokenType.IF)) {
            return ifStmt();
        } else if (match(TokenType.CALL)) {
            return callStmt();
        }
        throw error(peek(), "Expected statement.");
    }

    private ASTNode.Stmt loadImageStmt() {
        Token varName = consume(TokenType.STRING, "Expect string for variable name.");
        Token path = consume(TokenType.STRING, "Expect string for path.");
        return new ASTNode.LoadImage(new Token(TokenType.IDENTIFIER, varName.literal.toString(), null, varName.line), KuromiValue.string(path.literal.toString()));
    }

    private ASTNode.Stmt displayImageStmt() {
        Token varName = consume(TokenType.IDENTIFIER, "Expect identifier for image.");
        ASTNode.Expr x = expression();
        ASTNode.Expr y = expression();
        return new ASTNode.DisplayImage(varName, x, y);
    }

    private ASTNode.Stmt showTextStmt() {
        Token alignment = null;
        if (match(TokenType.LEFT_PAREN)) {
            alignment = consume(TokenType.CENTERED, TokenType.LEFT, TokenType.RIGHT, "Expect alignment.");
            consume(TokenType.RIGHT_PAREN, "Expect ')' after alignment.");
        }
        Token textToken = consume(TokenType.STRING, "Expect string for text.");
        ASTNode.Expr x = expression();
        ASTNode.Expr y = expression();
        return new ASTNode.ShowText(alignment, KuromiValue.string(textToken.literal.toString()), x, y);
    }

    private ASTNode.Stmt playSoundStmt() {
        Token path = consume(TokenType.STRING, "Expect string for sound path.");
        return new ASTNode.PlaySound(KuromiValue.string(path.literal.toString()));
    }

    private ASTNode.Stmt drawRectangleStmt() {
        ASTNode.Expr x = expression();
        ASTNode.Expr y = expression();
        ASTNode.Expr w = expression();
        ASTNode.Expr h = expression();
        Token color = consume(TokenType.STRING, "Expect string for color.");
        return new ASTNode.DrawRectangle(x, y, w, h, KuromiValue.string(color.literal.toString()));
    }

    private ASTNode.Stmt drawCircleStmt() {
        ASTNode.Expr x = expression();
        ASTNode.Expr y = expression();
        ASTNode.Expr r = expression();
        Token color = consume(TokenType.STRING, "Expect string for color.");
        return new ASTNode.DrawCircle(x, y, r, KuromiValue.string(color.literal.toString()));
    }

    private ASTNode.Stmt drawLineStmt() {
        ASTNode.Expr x1 = expression();
        ASTNode.Expr y1 = expression();
        ASTNode.Expr x2 = expression();
        ASTNode.Expr y2 = expression();
        Token color = consume(TokenType.STRING, "Expect string for color.");
        return new ASTNode.DrawLine(x1, y1, x2, y2, KuromiValue.string(color.literal.toString()));
    }

    private ASTNode.Stmt drawTriangleStmt() {
        ASTNode.Expr x1 = expression();
        ASTNode.Expr y1 = expression();
        ASTNode.Expr x2 = expression();
        ASTNode.Expr y2 = expression();
        ASTNode.Expr x3 = expression();
        ASTNode.Expr y3 = expression();
        Token color = consume(TokenType.STRING, "Expect string for color.");
        return new ASTNode.DrawTriangle(x1, y1, x2, y2, x3, y3, KuromiValue.string(color.literal.toString()));
    }

    private ASTNode.Stmt printStmt() {
        ASTNode.Expr expr = expression();
        return new ASTNode.PrintStmt(expr);
    }

    private ASTNode.Stmt varDecl() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
        consume(TokenType.EQUAL, "Expect '=' after variable name.");
        ASTNode.Expr initializer = expression();
        return new ASTNode.VarDecl(name, initializer);
    }

    private ASTNode.Stmt repeatStmt() {
        ASTNode.Expr count = expression();
        consume(TokenType.TIMES, "Expect 'Times' after count.");
        consume(TokenType.COLON, "Expect ':' after 'Times'.");
        List<ASTNode.Stmt> body = new ArrayList<>();
        while (!isAtEnd() && !check(TokenType.EOF)) {
            body.add(statement());
        }
        return new ASTNode.Repeat(count, body);
    }

    private ASTNode.Stmt waitStmt() {
        ASTNode.Expr millis = expression();
        return new ASTNode.Wait(millis);
    }

    private ASTNode.Stmt ifStmt() {
        ASTNode.Expr condition = expression();
        consume(TokenType.THEN, "Expect 'Then' after condition.");
        consume(TokenType.COLON, "Expect ':' after 'Then'.");
        List<ASTNode.Stmt> thenBranch = new ArrayList<>();
        while (!isAtEnd() && !check(TokenType.ELSE) && !check(TokenType.EOF)) {
            thenBranch.add(statement());
        }
        List<ASTNode.Stmt> elseBranch = new ArrayList<>();
        if (match(TokenType.ELSE)) {
            consume(TokenType.COLON, "Expect ':' after 'Else'.");
            while (!isAtEnd() && !check(TokenType.EOF)) {
                elseBranch.add(statement());
            }
        }
        return new ASTNode.IfStmt(condition, thenBranch, elseBranch);
    }

    private ASTNode.Stmt callStmt() {
        Token name = previous();
        List<ASTNode.Expr> args = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                args.add(expression());
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");
        return new ASTNode.Call(name, args);
    }

    private ASTNode.Expr expression() {
        if (match(TokenType.NUMBER)) {
            return new ASTNode.Literal(previous().literal);
        } else if (match(TokenType.STRING)) {
            return new ASTNode.Literal(previous().literal);
        } else if (match(TokenType.IDENTIFIER)) {
            return new ASTNode.Variable(previous());
        }
        throw error(peek(), "Expected expression.");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private Token consume(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) return advance();
        }
        throw error(peek(), "Expected one of the types.");
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

    private Token previous() {
        return tokens.get(current - 1);
    }

    private RuntimeException error(Token token, String message) {
        System.err.println("[line " + token.line + "] Error at '" + token.lexeme + "': " + message);
        return new RuntimeException();
    }
}