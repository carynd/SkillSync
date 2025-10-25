import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import Select from 'react-select'
import userService from '../services/userService'
import jobService from '../services/jobService'
import api from '../services/api'

const Profile = () => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [availableRoles, setAvailableRoles] = useState([])
  const [availableSkills, setAvailableSkills] = useState([])

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    targetRole: '',
    skills: [],
    experienceLevel: 'BEGINNER'
  })

  const experienceLevels = [
    { value: 'BEGINNER', label: '0-2 years (Beginner)' },
    { value: 'INTERMEDIATE', label: '3-5 years (Intermediate)' },
    { value: 'ADVANCED', label: '6-10 years (Advanced)' },
    { value: 'EXPERT', label: '10+ years (Expert)' }
  ]

  useEffect(() => {
    loadProfileAndOptions()
  }, [])

  const loadProfileAndOptions = async () => {
    try {
      setLoading(true)

      // Load user profile
      const profile = await userService.getUserProfile(user.userId)
      setFormData({
        name: profile.name || '',
        email: profile.email || '',
        targetRole: profile.targetRole || '',
        skills: profile.skills || [],
        experienceLevel: profile.experienceLevel || 'BEGINNER'
      })

      // Load available roles
      const roles = await jobService.getAvailableRoles()
      setAvailableRoles(roles)

      // Load available skills
      const skillsResponse = await api.get('/users/skills')
      setAvailableSkills(skillsResponse.data)

      setError('')
    } catch (err) {
      setError('Failed to load profile. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
    setError('')
    setSuccess('')
  }

  const handleSkillsChange = (selectedOptions) => {
    const selectedSkills = selectedOptions ? selectedOptions.map(option => option.value) : []
    setFormData({
      ...formData,
      skills: selectedSkills
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setSaving(true)

    if (!formData.targetRole) {
      setError('Please select a target role')
      setSaving(false)
      return
    }

    if (formData.skills.length === 0) {
      setError('Please select at least one skill')
      setSaving(false)
      return
    }

    try {
      console.log('Profile Update - Step 1: Updating user profile with:', {
        targetRole: formData.targetRole,
        skills: formData.skills,
        experienceLevel: formData.experienceLevel
      })

      // Step 1: Update user profile
      await userService.updateUserProfile(user.userId, formData)
      setSuccess('Profile updated! Syncing job market data...')
      console.log('Profile Update - Step 1 Complete: User profile updated')

      // Step 2: Sync job data for target role
      try {
        console.log('Profile Update - Step 2: Syncing job data for role:', formData.targetRole)
        const syncResponse = await jobService.syncJobData({
          role: formData.targetRole,
          location: 'Remote',
          maxResults: 100
        })
        console.log('Profile Update - Step 2 Complete: Job data synced, skills count:', syncResponse.skills?.length || 0)
      } catch (syncErr) {
        console.warn('Profile Update - Step 2 Warning: Job sync failed', syncErr.response?.data?.message || syncErr.message)
      }

      setSuccess('Profile updated successfully! Generating recommendations...')

      // Step 3: Generate recommendations
      try {
        console.log('Profile Update - Step 3: Generating recommendations for user:', user.userId)
        const recResponse = await jobService.generateRecommendations(user.userId)
        console.log('Profile Update - Step 3 Complete: Recommendations generated', {
          skillGapPercentage: recResponse.skillGapPercentage,
          alignmentScore: recResponse.alignmentScore
        })
      } catch (recErr) {
        console.warn('Profile Update - Step 3 Warning: Recommendation generation failed', recErr.response?.data?.message || recErr.message)
      }

      console.log('Profile Update - All steps complete, redirecting to dashboard')
      setTimeout(() => {
        navigate('/dashboard')
      }, 2000)
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to update profile. Please try again.'
      console.error('Profile Update - Error:', errorMessage, err)
      setError(errorMessage)
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="profile-container">
        <div className="loading-spinner">Loading your profile...</div>
      </div>
    )
  }

  // Convert skills array to react-select format
  const skillOptions = availableSkills.map(skill => ({ value: skill, label: skill }))
  const selectedSkillOptions = formData.skills.map(skill => ({ value: skill, label: skill }))

  return (
    <div className="profile-container">
      <div className="profile-header">
        <h1>My Profile</h1>
        <p className="subtitle">Set up your career profile to get personalized recommendations</p>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <form onSubmit={handleSubmit} className="profile-form">
        <div className="form-section">
          <h2>Personal Information</h2>

          <div className="form-group">
            <label htmlFor="name">Full Name</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              disabled={saving}
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
              disabled={saving}
            />
          </div>
        </div>

        <div className="form-section">
          <h2>Career Information</h2>

          <div className="form-group">
            <label htmlFor="targetRole">Target Role *</label>
            <select
              id="targetRole"
              name="targetRole"
              value={formData.targetRole}
              onChange={handleChange}
              required
              disabled={saving}
            >
              <option value="">Select your target role...</option>
              {availableRoles.map((role, index) => (
                <option key={index} value={role}>{role}</option>
              ))}
            </select>
            <p className="field-help">What role are you aiming for?</p>
          </div>

          <div className="form-group">
            <label htmlFor="experienceLevel">Experience Level *</label>
            <select
              id="experienceLevel"
              name="experienceLevel"
              value={formData.experienceLevel}
              onChange={handleChange}
              required
              disabled={saving}
            >
              {experienceLevels.map((level) => (
                <option key={level.value} value={level.value}>
                  {level.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="form-section">
          <h2>Current Skills *</h2>
          <p className="section-help">Search and select the skills you currently have</p>

          <div className="form-group">
            <label>Skills</label>
            <Select
              isMulti
              options={skillOptions}
              value={selectedSkillOptions}
              onChange={handleSkillsChange}
              placeholder="Search and select your skills..."
              isDisabled={saving}
              className="react-select-container"
              classNamePrefix="react-select"
            />
            <p className="field-help">
              {formData.skills.length} skill{formData.skills.length !== 1 ? 's' : ''} selected
            </p>
          </div>

          {formData.skills.length === 0 && (
            <p className="no-skills">No skills selected. Please select at least one skill to continue.</p>
          )}
        </div>

        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate('/dashboard')}
            className="btn-secondary"
            disabled={saving}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn-primary"
            disabled={saving}
          >
            {saving ? 'Saving...' : 'Save Profile'}
          </button>
        </div>
      </form>
    </div>
  )
}

export default Profile
