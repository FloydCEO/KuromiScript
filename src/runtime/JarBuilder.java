// src/runtime/JarBuilder.java
package runtime;

import parser.ASTNode;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

public class JarBuilder {

    public void buildJar(List<ASTNode.Stmt> statements, String sourceCode, String outputPath) throws IOException {
        System.out.println("📦 Building standalone JAR...");

        // Create temporary directory for building
        Path tempDir = Files.createTempDirectory("kuromi-build");

        try {
            // Step 1: Copy all compiled classes
            System.out.println("  → Copying runtime classes...");
            copyRuntimeClasses(tempDir);

            // Step 2: Create embedded script
            System.out.println("  → Embedding KuromiScript...");
            createEmbeddedScript(tempDir, sourceCode);

            // Step 3: Create manifest
            System.out.println("  → Creating manifest...");
            createManifest(tempDir);

            // Step 4: Build JAR file
            System.out.println("  → Building JAR archive...");
            createJarFile(tempDir, outputPath);

            System.out.println("✅ JAR built successfully: " + outputPath);

        } finally {
            // Cleanup temp directory
            deleteDirectory(tempDir.toFile());
        }
    }

    private void copyRuntimeClasses(Path tempDir) throws IOException {
        // Get the directory where our compiled classes are
        String classPath = System.getProperty("java.class.path");
        Path outDir = Paths.get("out");

        if (!Files.exists(outDir)) {
            throw new IOException("Classes not found. Please compile the project first.");
        }

        // Copy all .class files maintaining package structure
        copyDirectory(outDir, tempDir);
    }

    private void createEmbeddedScript(Path tempDir, String sourceCode) throws IOException {
        // Create a resources directory
        Path resourceDir = tempDir.resolve("resources");
        Files.createDirectories(resourceDir);

        // Write the script to a resource file
        Path scriptFile = resourceDir.resolve("embedded.kuromi");
        Files.write(scriptFile, sourceCode.getBytes());
    }

    private void createManifest(Path tempDir) throws IOException {
        Manifest manifest = new Manifest();
        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attrs.put(Attributes.Name.MAIN_CLASS, "runtime.StandaloneRunner");
        attrs.put(new Attributes.Name("Created-By"), "KuromiCore Engine");

        Path manifestDir = tempDir.resolve("META-INF");
        Files.createDirectories(manifestDir);

        try (FileOutputStream fos = new FileOutputStream(manifestDir.resolve("MANIFEST.MF").toFile())) {
            manifest.write(fos);
        }
    }

    private void createJarFile(Path tempDir, String outputPath) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(
                new FileOutputStream(outputPath))) {

            addDirectoryToJar(jos, tempDir.toFile(), "");
        }
    }

    private void addDirectoryToJar(JarOutputStream jos, File directory, String prefix) throws IOException {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            String entryName = prefix + file.getName();

            if (file.isDirectory()) {
                addDirectoryToJar(jos, file, entryName + "/");
            } else {
                JarEntry entry = new JarEntry(entryName);
                jos.putNextEntry(entry);

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        jos.write(buffer, 0, bytesRead);
                    }
                }

                jos.closeEntry();
            }
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetPath = target.resolve(source.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}