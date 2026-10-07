@echo off
setlocal
cd /d "%~dp0"
call Build-game.cmd
if errorlevel 1 exit /b 1
if not exist "build\tests" mkdir "build\tests"
javac -encoding UTF-8 -source 8 -target 8 -cp "build\classes" -d "build\tests" model\GameTest.java model\ThemeTest.java model\SwingTest.java
if errorlevel 1 exit /b 1
java -cp "build\classes;build\tests" boomboomkiss.model.GameTest
if errorlevel 1 exit /b 1
java -cp "build\classes;build\tests" boomboomkiss.model.ThemeTest
if errorlevel 1 exit /b 1
cd /d ".."
java -cp "boomboomkiss\build\classes;boomboomkiss\build\tests" boomboomkiss.model.SwingTest
if errorlevel 1 exit /b 1
echo Tests passed.
