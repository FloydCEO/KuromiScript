@echo off
title KuromiCore - Install Dependencies
color 0d

echo.
echo ============================================================
echo          KUROMICORE DEPENDENCY INSTALLER v2
echo ============================================================
echo.
echo This will install ALL required Python packages:
echo - tkinter (built into Python)
echo - pygame (for sound/music)
echo - pyinstaller (for EXE export)
echo - pillow (for image handling)
echo - requests (for any network ops)
echo - setuptools and wheel (for safe installs)
echo.
pause

:: Check for Python
echo.
echo [CHECK] Verifying Python installation...
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Python is not installed or not in PATH.
    echo Please install Python 3.10+ from https://www.python.org/downloads/
    echo and make sure to select "Add Python to PATH" during setup.
    pause
    exit /b 1
)
for /f "tokens=2 delims== " %%I in ('python -c "import sys; print(sys.version.split()[0])"') do set PYVER=%%I
echo [OK] Python detected (v%PYVER%)
echo.

:: Upgrade pip first
echo [1/7] Upgrading pip...
python -m pip install --upgrade pip setuptools wheel
if %errorlevel% neq 0 (
    echo [WARN] pip upgrade failed, continuing...
)
echo.

:: Install dependencies one by one
set PACKAGES=pygame pyinstaller pillow requests
setlocal enabledelayedexpansion
set COUNT=2

for %%P in (%PACKAGES%) do (
    echo [!COUNT!/7] Installing %%P...
    python -m pip install --upgrade %%P
    if !errorlevel! neq 0 (
        echo [ERROR] Failed to install %%P, retrying once...
        python -m pip install --upgrade %%P
        if !errorlevel! neq 0 (
            echo [FATAL] Could not install %%P. Please install manually.
            pause
            exit /b 1
        )
    )
    set /a COUNT+=1
    echo.
)

:: Confirm pygame audio support
echo [CHECK] Verifying pygame installation...
python - <<END
try:
    import pygame
    print("[OK] pygame version:", pygame.__version__)
except Exception as e:
    print("[ERROR] pygame not working:", e)
END

echo.
echo ============================================================
echo [SUCCESS] All KuromiCore dependencies installed!
echo ============================================================
echo.
echo You can now:
echo   1. Launch KuromiCore with "python kuromi_engine.py"
echo   2. Use the BUILD EXE button inside the engine
echo   3. Export standalone Kuromi games (.exe)
echo.
pause
exit /b 0
