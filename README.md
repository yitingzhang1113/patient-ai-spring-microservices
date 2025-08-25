<div align="center" style="margin-bottom:20px"> <img src="assets/logo.png" alt="java-fullstack-spring-boot-ai-microservices" width="140" /> <div align="center"> <a href="https://github.com/<owner>/<repo>/actions/workflows/ci.yml"> <img src="https://github.com/<owner>/<repo>/actions/workflows/ci.yml/badge.svg?branch=main&style=flat-square"/> </a> <a href="./LICENSE"> <img src="https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square"/> </a> </div> </div>

A practical full-stack Java/Spring microservices system for patient management with event-driven workflows, AI insights (Gemini), a modern React frontend, multiple data stores, and production-ready DevOps. 🚀
💡 Focus is on engineering patterns: microservices, messaging, tracing, gRPC, CQRS-friendly vertical slices, CI/CD, and observability.

Table of Contents

The Goals of This Project

Plan

Technologies — Libraries

Domain and Bounded Context — Service Boundary

Architecture Overview

Project Structure
architecture
/Users/eratozhang/Desktop/java-spring-microservices-main/architecture.png

How to Run

Docker Compose

Build

Run

Test

APIs & Events

Configuration

Performance & Load Testing

Support

Contribution

License

The Goals of This Project

:sparkles: Vertical slices + microservices for clear separation of concerns

:sparkles: Event-driven architecture via Kafka/RabbitMQ

:sparkles: AI microservice powered by Google Gemini for clinical insights

:sparkles: gRPC between services where synchronous and strongly-typed APIs fit (e.g., Billing)

:sparkles: Polyglot persistence (PostgreSQL + MongoDB)

:sparkles: Observability (traces/metrics/logs) and CI/CD automation

:sparkles: Modern React/Next.js frontend (TypeScript, Tailwind, accessibility)

Plan

Work in progress; new capabilities will be added over time.

Feature	Status
Auth Service	✔️ Complete
Patient Service	✔️ Complete
Billing (gRPC)	✔️ Complete
Analytics Consumer	✔️ Complete
AI Service (Gemini)	✔️ Complete
Frontend (Next.js)	✔️ Complete
Kubernetes Manifests	⏳ Planned
Prometheus + Grafana Dashboards	⏳ Planned
WebSocket Notifications	⏳ Planned
FHIR Integration	⏳ Planned
Technologies — Libraries

Java 21, Spring Boot 3.2, Spring Cloud Gateway

Spring Data JPA (PostgreSQL), Spring Data MongoDB (AI outcomes)

Spring AMQP (RabbitMQ), Kafka (streaming)

gRPC + protobuf for typed sync calls

Next.js 14, TypeScript, Tailwind CSS, Axios

Docker / Docker Compose, GitHub Actions (CI)

OpenTelemetry, Prometheus/Grafana (planned dashboards)

Domain and Bounded Context — Service Boundary

Auth Service — Authentication/authorization (JWT issuance & validation).

Patient Service — CRUD over patient profiles and clinical data; publishes domain events.

Billing Service (gRPC) — Account creation, billing interactions over gRPC.

Analytics Service — Consumes events for insights/metrics.

AI Service — Subscribes to clinical events, calls Gemini, stores recommendations in MongoDB.

API Gateway — Single entry point, routing/filters, token relay to downstream services.

Frontend — React/Next.js dashboard for patient records and AI insights.

Architecture Overview

Event-Driven: Patient/Activity emit events → Event Bus (Kafka/RabbitMQ) → AI/Analytics consume.

AI Flow: Event → AI Service → Gemini → normalize JSON → save to MongoDB → expose via REST → frontend.

Serverless (optional): New-patient event → AWS SQS → Lambda → SES email (welcome/alerts).

Polyglot storage: Relational for core data (PostgreSQL) + document for AI artifacts (MongoDB).

Project Structure
/api-gateway
/auth-service
/patient-service
/billing-service
/analytics-service
/ai-service
/frontend
/deployments
  ├─ docker-compose.yml
  └─ k8s/ (manifests - planned)
assets/

How to Run
Prerequisites

Docker & Docker Compose

Java 21+ (local dev)

Node.js 18+ (frontend dev)

A Gemini API key

Docker Compose
# from project root
docker-compose up -d --build
docker-compose ps


Services (defaults):

Frontend: http://localhost:3000

API Gateway: http://localhost:8090

RabbitMQ UI: http://localhost:15672

Use secrets or env for credentials (do not commit). Example:

RABBITMQ_DEFAULT_USER_FILE=/run/secrets/rmq_user
RABBITMQ_DEFAULT_PASS_FILE=/run/secrets/rmq_pass


Kafka: 9093 (internal), Zookeeper: default

Build
chmod +x build-all.sh
./build-all.sh

Run
# Example: run a single service
cd patient-service && ./mvnw spring-boot:run

Test
./mvnw -q -DskipIT=false test

APIs & Events
AI Service (examples)
GET /api/ai-recommendations/patient/{patientId}
GET /api/ai-recommendations/patient/{patientId}/type/{type}
GET /api/ai-recommendations/patient/{patientId}/recent?days=7
GET /api/ai-recommendations/patient/{patientId}/summary
GET /api/ai-recommendations/{recommendationId}

Event Types

patient.note.created

patient.vitals.updated

patient.visit.completed

patient.symptoms.reported

Configuration
AI Service
gemini:
  api:
    url: https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent
    key: ${GEMINI_API_KEY}

spring:
  data:
    mongodb:
      host: ${MONGODB_HOST:mongodb}
      database: patient_ai_db
  rabbitmq:
    host: ${RABBITMQ_HOST:rabbitmq}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASS}


Environment variables

GEMINI_API_KEY

MONGODB_HOST

RABBITMQ_HOST, RABBITMQ_USER, RABBITMQ_PASS (inject via Secrets)

Patient Service (env excerpt)
SPRING_DATASOURCE_URL=jdbc:postgresql://patient-service-db:5432/db
SPRING_DATASOURCE_USERNAME=admin_user
SPRING_DATASOURCE_PASSWORD=password
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

Billing/Patient gRPC (pom and protobuf plugin)

See /billing-service/pom.xml and /patient-service/pom.xml for grpc/protobuf dependencies and the protobuf-maven-plugin build section.

Kafka Container (local dev)
KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094;KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER;KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka:9093;KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,PLAINTEXT:PLAINTEXT;KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094;KAFKA_CFG_NODE_ID=0;KAFKA_CFG_PROCESS_ROLES=controller,broker


Security note: never commit real passwords or API keys. Use .env.example + runtime Secrets (Compose/K8s).

Performance & Load Testing

k6 scripts to generate traffic to Gateway and AI endpoints

Run in Kubernetes via Job/CronJob; scale services using HPA

Capture RPS/P95/P99, error rate, CPU/memory, queue backlog

Dashboards with Prometheus/Grafana (planned)

Support

If you like this project, please ⭐ the repo. It helps a lot!

Contribution

PRs are welcome—see CONTRIBUTING.md (create one if missing). Please use feature branches and add tests where possible.

License

This project is released under the MIT License — see LICENSE
.

Acknowledgments

Based on Spring microservices course foundations

Inspired by fitness-app-microservices AI patterns

Google Gemini API for LLM features

Next.js community best practices