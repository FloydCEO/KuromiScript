// src/interpreter/Interpreter.java
package interpreter;

import parser.ASTNode;
import lexer.Token;
import lexer.TokenType;
import runtime.Value;
import java.util.ArrayList;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Interpreter {
    private Environment globals = new Environment();
    private Environment environment = globals;
    private GameWindow gameWindow;
    private Value returnValue;
    private boolean isReturning = false;

    public void interpret(java.util.List<ASTNode.Stmt> statements) {
        try {
            for (ASTNode.Stmt stmt : statements) {
                execute(stmt);
                if (isReturning) break;
            }
        } catch (Exception e) {
            System.err.println("Runtime Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Don't close the game window here - let it close independently
            // This allows the IDE to continue running
        }
    }

    private void execute(ASTNode.Stmt stmt) {
        if (isReturning) return;

        if (stmt instanceof ASTNode.GameStart) {
            ASTNode.GameStart game = (ASTNode.GameStart) stmt;
            gameWindow = new GameWindow(game.width, game.height);
            for (ASTNode.Stmt s : game.body) {
                execute(s);
                if (isReturning) break;
            }
        } else if (stmt instanceof ASTNode.Let) {
            ASTNode.Let let = (ASTNode.Let) stmt;
            environment.define(let.name.lexeme, evaluate(let.initializer));
        } else if (stmt instanceof ASTNode.Assignment) {
            ASTNode.Assignment assign = (ASTNode.Assignment) stmt;
            environment.set(assign.name.lexeme, evaluate(assign.value));
        } else if (stmt instanceof ASTNode.Function) {
            ASTNode.Function func = (ASTNode.Function) stmt;
            environment.define(func.name.lexeme, Value.function(func));
        } else if (stmt instanceof ASTNode.Return) {
            ASTNode.Return ret = (ASTNode.Return) stmt;
            returnValue = ret.value != null ? evaluate(ret.value) : Value.nil();
            isReturning = true;
        } else if (stmt instanceof ASTNode.If) {
            ASTNode.If ifStmt = (ASTNode.If) stmt;
            if (evaluate(ifStmt.condition).asBoolean()) {
                for (ASTNode.Stmt s : ifStmt.thenBranch) {
                    execute(s);
                    if (isReturning) break;
                }
            } else {
                for (ASTNode.Stmt s : ifStmt.elseBranch) {
                    execute(s);
                    if (isReturning) break;
                }
            }
        } else if (stmt instanceof ASTNode.While) {
            ASTNode.While whileStmt = (ASTNode.While) stmt;
            while (evaluate(whileStmt.condition).asBoolean()) {
                for (ASTNode.Stmt s : whileStmt.body) {
                    execute(s);
                    if (isReturning) break;
                }
                if (isReturning) break;
            }
        } else if (stmt instanceof ASTNode.For) {
            ASTNode.For forStmt = (ASTNode.For) stmt;
            Value iterable = evaluate(forStmt.iterable);
            if (iterable.type == Value.Type.ARRAY) {
                for (Value element : iterable.asArray()) {
                    environment.define(forStmt.variable.lexeme, element);
                    for (ASTNode.Stmt s : forStmt.body) {
                        execute(s);
                        if (isReturning) break;
                    }
                    if (isReturning) break;
                }
            }
        } else if (stmt instanceof ASTNode.Load) {
            ASTNode.Load load = (ASTNode.Load) stmt;
            try {
                BufferedImage img = ImageIO.read(new File("assets/" + load.path));
                environment.define(load.name.lexeme, Value.image(img));
                System.out.println("✓ Loaded: " + load.path);
            } catch (Exception e) {
                System.err.println("Failed to load: " + load.path);
            }
        } else if (stmt instanceof ASTNode.Draw) {
            ASTNode.Draw draw = (ASTNode.Draw) stmt;
            if (gameWindow == null) {
                System.err.println("Error: Game window not initialized");
                return;
            }
            executeDraw(draw);
        } else if (stmt instanceof ASTNode.Show) {
            ASTNode.Show show = (ASTNode.Show) stmt;
            if (gameWindow != null) {
                int x = (int) evaluate(show.x).asNumber();
                int y = (int) evaluate(show.y).asNumber();
                gameWindow.drawText(show.text, x, y, show.alignment);
            }
        } else if (stmt instanceof ASTNode.Play) {
            System.out.println("♪ Playing: " + ((ASTNode.Play) stmt).path);
        } else if (stmt instanceof ASTNode.Print) {
            ASTNode.Print print = (ASTNode.Print) stmt;
            System.out.println(evaluate(print.expression).asString());
        } else if (stmt instanceof ASTNode.Block) {
            ASTNode.Block block = (ASTNode.Block) stmt;
            for (ASTNode.Stmt s : block.statements) {
                execute(s);
                if (isReturning) break;
            }
        } else if (stmt instanceof ASTNode.ExpressionStmt) {
            ASTNode.ExpressionStmt exprStmt = (ASTNode.ExpressionStmt) stmt;
            evaluate(exprStmt.expression);
        }
    }

    private void executeDraw(ASTNode.Draw draw) {
        java.util.List<ASTNode.Expr> args = draw.args;
        String type = draw.type;

        if ("rect".equals(type) && args.size() >= 4) {
            int x = (int) evaluate(args.get(0)).asNumber();
            int y = (int) evaluate(args.get(1)).asNumber();
            int w = (int) evaluate(args.get(2)).asNumber();
            int h = (int) evaluate(args.get(3)).asNumber();
            gameWindow.fillRect(x, y, w, h, draw.color);
        } else if ("circle".equals(type) && args.size() >= 3) {
            int x = (int) evaluate(args.get(0)).asNumber();
            int y = (int) evaluate(args.get(1)).asNumber();
            int r = (int) evaluate(args.get(2)).asNumber();
            gameWindow.fillCircle(x, y, r, draw.color);
        } else if ("image".equals(type) && args.size() >= 3) {
            Value img = evaluate(args.get(0));
            int x = (int) evaluate(args.get(1)).asNumber();
            int y = (int) evaluate(args.get(2)).asNumber();
            if (img.type == Value.Type.IMAGE) {
                gameWindow.drawImage(img.asImage(), x, y);
            }
        } else if ("line".equals(type) && args.size() >= 4) {
            int x1 = (int) evaluate(args.get(0)).asNumber();
            int y1 = (int) evaluate(args.get(1)).asNumber();
            int x2 = (int) evaluate(args.get(2)).asNumber();
            int y2 = (int) evaluate(args.get(3)).asNumber();
            gameWindow.drawLine(x1, y1, x2, y2, draw.color);
        }
    }

    private Value evaluate(ASTNode.Expr expr) {
        if (expr instanceof ASTNode.Literal) {
            Object value = ((ASTNode.Literal) expr).value;
            if (value instanceof Double) return Value.number((Double) value);
            if (value instanceof String) return Value.string((String) value);
            if (value instanceof Boolean) return Value.bool((Boolean) value);
            return Value.nil();
        } else if (expr instanceof ASTNode.Variable) {
            return environment.get(((ASTNode.Variable) expr).name.lexeme);
        } else if (expr instanceof ASTNode.Binary) {
            return evaluateBinary((ASTNode.Binary) expr);
        } else if (expr instanceof ASTNode.Unary) {
            return evaluateUnary((ASTNode.Unary) expr);
        } else if (expr instanceof ASTNode.Call) {
            return evaluateCall((ASTNode.Call) expr);
        } else if (expr instanceof ASTNode.Index) {
            ASTNode.Index index = (ASTNode.Index) expr;
            Value arr = evaluate(index.object);
            int idx = (int) evaluate(index.index).asNumber();
            return arr.asArray().get(idx);
        } else if (expr instanceof ASTNode.ArrayLiteral) {
            ASTNode.ArrayLiteral arrLit = (ASTNode.ArrayLiteral) expr;
            java.util.List<Value> elements = new ArrayList<>();
            for (ASTNode.Expr e : arrLit.elements) {
                elements.add(evaluate(e));
            }
            return Value.array(elements);
        }
        return Value.nil();
    }

    private Value evaluateBinary(ASTNode.Binary binary) {
        Value left = evaluate(binary.left);
        Value right = evaluate(binary.right);
        TokenType op = binary.operator.type;

        switch (op) {
            case PLUS:
                if (left.type == Value.Type.STRING || right.type == Value.Type.STRING) {
                    return Value.string(left.asString() + right.asString());
                }
                return Value.number(left.asNumber() + right.asNumber());
            case MINUS:
                return Value.number(left.asNumber() - right.asNumber());
            case STAR:
                return Value.number(left.asNumber() * right.asNumber());
            case SLASH:
                double divisor = right.asNumber();
                if (divisor == 0) throw new RuntimeException("Division by zero");
                return Value.number(left.asNumber() / divisor);
            case PERCENT:
                return Value.number(left.asNumber() % right.asNumber());
            case LESS:
                return Value.bool(left.asNumber() < right.asNumber());
            case LESS_EQUAL:
                return Value.bool(left.asNumber() <= right.asNumber());
            case GREATER:
                return Value.bool(left.asNumber() > right.asNumber());
            case GREATER_EQUAL:
                return Value.bool(left.asNumber() >= right.asNumber());
            case EQUAL_EQUAL:
                return Value.bool(isEqual(left, right));
            case BANG_EQUAL:
                return Value.bool(!isEqual(left, right));
            case AND:
                return Value.bool(left.asBoolean() && right.asBoolean());
            case OR:
                return Value.bool(left.asBoolean() || right.asBoolean());
            default:
                return Value.nil();
        }
    }

    private Value evaluateUnary(ASTNode.Unary unary) {
        Value right = evaluate(unary.right);
        TokenType op = unary.operator.type;

        switch (op) {
            case MINUS:
                return Value.number(-right.asNumber());
            case BANG:
            case NOT:
                return Value.bool(!right.asBoolean());
            default:
                return Value.nil();
        }
    }

    private Value evaluateCall(ASTNode.Call call) {
        Value callee = evaluate(call.callee);
        if (callee.type != Value.Type.FUNCTION) {
            throw new RuntimeException("Not a function");
        }

        ASTNode.Function function = (ASTNode.Function) callee.data;
        Environment funcEnv = new Environment(globals);

        for (int i = 0; i < function.params.size(); i++) {
            Value arg = i < call.arguments.size() ? evaluate(call.arguments.get(i)) : Value.nil();
            funcEnv.define(function.params.get(i).lexeme, arg);
        }

        Environment previous = environment;
        environment = funcEnv;

        isReturning = false;
        for (ASTNode.Stmt stmt : function.body) {
            execute(stmt);
            if (isReturning) break;
        }

        Value result = isReturning ? returnValue : Value.nil();
        returnValue = Value.nil();
        isReturning = false;
        environment = previous;

        return result;
    }

    private boolean isEqual(Value a, Value b) {
        if (a.type != b.type) return false;
        if (a.type == Value.Type.NULL) return true;
        if (a.type == Value.Type.NUMBER) {
            return Math.abs(a.asNumber() - b.asNumber()) < 0.0001;
        }
        if (a.type == Value.Type.STRING) {
            return a.asString().equals(b.asString());
        }
        if (a.type == Value.Type.BOOL) {
            return a.asBoolean() == b.asBoolean();
        }
        return false;
    }

    public static class GameWindow extends JFrame {
        private final Canvas canvas;
        private boolean isClosing = false;

        public GameWindow(int width, int height) {
            setTitle("KuromiScript Game");
            setSize(width, height);
            // CRITICAL FIX: Don't exit the entire application when window closes
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setResizable(false);
            canvas = new Canvas(width, height);
            add(canvas);
            setVisible(true);

            // Add window listener to handle closing gracefully
            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    isClosing = true;
                    System.out.println("Game window closed");
                }
            });
        }

        public void fillRect(int x, int y, int w, int h, String color) {
            canvas.fillRect(x, y, w, h, parseColor(color));
        }

        public void fillCircle(int x, int y, int r, String color) {
            canvas.fillCircle(x, y, r, parseColor(color));
        }

        public void drawImage(BufferedImage img, int x, int y) {
            canvas.drawImage(img, x, y);
        }

        public void drawText(String text, int x, int y, String alignment) {
            canvas.drawText(text, x, y, alignment);
        }

        public void drawLine(int x1, int y1, int x2, int y2, String color) {
            canvas.drawLine(x1, y1, x2, y2, parseColor(color));
        }

        private Color parseColor(String colorName) {
            return switch (colorName.toLowerCase()) {
                case "red" -> Color.RED;
                case "green" -> Color.GREEN;
                case "blue" -> Color.BLUE;
                case "white" -> Color.WHITE;
                case "black" -> Color.BLACK;
                case "yellow" -> Color.YELLOW;
                case "cyan" -> Color.CYAN;
                case "magenta" -> Color.MAGENTA;
                case "gray" -> Color.GRAY;
                default -> Color.BLACK;
            };
        }
    }

    public static class Canvas extends JPanel {
        private final BufferedImage buffer;
        private final Graphics2D g2d;

        public Canvas(int width, int height) {
            buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            g2d = buffer.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, height);
        }

        public void fillRect(int x, int y, int w, int h, Color color) {
            g2d.setColor(color);
            g2d.fillRect(x, y, w, h);
            repaint();
        }

        public void fillCircle(int x, int y, int r, Color color) {
            g2d.setColor(color);
            g2d.fillOval(x - r, y - r, 2 * r, 2 * r);
            repaint();
        }

        public void drawImage(BufferedImage img, int x, int y) {
            g2d.drawImage(img, x, y, null);
            repaint();
        }

        public void drawText(String text, int x, int y, String alignment) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            FontMetrics fm = g2d.getFontMetrics();
            int width = fm.stringWidth(text);
            if ("center".equals(alignment)) x -= width / 2;
            else if ("right".equals(alignment)) x -= width;
            g2d.drawString(text, x, y);
            repaint();
        }

        public void drawLine(int x1, int y1, int x2, int y2, Color color) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x1, y1, x2, y2);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(buffer, 0, 0, null);
        }
    }
}