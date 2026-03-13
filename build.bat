@echo off
echo Compiling project...

if not exist bin mkdir bin

dir /s /b src\*.java > java_files.txt
javac -d bin -cp "lib/*" @java_files.txt
del java_files.txt

if %ERRORLEVEL% equ 0 (
    echo Compilation successful!
) else (
    echo Compilation failed.
)
pause
