@echo off
echo Compiling KuromiScript...

:: Ensure working directory is project root
cd /d %~dp0

:: Create bin directory if it doesn't exist
if not exist bin mkdir bin

:: First, disable problematic files by renaming them
echo Preparing files...
if exist "src\runtime\BuiltInFunctions.java" (
    ren "src\runtime\BuiltInFunctions.java" "BuiltInFunctions.java.bak" 2>nul
)
if exist "src\runtime\HTMLRenderer.java" (
    ren "src\runtime\HTMLRenderer.java" "HTMLRenderer.java.bak" 2>nul
)

:: Delete old interpreter if it exists
if exist "src\interpreter\Interpreter.java" (
    echo Removing old interpreter...
    del "src\interpreter\Interpreter.java" 2>nul
)

:: Compile all Java files in correct order
echo Compiling Java files...
javac -d bin src\lexer\*.java src\parser\*.java src\runtime\*.java src\Main.java 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Compilation failed!
    echo.
    echo Please make sure you have:
    echo 1. Fixed src\parser\Parser.java line 144 to: java.util.List^<ASTNode.Expr^> args = new ArrayList^<^>^(^);
    echo 2. Created src\runtime\Interpreter.java, Value.java, Environment.java, and Compiler.java
    echo.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Compilation successful!
echo.
echo To run a script:
echo   java -cp bin Main examples\test.kuromi
echo.
echo To compile to HTML:
echo   java -cp bin Main --web examples\test.kuromi
echo.

pause