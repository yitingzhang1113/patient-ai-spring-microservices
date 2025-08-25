import axios from 'axios'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_GATEWAY_URL || 'http://localhost:8090'

console.log('API Base URL:', API_BASE_URL) // Debug log

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 second timeout
})

// Request interceptor for auth tokens (when implemented)
apiClient.interceptors.request.use(
  (config) => {
    // Add auth token here when authentication is implemented
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

export interface AIRecommendation {
  id: string
  patientId: string
  sourceType: string
  sourceId: string
  type: RecommendationType
  title: string
  summary: string
  recommendations: string[]
  safetyNotes: string[]
  priority: 'low' | 'medium' | 'high' | 'critical'
  analysis: AIAnalysis
  createdAt: string
  updatedAt: string
}

export interface AIAnalysis {
  clinicalSummary: string
  suggestedDiagnosisCodes: string[]
  suggestedProcedureCodes: string[]
  triagePriority: string
  recommendedCareLevel: string
  confidenceScore: number
}

export enum RecommendationType {
  CLINICAL_NOTE_SUMMARY = 'CLINICAL_NOTE_SUMMARY',
  TRIAGE_ASSESSMENT = 'TRIAGE_ASSESSMENT',
  CODING_SUGGESTION = 'CODING_SUGGESTION',
  HEALTH_RECOMMENDATION = 'HEALTH_RECOMMENDATION',
  RISK_ASSESSMENT = 'RISK_ASSESSMENT'
}

export interface Patient {
  id: string
  firstName: string
  lastName: string
  email: string
  phone: string
  dateOfBirth: string
  address: string
  emergencyContact: string
  medicalHistory: string[]
  allergies: string[]
  medications: string[]
}

export interface RecommendationSummary {
  recentCount: number
  monthlyCount: number
  highPriorityCount: number
  criticalCount: number
}

export class ApiService {
  // AI Recommendations API
  static async getRecommendationsByPatient(patientId: string): Promise<AIRecommendation[]> {
    const response = await apiClient.get(`/api/ai-recommendations/patient/${patientId}`)
    return response.data
  }

  static async getRecommendationsByPatientAndType(
    patientId: string, 
    type: RecommendationType
  ): Promise<AIRecommendation[]> {
    const response = await apiClient.get(`/api/ai-recommendations/patient/${patientId}/type/${type}`)
    return response.data
  }

  static async getRecentRecommendationsByPatient(
    patientId: string, 
    days: number = 7
  ): Promise<AIRecommendation[]> {
    const response = await apiClient.get(`/api/ai-recommendations/patient/${patientId}/recent?days=${days}`)
    return response.data
  }

  static async getRecommendationSummary(patientId: string): Promise<RecommendationSummary> {
    const response = await apiClient.get(`/api/ai-recommendations/patient/${patientId}/summary`)
    return response.data
  }

  static async getRecommendationById(recommendationId: string): Promise<AIRecommendation> {
    const response = await apiClient.get(`/api/ai-recommendations/${recommendationId}`)
    return response.data
  }

  // Patient API (when implemented)
  static async getPatients(): Promise<Patient[]> {
    try {
      const response = await apiClient.get('/api/patients')
      return response.data
    } catch (error) {
      console.warn('Patient API not available, returning mock data')
      return [
        {
          id: '1',
          firstName: 'John',
          lastName: 'Doe',
          email: 'john.doe@email.com',
          phone: '+1-555-0123',
          dateOfBirth: '1985-06-15',
          address: '123 Main St, City, State 12345',
          emergencyContact: 'Jane Doe - +1-555-0124',
          medicalHistory: ['Hypertension', 'Type 2 Diabetes'],
          allergies: ['Penicillin', 'Nuts'],
          medications: ['Metformin 500mg', 'Lisinopril 10mg']
        },
        {
          id: '2',
          firstName: 'Jane',
          lastName: 'Smith',
          email: 'jane.smith@email.com',
          phone: '+1-555-0125',
          dateOfBirth: '1990-03-22',
          address: '456 Oak Ave, City, State 12345',
          emergencyContact: 'Bob Smith - +1-555-0126',
          medicalHistory: ['Asthma'],
          allergies: ['Shellfish'],
          medications: ['Albuterol inhaler']
        }
      ]
    }
  }

  static async getPatientById(patientId: string): Promise<Patient | null> {
    try {
      const response = await apiClient.get(`/api/patients/${patientId}`)
      return response.data
    } catch (error) {
      console.warn('Patient API not available, returning mock data')
      const patients = await this.getPatients()
      return patients.find(p => p.id === patientId) || null
    }
  }

  // Health check API
  static async healthCheck(): Promise<{ status: string; services: any }> {
    try {
      const response = await apiClient.get('/actuator/health')
      return response.data
    } catch (error) {
      return { status: 'DOWN', services: {} }
    }
  }
}
