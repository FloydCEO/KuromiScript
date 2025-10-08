@echo off
rem ------------------------------------------------------------
rem KuromiCore Engine Builder - stable safe CMD version
rem ------------------------------------------------------------

title KuromiCore Builder
color 0d
chcp 65001 >nul

setlocal
set "OUTPUT_DIR=C:\Users\LykoD\OneDrive\Documents\GitHub\KuromiScript\dist\dist\KuromiCore"

echo.
echo ============================================================
echo           KuromiCore Engine Builder - Cute Mode
echo ============================================================
echo.

echo [KuromiCore]: Initializing....
timeout /t 1 >nul
echo [KuromiCore]: Frolicking through the woods....
timeout /t 1 >nul
echo [KuromiCore]: Kuromi spawns!
timeout /t 1 >nul
echo [KuromiCore]: Playing awesome pranks!
timeout /t 1 >nul
echo [KuromiCore]: Okay, I'm gonna work on the engine now :3
timeout /t 1 >nul
echo [KuromiCore]: Maybe... I won't >:3
timeout /t 1 >nul
echo [KuromiCore]: Okay.....
timeout /t 1 >nul
echo [KuromiCore]: KS PKG
timeout /t 1 >nul
echo [KuromiCore]: KC PKG
timeout /t 1 >nul
echo.
echo [KuromiCore]: Starting build process...
echo ------------------------------------------------------------

rem ---- run PyInstaller silently and capture logs ----
python -m PyInstaller --onedir --clean --windowed --noconfirm --name "KuromiCore" --icon "assets\icon.ico" --add-data "assets\icon.ico;assets" --add-data "assets\splash_bg.png;assets" --add-data "assets\startup.wav;assets" --add-data "kuromi_interpreter.py;." --distpath "%OUTPUT_DIR%" kuromi_engine.py >nul 2>kuromi_build_log.txt

if errorlevel 1 (
    color 0c
    echo ------------------------------------------------------------
    echo [KuromiCore]: Build failed... Kuromi tripped over her tail!
    echo [KuromiCore]: See kuromi_build_log.txt for error details.
    pause
    exit /b 1
)

color 0a
echo ------------------------------------------------------------
echo [KuromiCore]: Build Finished!
echo [KuromiCore]: Engine packaged successfully.
echo [KuromiCore]: Output folder:
echo [KuromiCore]:     "%OUTPUT_DIR%\KuromiCore"
echo ------------------------------------------------------------
echo [KuromiCore]: Time for snacks!
echo.
start "" "%OUTPUT_DIR%"
pause
exit /b 0
