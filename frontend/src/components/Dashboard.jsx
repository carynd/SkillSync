import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import jobService from '../services/jobService'

const Dashboard = () => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [recommendations, setRecommendations] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadRecommendations()
  }, [])

  const loadRecommendations = async () => {
    try {
      setLoading(true)
      console.log('Dashboard - Loading recommendations for user:', user.userId)

      // Try to get cached recommendations first
      try {
        console.log('Dashboard - Attempt 1: Fetching cached recommendations...')
        const data = await jobService.getRecommendations(user.userId)
        console.log('Dashboard - Found cached recommendations:', {
          targetRole: data.targetRole,
          skillGapPercentage: data.skillGapPercentage,
          recommendationsCount: data.recommendations?.length || 0
        })
        setRecommendations(data)
        setError('')
        return
      } catch (err) {
        // If 404, recommendations don't exist yet - generate them
        if (err.response?.status === 404) {
          console.log('Dashboard - No cached recommendations found (404), generating new ones...')
          const data = await jobService.generateRecommendations(user.userId)
          console.log('Dashboard - Generated new recommendations:', {
            targetRole: data.targetRole,
            skillGapPercentage: data.skillGapPercentage,
            recommendationsCount: data.recommendations?.length || 0
          })
          setRecommendations(data)
          setError('')
          return
        }
        // If other error, throw to outer catch
        console.error('Dashboard - Unexpected error during recommendation fetch:', err.response?.status, err.response?.data)
        throw err
      }
    } catch (err) {
      console.error('Dashboard - Error loading recommendations:', err.response?.data || err.message)

      // Check for specific error messages
      const errorMessage = err.response?.data?.message || err.message || ''
      const errorStatus = err.response?.status || 'unknown'

      console.error('Dashboard - Error details:', { errorStatus, errorMessage })

      if (err.response?.status === 400 || errorMessage.includes('target role')) {
        setError('Please set your target role and skills in your profile first.')
      } else if (errorMessage.includes('No skill data')) {
        setError('No skill data available for your target role. Please sync job data first from the Jobs page. After syncing, refresh this page.')
      } else {
        setError('Failed to load recommendations. Please try again.')
      }
    } finally {
      setLoading(false)
    }
  }

  const getPriorityColor = (priority) => {
    switch (priority) {
      case 'HIGH':
      case 'High':
        return '#EF4444'
      case 'MEDIUM':
      case 'Medium':
        return '#F59E0B'
      case 'LOW':
      case 'Low':
        return '#10B981'
      default:
        return '#6B7280'
    }
  }

  if (loading) {
    return (
      <div className="dashboard-container">
        <div className="loading-spinner">Loading your dashboard...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="dashboard-container">
        <div className="error-card">
          <h2>Welcome to SkillSync!</h2>
          <p>{error}</p>
          <p className="info-text">
            To get started, you need to set your skills and target role in your profile.
          </p>
          <button className="btn-primary" onClick={() => navigate('/profile')}>
            Set Up My Profile
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Welcome back, {user?.name}!</h1>
        <p className="subtitle">Here's your career development overview</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <h3>Current Role</h3>
          <p className="stat-value">{recommendations.currentRole || 'Not Set'}</p>
        </div>

        <div className="stat-card">
          <h3>Target Role</h3>
          <p className="stat-value">{recommendations.targetRole}</p>
        </div>

        <div className="stat-card skill-gap">
          <h3>Skill Gap</h3>
          <div className="progress-circle">
            <svg width="120" height="120">
              <circle
                cx="60"
                cy="60"
                r="50"
                fill="none"
                stroke="#E5E7EB"
                strokeWidth="10"
              />
              <circle
                cx="60"
                cy="60"
                r="50"
                fill="none"
                stroke="#3B82F6"
                strokeWidth="10"
                strokeDasharray={`${(100 - recommendations.skillGapPercentage) * 3.14} 314`}
                transform="rotate(-90 60 60)"
              />
              <text
                x="60"
                y="65"
                textAnchor="middle"
                fontSize="24"
                fontWeight="bold"
                fill="#1F2937"
              >
                {Math.round(100 - recommendations.skillGapPercentage)}%
              </text>
            </svg>
            <p className="stat-label">Match</p>
          </div>
        </div>

        <div className="stat-card alignment">
          <h3>Alignment Score</h3>
          <div className="score-bar">
            <div
              className="score-fill"
              style={{ width: `${recommendations.alignmentScore}%` }}
            ></div>
          </div>
          <p className="stat-value">{Math.round(recommendations.alignmentScore)}%</p>
        </div>
      </div>

      <div className="recommendations-section">
        <div className="section-header">
          <h2>Recommended Skills to Learn</h2>
          <button className="btn-secondary" onClick={() => navigate('/ai-chat')}>
            Get AI Advice
          </button>
        </div>

        <div className="recommendations-grid">
          {recommendations.recommendations.slice(0, 6).map((rec, index) => (
            <div key={index} className="recommendation-card">
              <div className="rec-header">
                <h3>{rec.skillName}</h3>
                <span
                  className="priority-badge"
                  style={{ backgroundColor: getPriorityColor(rec.priority) }}
                >
                  {rec.priority}
                </span>
              </div>
              <div className="rec-details">
                <div className="detail-row">
                  <span className="label">Demand Score:</span>
                  <span className="value">{rec.demandScore}/100</span>
                </div>
                <div className="detail-row">
                  <span className="label">Learning Time:</span>
                  <span className="value">{rec.estimatedLearningTime}</span>
                </div>
              </div>
            </div>
          ))}
        </div>

        {recommendations.recommendations.length > 6 && (
          <p className="more-skills">
            + {recommendations.recommendations.length - 6} more skills to explore
          </p>
        )}
      </div>

      <div className="cta-section">
        <h3>Ready to accelerate your learning?</h3>
        <div className="cta-buttons">
          <button className="btn-primary" onClick={() => navigate('/ai-chat')}>
            Chat with AI Career Advisor
          </button>
          <button className="btn-secondary" onClick={() => navigate('/jobs')}>
            Explore Job Market
          </button>
        </div>
      </div>
    </div>
  )
}

export default Dashboard
