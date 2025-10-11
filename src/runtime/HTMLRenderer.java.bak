// src/runtime/HTMLRenderer.java
package runtime;

import interpreter.KuromiValue;
import parser.ASTNode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class HTMLRenderer {
    public void renderToHTML(List<ASTNode.Stmt> statements, String outputPath) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<title>KuromiScript Game</title>\n");
        html.append("<style>\ncanvas { background: black; }\n</style>\n");
        html.append("</head>\n<body>\n");
        html.append("<canvas id=\"gameCanvas\" width=\"800\" height=\"600\"></canvas>\n");
        html.append("<script>\n");
        html.append("const canvas = document.getElementById('gameCanvas');\n");
        html.append("const ctx = canvas.getContext('2d');\n");
        html.append("ctx.font = '16px Arial';\n");
        html.append("const images = {};\n");
        html.append("let loadedImages = 0, totalImages = 0;\n");
        html.append("document.addEventListener('keydown', e => { window.keyStates = window.keyStates || {}; window.keyStates[e.key.toUpperCase()] = true; });\n");
        html.append("document.addEventListener('keyup', e => { window.keyStates = window.keyStates || {}; window.keyStates[e.key.toUpperCase()] = false; });\n");
        html.append("function isKeyPressed(key) { return window.keyStates && window.keyStates[key.toUpperCase()] || false; }\n");

        int imageCount = 0;
        for (ASTNode.Stmt stmt : statements) {
            if (stmt instanceof ASTNode.LoadImage) imageCount++;
        }

        html.append("function startGame() {\n");
        for (ASTNode.Stmt stmt : statements) {
            html.append(translateStmt(stmt));
        }
        html.append("}\n");

        if (imageCount > 0) {
            html.append("function loadImages(callback) {\n");
            for (ASTNode.Stmt stmt : statements) {
                if (stmt instanceof ASTNode.LoadImage) {
                    ASTNode.LoadImage load = (ASTNode.LoadImage) stmt;
                    String varName = load.varName.lexeme;
                    String path = load.path.asString();
                    html.append(String.format("const %s = new Image();\n", varName));
                    html.append(String.format("images['%s'] = %s;\n", varName, varName));
                    html.append(String.format("%s.src = 'assets/%s';\n", varName, path));
                    html.append(String.format("totalImages++;\n"));
                    html.append(String.format("%s.onload = () => { loadedImages++; if (loadedImages === totalImages) callback(); };\n", varName));
                }
            }
            html.append("}\n");
            html.append("loadImages(startGame);\n");
        } else {
            html.append("startGame();\n");
        }

        html.append("</script>\n</body>\n</html>");

        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(html.toString());
            System.out.println("Compiled to " + outputPath);
        } catch (IOException e) {
            System.err.println("Error writing HTML: " + e.getMessage());
        }
    }

    private String translateStmt(ASTNode.Stmt stmt) {
        StringBuilder js = new StringBuilder();
        if (stmt instanceof ASTNode.StartGame) {
            js.append("ctx.fillStyle = 'black';\n");
            js.append("ctx.fillRect(0, 0, canvas.width, canvas.height);\n");
        } else if (stmt instanceof ASTNode.CloseGame) {
            js.append("// Game closed\n");
        } else if (stmt instanceof ASTNode.LoadImage) {
            // Handled in loadImages
        } else if (stmt instanceof ASTNode.DisplayImage) {
            ASTNode.DisplayImage display = (ASTNode.DisplayImage) stmt;
            js.append(String.format("ctx.drawImage(images['%s'], %s, %s);\n",
                    display.varName.lexeme, exprToJS(display.x), exprToJS(display.y)));
        } else if (stmt instanceof ASTNode.ShowText) {
            ASTNode.ShowText show = (ASTNode.ShowText) stmt;
            String align = show.alignment != null ? show.alignment.lexeme : "left";
            js.append(String.format("ctx.textAlign = '%s';\n", align));
            js.append(String.format("ctx.fillStyle = 'white';\n"));
            js.append(String.format("ctx.fillText('%s', %s, %s);\n",
                    show.text.asString().replace("'", "\\'"), exprToJS(show.x), exprToJS(show.y)));
        } else if (stmt instanceof ASTNode.PlaySound) {
            ASTNode.PlaySound play = (ASTNode.PlaySound) stmt;
            js.append(String.format("new Audio('assets/%s').play();\n", play.path.asString()));
        } else if (stmt instanceof ASTNode.DrawRectangle) {
            ASTNode.DrawRectangle draw = (ASTNode.DrawRectangle) stmt;
            js.append(String.format("ctx.fillStyle = '%s';\n", draw.color.asString()));
            js.append(String.format("ctx.fillRect(%s, %s, %s, %s);\n",
                    exprToJS(draw.x), exprToJS(draw.y), exprToJS(draw.width), exprToJS(draw.height)));
        } else if (stmt instanceof ASTNode.DrawCircle) {
            ASTNode.DrawCircle draw = (ASTNode.DrawCircle) stmt;
            js.append(String.format("ctx.beginPath();\n"));
            js.append(String.format("ctx.arc(%s, %s, %s, 0, 2*Math.PI);\n",
                    exprToJS(draw.x), exprToJS(draw.y), exprToJS(draw.radius)));
            js.append(String.format("ctx.fillStyle = '%s';\n", draw.color.asString()));
            js.append("ctx.fill();\n");
        } else if (stmt instanceof ASTNode.DrawLine) {
            ASTNode.DrawLine draw = (ASTNode.DrawLine) stmt;
            js.append(String.format("ctx.beginPath();\n"));
            js.append(String.format("ctx.moveTo(%s, %s);\n", exprToJS(draw.x1), exprToJS(draw.y1)));
            js.append(String.format("ctx.lineTo(%s, %s);\n", exprToJS(draw.x2), exprToJS(draw.y2)));
            js.append(String.format("ctx.strokeStyle = '%s';\n", draw.color.asString()));
            js.append("ctx.lineWidth = 2;\n");
            js.append("ctx.stroke();\n");
        } else if (stmt instanceof ASTNode.DrawTriangle) {
            ASTNode.DrawTriangle draw = (ASTNode.DrawTriangle) stmt;
            js.append(String.format("ctx.beginPath();\n"));
            js.append(String.format("ctx.moveTo(%s, %s);\n", exprToJS(draw.x1), exprToJS(draw.y1)));
            js.append(String.format("ctx.lineTo(%s, %s);\n", exprToJS(draw.x2), exprToJS(draw.y2)));
            js.append(String.format("ctx.lineTo(%s, %s);\n", exprToJS(draw.x3), exprToJS(draw.y3)));
            js.append("ctx.closePath();\n");
            js.append(String.format("ctx.fillStyle = '%s';\n", draw.color.asString()));
            js.append("ctx.fill();\n");
        } else if (stmt instanceof ASTNode.PrintStmt) {
            ASTNode.PrintStmt print = (ASTNode.PrintStmt) stmt;
            js.append(String.format("console.log(%s);\n", exprToJS(print.expression)));
        } else if (stmt instanceof ASTNode.VarDecl) {
            ASTNode.VarDecl var = (ASTNode.VarDecl) stmt;
            js.append(String.format("let %s = %s;\n", var.name.lexeme, exprToJS(var.initializer)));
        } else if (stmt instanceof ASTNode.Repeat) {
            ASTNode.Repeat repeat = (ASTNode.Repeat) stmt;
            js.append(String.format("for (let i = 0; i < %s; i++) {\n", exprToJS(repeat.count)));
            for (ASTNode.Stmt bodyStmt : repeat.body) {
                js.append(translateStmt(bodyStmt));
            }
            js.append("}\n");
        } else if (stmt instanceof ASTNode.Wait) {
            js.append("// Wait not fully supported in web; use animations for delays\n");
        } else if (stmt instanceof ASTNode.IfStmt) {
            ASTNode.IfStmt ifStmt = (ASTNode.IfStmt) stmt;
            js.append(String.format("if (%s) {\n", exprToJS(ifStmt.condition)));
            for (ASTNode.Stmt thenStmt : ifStmt.thenBranch) {
                js.append(translateStmt(thenStmt));
            }
            js.append("} else {\n");
            for (ASTNode.Stmt elseStmt : ifStmt.elseBranch) {
                js.append(translateStmt(elseStmt));
            }
            js.append("}\n");
        } else if (stmt instanceof ASTNode.Call) {
            ASTNode.Call call = (ASTNode.Call) stmt;
            StringBuilder args = new StringBuilder();
            for (int i = 0; i < call.arguments.size(); i++) {
                args.append(exprToJS(call.arguments.get(i)));
                if (i < call.arguments.size() - 1) args.append(", ");
            }
            js.append(String.format("%s(%s);\n", call.name.lexeme, args));
        }
        return js.toString();
    }

    private String exprToJS(ASTNode.Expr expr) {
        if (expr instanceof ASTNode.Literal) {
            ASTNode.Literal lit = (ASTNode.Literal) expr;
            if (lit.value instanceof String) return "'" + lit.value + "'";
            return lit.value.toString();
        } else if (expr instanceof ASTNode.Variable) {
            ASTNode.Variable var = (ASTNode.Variable) expr;
            return var.name.lexeme;
        }
        return "0";
    }
}