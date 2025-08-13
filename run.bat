@echo off
setlocal ENABLEDELAYEDEXPANSION

REM Create bin directory
if not exist bin mkdir bin

REM Compile sources
echo Compiling sources...
javac -d bin -cp src src\*.java
if errorlevel 1 (
  echo Compilation failed.
  exit /b 1
)

REM Copy sound assets
if exist src\sounds (
  xcopy /E /I /Y src\sounds bin\sounds >nul 2>nul
)

REM Run the app
echo Running Snake Game...
java -cp bin App

endlocal

