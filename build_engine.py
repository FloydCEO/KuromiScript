@echo off
title Kuromi Engine Builder
color 0d

echo.
echo ============================================================
echo              KUROMI ENGINE BUILDER
echo ============================================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Python is not installed or not in PATH!
    echo Please install Python 3.7+ and try again.
    pause
    exit /b 1
)

REM Check if PyInstaller is installed
python -c "import PyInstaller" >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] PyInstaller not found. Installing...
    pip install pyinstaller
    if %errorlevel% neq 0 (
        echo [ERROR] Failed to install PyInstaller
        pause
        exit /b 1
    )
)

REM Check for required files
if not exist "kuromi_engine.py" (
    echo [ERROR] kuromi_engine.py not found!
    pause
    exit /b 1
)

if not exist "kuromi_interpreter.py" (
    echo [ERROR] kuromi_interpreter.py not found!
    pause
    exit /b 1
)

if not exist "assets" (
    echo [ERROR] assets folder not found!
    pause
    exit /b 1
)

REM Get version info
set /p VERSION="Enter build version (e.g. 1.0.0): "
set /p LABEL="Optional label (e.g. beta, dev, stable): "

if "%VERSION%"=="" set VERSION=1.0.0
if "%LABEL%"=="" (
    set OUTPUT_NAME=KuromiCore_v%VERSION%
) else (
    set OUTPUT_NAME=KuromiCore_v%VERSION%_%LABEL%
)

echo.
echo [BUILD] Building %OUTPUT_NAME%.exe...
echo [BUILD] This may take 1-2 minutes...
echo.

REM Clean previous builds
if exist "build" rmdir /s /q build
if exist "dist\%OUTPUT_NAME%" rmdir /s /q "dist\%OUTPUT_NAME%"
if exist "dist\%OUTPUT_NAME%.exe" del /f "dist\%OUTPUT_NAME%.exe"

REM Build with PyInstaller
python -m PyInstaller ^
    --onefile ^
    --noconsole ^
    --name "%OUTPUT_NAME%" ^
    --icon="assets/icon.ico" ^
    --add-data "assets;assets" ^
    --add-data "kuromi_interpreter.py;." ^
    --hidden-import pygame ^
    --collect-all pygame ^
    kuromi_engine.py

REM Check if build succeeded
if exist "dist\%OUTPUT_NAME%.exe" (
    echo.
    echo ============================================================
    echo [SUCCESS] Build complete!
    echo ============================================================
    echo.
    echo Location: dist\%OUTPUT_NAME%.exe
    
    REM Get file size
    for %%A in ("dist\%OUTPUT_NAME%.exe") do (
        set SIZE=%%~zA
        set /a SIZE_MB=%%~zA/1024/1024
    )
    echo Size: !SIZE_MB! MB
    echo.
    echo Your engine is ready to distribute!
    echo ============================================================
) else (
    echo.
    echo ============================================================
    echo [ERROR] Build failed - EXE not created
    echo ============================================================
    echo.
    echo Check if:
    echo - kuromi_engine.py has no syntax errors
    echo - All required files are present
    echo - PyInstaller is working correctly
    echo.
    echo Try running: python kuromi_engine.py
    echo to test for errors first.
    echo ============================================================
)

echo.
pause