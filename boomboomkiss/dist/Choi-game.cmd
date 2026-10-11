@echo off
cd /d "%~dp0"
java -jar "BoomBoomKiss.jar"
if errorlevel 1 (
    echo.
    echo Can Java 8 tro len de chay game. Co the chay boomboomkiss.model.Main trong IntelliJ.
    pause
)
