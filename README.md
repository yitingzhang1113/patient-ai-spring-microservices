<div align="center" style="margin-bottom:20px">
  <img src="assets/logo.png" alt="java-fullstack-spring-boot-ai-microservices" width="140" />
  <div align="center">
    <a href="https://github.com/<owner>/<repo>/actions/workflows/ci.yml">
      <img src="https://github.com/<owner>/<repo>/actions/workflows/ci.yml/badge.svg?branch=main&style=flat-square"/>
    </a>
    <a href="./LICENSE">
      <img src="https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square"/>
    </a>
  </div>
</div>

> **A practical full-stack Java/Spring microservices system for patient management** with event-driven workflows, AI insights (Gemini), a modern React frontend, multiple data stores, and production-ready DevOps. 🚀  
> 💡 Focus is on engineering patterns: microservices, messaging, tracing, gRPC, CQRS-friendly vertical slices, CI/CD, and observability.

# Table of Contents

- [The Goals of This Project](#the-goals-of-this-project)
- [Plan](#plan)
- [Technologies — Libraries](#technologies--libraries)
- [Domain and Bounded Context — Service Boundary](#domain-and-bounded-context--service-boundary)
- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [How to Run](#how-to-run)
  - [Docker Compose](#docker-compose)
  - [Build](#build)
  - [Run](#run)
  - [Test](#test)
- [APIs & Events](#apis--events)
- [Configuration](#configuration)
- [Performance & Load Testing](#performance--load-testing)
- [Join the Discord Community](#join-the-discord-community)
- [Service Reference — Patient/Billing/Kafka/Notification/Auth](#service-reference--patientbillingkafkanotificationauth)
- [Support](#support)
- [Contribution](#contribution)
- [License](#license)
- [Acknowledgments](#acknowledgments)

---

## The Goals of This Project

- :sparkles: Vertical slices + microservices for clear separation of concerns  
- :sparkles: Event-driven architecture via Kafka/RabbitMQ  
- :sparkles: AI microservice powered by Google Gemini for clinical insights  
- :sparkles: gRPC where synchronous and strongly-typed APIs fit (e.g., Billing)  
- :sparkles: Polyglot persistence (PostgreSQL + MongoDB)  
- :sparkles: Observability (traces/metrics/logs) and CI/CD automation  
- :sparkles: Modern React/Next.js frontend (TypeScript, Tailwind, accessibility)

---

## Plan

Work in progress; new capabilities will be added over time.

| Feature                         | Status      |
|---------------------------------|-------------|
| Auth Service                    | ✔️ Complete |
| Patient Service                 | ✔️ Complete |
| Billing (gRPC)                  | ✔️ Complete |
| Analytics Consumer              | ✔️ Complete |
| AI Service (Gemini)             | ✔️ Complete |
| Frontend (Next.js)              | ✔️ Complete |
| Kubernetes Manifests            | ⏳ Planned  |
| Prometheus + Grafana Dashboards | ⏳ Planned  |
| WebSocket Notifications         | ⏳ Planned  |
| FHIR Integration                | ⏳ Planned  |

---

## Technologies — Libraries

- Java 21, Spring Boot 3.2, Spring Cloud Gateway  
- Spring Data JPA (PostgreSQL), Spring Data MongoDB (AI outcomes)  
- Spring AMQP (RabbitMQ), Kafka (streaming)  
- gRPC + protobuf for typed sync calls  
- Next.js 14, TypeScript, Tailwind CSS, Axios  
- Docker / Docker Compose, GitHub Actions (CI)  
- OpenTelemetry, Prometheus/Grafana (planned dashboards)

---

## Domain and Bounded Context — Service Boundary

- **Auth Service** — Authentication/authorization (JWT issuance & validation)  
- **Patient Service** — CRUD over patient profiles and clinical data; publishes domain events  
- **Billing Service (gRPC)** — Account creation, billing interactions over gRPC  
- **Analytics Service** — Consumes events for insights/metrics  
- **AI Service** — Subscribes to clinical events, calls Gemini, stores recommendations in MongoDB  
- **API Gateway** — Single entry point, routing/filters, token relay to downstream services  
- **Frontend** — React/Next.js dashboard for patient records and AI insights

---

## Architecture Overview
<p align="center">
  <img src="assets/architecture.png" alt="Architecture Diagram" />
</p>

- **Event-Driven**: Patient/Activity emit events → **Event Bus (Kafka/RabbitMQ)** → AI/Analytics consume  
- **AI Flow**: Event → AI Service → Gemini → normalize JSON → save to MongoDB → REST → frontend  
- **Serverless (optional)**: New-patient event → **AWS SQS** → **Lambda** → SES email (welcome/alerts)  
- **Polyglot storage**: Relational (PostgreSQL) + document (MongoDB)

---

## Project Structure

```
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
```

---

## How to Run

### Prerequisites

- Docker & Docker Compose  
- Java 21+ (local dev)  
- Node.js 18+ (frontend dev)  
- A Gemini API key

### Docker Compose

```bash
# from project root
docker-compose up -d --build
docker-compose ps
```

**Services (defaults):**

- Frontend: http://localhost:3000  
- API Gateway: http://localhost:8090  
- RabbitMQ UI: http://localhost:15672  

Use **secrets** or env for credentials (do not commit). Example:

```bash
RABBITMQ_DEFAULT_USER_FILE=/run/secrets/rmq_user
RABBITMQ_DEFAULT_PASS_FILE=/run/secrets/rmq_pass
```

Kafka: `9093` (internal), Zookeeper: default

### Build

```bash
chmod +x build-all.sh
./build-all.sh
```

### Run

```bash
# Example: run a single service
cd patient-service && ./mvnw spring-boot:run
```

### Test

```bash
./mvnw -q -DskipIT=false test
```

---

## APIs & Events

### AI Service (examples)

```
GET /api/ai-recommendations/patient/{patientId}
GET /api/ai-recommendations/patient/{patientId}/type/{type}
GET /api/ai-recommendations/patient/{patientId}/recent?days=7
GET /api/ai-recommendations/patient/{patientId}/summary
GET /api/ai-recommendations/{recommendationId}
```

### Event Types

- `patient.note.created`  
- `patient.vitals.updated`  
- `patient.visit.completed`  
- `patient.symptoms.reported`

---

## Configuration

### AI Service

```yaml
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
```

**Environment variables**

- `GEMINI_API_KEY`  
- `MONGODB_HOST`  
- `RABBITMQ_HOST`, `RABBITMQ_USER`, `RABBITMQ_PASS` (inject via Secrets)

### Patient Service (env excerpt)

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://patient-service-db:5432/db
SPRING_DATASOURCE_USERNAME=admin_user
SPRING_DATASOURCE_PASSWORD=password
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

### Billing/Patient gRPC (pom and protobuf plugin)

See `/billing-service/pom.xml` and `/patient-service/pom.xml` for grpc/protobuf dependencies and the `protobuf-maven-plugin` build section.

### Kafka Container (local dev)

```bash
KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094;KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER;KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka:9093;KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,PLAINTEXT:PLAINTEXT;KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094;KAFKA_CFG_NODE_ID=0;KAFKA_CFG_PROCESS_ROLES=controller,broker
```

> **Security note**: never commit real passwords or API keys. Use `.env.example` + runtime Secrets (Compose/K8s).

---

## Performance & Load Testing

- k6 scripts to generate traffic to Gateway and AI endpoints  
- Run in Kubernetes via Job/CronJob; scale services using HPA  
- Capture RPS/P95/P99, error rate, CPU/memory, queue backlog  
- Dashboards with Prometheus/Grafana (planned)



---

## Service Reference — Patient/Billing/Kafka/Notification/Auth

### Patient Service — Environment Variables

```bash
JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005;
SPRING_DATASOURCE_PASSWORD=password;
SPRING_DATASOURCE_URL=jdbc:postgresql://patient-service-db:5432/db;
SPRING_DATASOURCE_USERNAME=admin_user;
SPRING_JPA_HIBERNATE_DDL_AUTO=update;
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092;
SPRING_SQL_INIT_MODE=always
```

### Billing Service — gRPC Setup

Add the following to the `<dependencies>` section:

```xml
<!-- GRPC -->
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>1.69.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>1.69.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>1.69.0</version>
</dependency>
<dependency> <!-- necessary for Java 9+ -->
    <groupId>org.apache.tomcat</groupId>
    <artifactId>annotations-api</artifactId>
    <version>6.0.53</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-spring-boot-starter</artifactId>
    <version>3.1.0.RELEASE</version>
</dependency>
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>4.29.1</version>
</dependency>
```

Replace the `<build>` section with the following:

```xml
<build>
    <extensions>
        <!-- Ensure OS compatibility for protoc -->
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.7.0</version>
        </extension>
    </extensions>
    <plugins>
        <!-- Spring boot / maven  -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>

        <!-- PROTO -->
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
                <protocArtifact>com.google.protobuf:protoc:3.25.5:exe:${os.detected.classifier}</protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.68.1:exe:${os.detected.classifier}</pluginArtifact>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Patient Service — Environment Variables (complete list)

```bash
BILLING_SERVICE_ADDRESS=billing-service;
BILLING_SERVICE_GRPC_PORT=9005;
JAVA_TOOL_OPTIONS=-agentlib:jdwp\=transport\=dt_socket,server\=y,suspend\=n,address\=*:5005;
SPRING_DATASOURCE_PASSWORD=password;
SPRING_DATASOURCE_URL=jdbc:postgresql://patient-service-db:5432/db;
SPRING_DATASOURCE_USERNAME=admin_user;
SPRING_JPA_HIBERNATE_DDL_AUTO=update;
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092;
SPRING_SQL_INIT_MODE=always
```

### Patient Service — gRPC Setup

Add the following to the `<dependencies>` section:

```xml
<!-- GRPC -->
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty-shaded</artifactId>
    <version>1.69.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>1.69.0</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>1.69.0</version>
</dependency>
<dependency> <!-- necessary for Java 9+ -->
    <groupId>org.apache.tomcat</groupId>
    <artifactId>annotations-api</artifactId>
    <version>6.0.53</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-spring-boot-starter</artifactId>
    <version>3.1.0.RELEASE</version>
</dependency>
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>4.29.1</version>
</dependency>
```

Replace the `<build>` section with the following:

```xml
<build>
    <extensions>
        <!-- Ensure OS compatibility for protoc -->
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.7.0</version>
        </extension>
    </extensions>
    <plugins>
        <!-- Spring boot / maven  -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>

        <!-- PROTO -->
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
                <protocArtifact>com.google.protobuf:protoc:3.25.5:exe:${os.detected.classifier}</protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.68.1:exe:${os.detected.classifier}</pluginArtifact>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Kafka Container

Copy/paste this line into the environment variables when running the container in IntelliJ:

```bash
KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://localhost:9094;KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER;KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka:9093;KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,PLAINTEXT:PLAINTEXT;KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094;KAFKA_CFG_NODE_ID=0;KAFKA_CFG_PROCESS_ROLES=controller,broker
```

### Kafka Producer Setup (Patient Service)

Add the following to `application.properties`:

```properties
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer
```

### Notification Service — Environment Vars

```bash
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

### Notification Service — Protobuf/Kafka Dependencies

Add these in addition to what's already there:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
    <version>3.3.0</version>
</dependency>

<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>4.29.1</version>
</dependency>
```

Update the build section in `pom.xml`:

```xml
<build>
    <extensions>
        <!-- Ensure OS compatibility for protoc -->
        <extension>
            <groupId>kr.motd.maven</groupId>
            <artifactId>os-maven-plugin</artifactId>
            <version>1.7.0</version>
        </extension>
    </extensions>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>

        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
                <protocArtifact>com.google.protobuf:protoc:3.25.5:exe:${os.detected.classifier}</protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.68.1:exe:${os.detected.classifier}</pluginArtifact>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Auth Service — Dependencies (additions)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
</dependency>
```

### Auth Service — Environment Variables

```bash
SPRING_DATASOURCE_PASSWORD=password
SPRING_DATASOURCE_URL=jdbc:postgresql://auth-service-db:5432/db
SPRING_DATASOURCE_USERNAME=admin_user
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_SQL_INIT_MODE=always
```

### Auth Service — `data.sql`

```sql
-- Ensure the 'users' table exists
CREATE TABLE IF NOT EXISTS "users" (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- Insert the user if no existing user with the same id or email exists
INSERT INTO "users" (id, email, password, role)
SELECT '223e4567-e89b-12d3-a456-426614174006', 'testuser@test.com',
       '$2b$12$7hoRZfJrRKD2nIm2vHLs7OBETy.LWenXXMLKf99W8M4PUwO6KB7fu', 'ADMIN'
WHERE NOT EXISTS (
    SELECT 1
    FROM "users"
    WHERE id = '223e4567-e89b-12d3-a456-426614174006'
       OR email = 'testuser@test.com'
);
```

### Auth Service DB — Environment Variables

```bash
POSTGRES_DB=db
POSTGRES_PASSWORD=password
POSTGRES_USER=admin_user
```

---

## Support

If you like this project, please ⭐ the repo. It helps a lot!

---

## Contribution

PRs are welcome—see `CONTRIBUTING.md` (create one if missing). Use feature branches and add tests where possible.

---

## License

This project is released under the **MIT License** — see [LICENSE](./LICENSE).

---

## Acknowledgments

- Based on Spring microservices course foundations  
- Inspired by fitness-app-microservices AI patterns  
- Google Gemini API for LLM features  
- Next.js community best practices
