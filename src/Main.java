// src/kuromiscript/Main.java
package kuromiscript;

import interpreter.Interpreter;
import lexer.Lexer;
import lexer.Token;
import parser.ASTNode;
import parser.Parser;
import runtime.HTMLRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar KuromiScript.jar [--web] <script.kuromi>");
            System.exit(64);
        }

        String source = readFile(args[args.length - 1]);
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens);
        List<ASTNode.Stmt> statements = parser.parse();

        if (args.length > 1 && args[0].equals("--web")) {
            HTMLRenderer renderer = new HTMLRenderer();
            renderer.renderToHTML(statements, "output.html");
        } else {
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(statements);
        }
    }

    private static String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }
}