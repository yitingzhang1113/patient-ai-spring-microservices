# Java Full Stack Spring Boot AI Microservices

A comprehensive patient management system built with Spring Boot microservices, AI-powered insights, and a modern React frontend.

## 🏗️ Architecture Overview

This project demonstrates a modern microservices architecture with AI capabilities:

### 🚀 Core Services
- **Auth Service** (Port 4015) - Authentication & authorization
- **Patient Service** (Port 4013) - Patient data management
- **Billing Service** (Port 4011) - Billing and gRPC services
- **Analytics Service** (Port 4014) - Data analytics and insights
- **API Gateway** (Port 8090) - Unified entry point with routing

### 🧠 AI & Modern Stack
- **AI Service** (Port 4016) - AI-powered clinical insights using Gemini API
- **Frontend** (Port 3000) - React/Next.js dashboard with Tailwind CSS
- **Event-Driven Architecture** - RabbitMQ for async messaging
- **Multiple Databases** - PostgreSQL + MongoDB for different data needs

### 🛠️ Infrastructure
- **PostgreSQL** (Port 5433) - Relational data storage
- **MongoDB** (Port 27017) - AI recommendations storage
- **Kafka** (Port 9093) - Event streaming
- **RabbitMQ** (Port 5672) - Message queuing + Management UI (15672)
- **Zookeeper** - Kafka coordination

## ✨ AI Features

### 🔬 Clinical AI Capabilities
- **Clinical Note Summarization** - SOAP-structured note analysis
- **Triage Assessment** - Priority scoring and care level recommendations
- **Coding Suggestions** - ICD-10/CPT code recommendations
- **Risk Assessment** - Patient risk analysis and safety alerts
- **Health Recommendations** - Personalized health advice

### 🤖 AI Technology Stack
- **Google Gemini API** integration for natural language processing
- **Event-driven AI processing** via RabbitMQ messaging
- **MongoDB storage** for AI recommendations and insights
- **RESTful API** for frontend integration
- **Real-time insights** dashboard

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Node.js 18+ (for frontend development)
- Java 21+ (for backend development)
- Gemini API Key (for AI features)

### 1. Environment Setup
```bash
# Clone the repository
git clone <repository-url>
cd java-spring-microservices-main

# Set up environment variables
export GEMINI_API_KEY="your-gemini-api-key-here"
```

### 2. Build All Services
```bash
# Make build script executable and run
chmod +x build-all.sh
./build-all.sh
```

### 3. Start the Full Stack
```bash
# Start all services with Docker Compose
docker-compose up --build

# Or run in background
docker-compose up -d --build
```

### 4. Access the Applications

**Frontend Dashboard**: http://localhost:3000
- Modern React interface with AI insights
- Patient management and analytics
- Real-time AI recommendations display

**API Gateway**: http://localhost:8090
- Unified API access point
- Routes to all microservices
- Health checks and monitoring

**RabbitMQ Management**: http://localhost:15672
- Username: `admin`
- Password: `admin123`
- Monitor message queues and AI processing

### 5. Verify Installation
```bash
# Check all services are running
docker-compose ps

# View logs
docker-compose logs -f

# Test API health
curl http://localhost:8090/actuator/health
```

## 📱 Using the Application

### Patient Dashboard
1. Navigate to http://localhost:3000
2. Select a patient from the dropdown
3. View patient details and AI insights summary
4. Access quick actions for clinical workflows

### AI Recommendations
1. Click "AI Insights" in the navigation
2. Select patient and filter by recommendation type
3. View detailed AI analysis including:
   - Clinical summaries
   - Safety notes and recommendations
   - Suggested diagnosis codes
   - Triage priority and care level
   - Confidence scores

### Message Flow Example
1. Patient data is created/updated in Patient Service
2. Event is published to RabbitMQ
3. AI Service consumes the event
4. Gemini API processes the clinical data
5. AI recommendations are stored in MongoDB
6. Frontend displays insights in real-time

## 🔧 Development

### API Endpoints

#### AI Service API
```
GET /api/ai-recommendations/patient/{patientId}
GET /api/ai-recommendations/patient/{patientId}/type/{type}
GET /api/ai-recommendations/patient/{patientId}/recent?days=7
GET /api/ai-recommendations/patient/{patientId}/summary
GET /api/ai-recommendations/{recommendationId}
```

#### Event Types
- `patient.note.created` - New clinical note
- `patient.vitals.updated` - Vital signs update
- `patient.visit.completed` - Visit completion
- `patient.symptoms.reported` - Symptom reporting

### Configuration

#### AI Service Configuration
```yaml
gemini:
  api:
    url: https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent
    key: ${GEMINI_API_KEY}

spring:
  data:
    mongodb:
      host: mongodb
      database: patient_ai_db
  rabbitmq:
    host: rabbitmq
    username: admin
    password: admin123
```

#### Environment Variables
- `GEMINI_API_KEY` - Your Google Gemini API key
- `MONGODB_HOST` - MongoDB connection host
- `RABBITMQ_HOST` - RabbitMQ connection host

## 🏥 Sample AI Workflows

### Clinical Note Processing
```json
{
  "eventType": "patient.note.created",
  "patientId": "patient-123",
  "eventData": {
    "note": "Patient presents with chest pain and shortness of breath...",
    "vitals": {"bp": "140/90", "hr": "95", "temp": "98.6"},
    "symptoms": ["chest pain", "dyspnea"]
  }
}
```

### AI Response Example
```json
{
  "type": "TRIAGE_ASSESSMENT",
  "priority": "high",
  "recommendations": [
    "Immediate ECG and cardiac enzymes",
    "Consider emergency cardiology consultation"
  ],
  "safetyNotes": [
    "Monitor for signs of acute coronary syndrome",
    "Continuous cardiac monitoring recommended"
  ],
  "analysis": {
    "triagePriority": "ESI Level 2",
    "recommendedCareLevel": "emergency",
    "confidenceScore": 0.87
  }
}
```

## 🌟 Technology Stack

### Backend
- **Spring Boot 3.2** - Microservices framework
- **Spring Cloud Gateway** - API routing and filtering
- **Spring Data JPA** - Data persistence
- **Spring Data MongoDB** - NoSQL data access
- **Spring AMQP** - RabbitMQ integration
- **WebFlux** - Reactive programming
- **Docker** - Containerization

### Frontend
- **Next.js 14** - React framework
- **TypeScript** - Type safety
- **Tailwind CSS** - Utility-first styling
- **Axios** - HTTP client
- **React Hooks** - State management

### AI & Data
- **Google Gemini API** - Large language model
- **MongoDB** - AI recommendations storage
- **RabbitMQ** - Event-driven messaging
- **PostgreSQL** - Relational data
- **Apache Kafka** - Event streaming

## 🔮 Future Enhancements

### Planned Features
- [ ] **Authentication & Authorization** - OAuth2/OIDC integration
- [ ] **Real-time Notifications** - WebSocket connections
- [ ] **Advanced Analytics** - ML model integration
- [ ] **Mobile App** - React Native companion
- [ ] **Voice Input** - Speech-to-text for clinical notes
- [ ] **FHIR Integration** - Healthcare data standards
- [ ] **Kubernetes Deployment** - Cloud-native scaling
- [ ] **Monitoring & Observability** - Prometheus + Grafana

### AI Roadmap
- [ ] **Multi-modal AI** - Image and document analysis
- [ ] **Predictive Analytics** - Risk scoring models
- [ ] **Clinical Decision Support** - Treatment recommendations
- [ ] **Natural Language Interface** - Chat-based interactions
- [ ] **Integration with EHR systems** - Real-world deployment

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/ai-enhancement`)
3. Commit your changes (`git commit -am 'Add new AI feature'`)
4. Push to the branch (`git push origin feature/ai-enhancement`)
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built upon the foundation of spring-microservices architecture
- Inspired by fitness-app-microservices AI integration patterns
- Utilizes Google Gemini API for advanced language processing
- Modern frontend patterns from Next.js community

---

**🏥 Ready to revolutionize healthcare with AI-powered microservices!** 🚀
