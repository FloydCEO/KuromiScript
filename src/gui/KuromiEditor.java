// src/gui/KuromiEditor.java
package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KuromiEditor extends JFrame {
    private JTabbedPane mainTabs;
    private JTextArea codeEditor;
    private JTextArea console;
    private JPanel previewPanel;
    private JComboBox<String> projectTypeCombo;
    private JLabel statusLabel;
    private File currentFile;
    private boolean isDarkMode = true;

    // Colors for dark theme
    private static final Color BG_DARK = new Color(30, 30, 35);
    private static final Color PANEL_DARK = new Color(45, 45, 50);
    private static final Color ACCENT = new Color(139, 69, 255); // Purple
    private static final Color ACCENT_LIGHT = new Color(180, 100, 255);
    private static final Color TEXT_LIGHT = new Color(220, 220, 220);
    private static final Color TEXT_DIM = new Color(150, 150, 150);

    public KuromiEditor() {
        initializeFrame();
        setupUI();
        setupTheme();
    }

    private void initializeFrame() {
        setTitle("KuromiCore Engine - Visual Programming");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("kuromi.ico").getImage());
    }

    private void setupUI() {
        // Main layout
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        // Toolbar
        JPanel toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);

        // Main content with tabs
        mainTabs = new JTabbedPane();
        mainTabs.setTabPlacement(JTabbedPane.TOP);

        // Code Editor Tab
        mainTabs.addTab("Code", createCodeEditorTab());

        // Designer Tab
        mainTabs.addTab("Designer", createDesignerTab());

        // Preview Tab
        mainTabs.addTab("Preview", createPreviewTab());

        add(mainTabs, BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        toolbar.setBackground(PANEL_DARK);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT));

        // New Project
        JButton newBtn = createToolButton("New", KeyEvent.VK_N);
        newBtn.addActionListener(e -> newProject());

        // Open
        JButton openBtn = createToolButton("Open", KeyEvent.VK_O);
        openBtn.addActionListener(e -> openProject());

        // Save
        JButton saveBtn = createToolButton("Save", KeyEvent.VK_S);
        saveBtn.addActionListener(e -> saveProject());

        toolbar.add(newBtn);
        toolbar.add(openBtn);
        toolbar.add(saveBtn);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));

        // Project Type
        projectTypeCombo = new JComboBox<>(new String[]{"Game", "Website"});
        projectTypeCombo.setBackground(PANEL_DARK);
        projectTypeCombo.setForeground(TEXT_LIGHT);
        toolbar.add(new JLabel("Type:"));
        toolbar.add(projectTypeCombo);

        toolbar.add(Box.createHorizontalStrut(30));

        // Run/Play
        JButton runBtn = createToolButton("▶ Run", KeyEvent.VK_F5);
        runBtn.setBackground(ACCENT);
        runBtn.addActionListener(e -> runProject());

        // Compile to Web
        JButton webBtn = createToolButton("🌐 Web");
        webBtn.addActionListener(e -> compileToWeb());

        // Compile to EXE
        JButton exeBtn = createToolButton("📦 EXE");
        exeBtn.addActionListener(e -> compileToEXE());

        toolbar.add(runBtn);
        toolbar.add(webBtn);
        toolbar.add(exeBtn);

        return toolbar;
    }

    private JButton createToolButton(String text, int mnemonic) {
        JButton btn = new JButton(text);
        btn.setBackground(PANEL_DARK);
        btn.setForeground(TEXT_LIGHT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(ACCENT, 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (mnemonic > 0) {
            btn.setMnemonic(mnemonic);
        }
        return btn;
    }

    private JButton createToolButton(String text) {
        return createToolButton(text, -1);
    }

    private JPanel createCodeEditorTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        // Code editor
        codeEditor = new JTextArea();
        codeEditor.setFont(new Font("Consolas", Font.PLAIN, 13));
        codeEditor.setBackground(BG_DARK);
        codeEditor.setForeground(TEXT_LIGHT);
        codeEditor.setCaretColor(ACCENT);
        codeEditor.setMargin(new Insets(10, 10, 10, 10));
        codeEditor.setLineWrap(false);

        // Line numbers
        JPanel lineNumberPanel = createLineNumbers(codeEditor);

        JScrollPane scrollPane = new JScrollPane(codeEditor);
        scrollPane.setRowHeaderView(lineNumberPanel);
        scrollPane.setBackground(PANEL_DARK);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom console
        console = new JTextArea(5, 50);
        console.setFont(new Font("Consolas", Font.PLAIN, 11));
        console.setBackground(new Color(20, 20, 25));
        console.setForeground(new Color(100, 200, 100));
        console.setEditable(false);
        console.setMargin(new Insets(5, 5, 5, 5));

        JScrollPane consoleScroll = new JScrollPane(console);
        consoleScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT),
                "Console", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11), ACCENT));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, consoleScroll);
        split.setDividerLocation(600);
        split.setResizeWeight(0.7);

        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLineNumbers(JTextArea textArea) {
        JPanel linePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setFont(new Font("Consolas", Font.PLAIN, 12));
                g2.setColor(TEXT_DIM);

                int lineCount = textArea.getLineCount();
                FontMetrics fm = g2.getFontMetrics();
                int lineHeight = fm.getHeight();

                for (int i = 1; i <= lineCount; i++) {
                    g2.drawString(String.valueOf(i), 5, i * lineHeight);
                }
            }
        };
        linePanel.setBackground(PANEL_DARK);
        linePanel.setPreferredSize(new Dimension(40, 0));
        return linePanel;
    }

    private JPanel createDesignerTab() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_DARK);
        panel.add(new JLabel("Visual Designer (Coming Soon)"));
        return panel;
    }

    private JPanel createPreviewTab() {
        previewPanel = new JPanel();
        previewPanel.setBackground(BG_DARK);
        previewPanel.add(new JLabel("Live Preview"));
        return previewPanel;
    }

    private void setupTheme() {
        UIManager.put("TabbedPane.background", PANEL_DARK);
        UIManager.put("TabbedPane.foreground", TEXT_LIGHT);
        UIManager.put("TabbedPane.selected", ACCENT);
    }

    private void newProject() {
        codeEditor.setText("project \"NewProject\" {\n" +
                "    config {\n" +
                "        type: \"game\"\n" +
                "        width: 800\n" +
                "        height: 600\n" +
                "        title: \"My Game\"\n" +
                "    }\n" +
                "\n" +
                "    screen \"main\" {\n" +
                "        show \"Welcome!\" 400 300 center\n" +
                "    }\n" +
                "}\n");
        currentFile = null;
        updateStatus("New project created");
    }

    private void openProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Kuromi Projects (*.kuromi)", "kuromi"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentFile = chooser.getSelectedFile();
                String content = new String(Files.readAllBytes(currentFile.toPath()));
                codeEditor.setText(content);
                updateStatus("Opened: " + currentFile.getName());
            } catch (IOException ex) {
                showError("Failed to open file: " + ex.getMessage());
            }
        }
    }

    private void saveProject() {
        if (currentFile == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Kuromi Projects (*.kuromi)", "kuromi"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            currentFile = chooser.getSelectedFile();
        }

        try {
            Files.write(currentFile.toPath(), codeEditor.getText().getBytes());
            updateStatus("Saved: " + currentFile.getName());
        } catch (IOException ex) {
            showError("Failed to save: " + ex.getMessage());
        }
    }

    private void runProject() {
        console.setText("");
        console.append("Running project...\n");
        updateStatus("Running...");
        // Compile and run
    }

    private void compileToWeb() {
        console.append("Compiling to HTML...\n");
        updateStatus("Compiling to web...");
        // Export as HTML
    }

    private void compileToEXE() {
        console.append("Building EXE...\n");
        updateStatus("Building EXE...");
        // Export as EXE
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        console.append("ERROR: " + message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KuromiEditor().setVisible(true));
    }
}