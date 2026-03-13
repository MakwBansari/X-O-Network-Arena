# X-O Network Arena Run Client (PowerShell)

$javaCmd = "java"
if (!(Get-Command $javaCmd -ErrorAction SilentlyContinue)) {
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

Write-Host "Starting X-O Network Arena Client..." -ForegroundColor Cyan
& $javaCmd -cp "bin" com.xonetwork.client.GameClient
