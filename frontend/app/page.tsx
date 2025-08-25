'use client'

import { useState, useEffect } from 'react'
import { ApiService } from './lib/api-service'
import { PatientDashboard } from './components/PatientDashboard'
import { AIRecommendations } from './components/AIRecommendations'
import { Navigation } from './components/Navigation'

export default function Home() {
  const [currentView, setCurrentView] = useState('dashboard')
  const [isLoading, setIsLoading] = useState(false)

  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation currentView={currentView} onViewChange={setCurrentView} />
      
      <main className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
        {currentView === 'dashboard' && <PatientDashboard />}
        {currentView === 'ai-recommendations' && <AIRecommendations />}
        {currentView === 'patients' && <div>Patient Management Coming Soon...</div>}
        {currentView === 'analytics' && <div>Analytics Coming Soon...</div>}
      </main>
    </div>
  )
}
