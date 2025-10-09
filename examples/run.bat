javac -d bin src/*.java src/interpreter/*.java
jar cfe KuromiScript.jar kuromiscript.Main -C bin .