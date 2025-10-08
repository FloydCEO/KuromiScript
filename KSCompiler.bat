@echo off
chcp 65001 >nul
title KuromiScript Compiler (KSCompiler)
color 0d

setlocal

set "BASE=%~dp0"
set "PYTHON=%BASE%..\AppData\Local\Programs\Python\Python311-32\python.exe"
if not exist "%PYTHON%" (
    for /f "usebackq delims=" %%p in (`where python 2^>nul`) do set "PYTHON=%%p"
)

if "%~1"=="" (
    echo ============================================================
    echo           🎀 KuromiScript Compiler (KSCompiler) 🎀
    echo ============================================================
    echo.
    echo [KuromiCore]: Drag and drop your .KUROMI file here, or paste path:
    set /p "KS_FILE=Path: "
) else (
    set "KS_FILE=%~1"
)

if not exist "%KS_FILE%" (
    echo [KuromiCore]: File not found: "%KS_FILE%"
    pause
    exit /b 1
)

echo [KuromiCore]: Building "%KS_FILE%" ...
echo.

"%PYTHON%" "%~dp0exporter.py" "%KS_FILE%"

if errorlevel 1 (
    color 0c
    echo [KuromiCore]: Build failed. See kuromi_export_log.txt
    pause
    exit /b 1
)

color 0a
echo [KuromiCore]: Build finished. Check dist\dist\builds\<GameName>\
pause
endlocal
exit /b 0
