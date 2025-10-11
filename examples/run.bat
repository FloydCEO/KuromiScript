@echo off
setlocal enabledelayedexpansion

echo Compiling KuromiScript...
echo Current directory: %CD%
echo.

REM Create out directory if it doesn't exist
if not exist "out" mkdir out

REM Check if we're in the examples directory (wrong location)
if exist "..\src" (
    echo ERROR: You are in the examples directory
    echo Please run this script from the project root directory.
    echo Switching to parent directory...
    cd ..
)

echo Searching for Java files...
echo.

REM Find all Java files recursively and compile them
echo Compiling files...

REM Method 1: Try compiling with package structure
if exist "src\main\Main.java" (
    javac -d out src\lexer\*.java src\parser\*.java src\runtime\*.java src\main\*.java 2>&1
) else (
    REM Method 2: If Main.java is directly in src
    javac -d out src\lexer\*.java src\parser\*.java src\runtime\*.java src\Main.java 2>&1
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Compilation failed
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Compilation successful!
echo ========================================
echo.

REM Determine the correct class path
if exist "src\main\Main.java" (
    echo To run a script:
    echo   java -cp out main.Main examples\test.kuromi
    echo.
    echo To compile to HTML:
    echo   java -cp out main.Main --web examples\test.kuromi
) else (
    echo To run a script:
    echo   java -cp out Main examples\test.kuromi
    echo.
    echo To compile to HTML:
    echo   java -cp out Main --web examples\test.kuromi
)

echo.
pause
endlocal