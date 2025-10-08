@echo off
setlocal enabledelayedexpansion
title Kuromi Engine Builder
color 0d
mode con: cols=60 lines=25

:banner
cls
echo.
echo ============================================================
echo                  KUROMI ENGINE BUILDER
echo ============================================================
echo.

REM Check if PyInstaller is installed
python -c "import PyInstaller" 2>nul
if errorlevel 1 (
    echo [X] ERROR: PyInstaller not found!
    echo.
    echo     Install with: pip install pyinstaller
    echo.
    goto prompt_retry
)

REM Check if required files exist
if not exist "engine.py" (
    echo [X] ERROR: engine.py not found!
    goto prompt_retry
)

if not exist "kuromi_interpreter.py" (
    echo [X] ERROR: kuromi_interpreter.py not found!
    goto prompt_retry
)

REM Set up icon flag
if not exist "assets\icon.ico" (
    echo [!] WARNING: assets\icon.ico not found
    echo     Building without custom icon...
    echo.
    set ICON_FLAG=
) else (
    set ICON_FLAG=--icon=assets/icon.ico
)

echo [1/4] Preparing build environment...
call :progress_bar 25
echo      [OK] Environment ready
echo.

echo [2/4] Analyzing dependencies...
call :progress_bar 50
echo      [OK] Dependencies found
echo.

echo [3/4] Compiling KuromiCore...
echo      This may take 30-60 seconds...
echo.

REM Create dist/builds directory structure
if not exist "dist\builds" mkdir "dist\builds"

REM Start the progress bar animation in background
start /b cmd /c "for /l %%i in (1,1,20) do (echo [PROGRESS]%%i & timeout /t 2 /nobreak >nul)" > build_progress.tmp 2>&1

REM Run PyInstaller with all necessary flags
python -m PyInstaller ^
    --onefile ^
    --clean ^
    --noconfirm ^
    --noconsole ^
    --name "KuromiCore" ^
    --log-level ERROR ^
    --distpath "dist\builds" ^
    %ICON_FLAG% ^
    --hidden-import pygame ^
    --hidden-import tkinter ^
    --collect-all pygame ^
    --collect-all tkinter ^
    --add-data "assets/icon.ico;assets" ^
    --add-data "assets/splash_bg.png;assets" ^
    --add-data "assets/startup.wav;assets" ^
    --add-data "kuromi_interpreter.py;." ^
    engine.py 2>build_errors.tmp

REM Clean up progress temp file
del build_progress.tmp 2>nul

if errorlevel 1 (
    echo      [X] Build FAILED
    echo.
    echo ============================================================
    echo                      ERROR DETAILS
    echo ============================================================
    type build_errors.tmp
    del build_errors.tmp 2>nul
    echo.
    goto prompt_retry
)

del build_errors.tmp 2>nul
call :progress_bar 75
echo      [OK] Compilation complete
echo.

echo [4/4] Finalizing executable...
call :progress_bar 100
timeout /t 1 /nobreak >nul

if exist "dist\builds\KuromiCore.exe" (
    for %%A in ("dist\builds\KuromiCore.exe") do set SIZE=%%~zA
    set /a SIZE_MB=!SIZE! / 1048576
    
    echo      [OK] Build successful!
    echo.
    echo ============================================================
    echo                    BUILD COMPLETE!
    echo ============================================================
    echo.
    echo  Executable: dist\builds\KuromiCore.exe
    echo  File Size:  !SIZE_MB! MB
    echo.
    echo  You can now distribute this standalone executable!
    echo.
    echo ============================================================
    echo.
    
    REM Copy assets folder for debugging
    if exist "assets" (
        if not exist "dist\builds\assets" (
            xcopy /E /I /Q "assets" "dist\builds\assets" >nul 2>&1
            echo  [+] Assets copied to dist\builds\assets for debugging
            echo.
        )
    )
    
    goto prompt_retry
) else (
    echo      [X] ERROR: Executable not found at dist\builds\
    echo.
    
    REM Check if it's in the default dist folder
    if exist "dist\KuromiCore.exe" (
        echo [!] Found in dist\ - moving to dist\builds\...
        move "dist\KuromiCore.exe" "dist\builds\KuromiCore.exe" >nul
        echo [OK] Moved to correct location!
        echo.
    )
    
    goto prompt_retry
)

:progress_bar
REM Draw a progress bar - parameter is percentage (0-100)
set /a percent=%1
set /a bars=%percent% / 5
set /a spaces=20 - %bars%
set "bar_str="
set "space_str="

for /l %%i in (1,1,%bars%) do set "bar_str=!bar_str!█"
for /l %%i in (1,1,%spaces%) do set "space_str=!space_str! "

echo      [!bar_str!!space_str!] %percent%%%
timeout /t 1 /nobreak >nul
goto :eof

:prompt_retry
echo.
set /p choice="Type 'retry' to build again, or press Enter to exit: "
if /i "%choice%"=="retry" goto banner
if /i "%choice%"=="r" goto banner
exit /b 0