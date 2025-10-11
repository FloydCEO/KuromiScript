// src/Main.java
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import parser.ASTNode;
import runtime.Interpreter;
import runtime.Compiler;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("KuromiScript - Game Development Language");
            System.out.println("Usage: java -cp out Main [--web] <file.kuromi>");
            System.out.println("\nExamples:");
            System.out.println("  java -cp out Main game.kuromi         # Run on desktop");
            System.out.println("  java -cp out Main --web game.kuromi  # Compile to HTML");
            System.exit(1);
        }

        String filepath = args[args.length - 1];
        boolean compileToWeb = args.length > 1 && "--web".equals(args[0]);

        if (!filepath.endsWith(".kuromi")) {
            System.err.println("Error: File must have .kuromi extension");
            System.exit(1);
        }

        try {
            // Read source
            System.out.println("→ Reading " + filepath + "...");
            String source = new String(Files.readAllBytes(Paths.get(filepath)));

            // Tokenize
            System.out.println("→ Tokenizing...");
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens();

            // Parse
            System.out.println("→ Parsing...");
            Parser parser = new Parser(tokens);
            List<ASTNode.Stmt> statements = parser.parse();

            if (compileToWeb) {
                // Compile to HTML
                System.out.println("→ Compiling to HTML...");
                Compiler compiler = new Compiler();
                compiler.compile(statements, "game.html");
                System.out.println("✓ Done! Open game.html in your browser.");
            } else {
                // Run on desktop
                System.out.println("→ Running...\n");
                Interpreter interpreter = new Interpreter();
                interpreter.interpret(statements);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}