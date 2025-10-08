@echo off
title KuromiCore - Install Dependencies
color 0d

echo.
echo ============================================================
echo         KUROMICORE DEPENDENCY INSTALLER
echo ============================================================
echo.
echo This will install the required Python packages:
echo - pygame (for sound/music)
echo - pyinstaller (for building EXEs)
echo.
pause

echo.
echo [1/2] Installing pygame...
python -m pip install pygame
if %errorlevel% neq 0 (
    echo [ERROR] Failed to install pygame
    pause
    exit /b 1
)

echo.
echo [2/2] Installing pyinstaller...
python -m pip install pyinstaller
if %errorlevel% neq 0 (
    echo [ERROR] Failed to install pyinstaller
    pause
    exit /b 1
)

echo.
echo ============================================================
echo [SUCCESS] All dependencies installed!
echo ============================================================
echo.
echo You can now:
echo 1. Run build_engine.bat to build KuromiCore engine
echo 2. Use the BUILD EXE button inside the engine to export games
echo.
pause