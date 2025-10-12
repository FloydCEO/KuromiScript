// src/gui/KuromiCoreGUI.java
package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KuromiCoreGUI extends JFrame {
    private JTextArea codeEditor;
    private JTextArea consoleOutput;
    private JLabel statusLabel;
    private File currentFile;
    private boolean isModified = false;

    public KuromiCoreGUI() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("KuromiCore Engine - Easy Game & Web Development");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Menu bar
        createMenuBar();

        // Main panel with split pane
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setDividerLocation(500);
        mainSplit.setResizeWeight(0.7);

        // Code editor panel
        JPanel editorPanel = createEditorPanel();
        mainSplit.setTopComponent(editorPanel);

        // Console panel
        JPanel consolePanel = createConsolePanel();
        mainSplit.setBottomComponent(consolePanel);

        // Toolbar
        JPanel toolbar = createToolbar();

        // Status bar
        statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Layout
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Set default template
        setDefaultTemplate();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem newItem = new JMenuItem("New", KeyEvent.VK_N);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newItem.addActionListener(e -> newFile());

        JMenuItem openItem = new JMenuItem("Open...", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> openFile());

        JMenuItem saveItem = new JMenuItem("Save", KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> saveFile());

        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.addActionListener(e -> saveFileAs());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Build menu
        JMenu buildMenu = new JMenu("Build");
        buildMenu.setMnemonic(KeyEvent.VK_B);

        JMenuItem runItem = new JMenuItem("Run", KeyEvent.VK_R);
        runItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        runItem.addActionListener(e -> runScript());

        JMenuItem compileWebItem = new JMenuItem("Compile to HTML");
        compileWebItem.addActionListener(e -> compileToWeb());

        JMenuItem compileJarItem = new JMenuItem("Build JAR");
        compileJarItem.addActionListener(e -> buildJar());

        buildMenu.add(runItem);
        buildMenu.addSeparator();
        buildMenu.add(compileWebItem);
        buildMenu.add(compileJarItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem examplesItem = new JMenuItem("Examples");
        examplesItem.addActionListener(e -> showExamples());

        JMenuItem docsItem = new JMenuItem("Documentation");
        docsItem.addActionListener(e -> showDocumentation());

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());

        helpMenu.add(examplesItem);
        helpMenu.add(docsItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(buildMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private JPanel createEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Code Editor"));

        codeEditor = new JTextArea();
        codeEditor.setFont(new Font("Consolas", Font.PLAIN, 14));
        codeEditor.setTabSize(4);
        codeEditor.setLineWrap(false);
        codeEditor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { setModified(true); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { setModified(true); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { setModified(true); }
        });

        JScrollPane scrollPane = new JScrollPane(codeEditor);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Console Output"));

        consoleOutput = new JTextArea();
        consoleOutput.setFont(new Font("Consolas", Font.PLAIN, 12));
        consoleOutput.setEditable(false);
        consoleOutput.setBackground(new Color(30, 30, 30));
        consoleOutput.setForeground(new Color(200, 200, 200));

        JScrollPane scrollPane = new JScrollPane(consoleOutput);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBorder(BorderFactory.createEtchedBorder());

        JButton runBtn = new JButton("▶ Run (F5)");
        runBtn.addActionListener(e -> runScript());
        runBtn.setToolTipText("Run your KuromiScript");

        JButton webBtn = new JButton("🌐 Web");
        webBtn.addActionListener(e -> compileToWeb());
        webBtn.setToolTipText("Compile to HTML website");

        JButton jarBtn = new JButton("📦 JAR");
        jarBtn.addActionListener(e -> buildJar());
        jarBtn.setToolTipText("Build standalone JAR file");

        JButton clearBtn = new JButton("🗑️ Clear Console");
        clearBtn.addActionListener(e -> consoleOutput.setText(""));
        clearBtn.setToolTipText("Clear console output");

        toolbar.add(runBtn);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(webBtn);
        toolbar.add(jarBtn);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(clearBtn);

        return toolbar;
    }

    private void setDefaultTemplate() {
        codeEditor.setText(
                "// Welcome to KuromiCore!\n" +
                        "// Create games and websites with ease!\n\n" +
                        "game 800 600 {\n" +
                        "    // Draw a welcome message\n" +
                        "    show \"Welcome to KuromiScript!\" 400 300 center\n" +
                        "    \n" +
                        "    // Draw a colorful rectangle\n" +
                        "    draw rect 300 350 200 100 \"blue\"\n" +
                        "    \n" +
                        "    // Draw a circle\n" +
                        "    draw circle 400 250 50 \"red\"\n" +
                        "    \n" +
                        "    print \"Game started!\"\n" +
                        "}\n"
        );
        isModified = false;
        updateTitle();
    }

    private void newFile() {
        if (checkSaveBeforeAction()) {
            currentFile = null;
            setDefaultTemplate();
            updateStatus("New file created");
        }
    }

    private void openFile() {
        if (!checkSaveBeforeAction()) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("KuromiScript Files (*.kuromi)", "kuromi"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentFile = fileChooser.getSelectedFile();
                String content = new String(Files.readAllBytes(currentFile.toPath()));
                codeEditor.setText(content);
                isModified = false;
                updateTitle();
                updateStatus("Opened: " + currentFile.getName());
            } catch (IOException e) {
                showError("Error opening file: " + e.getMessage());
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
        } else {
            try {
                Files.write(currentFile.toPath(), codeEditor.getText().getBytes());
                isModified = false;
                updateTitle();
                updateStatus("Saved: " + currentFile.getName());
            } catch (IOException e) {
                showError("Error saving file: " + e.getMessage());
            }
        }
    }

    private void saveFileAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("KuromiScript Files (*.kuromi)", "kuromi"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".kuromi")) {
                    file = new File(file.getAbsolutePath() + ".kuromi");
                }
                Files.write(file.toPath(), codeEditor.getText().getBytes());
                currentFile = file;
                isModified = false;
                updateTitle();
                updateStatus("Saved as: " + currentFile.getName());
            } catch (IOException e) {
                showError("Error saving file: " + e.getMessage());
            }
        }
    }

    private void runScript() {
        saveToTemp();
        updateStatus("Running script...");
        consoleOutput.append("\n▶ Running...\n");

        new Thread(() -> {
            try {
                PrintStream ps = new PrintStream(new ConsoleOutputStream());
                System.setOut(ps);
                System.setErr(ps);

                // Use reflection to call Main.processKuromiScript
                try {
                    Class<?> mainClass = Class.forName("Main");
                    java.lang.reflect.Method method = mainClass.getMethod("processKuromiScript", String.class, String.class, String.class);
                    method.invoke(null, "temp.kuromi", "run", "output");
                } catch (ClassNotFoundException e) {
                    consoleOutput.append("❌ Error: Main class not found\n");
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    updateStatus("Script executed successfully");
                    consoleOutput.append("✅ Done!\n");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    consoleOutput.append("❌ Error: " + e.getMessage() + "\n");
                    updateStatus("Error during execution");
                });
            }
        }).start();
    }

    private void compileToWeb() {
        saveToTemp();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save HTML as...");
        fileChooser.setSelectedFile(new File("game.html"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String outputPath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!outputPath.endsWith(".html")) {
                outputPath += ".html";
            }

            final String finalPath = outputPath;
            new Thread(() -> {
                try {
                    consoleOutput.append("\n🌐 Compiling to HTML...\n");

                    // Use reflection to call Main.processKuromiScript
                    Class<?> mainClass = Class.forName("Main");
                    java.lang.reflect.Method method = mainClass.getMethod("processKuromiScript", String.class, String.class, String.class);
                    method.invoke(null, "temp.kuromi", "web", finalPath.replace(".html", ""));

                    SwingUtilities.invokeLater(() -> {
                        consoleOutput.append("✅ HTML created: " + finalPath + "\n");
                        updateStatus("Compiled to HTML successfully");
                        JOptionPane.showMessageDialog(this,
                                "HTML created successfully!\n" + finalPath,
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        showError("Error compiling to HTML: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void buildJar() {
        saveToTemp();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save JAR as...");
        fileChooser.setSelectedFile(new File("game.jar"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String outputPath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!outputPath.endsWith(".jar")) {
                outputPath += ".jar";
            }

            final String finalPath = outputPath;
            new Thread(() -> {
                try {
                    consoleOutput.append("\n📦 Building JAR...\n");

                    // Use reflection to call Main.processKuromiScript
                    Class<?> mainClass = Class.forName("Main");
                    java.lang.reflect.Method method = mainClass.getMethod("processKuromiScript", String.class, String.class, String.class);
                    method.invoke(null, "temp.kuromi", "jar", finalPath.replace(".jar", ""));

                    SwingUtilities.invokeLater(() -> {
                        consoleOutput.append("✅ JAR created: " + finalPath + "\n");
                        updateStatus("JAR built successfully");
                        JOptionPane.showMessageDialog(this,
                                "JAR created successfully!\nRun with: java -jar " + new File(finalPath).getName(),
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        showError("Error building JAR: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void saveToTemp() {
        try {
            Files.write(Paths.get("temp.kuromi"), codeEditor.getText().getBytes());
        } catch (IOException e) {
            showError("Error creating temporary file: " + e.getMessage());
        }
    }

    private void showExamples() {
        String[] examples = {
                "Hello World",
                "Moving Ball",
                "Interactive Button",
                "Animation Loop"
        };

        String choice = (String) JOptionPane.showInputDialog(this,
                "Choose an example:",
                "Examples",
                JOptionPane.PLAIN_MESSAGE,
                null,
                examples,
                examples[0]);

        if (choice != null) {
            loadExample(choice);
        }
    }

    private void loadExample(String example) {
        String code = "";
        switch (example) {
            case "Hello World":
                code = "game 800 600 {\n    show \"Hello, World!\" 400 300 center\n    draw circle 400 350 30 \"pink\"\n}";
                break;
            case "Moving Ball":
                code = "game 800 600 {\n    let x = 0\n    while (x < 800) {\n        draw circle x 300 20 \"blue\"\n        x = x + 10\n    }\n}";
                break;
            case "Interactive Button":
                code = "game 800 600 {\n    draw rect 300 250 200 100 \"green\"\n    show \"Click Me!\" 400 300 center\n}";
                break;
            case "Animation Loop":
                code = "game 800 600 {\n    let colors = [\"red\", \"blue\", \"green\", \"yellow\"]\n    for c in colors {\n        draw circle 400 300 50 c\n    }\n}";
                break;
        }
        codeEditor.setText(code);
        isModified = true;
        updateTitle();
    }

    private void showDocumentation() {
        JTextArea docArea = new JTextArea(20, 50);
        docArea.setEditable(false);
        docArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        docArea.setText(
                "╔════════════════════════════════════════════════════════════╗\n" +
                        "║        KUROMISCRIPT DOCUMENTATION                       ║\n" +
                        "╚════════════════════════════════════════════════════════════╝\n\n" +
                        "BASIC STRUCTURE:\n" +
                        "  game WIDTH HEIGHT { ... }  - Start a game\n\n" +
                        "VARIABLES:\n" +
                        "  let name = value          - Declare variable\n" +
                        "  name = newValue           - Assign value\n\n" +
                        "DRAWING:\n" +
                        "  draw rect x y w h \"color\" - Draw rectangle\n" +
                        "  draw circle x y r \"color\" - Draw circle\n" +
                        "  draw line x1 y1 x2 y2 \"color\" - Draw line\n" +
                        "  draw image imgVar x y    - Draw image\n\n" +
                        "TEXT:\n" +
                        "  show \"text\" x y          - Display text\n" +
                        "  show \"text\" x y center   - Centered text\n\n" +
                        "CONTROL FLOW:\n" +
                        "  if (condition) { ... }   - Conditional\n" +
                        "  else { ... }             - Alternative branch\n" +
                        "  while (condition) { ... } - Loop\n" +
                        "  for item in array { ... } - For-each loop\n\n" +
                        "FUNCTIONS:\n" +
                        "  fn name(param1, param2) {\n" +
                        "    return value\n" +
                        "  }\n\n" +
                        "ARRAYS:\n" +
                        "  let arr = [1, 2, 3]\n" +
                        "  arr[0]                   - Access element\n\n" +
                        "OPERATORS:\n" +
                        "  +, -, *, /, %           - Math\n" +
                        "  ==, !=, <, >, <=, >=    - Comparison\n" +
                        "  and, or, not            - Logic\n\n" +
                        "COLORS:\n" +
                        "  red, green, blue, yellow, cyan, magenta,\n" +
                        "  white, black, gray\n\n" +
                        "EXAMPLE:\n" +
                        "  game 800 600 {\n" +
                        "    let x = 100\n" +
                        "    draw circle x 300 50 \"blue\"\n" +
                        "    show \"Hello!\" 400 100 center\n" +
                        "  }\n"
        );

        JScrollPane scrollPane = new JScrollPane(docArea);
        scrollPane.setPreferredSize(new Dimension(600, 500));

        JOptionPane.showMessageDialog(this,
                scrollPane,
                "KuromiScript Documentation",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        JPanel aboutPanel = new JPanel(new BorderLayout(10, 10));
        aboutPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("KuromiCore Engine", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setOpaque(false);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 12));
        infoArea.setText(
                "Version: 1.0.0\n\n" +
                        "Easy Game & Web Development for Beginners\n\n" +
                        "Create games and websites with simple KuromiScript!\n\n" +
                        "Features:\n" +
                        "  • Visual game development\n" +
                        "  • Compile to HTML websites\n" +
                        "  • Build standalone JAR files\n" +
                        "  • Beginner-friendly syntax\n" +
                        "  • Real-time testing\n\n" +
                        "Created with ❤️ for aspiring developers\n\n" +
                        "Start creating today!"
        );

        aboutPanel.add(titleLabel, BorderLayout.NORTH);
        aboutPanel.add(infoArea, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this,
                aboutPanel,
                "About KuromiCore",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean checkSaveBeforeAction() {
        if (isModified) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Do you want to save changes?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                saveFile();
                return true;
            } else if (result == JOptionPane.NO_OPTION) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    private void exitApplication() {
        if (checkSaveBeforeAction()) {
            System.exit(0);
        }
    }

    private void setModified(boolean modified) {
        this.isModified = modified;
        updateTitle();
    }

    private void updateTitle() {
        String title = "KuromiCore Engine";
        if (currentFile != null) {
            title += " - " + currentFile.getName();
        }
        if (isModified) {
            title += " *";
        }
        setTitle(title);
    }

    private void updateStatus(String message) {
        statusLabel.setText(" " + message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
        consoleOutput.append("❌ " + message + "\n");
    }

    // Custom OutputStream to redirect console to GUI
    private class ConsoleOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            SwingUtilities.invokeLater(() -> {
                consoleOutput.append(String.valueOf((char) b));
                consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
            });
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            String text = new String(b, off, len);
            SwingUtilities.invokeLater(() -> {
                consoleOutput.append(text);
                consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
            });
        }
    }
}