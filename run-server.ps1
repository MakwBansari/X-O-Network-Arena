# X-O Network Arena Run Server (PowerShell)

$javaCmd = "java"
if (!(Get-Command $javaCmd -ErrorAction SilentlyContinue)) {
    # Try to find java.exe in the same places as javac.exe in build.ps1
    $potentialPaths = @(
        "$env:USERPROFILE\.jdks\*\bin\java.exe",
        "C:\Program Files\Java\jdk*\bin\java.exe"
    )
    
    foreach ($path in $potentialPaths) {
        $found = Get-Item -Path $path -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $javaCmd = $found.FullName
            break
        }
    }
}

Write-Host "Starting X-O Network Arena Server..." -ForegroundColor Cyan
& $javaCmd -cp "bin;lib/*" com.xonetwork.server.GameServer
