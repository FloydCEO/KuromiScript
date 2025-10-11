// src/parser/ASTNode.java - Complete version with all classes
package parser;

import lexer.Token;
import java.util.List;

public abstract class ASTNode {
    // Base interfaces
    public interface Expr {}
    public interface Stmt {}

    // EXPRESSION CLASSES

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

    public static class Binary implements Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;
        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    public static class Unary implements Expr {
        public final Token operator;
        public final Expr right;
        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }
    }

    public static class Call implements Expr {
        public final Expr callee;
        public final List<Expr> arguments;
        public Call(Expr callee, List<Expr> arguments) {
            this.callee = callee;
            this.arguments = arguments;
        }
    }

    public static class Index implements Expr {
        public final Expr object;
        public final Expr index;
        public Index(Expr object, Expr index) {
            this.object = object;
            this.index = index;
        }
    }

    public static class ArrayLiteral implements Expr {
        public final List<Expr> elements;
        public ArrayLiteral(List<Expr> elements) {
            this.elements = elements;
        }
    }

    // STATEMENT CLASSES

    public static class GameStart implements Stmt {
        public final int width;
        public final int height;
        public final List<Stmt> body;
        public GameStart(int width, int height, List<Stmt> body) {
            this.width = width;
            this.height = height;
            this.body = body;
        }
    }

    public static class Let implements Stmt {
        public final Token name;
        public final Expr initializer;
        public Let(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }
    }

    public static class Assignment implements Stmt {
        public final Token name;
        public final Expr value;
        public Assignment(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class Function implements Stmt {
        public final Token name;
        public final List<Token> params;
        public final List<Stmt> body;
        public Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }
    }

    public static class Return implements Stmt {
        public final Expr value;
        public Return(Expr value) {
            this.value = value;
        }
    }

    public static class If implements Stmt {
        public final Expr condition;
        public final List<Stmt> thenBranch;
        public final List<Stmt> elseBranch;
        public If(Expr condition, List<Stmt> thenBranch, List<Stmt> elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
    }

    public static class While implements Stmt {
        public final Expr condition;
        public final List<Stmt> body;
        public While(Expr condition, List<Stmt> body) {
            this.condition = condition;
            this.body = body;
        }
    }

    public static class For implements Stmt {
        public final Token variable;
        public final Expr iterable;
        public final List<Stmt> body;
        public For(Token variable, Expr iterable, List<Stmt> body) {
            this.variable = variable;
            this.iterable = iterable;
            this.body = body;
        }
    }

    public static class Load implements Stmt {
        public final Token name;
        public final String path;
        public Load(Token name, String path) {
            this.name = name;
            this.path = path;
        }
    }

    public static class Draw implements Stmt {
        public final String type;
        public final List<Expr> args;
        public final String color;
        public Draw(String type, List<Expr> args, String color) {
            this.type = type;
            this.args = args;
            this.color = color;
        }
    }

    public static class Show implements Stmt {
        public final String text;
        public final Expr x;
        public final Expr y;
        public final String alignment;
        public Show(String text, Expr x, Expr y, String alignment) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.alignment = alignment;
        }
    }

    public static class Play implements Stmt {
        public final String path;
        public Play(String path) {
            this.path = path;
        }
    }

    public static class Print implements Stmt {
        public final Expr expression;
        public Print(Expr expression) {
            this.expression = expression;
        }
    }

    public static class Block implements Stmt {
        public final List<Stmt> statements;
        public Block(List<Stmt> statements) {
            this.statements = statements;
        }
    }

    public static class ExpressionStmt implements Stmt {
        public final Expr expression;
        public ExpressionStmt(Expr expression) {
            this.expression = expression;
        }
    }
}