@echo off
REM Build + run BlackJack Pro (Swing desktop) via Gradle wrapper.
REM
REM First time only (Gradle on PATH required once):
REM   gradle wrapper --gradle-version 8.10
REM
REM After that, double-click this file or run: gradlew.bat :swing:run

cd /d %~dp0
if exist gradlew.bat (
    call gradlew.bat :swing:run
) else (
    where gradle >nul 2>&1
    if errorlevel 1 (
        echo Gradle not found. Install with: winget install Gradle.Gradle
        echo Then in this folder run: gradle wrapper --gradle-version 8.10
        pause
        exit /b 1
    )
    call gradle :swing:run
)
pause
