@echo off
title Kuromi Engine Builder 🎀
color 0d
echo.
echo ====================================
echo       🎀 Kuromi Engine Builder 🎀
echo ====================================
python -m PyInstaller --onefile --clean --noconsole --name "KuromiCore" --icon=assets/icon.ico --add-data "assets/icon.ico;assets" --add-data "assets/splash_bg.png;assets" --add-data "assets/startup.wav;assets" --add-data "kuromi_interpreter.py;." kuromi_engine.py
pause