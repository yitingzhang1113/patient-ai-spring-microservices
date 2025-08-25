'use client'

import { useState, useEffect } from 'react'
import { ApiService, Patient, RecommendationSummary } from '../lib/api-service'

export function PatientDashboard() {
  const [patients, setPatients] = useState<Patient[]>([])
  const [selectedPatient, setSelectedPatient] = useState<Patient | null>(null)
  const [summary, setSummary] = useState<RecommendationSummary | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    loadPatients()
  }, [])

  useEffect(() => {
    if (selectedPatient) {
      loadPatientSummary(selectedPatient.id)
    }
  }, [selectedPatient])

  const loadPatients = async () => {
    setIsLoading(true)
    try {
      const patientsData = await ApiService.getPatients()
      setPatients(patientsData)
      if (patientsData.length > 0) {
        setSelectedPatient(patientsData[0])
      }
    } catch (error) {
      console.error('Failed to load patients:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const loadPatientSummary = async (patientId: string) => {
    try {
      const summaryData = await ApiService.getRecommendationSummary(patientId)
      setSummary(summaryData)
    } catch (error) {
      console.error('Failed to load patient summary:', error)
      setSummary(null)
    }
  }

  const getPriorityColor = (count: number) => {
    if (count === 0) return 'text-gray-500'
    if (count < 5) return 'text-green-600'
    if (count < 10) return 'text-yellow-600'
    return 'text-red-600'
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="loading-spinner w-8 h-8 border-4 border-primary-500 border-t-transparent rounded-full"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="bg-white overflow-hidden shadow rounded-lg">
        <div className="px-4 py-5 sm:p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">Patient Overview</h2>
          
          {/* Patient Selection */}
          <div className="mb-6">
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
                  {patient.firstName} {patient.lastName} - {patient.email}
                </option>
              ))}
            </select>
          </div>

          {/* Patient Details */}
          {selectedPatient && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <div>
                  <h3 className="text-lg font-medium text-gray-900">
                    {selectedPatient.firstName} {selectedPatient.lastName}
                  </h3>
                  <p className="text-sm text-gray-500">{selectedPatient.email}</p>
                  <p className="text-sm text-gray-500">{selectedPatient.phone}</p>
                </div>
                
                <div>
                  <h4 className="text-sm font-medium text-gray-900">Medical History</h4>
                  <div className="mt-1">
                    {selectedPatient.medicalHistory.map((condition, index) => (
                      <span
                        key={index}
                        className="inline-block bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full mr-2 mb-1"
                      >
                        {condition}
                      </span>
                    ))}
                  </div>
                </div>
                
                <div>
                  <h4 className="text-sm font-medium text-gray-900">Allergies</h4>
                  <div className="mt-1">
                    {selectedPatient.allergies.map((allergy, index) => (
                      <span
                        key={index}
                        className="inline-block bg-red-100 text-red-800 text-xs px-2 py-1 rounded-full mr-2 mb-1"
                      >
                        {allergy}
                      </span>
                    ))}
                  </div>
                </div>
              </div>

              {/* AI Insights Summary */}
              <div className="space-y-4">
                <h4 className="text-sm font-medium text-gray-900">AI Insights Summary</h4>
                {summary ? (
                  <div className="grid grid-cols-2 gap-4">
                    <div className="bg-gray-50 p-3 rounded-lg">
                      <div className="text-2xl font-bold text-gray-900">{summary.recentCount}</div>
                      <div className="text-xs text-gray-500">Recent (7 days)</div>
                    </div>
                    <div className="bg-gray-50 p-3 rounded-lg">
                      <div className="text-2xl font-bold text-gray-900">{summary.monthlyCount}</div>
                      <div className="text-xs text-gray-500">Monthly</div>
                    </div>
                    <div className="bg-orange-50 p-3 rounded-lg">
                      <div className={`text-2xl font-bold ${getPriorityColor(summary.highPriorityCount)}`}>
                        {summary.highPriorityCount}
                      </div>
                      <div className="text-xs text-gray-500">High Priority</div>
                    </div>
                    <div className="bg-red-50 p-3 rounded-lg">
                      <div className={`text-2xl font-bold ${getPriorityColor(summary.criticalCount)}`}>
                        {summary.criticalCount}
                      </div>
                      <div className="text-xs text-gray-500">Critical</div>
                    </div>
                  </div>
                ) : (
                  <div className="text-sm text-gray-500">Loading AI insights...</div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white overflow-hidden shadow rounded-lg">
        <div className="px-4 py-5 sm:p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Quick Actions</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <button className="bg-primary-500 text-white px-4 py-2 rounded-md hover:bg-primary-600 transition-colors">
              📝 Add Clinical Note
            </button>
            <button className="bg-green-500 text-white px-4 py-2 rounded-md hover:bg-green-600 transition-colors">
              🏥 Schedule Appointment
            </button>
            <button className="bg-purple-500 text-white px-4 py-2 rounded-md hover:bg-purple-600 transition-colors">
              🧬 Request AI Analysis
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
