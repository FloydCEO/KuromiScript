@echo off
setlocal enabledelayedexpansion

echo ╔════════════════════════════════════════╗
echo ║        KuromiCore Quick Launcher      ║
echo ╚════════════════════════════════════════╝
echo.

REM Check if KuromiCore.jar exists
if exist "KuromiCore.jar" (
    echo ✓ Found KuromiCore.jar
    echo.

    REM If no arguments, launch GUI
    if "%~1"=="" (
        echo 🎮 Launching GUI Editor...
        echo.
        java -jar KuromiCore.jar
        goto :end
    )

    REM Pass all arguments to KuromiCore
    echo ▶️  Running: java -jar KuromiCore.jar %*
    echo.
    java -jar KuromiCore.jar %*
    goto :end
)

REM KuromiCore.jar not found, check if compiled
if not exist "out" (
    echo ❌ KuromiCore not built yet!
    echo.
    echo Please run build.bat first:
    echo   build.bat
    echo.
    pause
    exit /b 1
)

echo ℹ️  Using compiled classes...
echo.

REM Check if we're in the examples directory
if exist "..\src" (
    echo 📁 Detected examples directory, moving to project root...
    cd ..
)

REM If no arguments, try to launch GUI
if "%~1"=="" (
    echo 🎮 Launching GUI Editor...
    echo.
    java -cp out Main
    goto :end
)

REM Run with arguments
echo ▶️  Running: java -cp out Main %*
echo.
java -cp out Main %*

:end
echo.
echo ═══════════════════════════════════════
if %ERRORLEVEL% EQU 0 (
    echo ✅ Completed successfully!
) else (
    echo ❌ Exited with error code %ERRORLEVEL%
)
echo ═══════════════════════════════════════
echo.
pause
endlocal