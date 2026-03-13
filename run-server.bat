@echo off
set JAVA_CMD=java

java -version >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo java not found in PATH. Searching for local JRE/JDKs...
    for /d %%i in ("%USERPROFILE%\.jdks\*") do if exist "%%i\bin\java.exe" set JAVA_CMD="%%i\bin\java.exe"
    for /d %%i in ("C:\Program Files\Java\jdk*") do if exist "%%i\bin\java.exe" set JAVA_CMD="%%i\bin\java.exe"
    for /d %%i in ("C:\Program Files\Java\jre*") do if exist "%%i\bin\java.exe" set JAVA_CMD="%%i\bin\java.exe"
)

echo Starting X-O Network Arena Server using %JAVA_CMD%...
%JAVA_CMD% -cp "bin;lib/*" com.xonetwork.server.GameServer
pause
