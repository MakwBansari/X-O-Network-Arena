@echo off
set JAVAC_CMD=javac

javac -version >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo javac not found in PATH. Searching for local JDKs...
    for /d %%i in ("%USERPROFILE%\.jdks\*") do if exist "%%i\bin\javac.exe" set JAVAC_CMD="%%i\bin\javac.exe"
    for /d %%i in ("C:\Program Files\Java\jdk*") do if exist "%%i\bin\javac.exe" set JAVAC_CMD="%%i\bin\javac.exe"
)

echo Compiling project using %JAVAC_CMD%...

if not exist bin mkdir bin

dir /s /b src\*.java > java_files.txt
%JAVAC_CMD% -d bin -cp "lib/*" @java_files.txt
del java_files.txt

if %ERRORLEVEL% equ 0 (
    echo Compilation successful!
) else (
    echo Compilation failed.
)
pause
