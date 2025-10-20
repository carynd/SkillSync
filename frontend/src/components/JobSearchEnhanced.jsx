import { useState, useEffect } from 'react'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { Bar, Doughnut } from 'react-chartjs-2'
import jobService from '../services/jobService'
import { useAuth } from '../context/AuthContext'

// Register ChartJS components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
)

const JobSearchEnhanced = () => {
  const { user } = useAuth()
  const [roles, setRoles] = useState([])
  const [selectedRole, setSelectedRole] = useState(null)
  const [compareRole, setCompareRole] = useState(null)
  const [skills, setSkills] = useState([])
  const [compareSkills, setCompareSkills] = useState([])
  const [loading, setLoading] = useState(true)
  const [skillsLoading, setSkillsLoading] = useState(false)
  const [compareLoading, setCompareLoading] = useState(false)
  const [error, setError] = useState('')
  const [viewMode, setViewMode] = useState('grid') // 'grid', 'chart', 'compare'
  const [userSkills, setUserSkills] = useState([])

  useEffect(() => {
    loadRoles()
    if (user) {
      // Get user's current skills for matching
      setUserSkills(user.skills || [])
    }
  }, [user])

  const loadRoles = async () => {
    try {
      setLoading(true)
      const data = await jobService.getAvailableRoles()
      setRoles(data)
      setError('')
    } catch (err) {
      setError('Failed to load job roles. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleRoleClick = async (role) => {
    setSelectedRole(role)
    setSkillsLoading(true)

    try {
      const skillsData = await jobService.getSkillsForRole(role)
      setSkills(skillsData)
      setError('')
    } catch (err) {
      setError('Failed to load skills for this role.')
      setSkills([])
    } finally {
      setSkillsLoading(false)
    }
  }

  const handleCompareRole = async (role) => {
    if (role === compareRole) {
      setCompareRole(null)
      setCompareSkills([])
      return
    }

    setCompareRole(role)
    setCompareLoading(true)

    try {
      const skillsData = await jobService.getSkillsForRole(role)
      setCompareSkills(skillsData)
      setError('')
    } catch (err) {
      setError('Failed to load skills for comparison.')
      setCompareSkills([])
    } finally {
      setCompareLoading(false)
    }
  }

  const getDemandColor = (demandScore) => {
    if (demandScore >= 80) return '#10B981'
    if (demandScore >= 60) return '#F59E0B'
    return '#EF4444'
  }

  const getDemandLabel = (demandScore) => {
    if (demandScore >= 80) return 'HIGH'
    if (demandScore >= 60) return 'MEDIUM'
    return 'LOW'
  }

  const calculateSkillMatch = (skill) => {
    return userSkills.some(userSkill =>
      userSkill.toLowerCase() === skill.skill.toLowerCase()
    )
  }

  const getSkillMatchPercentage = (roleSkills) => {
    if (!userSkills.length || !roleSkills.length) return 0
    const matchCount = roleSkills.filter(skill => calculateSkillMatch(skill)).length
    return Math.round((matchCount / roleSkills.length) * 100)
  }

  // Prepare data for bar chart
  const getBarChartData = () => {
    if (!skills.length) return null

    const topSkills = skills.slice(0, 10)

    return {
      labels: topSkills.map(s => s.skill),
      datasets: [
        {
          label: 'Demand Score',
          data: topSkills.map(s => s.demandScore),
          backgroundColor: topSkills.map(s => getDemandColor(s.demandScore)),
          borderColor: topSkills.map(s => getDemandColor(s.demandScore)),
          borderWidth: 1
        }
      ]
    }
  }

  // Prepare data for pie chart (categories)
  const getCategoryChartData = () => {
    if (!skills.length) return null

    const categoryCount = {}
    skills.forEach(skill => {
      const cat = skill.category || 'Other'
      categoryCount[cat] = (categoryCount[cat] || 0) + 1
    })

    const colors = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899']

    return {
      labels: Object.keys(categoryCount),
      datasets: [
        {
          data: Object.values(categoryCount),
          backgroundColor: colors,
          borderColor: '#fff',
          borderWidth: 2
        }
      ]
    }
  }

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false
      },
      title: {
        display: true,
        text: 'Top Skills Demand Score',
        color: '#fff',
        font: {
          size: 16
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        max: 100,
        ticks: {
          color: '#9CA3AF'
        },
        grid: {
          color: 'rgba(75, 85, 99, 0.2)'
        }
      },
      x: {
        ticks: {
          color: '#9CA3AF'
        },
        grid: {
          color: 'rgba(75, 85, 99, 0.2)'
        }
      }
    }
  }

  const doughnutOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'right',
        labels: {
          color: '#fff',
          padding: 15
        }
      },
      title: {
        display: true,
        text: 'Skills by Category',
        color: '#fff',
        font: {
          size: 16
        }
      }
    }
  }

  if (loading) {
    return (
      <div className="jobs-container">
        <div className="loading-spinner">Loading job market data...</div>
      </div>
    )
  }

  return (
    <div className="jobs-container">
      <div className="jobs-header">
        <h1>Job Market Intelligence</h1>
        <p className="subtitle">Explore in-demand roles and required skills with visual insights</p>
      </div>

      {error && <div className="error-message">{error}</div>}

      {/* View Mode Tabs */}
      <div className="view-mode-tabs">
        <button
          className={`tab-button ${viewMode === 'grid' ? 'active' : ''}`}
          onClick={() => setViewMode('grid')}
        >
          Grid View
        </button>
        <button
          className={`tab-button ${viewMode === 'chart' ? 'active' : ''}`}
          onClick={() => setViewMode('chart')}
          disabled={!selectedRole}
        >
          Chart View
        </button>
        <button
          className={`tab-button ${viewMode === 'compare' ? 'active' : ''}`}
          onClick={() => setViewMode('compare')}
          disabled={!selectedRole}
        >
          Compare Roles
        </button>
      </div>

      <div className="jobs-content">
        <div className="roles-sidebar">
          <h2>Available Roles</h2>
          <div className="roles-list">
            {roles.map((role, index) => (
              <div key={index} className="role-item-wrapper">
                <button
                  className={`role-item ${selectedRole === role ? 'active' : ''}`}
                  onClick={() => handleRoleClick(role)}
                >
                  <span className="role-name">{role}</span>
                  {selectedRole === role && skills.length > 0 && userSkills.length > 0 && (
                    <span className="match-badge">
                      {getSkillMatchPercentage(skills)}% Match
                    </span>
                  )}
                </button>
                {viewMode === 'compare' && selectedRole && selectedRole !== role && (
                  <button
                    className={`compare-btn ${compareRole === role ? 'active' : ''}`}
                    onClick={() => handleCompareRole(role)}
                    title="Compare with selected role"
                  >
                    {compareRole === role ? '✓' : '+'}
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>

        <div className="skills-main">
          {!selectedRole ? (
            <div className="placeholder">
              <h2>Select a role to view required skills</h2>
              <p>Choose a job role from the list to see the most in-demand skills</p>
            </div>
          ) : skillsLoading ? (
            <div className="loading-spinner">Loading skills...</div>
          ) : (
            <>
              {/* Grid View */}
              {viewMode === 'grid' && (
                <>
                  <div className="skills-header">
                    <h2>{selectedRole}</h2>
                    <p>{skills.length} in-demand skills identified</p>
                  </div>

                  <div className="skills-grid">
                    {skills.map((skill, index) => (
                      <div key={index} className={`skill-card ${calculateSkillMatch(skill) ? 'matched' : ''}`}>
                        <div className="skill-header">
                          <h3>{skill.skill}</h3>
                          <div className="badges">
                            {calculateSkillMatch(skill) && (
                              <span className="match-indicator">✓ You have this</span>
                            )}
                            <span
                              className="demand-badge"
                              style={{ backgroundColor: getDemandColor(skill.demandScore) }}
                            >
                              {getDemandLabel(skill.demandScore)}
                            </span>
                          </div>
                        </div>

                        <div className="skill-details">
                          <div className="detail-item">
                            <span className="label">Demand Score</span>
                            <div className="score-bar">
                              <div
                                className="score-fill"
                                style={{
                                  width: `${skill.demandScore}%`,
                                  backgroundColor: getDemandColor(skill.demandScore)
                                }}
                              ></div>
                            </div>
                            <span className="value">{skill.demandScore}/100</span>
                          </div>

                          {skill.category && (
                            <div className="detail-item">
                              <span className="label">Category</span>
                              <span className="value">{skill.category}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </>
              )}

              {/* Chart View */}
              {viewMode === 'chart' && (
                <>
                  <div className="skills-header">
                    <h2>{selectedRole} - Visual Analysis</h2>
                  </div>

                  <div className="charts-container">
                    <div className="chart-box">
                      {getBarChartData() && (
                        <Bar data={getBarChartData()} options={chartOptions} />
                      )}
                    </div>
                    <div className="chart-box doughnut">
                      {getCategoryChartData() && (
                        <Doughnut data={getCategoryChartData()} options={doughnutOptions} />
                      )}
                    </div>
                  </div>

                  {userSkills.length > 0 && (
                    <div className="match-summary">
                      <h3>Your Skill Match</h3>
                      <div className="match-progress">
                        <div className="progress-bar">
                          <div
                            className="progress-fill"
                            style={{ width: `${getSkillMatchPercentage(skills)}%` }}
                          ></div>
                        </div>
                        <span className="match-percentage">{getSkillMatchPercentage(skills)}%</span>
                      </div>
                      <p>You have {skills.filter(s => calculateSkillMatch(s)).length} out of {skills.length} required skills</p>
                    </div>
                  )}
                </>
              )}

              {/* Compare View */}
              {viewMode === 'compare' && (
                <>
                  <div className="skills-header">
                    <h2>Role Comparison</h2>
                    <p>Compare {selectedRole} {compareRole ? `with ${compareRole}` : '(select another role to compare)'}</p>
                  </div>

                  {!compareRole ? (
                    <div className="placeholder">
                      <p>Click the + button next to another role to compare</p>
                    </div>
                  ) : compareLoading ? (
                    <div className="loading-spinner">Loading comparison...</div>
                  ) : (
                    <div className="comparison-grid">
                      <div className="comparison-column">
                        <h3>{selectedRole}</h3>
                        <div className="skills-list">
                          {skills.slice(0, 15).map((skill, index) => (
                            <div key={index} className="comparison-skill">
                              <span className="skill-name">{skill.skill}</span>
                              <span className="skill-score">{skill.demandScore}</span>
                            </div>
                          ))}
                        </div>
                      </div>

                      <div className="comparison-column">
                        <h3>{compareRole}</h3>
                        <div className="skills-list">
                          {compareSkills.slice(0, 15).map((skill, index) => (
                            <div key={index} className="comparison-skill">
                              <span className="skill-name">{skill.skill}</span>
                              <span className="skill-score">{skill.demandScore}</span>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  )}
                </>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  )
}

export default JobSearchEnhanced
