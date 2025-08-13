@echo off
setlocal ENABLEDELAYEDEXPANSION

REM Build classes
if not exist bin mkdir bin
echo Compiling sources...
javac -d bin -cp src src\*.java
if errorlevel 1 (
  echo Compilation failed.
  exit /b 1
)

REM Prepare dist directory
if exist dist rmdir /S /Q dist
mkdir dist

REM Create manifest
echo Main-Class: App> dist\MANIFEST.MF

REM Create jar with classes
pushd bin
jar cfm ..\dist\SnakeGame.jar ..\dist\MANIFEST.MF .
popd

REM Add resources (sounds) inside JAR at sounds/
if exist src\sounds (
  pushd src
  jar uf ..\dist\SnakeGame.jar sounds
  popd
)

REM Copy for GitHub Pages (docs/)
if not exist docs mkdir docs
copy /Y dist\SnakeGame.jar docs\SnakeGame.jar >nul
copy /Y docs\index.html docs\index.html >nul 2>nul

echo Built dist\SnakeGame.jar and copied to docs\SnakeGame.jar

endlocal

