@echo off
REM Build + run BlackJackPro (Swing - no JavaFX needed)
cd /d %~dp0
if not exist bin mkdir bin
javac -d bin src\BlackJackPro.java
if errorlevel 1 (
    echo Compile failed.
    pause
    exit /b 1
)
java -cp bin BlackJackPro
pause
