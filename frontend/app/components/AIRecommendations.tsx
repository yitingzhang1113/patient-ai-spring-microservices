'use client'

import { useState, useEffect } from 'react'
import { ApiService, AIRecommendation, RecommendationType, Patient } from '../lib/api-service'

export function AIRecommendations() {
  const [patients, setPatients] = useState<Patient[]>([])
  const [selectedPatient, setSelectedPatient] = useState<Patient | null>(null)
  const [recommendations, setRecommendations] = useState<AIRecommendation[]>([])
  const [selectedType, setSelectedType] = useState<RecommendationType | 'all'>('all')
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    loadPatients()
  }, [])

  useEffect(() => {
    if (selectedPatient) {
      loadRecommendations()
    }
  }, [selectedPatient, selectedType])

  const loadPatients = async () => {
    try {
      const patientsData = await ApiService.getPatients()
      setPatients(patientsData)
      if (patientsData.length > 0) {
        setSelectedPatient(patientsData[0])
      }
    } catch (error) {
      console.error('Failed to load patients:', error)
    }
  }

  const loadRecommendations = async () => {
    if (!selectedPatient) return
    
    setIsLoading(true)
    try {
      let recommendationsData: AIRecommendation[]
      
      if (selectedType === 'all') {
        recommendationsData = await ApiService.getRecommendationsByPatient(selectedPatient.id)
      } else {
        recommendationsData = await ApiService.getRecommendationsByPatientAndType(
          selectedPatient.id, 
          selectedType as RecommendationType
        )
      }
      
      setRecommendations(recommendationsData)
    } catch (error) {
      console.error('Failed to load recommendations:', error)
      setRecommendations([])
    } finally {
      setIsLoading(false)
    }
  }

  const getPriorityBadgeClass = (priority: string) => {
    switch (priority) {
      case 'low': return 'status-low'
      case 'medium': return 'status-medium'  
      case 'high': return 'status-high'
      case 'critical': return 'status-critical'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getTypeIcon = (type: RecommendationType) => {
    switch (type) {
      case RecommendationType.CLINICAL_NOTE_SUMMARY: return '📋'
      case RecommendationType.TRIAGE_ASSESSMENT: return '🚨'
      case RecommendationType.CODING_SUGGESTION: return '💉'
      case RecommendationType.HEALTH_RECOMMENDATION: return '💊'
      case RecommendationType.RISK_ASSESSMENT: return '⚠️'
      default: return '📝'
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  return (
    <div className="space-y-6">
      {/* Controls */}
      <div className="bg-white overflow-hidden shadow rounded-lg">
        <div className="px-4 py-5 sm:p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">AI Recommendations & Insights</h2>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Patient Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Select Patient
              </label>
              <select
                value={selectedPatient?.id || ''}
                onChange={(e) => {
                  const patient = patients.find(p => p.id === e.target.value)
                  setSelectedPatient(patient || null)
                }}
                className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              >
                {patients.map((patient) => (
                  <option key={patient.id} value={patient.id}>
                    {patient.firstName} {patient.lastName}
                  </option>
                ))}
              </select>
            </div>
            
            {/* Type Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Filter by Type
              </label>
              <select
                value={selectedType}
                onChange={(e) => setSelectedType(e.target.value as RecommendationType | 'all')}
                className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="all">All Types</option>
                <option value={RecommendationType.CLINICAL_NOTE_SUMMARY}>Clinical Note Summary</option>
                <option value={RecommendationType.TRIAGE_ASSESSMENT}>Triage Assessment</option>
                <option value={RecommendationType.CODING_SUGGESTION}>Coding Suggestion</option>
                <option value={RecommendationType.HEALTH_RECOMMENDATION}>Health Recommendation</option>
                <option value={RecommendationType.RISK_ASSESSMENT}>Risk Assessment</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Recommendations List */}
      <div className="space-y-4">
        {isLoading ? (
          <div className="flex items-center justify-center h-32">
            <div className="loading-spinner w-8 h-8 border-4 border-primary-500 border-t-transparent rounded-full"></div>
          </div>
        ) : recommendations.length === 0 ? (
          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="px-4 py-12 text-center">
              <div className="text-gray-400 text-4xl mb-4">🤖</div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">No AI Recommendations</h3>
              <p className="text-gray-500">
                No AI recommendations found for the selected patient and filters.
              </p>
            </div>
          </div>
        ) : (
          recommendations.map((recommendation) => (
            <div key={recommendation.id} className="bg-white overflow-hidden shadow rounded-lg">
              <div className="px-4 py-5 sm:p-6">
                <div className="flex items-start justify-between">
                  <div className="flex items-start space-x-3">
                    <div className="text-2xl">{getTypeIcon(recommendation.type)}</div>
                    <div className="flex-1">
                      <div className="flex items-center space-x-2 mb-2">
                        <h3 className="text-lg font-medium text-gray-900">
                          {recommendation.title}
                        </h3>
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getPriorityBadgeClass(recommendation.priority)}`}>
                          {recommendation.priority.toUpperCase()}
                        </span>
                      </div>
                      <p className="text-sm text-gray-600 mb-3">
                        {recommendation.summary}
                      </p>
                      
                      {/* Recommendations */}
                      {recommendation.recommendations.length > 0 && (
                        <div className="mb-3">
                          <h4 className="text-sm font-medium text-gray-900 mb-1">Recommendations:</h4>
                          <ul className="list-disc list-inside text-sm text-gray-600 space-y-1">
                            {recommendation.recommendations.map((rec, index) => (
                              <li key={index}>{rec}</li>
                            ))}
                          </ul>
                        </div>
                      )}
                      
                      {/* Safety Notes */}
                      {recommendation.safetyNotes.length > 0 && (
                        <div className="mb-3">
                          <h4 className="text-sm font-medium text-red-700 mb-1">⚠️ Safety Notes:</h4>
                          <ul className="list-disc list-inside text-sm text-red-600 space-y-1">
                            {recommendation.safetyNotes.map((note, index) => (
                              <li key={index}>{note}</li>
                            ))}
                          </ul>
                        </div>
                      )}
                      
                      {/* Analysis Details */}
                      {recommendation.analysis && (
                        <div className="bg-gray-50 p-3 rounded-lg">
                          <h4 className="text-sm font-medium text-gray-900 mb-2">AI Analysis Details</h4>
                          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm">
                            {recommendation.analysis.triagePriority && (
                              <div>
                                <span className="font-medium">Triage Priority:</span>
                                <span className="ml-1">{recommendation.analysis.triagePriority}</span>
                              </div>
                            )}
                            {recommendation.analysis.recommendedCareLevel && (
                              <div>
                                <span className="font-medium">Care Level:</span>
                                <span className="ml-1">{recommendation.analysis.recommendedCareLevel}</span>
                              </div>
                            )}
                            {recommendation.analysis.confidenceScore > 0 && (
                              <div>
                                <span className="font-medium">Confidence:</span>
                                <span className="ml-1">{Math.round(recommendation.analysis.confidenceScore * 100)}%</span>
                              </div>
                            )}
                          </div>
                          
                          {recommendation.analysis.suggestedDiagnosisCodes.length > 0 && (
                            <div className="mt-2">
                              <span className="font-medium text-sm">Suggested ICD-10:</span>
                              <div className="mt-1">
                                {recommendation.analysis.suggestedDiagnosisCodes.map((code, index) => (
                                  <span
                                    key={index}
                                    className="inline-block bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded mr-1"
                                  >
                                    {code}
                                  </span>
                                ))}
                              </div>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="text-xs text-gray-500">
                    {formatDate(recommendation.createdAt)}
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
