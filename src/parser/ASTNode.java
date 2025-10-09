// src/parser/ASTNode.java
package parser;

import interpreter.KuromiValue;
import lexer.Token;

import java.util.List;

public abstract class ASTNode {
    public interface Expr {
    }

    public interface Stmt {
    }

    public static class Literal implements Expr {
        public final Object value;

        public Literal(Object value) {
            this.value = value;
        }
    }

    public static class Variable implements Expr {
        public final Token name;

        public Variable(Token name) {
            this.name = name;
        }
    }

    public static class StartGame implements Stmt {
    }

    public static class CloseGame implements Stmt {
    }

    public static class LoadImage implements Stmt {
        public final Token varName;
        public final KuromiValue path;

        public LoadImage(Token varName, KuromiValue path) {
            this.varName = varName;
            this.path = path;
        }
    }

    public static class DisplayImage implements Stmt {
        public final Token varName;
        public final Expr x;
        public final Expr y;

        public DisplayImage(Token varName, Expr x, Expr y) {
            this.varName = varName;
            this.x = x;
            this.y = y;
        }
    }

    public static class ShowText implements Stmt {
        public final Token alignment;
        public final KuromiValue text;
        public final Expr x;
        public final Expr y;

        public ShowText(Token alignment, KuromiValue text, Expr x, Expr y) {
            this.alignment = alignment;
            this.text = text;
            this.x = x;
            this.y = y;
        }
    }

    public static class PlaySound implements Stmt {
        public final KuromiValue path;

        public PlaySound(KuromiValue path) {
            this.path = path;
        }
    }

    public static class DrawRectangle implements Stmt {
        public final Expr x, y, width, height;
        public final KuromiValue color;

        public DrawRectangle(Expr x, Expr y, Expr width, Expr height, KuromiValue color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }
    }

    public static class DrawCircle implements Stmt {
        public final Expr x, y, radius;
        public final KuromiValue color;

        public DrawCircle(Expr x, Expr y, Expr radius, KuromiValue color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
        }
    }

    public static class DrawLine implements Stmt {
        public final Expr x1, y1, x2, y2;
        public final KuromiValue color;

        public DrawLine(Expr x1, Expr y1, Expr x2, Expr y2, KuromiValue color) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = color;
        }
    }

    public static class DrawTriangle implements Stmt {
        public final Expr x1, y1, x2, y2, x3, y3;
        public final KuromiValue color;

        public DrawTriangle(Expr x1, Expr y1, Expr x2, Expr y2, Expr x3, Expr y3, KuromiValue color) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.x3 = x3;
            this.y3 = y3;
            this.color = color;
        }
    }

    public static class PrintStmt implements Stmt {
        public final Expr expression;

        public PrintStmt(Expr expression) {
            this.expression = expression;
        }
    }

    public static class VarDecl implements Stmt {
        public final Token name;
        public final Expr initializer;

        public VarDecl(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }
    }

    public static class Repeat implements Stmt {
        public final Expr count;
        public final List<Stmt> body;

        public Repeat(Expr count, List<Stmt> body) {
            this.count = count;
            this.body = body;
        }
    }

    public static class Wait implements Stmt {
        public final Expr millis;

        public Wait(Expr millis) {
            this.millis = millis;
        }
    }

    public static class IfStmt implements Stmt {
        public final Expr condition;
        public final List<Stmt> thenBranch;
        public final List<Stmt> elseBranch;

        public IfStmt(Expr condition, List<Stmt> thenBranch, List<Stmt> elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
    }

    public static class Call implements Stmt {
        public final Token name;
        public final List<Expr> arguments;

        public Call(Token name, List<Expr> arguments) {
            this.name = name;
            this.arguments = arguments;
        }
    }
}