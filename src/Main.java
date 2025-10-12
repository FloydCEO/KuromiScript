// src/Main.java - NO PACKAGE DECLARATION
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import parser.ASTNode;
import interpreter.Interpreter;
import runtime.Compiler;
import runtime.JarBuilder;
import gui.KuromiCoreGUI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Launch GUI if no arguments provided
        if (args.length == 0) {
            System.out.println("🎮 Launching KuromiCore GUI...");
            javax.swing.SwingUtilities.invokeLater(() -> {
                new KuromiCoreGUI().setVisible(true);
            });
            return;
        }

        // CLI mode - handle special flags
        if (args[0].equals("--help") || args[0].equals("-h")) {
            printHelp();
            return;
        }

        if (args[0].equals("--version") || args[0].equals("-v")) {
            System.out.println("╔════════════════════════════════════════╗");
            System.out.println("║    KuromiCore Engine v1.0.0            ║");
            System.out.println("║  Easy Game & Web Development           ║");
            System.out.println("╚════════════════════════════════════════╝");
            return;
        }

        // Parse command line arguments
        String mode = null;
        String filepath = null;
        String outputName = "game";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--web") || args[i].equals("-w")) {
                mode = "web";
            } else if (args[i].equals("--jar") || args[i].equals("-j")) {
                mode = "jar";
            } else if (args[i].equals("--run") || args[i].equals("-r")) {
                mode = "run";
            } else if (args[i].equals("--output") || args[i].equals("-o")) {
                if (i + 1 < args.length) {
                    outputName = args[++i];
                }
            } else if (args[i].endsWith(".kuromi")) {
                filepath = args[i];
            }
        }

        // Validate filepath
        if (filepath == null) {
            System.err.println("❌ Error: No .kuromi file specified");
            System.err.println("Usage: java -jar KuromiCore.jar [OPTIONS] <file.kuromi>");
            System.err.println("Run with --help for more information");
            System.exit(1);
        }

        // Default mode is run
        if (mode == null) {
            mode = "run";
        }

        // Process the script
        try {
            processKuromiScript(filepath, mode, outputName);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void processKuromiScript(String filepath, String mode, String outputName) throws Exception {
        // Read source file
        System.out.println("📖 Reading " + filepath + "...");
        if (!Files.exists(Paths.get(filepath))) {
            throw new Exception("File not found: " + filepath);
        }

        String source = new String(Files.readAllBytes(Paths.get(filepath)));

        // Tokenize
        System.out.println("🔤 Tokenizing...");
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        if (tokens.isEmpty()) {
            throw new Exception("No tokens generated - file may be empty");
        }

        // Parse
        System.out.println("🌳 Parsing...");
        Parser parser = new Parser(tokens);
        List<ASTNode.Stmt> statements = parser.parse();

        if (statements.isEmpty()) {
            throw new Exception("No statements parsed - check syntax");
        }

        // Execute based on mode
        switch (mode) {
            case "web":
                System.out.println("🌐 Compiling to HTML...");
                Compiler compiler = new Compiler();
                String htmlPath = outputName.endsWith(".html") ? outputName : outputName + ".html";
                compiler.compile(statements, htmlPath);
                System.out.println("✅ Done! Open " + htmlPath + " in your browser.");
                System.out.println("💡 Tip: Run a local server for image loading:");
                System.out.println("   python -m http.server");
                break;

            case "jar":
                System.out.println("📦 Building JAR file...");
                JarBuilder jarBuilder = new JarBuilder();
                String jarPath = outputName.endsWith(".jar") ? outputName : outputName + ".jar";
                jarBuilder.buildJar(statements, source, jarPath);
                System.out.println("✅ Done! Run with: java -jar " + jarPath);
                break;

            case "run":
            default:
                System.out.println("▶️  Running...\n");
                System.out.println("╔════════════════════════════════════════╗");
                System.out.println("║          PROGRAM OUTPUT                ║");
                System.out.println("╚════════════════════════════════════════╝");
                System.out.println();

                Interpreter interpreter = new Interpreter();
                interpreter.interpret(statements);

                System.out.println();
                System.out.println("╔════════════════════════════════════════╗");
                System.out.println("║       ✅ EXECUTION COMPLETED           ║");
                System.out.println("╚════════════════════════════════════════╝");
                break;
        }
    }

    private static void printHelp() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║        KuromiCore - Easy Game & Web Development              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("USAGE:");
        System.out.println("  java -jar KuromiCore.jar [OPTIONS] <file.kuromi>");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  (no args)          Launch GUI editor");
        System.out.println("  -r, --run          Run script directly (default)");
        System.out.println("  -w, --web          Compile to HTML website");
        System.out.println("  -j, --jar          Build standalone JAR file");
        System.out.println("  -o, --output NAME  Specify output filename");
        System.out.println("  -h, --help         Show this help");
        System.out.println("  -v, --version      Show version info");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  java -jar KuromiCore.jar");
        System.out.println("  java -jar KuromiCore.jar game.kuromi");
        System.out.println("  java -jar KuromiCore.jar --web game.kuromi");
        System.out.println("  java -jar KuromiCore.jar --jar game.kuromi");
        System.out.println();
        System.out.println("Learn more: README.md");
    }
}