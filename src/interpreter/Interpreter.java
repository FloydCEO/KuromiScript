// src/interpreter/Interpreter.java
package interpreter;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import lexer.Token;
import parser.ASTNode;
import runtime.BuiltInFunctions;

public class Interpreter {
    private final Environment environment = new Environment();
    private final BuiltInFunctions builtIns;
    private GameWindow window;
    private final Map<String, Boolean> keyStates = new HashMap<>();
    private final Map<String, Point> spritePositions = new HashMap<>();

    public Interpreter() {
        this.builtIns = new BuiltInFunctions(this);
    }

    public void interpret(List<ASTNode.Stmt> statements) {
        window = new GameWindow();
        builtIns.registerKeyListener(window);
        for (ASTNode.Stmt stmt : statements) {
            execute(stmt);
        }
    }

    private void execute(ASTNode.Stmt stmt) {
        if (stmt instanceof ASTNode.StartGame) {
            System.out.println("✨ Kuromi Game Started ✨");
            window.clear();
        } else if (stmt instanceof ASTNode.CloseGame) {
            System.out.println("🛑 Kuromi Game Closing...");
            window.close();
        } else if (stmt instanceof ASTNode.LoadImage) {
            ASTNode.LoadImage load = (ASTNode.LoadImage) stmt;
            try {
                BufferedImage img = javax.imageio.ImageIO.read(new File("assets/" + load.path.asString()));
                environment.define(load.varName.lexeme, KuromiValue.image(img));
                System.out.println("✓ Loaded image: " + load.varName.lexeme);
            } catch (IOException e) {
                System.err.println("[Error] Failed to load image: " + e.getMessage());
            }
        } else if (stmt instanceof ASTNode.DisplayImage) {
            ASTNode.DisplayImage display = (ASTNode.DisplayImage) stmt;
            KuromiValue imgVal = environment.get(display.varName.lexeme);
            double x = display.x.asNumber();
            double y = display.y.asNumber();
            spritePositions.put(display.varName.lexeme, new Point((int) x, (int) y));
            window.drawImage(imgVal.asImage(), (int) x, (int) y);
            System.out.println("✓ Displayed " + display.varName.lexeme + " at (" + x + ", " + y + ")");
        } else if (stmt instanceof ASTNode.ShowText) {
            ASTNode.ShowText show = (ASTNode.ShowText) stmt;
            String text = show.text.asString();
            double x = show.x.asNumber();
            double y = show.y.asNumber();
            String alignment = show.alignment != null ? show.alignment.lexeme : "left";
            window.drawText(text, (int) x, (int) y, alignment);
            System.out.println("✓ Displayed text at (" + x + ", " + y + ")");
        } else if (stmt instanceof ASTNode.PlaySound) {
            ASTNode.PlaySound play = (ASTNode.PlaySound) stmt;
            String path = "assets/" + play.path.asString();
            try {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(path));
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                System.out.println("♪ Playing sound: " + play.path.asString());
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("[Error] Failed to play sound: " + e.getMessage());
            }
        } else if (stmt instanceof ASTNode.DrawRectangle) {
            ASTNode.DrawRectangle draw = (ASTNode.DrawRectangle) stmt;
            double x = draw.x.asNumber();
            double y = draw.y.asNumber();
            double w = draw.width.asNumber();
            double h = draw.height.asNumber();
            Color color = parseColor(draw.color.asString());
            window.drawRectangle((int) x, (int) y, (int) w, (int) h, color);
            System.out.println("✓ Drew rectangle at (" + x + ", " + y + ") - " + w + "x" + h + " - " + draw.color.asString());
        } else if (stmt instanceof ASTNode.DrawCircle) {
            ASTNode.DrawCircle draw = (ASTNode.DrawCircle) stmt;
            double x = draw.x.asNumber();
            double y = draw.y.asNumber();
            double r = draw.radius.asNumber();
            Color color = parseColor(draw.color.asString());
            window.drawCircle((int) x, (int) y, (int) r, color);
            System.out.println("✓ Drew circle at (" + x + ", " + y + ") - radius " + r + " - " + draw.color.asString());
        } else if (stmt instanceof ASTNode.DrawLine) {
            ASTNode.DrawLine draw = (ASTNode.DrawLine) stmt;
            double x1 = draw.x1.asNumber();
            double y1 = draw.y1.asNumber();
            double x2 = draw.x2.asNumber();
            double y2 = draw.y2.asNumber();
            Color color = parseColor(draw.color.asString());
            window.drawLine((int) x1, (int) y1, (int) x2, (int) y2, color);
            System.out.println("✓ Drew line from (" + x1 + ", " + y1 + ") to (" + x2 + ", " + y2 + ") - " + draw.color.asString());
        } else if (stmt instanceof ASTNode.DrawTriangle) {
            ASTNode.DrawTriangle draw = (ASTNode.DrawTriangle) stmt;
            double x1 = draw.x1.asNumber();
            double y1 = draw.y1.asNumber();
            double x2 = draw.x2.asNumber();
            double y2 = draw.y2.asNumber();
            double x3 = draw.x3.asNumber();
            double y3 = draw.y3.asNumber();
            Color color = parseColor(draw.color.asString());
            window.drawTriangle((int) x1, (int) y1, (int) x2, (int) y2, (int) x3, (int) y3, color);
            System.out.println("✓ Drew triangle - " + draw.color.asString());
        } else if (stmt instanceof ASTNode.PrintStmt) {
            ASTNode.PrintStmt printStmt = (ASTNode.PrintStmt) stmt;
            KuromiValue value = evaluate(printStmt.expression);
            System.out.println(value.asString());
        } else if (stmt instanceof ASTNode.VarDecl) {
            ASTNode.VarDecl varDecl = (ASTNode.VarDecl) stmt;
            KuromiValue value = evaluate(varDecl.initializer);
            environment.define(varDecl.name.lexeme, value);
        } else if (stmt instanceof ASTNode.Repeat) {
            ASTNode.Repeat repeat = (ASTNode.Repeat) stmt;
            int count = (int) evaluate(repeat.count).asNumber();
            for (int i = 0; i < count; i++) {
                for (ASTNode.Stmt bodyStmt : repeat.body) {
                    execute(bodyStmt);
                }
            }
        } else if (stmt instanceof ASTNode.Wait) {
            ASTNode.Wait wait = (ASTNode.Wait) stmt;
            long ms = (long) evaluate(wait.millis).asNumber();
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                System.err.println("[Error] Wait interrupted: " + e.getMessage());
            }
        } else if (stmt instanceof ASTNode.Call) {
            ASTNode.Call call = (ASTNode.Call) stmt;
            KuromiValue[] args = new KuromiValue[call.arguments.size()];
            for (int i = 0; i < args.length; i++) {
                args[i] = evaluate(call.arguments.get(i));
            }
            builtIns.call(call.name.lexeme, args);
            System.out.println("✓ Called function: " + call.name.lexeme);
        } else if (stmt instanceof ASTNode.IfStmt) {
            ASTNode.IfStmt ifStmt = (ASTNode.IfStmt) stmt;
            KuromiValue condition = evaluate(ifStmt.condition);
            if (condition.asBoolean()) {
                for (ASTNode.Stmt thenStmt : ifStmt.thenBranch) {
                    execute(thenStmt);
                }
            } else {
                for (ASTNode.Stmt elseStmt : ifStmt.elseBranch) {
                    execute(elseStmt);
                }
            }
        }
    }

    private KuromiValue evaluate(ASTNode.Expr expr) {
        if (expr instanceof ASTNode.Literal) {
            ASTNode.Literal lit = (ASTNode.Literal) expr;
            if (lit.value instanceof Double) return KuromiValue.number((Double) lit.value);
            if (lit.value instanceof String) return KuromiValue.string((String) lit.value);
            if (lit.value instanceof Boolean) return KuromiValue.bool((Boolean) lit.value);
        } else if (expr instanceof ASTNode.Variable) {
            ASTNode.Variable var = (ASTNode.Variable) expr;
            return environment.get(var.name.lexeme);
        }
        throw new RuntimeException("Unsupported expression.");
    }

    private Color parseColor(String colorStr) {
        try {
            return (Color) Color.class.getField(colorStr.toLowerCase()).get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Color.BLACK;
        }
    }

    public void setKeyState(String key, boolean pressed) {
        keyStates.put(key.toUpperCase(), pressed);
    }

    public boolean isKeyPressed(String key) {
        return keyStates.getOrDefault(key.toUpperCase(), false);
    }

    public void moveSprite(String name, double dx, double dy) {
        KuromiValue img = environment.get(name);
        Point pos = spritePositions.computeIfAbsent(name, k -> new Point(0, 0));
        pos.x += (int) dx;
        pos.y += (int) dy;
        window.drawImage(img.asImage(), pos.x, pos.y);
    }

    private class GameWindow extends JFrame {
        private final DrawingPanel panel;

        public GameWindow() {
            setTitle("KuromiCore Game");
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            panel = new DrawingPanel();
            add(panel);
            setFocusable(true);
            requestFocusInWindow();
            setVisible(true);
        }

        public void clear() {
            panel.clear();
        }

        public void close() {
            dispose();
        }

        public void drawImage(BufferedImage img, int x, int y) {
            panel.drawImage(img, x, y);
        }

        public void drawText(String text, int x, int y, String alignment) {
            panel.drawText(text, x, y, alignment);
        }

        public void drawRectangle(int x, int y, int w, int h, Color color) {
            panel.drawRectangle(x, y, w, h, color);
        }

        public void drawCircle(int x, int y, int r, Color color) {
            panel.drawCircle(x, y, r, color);
        }

        public void drawLine(int x1, int y1, int x2, int y2, Color color) {
            panel.drawLine(x1, y1, x2, y2, color);
        }

        public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Color color) {
            panel.drawTriangle(x1, y1, x2, y2, x3, y3, color);
        }
    }

    private class DrawingPanel extends JPanel {
        private BufferedImage buffer = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        private Graphics2D g2d = buffer.createGraphics();

        public DrawingPanel() {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, 800, 600);
        }

        public void clear() {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, 800, 600);
            repaint();
        }

        public void drawImage(BufferedImage img, int x, int y) {
            g2d.drawImage(img, x, y, null);
            repaint();
        }

        public void drawText(String text, int x, int y, String alignment) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 16));
            if ("center".equals(alignment)) {
                int width = g2d.getFontMetrics().stringWidth(text);
                x -= width / 2;
            } else if ("right".equals(alignment)) {
                int width = g2d.getFontMetrics().stringWidth(text);
                x -= width;
            }
            g2d.drawString(text, x, y);
            repaint();
        }

        public void drawRectangle(int x, int y, int w, int h, Color color) {
            g2d.setColor(color);
            g2d.fillRect(x, y, w, h);
            repaint();
        }

        public void drawCircle(int x, int y, int r, Color color) {
            g2d.setColor(color);
            g2d.fillOval(x - r, y - r, 2 * r, 2 * r);
            repaint();
        }

        public void drawLine(int x1, int y1, int x2, int y2, Color color) {
            g2d.setColor(color);
            g2d.drawLine(x1, y1, x2, y2);
            repaint();
        }

        public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Color color) {
            g2d.setColor(color);
            int[] xs = {x1, x2, x3};
            int[] ys = {y1, y2, y3};
            g2d.fillPolygon(xs, ys, 3);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(buffer, 0, 0, null);
        }
    }
}