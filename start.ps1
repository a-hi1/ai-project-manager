# AI项目管理系统 - Windows一键启动脚本
# 版本: 3.0
# 功能: 自动启动后端和前端（使用独立窗口）

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AI项目管理系统 - 一键启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$ErrorActionPreference = "Continue"

# 检查环境
Write-Host ""
Write-Host "检查环境..." -ForegroundColor Yellow

try {
    $nodeVersion = node --version
    Write-Host "✓ Node.js: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ 错误: 未找到Node.js，请先安装Node.js" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

try {
    $mvnVersion = mvn --version | Select-Object -First 1
    Write-Host "✓ Maven: 已安装" -ForegroundColor Green
} catch {
    Write-Host "✗ 错误: 未找到Maven，请先安装Maven" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "✓ Java: 已安装" -ForegroundColor Green
} catch {
    Write-Host "✗ 错误: 未找到Java，请先安装JDK 17+" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

Write-Host ""
Write-Host "所有依赖检查完成！" -ForegroundColor Green
Write-Host ""

# 启动后端
Write-Host "1. 启动后端服务（端口8080）..." -ForegroundColor Cyan
$backendCommand = @"
cd "D:\zhuomian\软件项目管理"
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "后端服务 - 端口 8080" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
mvn spring-boot:run
"@

Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendCommand
Write-Host "✓ 后端服务已启动" -ForegroundColor Green

Write-Host ""
Write-Host "2. 等待后端初始化（约20秒）..." -ForegroundColor Cyan
Start-Sleep -Seconds 20

# 启动前端
Write-Host ""
Write-Host "3. 启动前端服务（端口5173）..." -ForegroundColor Cyan
$frontendCommand = @"
cd "D:\zhuomian\软件项目管理\frontend"
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "前端服务 - 端口 5173" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
npm run dev
"@

Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendCommand
Write-Host "✓ 前端服务已启动" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "访问地址:" -ForegroundColor Yellow
Write-Host "  前端: http://localhost:5173" -ForegroundColor White
Write-Host "  后端: http://localhost:8080" -ForegroundColor White
Write-Host ""
Write-Host "演示账号:" -ForegroundColor Yellow
Write-Host "  管理员: admin / 123456" -ForegroundColor White
Write-Host "  项目经理: pm / 123456" -ForegroundColor White
Write-Host "  开发人员: developer / 123456" -ForegroundColor White
Write-Host "  测试人员: tester / 123456" -ForegroundColor White
Write-Host "  产品经理: product / 123456" -ForegroundColor White
Write-Host "  UI设计师: designer / 123456" -ForegroundColor White
Write-Host "  访客: guest / 123456" -ForegroundColor White
Write-Host ""
Write-Host "提示:" -ForegroundColor Gray
Write-Host "  - 后端和前端已在独立窗口运行" -ForegroundColor Gray
Write-Host "  - 可以在各自窗口查看日志" -ForegroundColor Gray
Write-Host "  - 关闭窗口即可停止对应服务" -ForegroundColor Gray
Write-Host "  - 确保PostgreSQL数据库已启动" -ForegroundColor Gray
Write-Host ""
Write-Host "按任意键关闭此启动窗口（服务继续运行）..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
