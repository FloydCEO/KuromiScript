@echo off
title Kuromi Engine Builder
color 0d
echo.
echo ====================================
echo       Kuromi Engine Builder
echo ====================================
echo.

REM Check if PyInstaller is installed
python -c "import PyInstaller" 2>nul
if errorlevel 1 (
    echo [ERROR] PyInstaller not found!
    echo Install it with: pip install pyinstaller
    pause
    exit /b 1
)

REM Check if required files exist
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

if not exist "assets\icon.ico" (
    echo [WARNING] assets\icon.ico not found - building without icon
    set ICON_FLAG=
) else (
    set ICON_FLAG=--icon=assets/icon.ico
)

echo [BUILD] Starting PyInstaller build...
echo.

python -m PyInstaller ^
    --onefile ^
    --clean ^
    --noconsole ^
    --name "KuromiCore" ^
    %ICON_FLAG% ^
    --add-data "assets/icon.ico;assets" ^
    --add-data "assets/splash_bg.png;assets" ^
    --add-data "assets/startup.wav;assets" ^
    kuromi_engine.py

if errorlevel 1 (
    echo.
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo.
echo ====================================
echo [SUCCESS] Build completed!
echo ====================================
echo.
echo Your executable is in the 'dist' folder:
echo   dist\KuromiCore.exe
echo.
pause