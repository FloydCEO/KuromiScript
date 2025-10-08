@echo off
echo Building Kuromi Engine...
python -m PyInstaller --onefile --noconsole --name "KuromiEngine" kuromi_engine.py
echo Done! Find your exe in the dist folder.
pause
