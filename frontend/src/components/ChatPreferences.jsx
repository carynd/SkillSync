import { useState, useEffect } from 'react'
import aiService from '../services/aiService'
import '../styles/ChatPreferences.css'

const ChatPreferences = ({ onClose }) => {
  const [preferences, setPreferences] = useState({
    context_level: 'BALANCED',
    response_style: 'BALANCED',
    tone: 'MENTORING',
    enable_caching: true,
    enable_quota_tracking: true
  })
  const [presets, setPresets] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    loadPreferences()
    loadPresets()
  }, [])

  const loadPreferences = async () => {
    try {
      setLoading(true)
      const data = await aiService.getUserPreferences()
      setPreferences(data || {
        context_level: 'BALANCED',
        response_style: 'BALANCED',
        tone: 'MENTORING',
        enable_caching: true,
        enable_quota_tracking: true
      })
      setError(null)
    } catch (err) {
      console.error('Error loading preferences:', err)
      setError('Failed to load preferences')
    } finally {
      setLoading(false)
    }
  }

  const loadPresets = async () => {
    try {
      const data = await aiService.getAvailablePresets()
      setPresets(data || [])
    } catch (err) {
      console.error('Error loading presets:', err)
    }
  }

  const handlePreferenceChange = (key, value) => {
    setPreferences(prev => ({
      ...prev,
      [key]: value
    }))
    setSuccess(false)
  }

  const handleSave = async () => {
    try {
      setSaving(true)
      await aiService.savePreferences(preferences)
      setSuccess(true)
      setError(null)
      setTimeout(() => setSuccess(false), 3000)
    } catch (err) {
      console.error('Error saving preferences:', err)
      setError('Failed to save preferences')
    } finally {
      setSaving(false)
    }
  }

  const handleApplyPreset = async (presetName) => {
    try {
      setSaving(true)
      const data = await aiService.applyPreset(presetName)
      setPreferences(data)
      setSuccess(true)
      setError(null)
      setTimeout(() => setSuccess(false), 3000)
    } catch (err) {
      console.error('Error applying preset:', err)
      setError('Failed to apply preset')
    } finally {
      setSaving(false)
    }
  }

  const handleReset = async () => {
    if (window.confirm('Reset preferences to defaults?')) {
      try {
        setSaving(true)
        await aiService.resetPreferences()
        await loadPreferences()
        setSuccess(true)
        setError(null)
        setTimeout(() => setSuccess(false), 3000)
      } catch (err) {
        console.error('Error resetting preferences:', err)
        setError('Failed to reset preferences')
      } finally {
        setSaving(false)
      }
    }
  }

  if (loading) {
    return (
      <div className="chat-preferences">
        <div className="loading">Loading preferences...</div>
      </div>
    )
  }

  return (
    <div className="chat-preferences">
      <div className="preferences-header">
        <h2>AI Preferences</h2>
        <button className="btn-close" onClick={onClose}>✕</button>
      </div>

      <div className="preferences-content">
        {error && (
          <div className="error-message">
            {error}
            <button onClick={loadPreferences} className="btn-retry">Retry</button>
          </div>
        )}

        {success && (
          <div className="success-message">
            Preferences saved successfully!
          </div>
        )}

        {/* Presets Section */}
        <div className="preferences-section">
          <h3>Quick Presets</h3>
          <p className="section-description">Apply a preset to quickly configure your preferences</p>
          <div className="preset-buttons">
            {presets.map(preset => (
              <button
                key={preset}
                className="btn-preset"
                onClick={() => handleApplyPreset(preset)}
                disabled={saving}
              >
                {preset}
              </button>
            ))}
          </div>
        </div>

        {/* Context Level */}
        <div className="preferences-section">
          <label htmlFor="context_level">Context Level</label>
          <select
            id="context_level"
            value={preferences.context_level}
            onChange={(e) => handlePreferenceChange('context_level', e.target.value)}
          >
            <option value="MINIMAL">Minimal - Basic conversation history</option>
            <option value="BALANCED">Balanced - History + your skills</option>
            <option value="DEEP">Deep - Full profile context</option>
          </select>
          <p className="option-description">
            How much context the AI uses when providing advice
          </p>
        </div>

        {/* Response Style */}
        <div className="preferences-section">
          <label htmlFor="response_style">Response Style</label>
          <select
            id="response_style"
            value={preferences.response_style}
            onChange={(e) => handlePreferenceChange('response_style', e.target.value)}
          >
            <option value="CONCISE">Concise - Short, direct answers</option>
            <option value="BALANCED">Balanced - Moderate detail</option>
            <option value="DETAILED">Detailed - Comprehensive explanations</option>
          </select>
          <p className="option-description">
            How detailed the AI responses should be
          </p>
        </div>

        {/* Tone */}
        <div className="preferences-section">
          <label htmlFor="tone">Tone</label>
          <select
            id="tone"
            value={preferences.tone}
            onChange={(e) => handlePreferenceChange('tone', e.target.value)}
          >
            <option value="PROFESSIONAL">Professional - Formal and business-like</option>
            <option value="CASUAL">Casual - Friendly and relaxed</option>
            <option value="MENTORING">Mentoring - Supportive and educational</option>
          </select>
          <p className="option-description">
            The tone the AI should use in its responses
          </p>
        </div>

        {/* Features Section */}
        <div className="preferences-section">
          <h3>Features</h3>

          <div className="checkbox-group">
            <label>
              <input
                type="checkbox"
                checked={preferences.enable_caching || false}
                onChange={(e) => handlePreferenceChange('enable_caching', e.target.checked)}
              />
              <span>Enable Response Caching</span>
            </label>
            <p className="option-description">
              Cache AI responses to reduce API usage and provide faster replies
            </p>
          </div>

          <div className="checkbox-group">
            <label>
              <input
                type="checkbox"
                checked={preferences.enable_quota_tracking || false}
                onChange={(e) => handlePreferenceChange('enable_quota_tracking', e.target.checked)}
              />
              <span>Track Token Usage</span>
            </label>
            <p className="option-description">
              Monitor your AI service token usage for quota management
            </p>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="preferences-actions">
          <button
            className="btn-save"
            onClick={handleSave}
            disabled={saving}
          >
            {saving ? 'Saving...' : 'Save Preferences'}
          </button>
          <button
            className="btn-reset"
            onClick={handleReset}
            disabled={saving}
          >
            Reset to Defaults
          </button>
        </div>
      </div>
    </div>
  )
}

export default ChatPreferences
