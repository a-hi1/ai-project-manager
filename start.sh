#!/bin/bash

# AI项目管理系统 一键启动脚本

echo "========================================"
echo "AI项目管理系统 - 一键启动"
echo "========================================"

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker未安装，请先安装Docker"
    exit 1
fi

# 检查Docker是否运行
if ! docker info &> /dev/null; then
    echo "错误: Docker未运行，请启动Docker"
    exit 1
fi

echo ""
echo "1. 启动数据库 (PostgreSQL + pgvector)..."
docker-compose up -d
echo "数据库启动完成"

echo ""
echo "2. 等待数据库初始化..."
sleep 5

echo ""
echo "3. 启动后端服务..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!
cd ..

echo ""
echo "4. 启动前端服务..."
cd frontend
npm install
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "========================================"
echo "启动完成!"
echo "========================================"
echo "前端地址: http://localhost:5173"
echo "后端地址: http://localhost:8080"
echo ""
echo "演示账号:"
echo "  管理员: admin / 123456"
echo "  项目经理: pm / 123456"
echo "  开发人员: developer / 123456"
echo "  测试人员: tester / 123456"
echo "  访客: guest / 123456"
echo ""
echo "按 Ctrl+C 停止所有服务"
echo "========================================"

# 等待用户按Ctrl+C
trap "echo '正在停止服务...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; docker-compose down; exit" INT TERM
wait