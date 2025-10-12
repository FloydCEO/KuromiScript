@echo off
echo ╔════════════════════════════════════════════════════════════╗
echo ║              KuromiCore Engine - Build System             ║
echo ║           Easy Game ^& Web Development for Beginners       ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM Store the current directory
set ORIGINAL_DIR=%CD%

REM Check if we're in the examples directory and move up if needed
if exist "..\src" (
    echo 📁 Detected examples directory, moving to project root...
    cd ..
)

REM Verify we're in the correct directory
if not exist "src" (
    echo ❌ ERROR: Cannot find src directory!
    echo Please run this script from the KuromiCore project root.
    pause
    exit /b 1
)

echo 📂 Project directory: %CD%
echo.

REM Create out directory
if not exist "out" (
    echo 📁 Creating out directory...
    mkdir out
) else (
    echo 🗑️  Cleaning previous build...
    del /s /q "out\*.class" > nul 2>&1
)

REM Check if Java is installed
java -version > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: Java is not installed or not in PATH!
    echo Please install Java JDK 11 or higher.
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

echo ✓ Java found
echo.

REM Check if required core files exist
echo 🔍 Checking required files...
set MISSING_FILES=0

if not exist "src\Main.java" (
    echo ❌ MISSING: src\Main.java
    set MISSING_FILES=1
)
if not exist "src\interpreter\Interpreter.java" (
    echo ❌ MISSING: src\interpreter\Interpreter.java
    set MISSING_FILES=1
)
if not exist "src\interpreter\Environment.java" (
    echo ❌ MISSING: src\interpreter\Environment.java
    set MISSING_FILES=1
)
if not exist "src\runtime\Value.java" (
    echo ❌ MISSING: src\runtime\Value.java
    set MISSING_FILES=1
)
if not exist "src\runtime\Compiler.java" (
    echo ❌ MISSING: src\runtime\Compiler.java
    set MISSING_FILES=1
)
if not exist "src\lexer\Lexer.java" (
    echo ❌ MISSING: src\lexer\Lexer.java
    set MISSING_FILES=1
)
if not exist "src\parser\Parser.java" (
    echo ❌ MISSING: src\parser\Parser.java
    set MISSING_FILES=1
)

if %MISSING_FILES%==1 (
    echo.
    echo ❌ ERROR: Some required files are missing!
    echo Please ensure all files are properly saved.
    pause
    exit /b 1
)

echo ✓ All required core files found!
echo.

REM Check for optional GUI files
set HAS_GUI=1
if not exist "src\gui\KuromiCoreGUI.java" (
    echo ⚠️  Warning: GUI not found, will build without GUI
    set HAS_GUI=0
)

REM Check for optional JAR builder files
set HAS_JARBUILDER=1
if not exist "src\runtime\JarBuilder.java" (
    echo ⚠️  Warning: JarBuilder not found, JAR export disabled
    set HAS_JARBUILDER=0
)
if not exist "src\runtime\StandaloneRunner.java" (
    echo ⚠️  Warning: StandaloneRunner not found, JAR export disabled
    set HAS_JARBUILDER=0
)

echo.

REM Compile core files
echo ⚙️  Compiling KuromiCore Engine...
echo.

REM Compile in stages to handle dependencies
echo [1/3] Compiling lexer and parser...
javac -encoding UTF-8 -d out -sourcepath src src\lexer\*.java src\parser\*.java 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Lexer/Parser compilation failed!
    pause
    exit /b 1
)

echo [2/3] Compiling interpreter and runtime...
javac -encoding UTF-8 -d out -sourcepath src -cp out src\interpreter\*.java src\runtime\*.java 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Interpreter/Runtime compilation failed!
    pause
    exit /b 1
)

echo [3/3] Compiling main and GUI...
if %HAS_GUI%==1 (
    javac -encoding UTF-8 -d out -sourcepath src -cp out src\gui\*.java src\Main.java 2>&1
) else (
    javac -encoding UTF-8 -d out -sourcepath src -cp out src\Main.java 2>&1
)

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Main/GUI compilation failed!
    pause
    exit /b 1
)

echo.
echo ╔════════════════════════════════════════╗
echo ║      ✅ COMPILATION SUCCESSFUL!        ║
echo ╚════════════════════════════════════════╝
echo.

REM Create KuromiCore.jar
echo 📦 Creating KuromiCore.jar launcher...
echo.

REM Create manifest file
echo Manifest-Version: 1.0 > out\MANIFEST.MF
echo Main-Class: Main >> out\MANIFEST.MF
echo Created-By: KuromiCore Engine >> out\MANIFEST.MF
echo. >> out\MANIFEST.MF

REM Build JAR
cd out
if %HAS_GUI%==1 (
    jar cfm KuromiCore.jar MANIFEST.MF *.class lexer\*.class parser\*.class interpreter\*.class runtime\*.class gui\*.class 2>&1
) else (
    jar cfm KuromiCore.jar MANIFEST.MF *.class lexer\*.class parser\*.class interpreter\*.class runtime\*.class 2>&1
)
cd ..

if exist "out\KuromiCore.jar" (
    move out\KuromiCore.jar KuromiCore.jar > nul
    echo ✅ KuromiCore.jar created successfully!
) else (
    echo ⚠️  Warning: Could not create KuromiCore.jar
    echo You can still use: java -cp out Main
)

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║                    🎮 READY TO USE!                        ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
echo 📖 Usage Examples:
echo.

if %HAS_GUI%==1 (
    echo   1. Launch GUI Editor:
    if exist "KuromiCore.jar" (
        echo      java -jar KuromiCore.jar
    ) else (
        echo      java -cp out Main
    )
    echo.
)

echo   2. Run a script directly:
if exist "KuromiCore.jar" (
    echo      java -jar KuromiCore.jar examples\test.kuromi
) else (
    echo      java -cp out Main examples\test.kuromi
)
echo.

echo   3. Compile to HTML website:
if exist "KuromiCore.jar" (
    echo      java -jar KuromiCore.jar --web examples\test.kuromi
) else (
    echo      java -cp out Main --web examples\test.kuromi
)
echo.

if %HAS_JARBUILDER%==1 (
    echo   4. Build standalone JAR game:
    if exist "KuromiCore.jar" (
        echo      java -jar KuromiCore.jar --jar examples\test.kuromi
    ) else (
        echo      java -cp out Main --jar examples\test.kuromi
    )
    echo.
)

if %HAS_GUI%==1 (
    if exist "KuromiCore.jar" (
        echo 💡 TIP: Double-click KuromiCore.jar to launch the GUI editor!
    )
)
echo.
pause