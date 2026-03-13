# Check if javac is in PATH, if not, try to find it
$javacCmd = "javac"
if (!(Get-Command $javacCmd -ErrorAction SilentlyContinue)) {
    Write-Host "javac not found in PATH. Searching for local JDKs..." -ForegroundColor Yellow
    
    # Common IntelliJ/User JDK paths
    $potentialPaths = @(
        "$env:USERPROFILE\.jdks\*\bin\javac.exe",
        "C:\Program Files\Java\jdk*\bin\javac.exe"
    )
    
    foreach ($path in $potentialPaths) {
        $found = Get-Item -Path $path -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $javacCmd = $found.FullName
            Write-Host "Found javac at: $javacCmd" -ForegroundColor Gray
            break
        }
    }
}

if (!(Get-Command $javacCmd -ErrorAction SilentlyContinue) -and ($javacCmd -eq "javac")) {
    Write-Host "ERROR: javac not found. Please install JDK or add it to PATH." -ForegroundColor Red
    return
}

Write-Host "Compiling project..." -ForegroundColor Cyan

# Find all Java files
$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object { "$($_.FullName)" }

if ($javaFiles.Count -eq 0) {
    Write-Host "No Java files found in src directory." -ForegroundColor Red
    return
}

# Run javac
& $javacCmd -d bin -cp "lib/*" $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful!" -ForegroundColor Green
    Write-Host "To run the server: java -cp 'bin;lib/*' com.xonetwork.server.GameServer"
    Write-Host "To run the client: java -cp bin com.xonetwork.client.GameClient"
} else {
    Write-Host "Compilation failed. Please ensure Java JDK is installed and 'javac' is in your PATH." -ForegroundColor Red
}
