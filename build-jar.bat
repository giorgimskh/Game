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
mkdir dist\sounds

REM Copy resources
if exist src\sounds xcopy /E /I /Y src\sounds dist\sounds >nul 2>nul

REM Create manifest
echo Main-Class: App> dist\MANIFEST.MF

REM Create jar
pushd bin
jar cfm ..\dist\SnakeGame.jar ..\dist\MANIFEST.MF .
popd

echo Built dist\SnakeGame.jar

endlocal

