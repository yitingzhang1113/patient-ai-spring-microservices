#!/usr/bin/env bash

# Maven wrapper script for the entire project
# This script will build all services

set -e

echo "🏗️  Building Java Full Stack Spring Boot AI Microservices Project..."

# Make sure all mvnw scripts are executable
echo "📋 Setting up permissions..."
find . -name "mvnw" -exec chmod +x {} \;

# Build services in order
echo "🔧 Building Auth Service..."
cd auth-service && ./mvnw clean package -DskipTests && cd ..

echo "🔧 Building Patient Service..."
cd patient-service && ./mvnw clean package -DskipTests && cd ..

echo "🔧 Building Billing Service..."
cd billing-service && ./mvnw clean package -DskipTests && cd ..

echo "🔧 Building Analytics Service..."
cd analytics-service && ./mvnw clean package -DskipTests && cd ..

echo "🔧 Building API Gateway..."
cd api-gateway && ./mvnw clean package -DskipTests && cd ..

echo "🧠 Building AI Service..."
cd ai-service && ./mvnw clean package -DskipTests && cd ..

echo "✅ All services built successfully!"
echo "🚀 You can now run: docker-compose up --build"
