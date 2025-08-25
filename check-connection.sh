#!/bin/bash

echo "🔍 检查前后端连接状态..."

echo "1. 检查 API Gateway 健康状态:"
curl -s http://localhost:8090/actuator/health | jq '.' 2>/dev/null || echo "❌ API Gateway 未响应"

echo ""
echo "2. 检查患者服务:"
curl -s http://localhost:8090/api/patients | jq '.' 2>/dev/null || echo "❌ 患者服务未响应"

echo ""
echo "3. 检查认证服务:"
curl -s http://localhost:8090/api/auth/health | jq '.' 2>/dev/null || echo "❌ 认证服务未响应"

echo ""
echo "4. 检查前端是否能连接 API:"
echo "访问 http://localhost:3000 查看前端"
echo "检查浏览器控制台是否有 CORS 或连接错误"

echo ""
echo "5. Docker 容器状态:"
docker-compose ps
