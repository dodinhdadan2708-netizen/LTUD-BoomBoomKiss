@echo off
setlocal
cd /d "%~dp0"
if not exist "build\classes" mkdir "build\classes"
if not exist "dist" mkdir "dist"
javac -encoding UTF-8 -source 8 -target 8 -d "build\classes" Board.java Cell.java Game.java GameConfig.java GameUi.java IconCatalog.java Main.java Position.java Theme.java
if errorlevel 1 goto failed
jar cfe "dist\BoomBoomKiss.jar" boomboomkiss.Main -C "build\classes" boomboomkiss
if errorlevel 1 goto failed
echo Build thanh cong: dist\BoomBoomKiss.jar
exit /b 0
:failed
echo Build that bai. Can JDK 8 tro len va javac/jar trong PATH.
exit /b 1
