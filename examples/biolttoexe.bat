@echo off
REM KuromiCore - Automated EXE Builder
REM This script builds the JAR and converts it to EXE using Launch4j

setlocal enabledelayedexpansion

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║              KuromiCore - EXE Builder                      ║
echo ║           Build as Standalone Windows Executable           ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM Check if Launch4j is installed
set LAUNCH4J_PATH=C:\launch4j
if not exist "%LAUNCH4J_PATH%\launch4jc.exe" (
    echo ❌ ERROR: Launch4j not found at %LAUNCH4J_PATH%
    echo.
    echo Please install Launch4j first:
    echo 1. Download from: https://launch4j.sourceforge.net/
    echo 2. Extract to: C:\launch4j
    echo 3. Run this script again
    echo.
    pause
    exit /b 1
)

echo ✓ Launch4j found
echo.

REM Step 1: Build the JAR
echo [Step 1/3] Building KuromiCore.jar...
call build.bat
if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: JAR build failed!
    pause
    exit /b 1
)

echo.
echo ✓ JAR built successfully
echo.

REM Step 2: Create/Verify config file
echo [Step 2/3] Preparing Launch4j configuration...

if not exist "kuromi-exe-config.xml" (
    echo Creating kuromi-exe-config.xml...
    (
        echo ^<?xml version="1.0" encoding="UTF-8"?^>
        echo ^<launch4jConfig^>
        echo   ^<dontWrapJar^>false^</dontWrapJar^>
        echo   ^<headerType^>gui^</headerType^>
        echo   ^<jar^>KuromiCore.jar^</jar^>
        echo   ^<outfile^>KuromiCore.exe^</outfile^>
        echo   ^<errTitle^>KuromiCore Error^</errTitle^>
        echo   ^<cmdLine^>^</cmdLine^>
        echo   ^<chdir^>.^</chdir^>
        echo   ^<priority^>normal^</priority^>
        echo   ^<downloadUrl^>https://adoptium.net/^</downloadUrl^>
        echo   ^<supportUrl^>https://github.com/yourusername/kuromiscript^</supportUrl^>
        echo   ^<stayAlive^>false^</stayAlive^>
        echo   ^<restartOnCrash^>false^</restartOnCrash^>
        echo   ^<manifest^>^</manifest^>
        if exist "kuromi.ico" (
            echo   ^<icon^>kuromi.ico^</icon^>
        )
        echo   ^<jre^>
        echo     ^<minVersion^>11.0^</minVersion^>
        echo     ^<maxVersion^>^</maxVersion^>
        echo     ^<jdkPreference^>preferJre^</jdkPreference^>
        echo     ^<runtimeBits^>64/32^</runtimeBits^>
        echo     ^<initialHeapSize^>256^</initialHeapSize^>
        echo     ^<maxHeapSize^>1024^</maxHeapSize^>
        echo   ^</jre^>
        echo ^</launch4jConfig^>
    ) > kuromi-exe-config.xml
) else (
    echo kuromi-exe-config.xml already exists (using existing config)
)

echo ✓ Configuration ready
echo.

REM Step 3: Convert JAR to EXE
echo [Step 3/3] Converting JAR to EXE...
"%LAUNCH4J_PATH%\launch4jc.exe" kuromi-exe-config.xml

if %ERRORLEVEL% NEQ 0 (
    echo ❌ ERROR: Conversion to EXE failed!
    echo.
    echo Troubleshooting:
    echo - Make sure KuromiCore.jar exists
    echo - Check Launch4j configuration is valid
    echo - Verify Java is installed (JDK 11+)
    echo.
    pause
    exit /b 1
)

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║            ✅ SUCCESS - EXE CREATED!                       ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
echo 📁 Location: KuromiCore.exe
echo 📊 Size: %~z[KuromiCore.exe] bytes
echo.

REM Optional: List what was created
dir /B KuromiCore.exe

echo.
echo 🚀 Next Steps:
echo   1. Test the EXE: Double-click KuromiCore.exe
echo   2. Verify it works like the JAR version
echo   3. Distribute to users - no Java needed!
echo.
echo 💾 To create an installer (optional):
echo   - Install NSIS: https://nsis.sourceforge.io/
echo   - Run: makensis.exe kuromi-installer.nsi
echo.
pause