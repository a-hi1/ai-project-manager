# AI Project Management - Startup Script
# Version 3.1
# Starts backend and frontend in separate windows

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AI Project Management - Quick Start" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "Checking environment..." -ForegroundColor Yellow

# Check Node.js
try {
    $nodeVer = node --version
    Write-Host "[OK] Node.js: $nodeVer" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Node.js not found!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check Maven
try {
    mvn --version | Out-Null
    Write-Host "[OK] Maven: Installed" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Maven not found!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check Java
try {
    java -version 2>&1 | Out-Null
    Write-Host "[OK] Java: Installed" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Java not found! (Need JDK 17+)" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "All dependencies check passed!" -ForegroundColor Green
Write-Host ""

# Start Backend
Write-Host "1. Starting Backend (port 8080)..." -ForegroundColor Cyan
$backendCmd = "cd 'D:\zhuomian\软件项目管理'; Write-Host '=== BACKEND - Port 8080 ===' -ForegroundColor Cyan; mvn spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendCmd
Write-Host "[OK] Backend started" -ForegroundColor Green

Write-Host ""
Write-Host "2. Waiting for backend initialization (20 seconds)..." -ForegroundColor Cyan
Start-Sleep -Seconds 20

# Start Frontend
Write-Host ""
Write-Host "3. Starting Frontend (port 5173)..." -ForegroundColor Cyan
$frontendCmd = "cd 'D:\zhuomian\软件项目管理\frontend'; Write-Host '=== FRONTEND - Port 5173 ===' -ForegroundColor Cyan; npm run dev"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendCmd
Write-Host "[OK] Frontend started" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Startup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Access URLs:" -ForegroundColor Yellow
Write-Host "  Frontend: http://localhost:5173" -ForegroundColor White
Write-Host "  Backend: http://localhost:8080" -ForegroundColor White
Write-Host ""
Write-Host "Demo Accounts:" -ForegroundColor Yellow
Write-Host "  Admin: admin / 123456" -ForegroundColor White
Write-Host "  PM: pm / 123456" -ForegroundColor White
Write-Host "  Developer: developer / 123456" -ForegroundColor White
Write-Host "  Tester: tester / 123456" -ForegroundColor White
Write-Host "  Product: product / 123456" -ForegroundColor White
Write-Host "  Designer: designer / 123456" -ForegroundColor White
Write-Host "  Guest: guest / 123456" -ForegroundColor White
Write-Host ""
Write-Host "Tips:" -ForegroundColor Gray
Write-Host "  - Backend and frontend run in separate windows" -ForegroundColor Gray
Write-Host "  - Close windows to stop services" -ForegroundColor Gray
Write-Host "  - Make sure PostgreSQL is running" -ForegroundColor Gray
Write-Host ""
Write-Host "Press any key to close this window..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
