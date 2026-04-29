# AI项目管理系统 - Windows一键启动脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AI项目管理系统 - 一键启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$ErrorActionPreference = "Stop"

# 检查Docker是否安装
try {
    docker --version | Out-Null
} catch {
    Write-Host "错误: Docker未安装，请先安装Docker" -ForegroundColor Red
    exit 1
}

# 检查Docker是否运行
try {
    docker info | Out-Null
} catch {
    Write-Host "错误: Docker未运行，请启动Docker" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "1. 启动数据库 (PostgreSQL + pgvector + Redis)..." -ForegroundColor Green
docker-compose up -d
Write-Host "数据库和Redis启动完成" -ForegroundColor Green

Write-Host ""
Write-Host "2. 等待数据库初始化（约10秒）..." -ForegroundColor Green
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "3. 启动后端服务（端口8080）..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot'; mvn spring-boot:run -Dspring-boot.run.profiles=dev"

Write-Host ""
Write-Host "4. 启动前端服务（端口5173）..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\frontend'; npm install; npm run dev"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "启动完成!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "前端地址: http://localhost:5173" -ForegroundColor Yellow
Write-Host "后端地址: http://localhost:8080" -ForegroundColor Yellow
Write-Host ""
Write-Host "演示账号:" -ForegroundColor White
Write-Host "  管理员: admin / 123456" -ForegroundColor White
Write-Host "  项目经理: pm / 123456" -ForegroundColor White
Write-Host "  开发人员: developer / 123456" -ForegroundColor White
Write-Host "  测试人员: tester / 123456" -ForegroundColor White
Write-Host "  访客: guest / 123456" -ForegroundColor White
Write-Host ""
Write-Host "按任意键退出此脚本" -ForegroundColor Gray
Read-Host
Write-Host "如需停止服务，请在Docker中执行: docker-compose down" -ForegroundColor Yellow
