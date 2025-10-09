:: KuromiScript/build.bat
@echo off
echo Compiling KuromiScript...
javac -d bin src/kuromiscript/*.java src/interpreter/*.java src/lexer/*.java src/parser/*.java src/runtime/*.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    exit /b %ERRORLEVEL%
)
echo Creating JAR...
jar cfe KuromiScript.jar kuromiscript.Main -C bin .
if %ERRORLEVEL% NEQ 0 (
    echo JAR creation failed!
    exit /b %ERRORLEVEL%
)
echo Build successful! To run: java -jar KuromiScript.jar examples/hello_world.kuromi
echo To compile to web: java -jar KuromiScript.jar --web examples/hello_world.kuromi