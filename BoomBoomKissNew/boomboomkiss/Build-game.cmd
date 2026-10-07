@echo off
setlocal
cd /d "%~dp0"
if not exist "build\classes" mkdir "build\classes"
if not exist "dist" mkdir "dist"
javac -encoding UTF-8 -source 8 -target 8 -d "build\classes" config\GameConfig.java repository\*.java model\AttackResult.java model\Board.java model\Cell.java model\CellType.java model\Game.java model\GameUi.java model\IconCatalog.java model\Main.java model\Position.java model\Theme.java
if errorlevel 1 goto failed
jar cfe "dist\BoomBoomKiss.jar" boomboomkiss.model.Main -C "build\classes" boomboomkiss
if errorlevel 1 goto failed
echo Build thanh cong: dist\BoomBoomKiss.jar
exit /b 0
:failed
echo Build that bai. Can JDK 8 tro len va javac/jar trong PATH.
exit /b 1
