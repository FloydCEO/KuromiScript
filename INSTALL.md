# 📦 KuromiCore Installation Guide

Complete installation instructions for Windows, Mac, and Linux.

## Prerequisites

### Java Development Kit (JDK)

KuromiCore requires **Java JDK 11 or higher**.

#### Check if you have Java:
```bash
java -version
javac -version
```

You should see version 11 or higher for both.

#### Install Java:

**Windows:**
1. Download from https://adoptium.net/
2. Run the installer
3. Check "Add to PATH" during installation
4. Restart Command Prompt
5. Verify: `java -version`

**Mac:**
```bash
# Using Homebrew
brew install openjdk@17

# Or download from https://adoptium.net/
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-17-jdk

# Verify
java -version
javac -version
```

**Linux (Fedora/RHEL):**
```bash
sudo dnf install java-17-openjdk-devel
```

## Installation Steps

### Option 1: Download Pre-built Release (Coming Soon)

1. Download `KuromiCore-v1.0.0.zip` from releases
2. Extract to a folder
3. Double-click `KuromiCore.jar` (Windows/Mac)
4. Or run: `java -jar KuromiCore.jar`

### Option 2: Build from Source (Current)

#### Windows:

1. **Download/Clone the Repository**
   ```bash
   git clone https://github.com/yourname/kuromiscript.git
   cd kuromiscript
   ```

2. **Run the Build Script**
   ```bash
   build.bat
   ```

3. **Wait for Success Message**
   ```
   ✅ COMPILATION SUCCESSFUL!
   ```

4. **Launch KuromiCore**
   ```bash
   java -jar KuromiCore.jar
   ```
   Or double-click `KuromiCore.jar`

#### Mac/Linux:

1. **Download/Clone the Repository**
   ```bash
   git clone https://github.com/yourname/kuromiscript.git
   cd kuromiscript
   ```

2. **Make Scripts Executable**
   ```bash
   chmod +x build.sh
   chmod +x run.sh
   ```

3. **Run the Build Script**
   ```bash
   ./build.sh
   ```

4. **Wait for Success Message**
   ```
   ✅ COMPILATION SUCCESSFUL!
   ```

5. **Launch KuromiCore**
   ```bash
   java -jar KuromiCore.jar
   # Or
   ./run.sh
   ```

## Directory Structure

After building, your directory should look like this:

```
KuromiCore/
├── KuromiCore.jar          ← Main executable
├── build.bat               ← Windows build script
├── build.sh                ← Linux/Mac build script
├── run.bat                 ← Windows run script
├── run.sh                  ← Linux/Mac run script
├── README.md               ← Full documentation
├── QUICKSTART.md           ← Quick start guide
├── INSTALL.md              ← This file
├── src/                    ← Source code
│   ├── Main.java
│   ├── lexer/
│   ├── parser/
│   ├── interpreter/
│   ├── runtime/
│   └── gui/
├── out/                    ← Compiled classes
│   ├── *.class
│   └── (packages)/
└── examples/               ← Example scripts
    ├── hello.kuromi
    ├── moving-ball.kuromi
    ├── rainbow-pattern.kuromi
    └── simple-game.kuromi
```

## First Run Test

### GUI Test:
```bash
java -jar KuromiCore.jar
```

You should see the KuromiCore editor window open.

### Script Test:
```bash
java -jar KuromiCore.jar examples/hello.kuromi
```

A game window should appear with "Hello, World!" message.

### Web Export Test:
```bash
java -jar KuromiCore.jar --web examples/hello.kuromi
```

Should create `hello.html` file. Open it in your browser!

### JAR Build Test:
```bash
java -jar KuromiCore.jar --jar examples/hello.kuromi
```

Should create `hello.jar`. Run it with: `java -jar hello.jar`

## Troubleshooting

### Problem: "Java not found" or "'java' is not recognized"

**Solution:**
- Java is not installed or not in PATH
- Install Java JDK from https://adoptium.net/
- Windows: Restart Command Prompt after installation
- Mac/Linux: Restart terminal or run `source ~/.bashrc`

**Verify Java is in PATH:**
```bash
# Should show Java installation path
where java         # Windows
which java         # Mac/Linux
```

### Problem: "javac: command not found" during build

**Solution:**
- You have JRE but need JDK (Development Kit)
- Install JDK (not just JRE) from https://adoptium.net/
- Make sure to install the **JDK**, not just the runtime

### Problem: Build script fails with "Cannot find src directory"

**Solution:**
```bash
# Make sure you're in the right directory
cd /path/to/kuromiscript

# Verify src folder exists
ls src          # Mac/Linux
dir src         # Windows
```

### Problem: "Permission denied" on Mac/Linux

**Solution:**
```bash
chmod +x build.sh
chmod +x run.sh
./build.sh
```

### Problem: "Class not found" errors

**Solution:**
- Clean and rebuild:
```bash
# Windows
rmdir /s /q out
build.bat

# Mac/Linux
rm -rf out
./build.sh
```

### Problem: GUI doesn't launch

**Solution:**
- Make sure you have a display (X11 on Linux)
- Try running a script instead: `java -jar KuromiCore.jar examples/hello.kuromi`
- Check console for error messages

### Problem: Game window appears but is black

**Solution:**
- This might be normal if the script draws a black background
- Check your script for drawing commands
- Try the examples to verify installation

### Problem: Images don't load in games

**Solution:**
```bash
# Create assets folder
mkdir assets

# Place images there
# Reference in scripts: load player: "player.png"
```

### Problem: HTML export doesn't work

**Solution:**
- Make sure output directory is writable
- Check console for error messages
- Try with a simple script first

## Uninstallation

To remove KuromiCore:

1. Delete the KuromiCore folder
2. That's it! No registry entries or system files modified

## Updating

To update to a new version:

1. Backup your scripts from `examples/` folder
2. Download new version
3. Extract and replace old folder
4. Run `build.bat` or `build.sh`
5. Copy your scripts back

Or with Git:
```bash
git pull
./build.sh  # or build.bat on Windows
```

## IDE Setup (Optional)

Want to develop KuromiCore itself?

### IntelliJ IDEA:
1. Open the project folder
2. Mark `src` as Sources Root
3. Build → Build Project
4. Run `Main.java`

### Eclipse:
1. File → Open Projects from File System
2. Import the KuromiCore folder
3. Right-click project → Build Project
4. Run `Main.java`

### VS Code:
1. Install Java Extension Pack
2. Open folder
3. The extension will auto-configure
4. Press F5 to run

## System Requirements

**Minimum:**
- Java JDK 11 or higher
- 512 MB RAM
- 50 MB disk space
- Any OS that runs Java (Windows, Mac, Linux)

**Recommended:**
- Java JDK 17 or higher
- 1 GB RAM
- 100 MB disk space
- Display for GUI (1024x768 or higher)

## Platform-Specific Notes

### Windows:
- Use PowerShell or Command Prompt
- Scripts have `.bat` extension
- Double-click `.jar` files to run
- Antivirus might scan JAR files (this is normal)

### macOS:
- May need to allow Java in Security & Privacy settings
- Right-click `.jar` → Open (first time only)
- Scripts have `.sh` extension
- Terminal required for command-line usage

### Linux:
- Some distributions need `java-openjdk-devel` package
- May need to install X11 libraries for GUI
- Scripts have `.sh` extension
- Make scripts executable: `chmod +x *.sh`

## Getting Help

If you're still having issues:

1. Check the error message carefully
2. Read the [QUICKSTART.md](QUICKSTART.md) guide
3. Try the examples to verify installation
4. Check that Java is properly installed
5. Make sure you're in the correct directory

## Next Steps

Once installed successfully:

1. Read [QUICKSTART.md](QUICKSTART.md) for your first program
2. Try the examples in `examples/` folder
3. Launch the GUI and experiment
4. Read full [README.md](README.md) for all features

## Success Checklist ✓

After installation, you should be able to:

- [ ] Run `java -version` (shows 11 or higher)
- [ ] Run `javac -version` (shows 11 or higher)
- [ ] `KuromiCore.jar` exists in project root
- [ ] `java -jar KuromiCore.jar` launches GUI
- [ ] Example scripts run: `java -jar KuromiCore.jar examples/hello.kuromi`
- [ ] Can compile to HTML: `java -jar KuromiCore.jar --web examples/hello.kuromi`
- [ ] Can build JAR: `java -jar KuromiCore.jar --jar examples/hello.kuromi`

If all checkmarks are complete, you're ready to start coding! 🎉

---

**Welcome to KuromiCore! Happy coding! 🎮✨**