import { useState, useEffect } from 'react'
import jobService from '../services/jobService'

const JobSearch = () => {
  const [roles, setRoles] = useState([])
  const [selectedRole, setSelectedRole] = useState(null)
  const [skills, setSkills] = useState([])
  const [loading, setLoading] = useState(true)
  const [skillsLoading, setSkillsLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    loadRoles()
  }, [])

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
        <p className="subtitle">Explore in-demand roles and required skills</p>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="jobs-content">
        <div className="roles-sidebar">
          <h2>Available Roles</h2>
          <div className="roles-list">
            {roles.map((role, index) => (
              <button
                key={index}
                className={`role-item ${selectedRole === role ? 'active' : ''}`}
                onClick={() => handleRoleClick(role)}
              >
                {role}
              </button>
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
              <div className="skills-header">
                <h2>{selectedRole}</h2>
                <p>{skills.length} in-demand skills identified</p>
              </div>

              <div className="skills-grid">
                {skills.map((skill, index) => (
                  <div key={index} className="skill-card">
                    <div className="skill-header">
                      <h3>{skill.skill}</h3>
                      <span
                        className="demand-badge"
                        style={{ backgroundColor: getDemandColor(skill.demandScore) }}
                      >
                        {getDemandLabel(skill.demandScore)}
                      </span>
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

                      {skill.frequency && (
                        <div className="detail-item">
                          <span className="label">Frequency in Jobs</span>
                          <span className="value">{skill.frequency.toFixed(1)}%</span>
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

export default JobSearch
