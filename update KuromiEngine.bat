@echo off
title Kuromi Engine Builder 🎀
color 0d
echo.
echo ====================================
echo       🎀 Kuromi Engine Builder 🎀
echo ====================================
echo.

set /p buildnum=Enter build version (e.g. 1.0.0): 
if "%buildnum%"=="" (
    echo ❌ No build number entered. Build cancelled.
    pause
    exit /b
)

set /p label=Optional label (e.g. dev, stable): 

if not "%label%"=="" (
    set exename=KuromiEngine_v%buildnum%_%label%
) else (
    set exename=KuromiEngine_v%buildnum%
)

echo.
echo 🚧 Building %exename%.exe ...
echo.

python -m PyInstaller --onefile --noconsole --name "%exename%" kuromi_engine.py

echo.
if exist "dist\%exename%.exe" (
    echo ✅ Build complete! Find your EXE here:
    echo    dist\%exename%.exe
) else (
    echo ❌ Build failed. Check errors above.
)
echo.
pause
