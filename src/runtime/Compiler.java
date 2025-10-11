// src/runtime/Compiler.java
package runtime;

import parser.ASTNode;
import lexer.TokenType;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Compiler {
    private StringBuilder js;
    private int indentLevel = 0;
    private int gameWidth = 800;
    private int gameHeight = 600;

    public void compile(List<ASTNode.Stmt> statements, String outputPath) throws IOException {
        js = new StringBuilder();

        // Generate JavaScript code
        for (ASTNode.Stmt stmt : statements) {
            compileStmt(stmt);
        }

        // Wrap in HTML template
        String html = generateHTML(js.toString());

        // Write to file
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(html);
        }
    }

    private void compileStmt(ASTNode.Stmt stmt) {
        if (stmt instanceof ASTNode.GameStart) {
            ASTNode.GameStart game = (ASTNode.GameStart) stmt;
            gameWidth = game.width;
            gameHeight = game.height;
            for (ASTNode.Stmt s : game.body) {
                compileStmt(s);
            }
        } else if (stmt instanceof ASTNode.Let) {
            ASTNode.Let let = (ASTNode.Let) stmt;
            emit("let " + let.name.lexeme + " = " + compileExpr(let.initializer) + ";");
        } else if (stmt instanceof ASTNode.Assignment) {
            ASTNode.Assignment assign = (ASTNode.Assignment) stmt;
            emit(assign.name.lexeme + " = " + compileExpr(assign.value) + ";");
        } else if (stmt instanceof ASTNode.Function) {
            ASTNode.Function func = (ASTNode.Function) stmt;
            String params = String.join(", ", func.params.stream()
                    .map(t -> t.lexeme).toArray(String[]::new));
            emit("function " + func.name.lexeme + "(" + params + ") {");
            indentLevel++;
            for (ASTNode.Stmt s : func.body) {
                compileStmt(s);
            }
            indentLevel--;
            emit("}");
        } else if (stmt instanceof ASTNode.Return) {
            ASTNode.Return ret = (ASTNode.Return) stmt;
            if (ret.value != null) {
                emit("return " + compileExpr(ret.value) + ";");
            } else {
                emit("return;");
            }
        } else if (stmt instanceof ASTNode.If) {
            ASTNode.If ifStmt = (ASTNode.If) stmt;
            emit("if (" + compileExpr(ifStmt.condition) + ") {");
            indentLevel++;
            for (ASTNode.Stmt s : ifStmt.thenBranch) {
                compileStmt(s);
            }
            indentLevel--;
            if (!ifStmt.elseBranch.isEmpty()) {
                emit("} else {");
                indentLevel++;
                for (ASTNode.Stmt s : ifStmt.elseBranch) {
                    compileStmt(s);
                }
                indentLevel--;
            }
            emit("}");
        } else if (stmt instanceof ASTNode.While) {
            ASTNode.While whileStmt = (ASTNode.While) stmt;
            emit("while (" + compileExpr(whileStmt.condition) + ") {");
            indentLevel++;
            for (ASTNode.Stmt s : whileStmt.body) {
                compileStmt(s);
            }
            indentLevel--;
            emit("}");
        } else if (stmt instanceof ASTNode.For) {
            ASTNode.For forStmt = (ASTNode.For) stmt;
            emit("for (let " + forStmt.variable.lexeme + " of " + compileExpr(forStmt.iterable) + ") {");
            indentLevel++;
            for (ASTNode.Stmt s : forStmt.body) {
                compileStmt(s);
            }
            indentLevel--;
            emit("}");
        } else if (stmt instanceof ASTNode.Load) {
            ASTNode.Load load = (ASTNode.Load) stmt;
            emit("// Load image: " + load.path);
            emit(load.name.lexeme + " = new Image();");
            emit(load.name.lexeme + ".src = 'assets/" + load.path + "';");
        } else if (stmt instanceof ASTNode.Draw) {
            ASTNode.Draw draw = (ASTNode.Draw) stmt;
            compileDraw(draw);
        } else if (stmt instanceof ASTNode.Show) {
            ASTNode.Show show = (ASTNode.Show) stmt;
            emit("ctx.fillStyle = 'white';");
            emit("ctx.font = '16px Arial';");
            String x = compileExpr(show.x);
            String y = compileExpr(show.y);
            if ("center".equals(show.alignment)) {
                emit("ctx.textAlign = 'center';");
            } else if ("right".equals(show.alignment)) {
                emit("ctx.textAlign = 'right';");
            } else {
                emit("ctx.textAlign = 'left';");
            }
            emit("ctx.fillText(\"" + show.text + "\", " + x + ", " + y + ");");
        } else if (stmt instanceof ASTNode.Play) {
            emit("// Play sound: " + ((ASTNode.Play) stmt).path);
        } else if (stmt instanceof ASTNode.Print) {
            ASTNode.Print print = (ASTNode.Print) stmt;
            emit("console.log(" + compileExpr(print.expression) + ");");
        } else if (stmt instanceof ASTNode.Block) {
            ASTNode.Block block = (ASTNode.Block) stmt;
            emit("{");
            indentLevel++;
            for (ASTNode.Stmt s : block.statements) {
                compileStmt(s);
            }
            indentLevel--;
            emit("}");
        } else if (stmt instanceof ASTNode.ExpressionStmt) {
            ASTNode.ExpressionStmt exprStmt = (ASTNode.ExpressionStmt) stmt;
            emit(compileExpr(exprStmt.expression) + ";");
        }
    }

    private void compileDraw(ASTNode.Draw draw) {
        List<ASTNode.Expr> args = draw.args;
        String type = draw.type;

        if ("rect".equals(type) && args.size() >= 4) {
            emit("ctx.fillStyle = '" + draw.color + "';");
            emit("ctx.fillRect(" +
                    compileExpr(args.get(0)) + ", " +
                    compileExpr(args.get(1)) + ", " +
                    compileExpr(args.get(2)) + ", " +
                    compileExpr(args.get(3)) + ");");
        } else if ("circle".equals(type) && args.size() >= 3) {
            emit("ctx.fillStyle = '" + draw.color + "';");
            emit("ctx.beginPath();");
            String x = compileExpr(args.get(0));
            String y = compileExpr(args.get(1));
            String r = compileExpr(args.get(2));
            emit("ctx.arc(" + x + ", " + y + ", " + r + ", 0, Math.PI * 2);");
            emit("ctx.fill();");
        } else if ("image".equals(type) && args.size() >= 3) {
            emit("ctx.drawImage(" +
                    compileExpr(args.get(0)) + ", " +
                    compileExpr(args.get(1)) + ", " +
                    compileExpr(args.get(2)) + ");");
        } else if ("line".equals(type) && args.size() >= 4) {
            emit("ctx.strokeStyle = '" + draw.color + "';");
            emit("ctx.lineWidth = 2;");
            emit("ctx.beginPath();");
            emit("ctx.moveTo(" + compileExpr(args.get(0)) + ", " + compileExpr(args.get(1)) + ");");
            emit("ctx.lineTo(" + compileExpr(args.get(2)) + ", " + compileExpr(args.get(3)) + ");");
            emit("ctx.stroke();");
        }
    }

    private String compileExpr(ASTNode.Expr expr) {
        if (expr instanceof ASTNode.Literal) {
            Object value = ((ASTNode.Literal) expr).value;
            if (value instanceof String) {
                return "\"" + value + "\"";
            }
            if (value == null) return "null";
            return value.toString();
        } else if (expr instanceof ASTNode.Variable) {
            return ((ASTNode.Variable) expr).name.lexeme;
        } else if (expr instanceof ASTNode.Binary) {
            ASTNode.Binary binary = (ASTNode.Binary) expr;
            String left = compileExpr(binary.left);
            String right = compileExpr(binary.right);
            String op = getJSOperator(binary.operator.type);
            return "(" + left + " " + op + " " + right + ")";
        } else if (expr instanceof ASTNode.Unary) {
            ASTNode.Unary unary = (ASTNode.Unary) expr;
            String op = getJSOperator(unary.operator.type);
            return "(" + op + compileExpr(unary.right) + ")";
        } else if (expr instanceof ASTNode.Call) {
            ASTNode.Call call = (ASTNode.Call) expr;
            String callee = compileExpr(call.callee);
            String args = String.join(", ", call.arguments.stream()
                    .map(this::compileExpr).toArray(String[]::new));
            return callee + "(" + args + ")";
        } else if (expr instanceof ASTNode.Index) {
            ASTNode.Index index = (ASTNode.Index) expr;
            return compileExpr(index.object) + "[" + compileExpr(index.index) + "]";
        } else if (expr instanceof ASTNode.ArrayLiteral) {
            ASTNode.ArrayLiteral arrLit = (ASTNode.ArrayLiteral) expr;
            String elements = String.join(", ", arrLit.elements.stream()
                    .map(this::compileExpr).toArray(String[]::new));
            return "[" + elements + "]";
        }
        return "null";
    }

    private String getJSOperator(TokenType type) {
        return switch (type) {
            case PLUS -> "+";
            case MINUS -> "-";
            case STAR -> "*";
            case SLASH -> "/";
            case PERCENT -> "%";
            case LESS -> "<";
            case LESS_EQUAL -> "<=";
            case GREATER -> ">";
            case GREATER_EQUAL -> ">=";
            case EQUAL_EQUAL -> "===";
            case BANG_EQUAL -> "!==";
            case AND -> "&&";
            case OR -> "||";
            case BANG, NOT -> "!";
            default -> "?";
        };
    }

    private void emit(String code) {
        for (int i = 0; i < indentLevel; i++) {
            js.append("    ");
        }
        js.append(code).append("\n");
    }

    private String generateHTML(String jsCode) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>KuromiScript Game</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            min-height: 100vh;\n" +
                "            background: #1a1a2e;\n" +
                "            font-family: Arial, sans-serif;\n" +
                "        }\n" +
                "        #gameCanvas {\n" +
                "            border: 2px solid #ff6b9d;\n" +
                "            box-shadow: 0 0 20px rgba(255, 107, 157, 0.5);\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <canvas id=\"gameCanvas\" width=\"" + gameWidth + "\" height=\"" + gameHeight + "\"></canvas>\n" +
                "    <script>\n" +
                "        const canvas = document.getElementById('gameCanvas');\n" +
                "        const ctx = canvas.getContext('2d');\n" +
                "        \n" +
                "        // Clear canvas\n" +
                "        ctx.fillStyle = 'black';\n" +
                "        ctx.fillRect(0, 0, canvas.width, canvas.height);\n" +
                "        \n" +
                "        // Game code\n" +
                jsCode + "\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}