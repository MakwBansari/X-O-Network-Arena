@echo off
echo Redirecting build to PowerShell for better path handling...
powershell -ExecutionPolicy Bypass -File build.ps1
pause
