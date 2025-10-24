import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import aiService from '../services/aiService'

const AIChat = () => {
  const { user } = useAuth()
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      content: 'Hello! I\'m your AI career advisor. Ask me anything about your career development, skill learning, or job market trends!'
    }
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!input.trim() || loading) return

    const userMessage = input.trim()
    setInput('')

    setMessages(prev => [...prev, { role: 'user', content: userMessage }])
    setLoading(true)

    try {
      const response = await aiService.getCareerAdvice(user.userId, userMessage)

      const assistantMessage = {
        role: 'assistant',
        content: response.advice || response.advice_text || 'I understand your question. Based on your profile, here\'s my recommendation...',
        reasoning: response.reasoning || response.explanation,
        actionItems: response.action_items || response.recommendations,
        timeline: response.estimated_timeline || response.timeline,
        confidence: response.confidence_score || response.confidence
      }

      setMessages(prev => [...prev, assistantMessage])
    } catch (err) {
      const errorMsg = err.response?.data?.message || err.message || 'Sorry, I encountered an error. Please try again.'

      // More specific error handling
      let userFriendlyError = errorMsg
      if (errorMsg.includes('No recommendations')) {
        userFriendlyError = 'Please set up your profile with your target role and skills first to get personalized advice.'
      } else if (errorMsg.includes('User not found')) {
        userFriendlyError = 'Could not find your profile. Please refresh the page.'
      } else if (errorMsg.includes('timeout') || errorMsg.includes('503')) {
        userFriendlyError = 'AI service is temporarily unavailable. Please try again in a moment.'
      }

      setMessages(prev => [...prev, {
        role: 'error',
        content: userFriendlyError
      }])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="chat-container">
      <div className="chat-header">
        <h1>AI Career Advisor</h1>
        <p className="subtitle">Get personalized career guidance powered by AI</p>
      </div>

      <div className="chat-messages">
        {messages.map((message, index) => (
          <div key={index} className={`message ${message.role}`}>
            {message.role === 'user' ? (
              <div className="message-content user-message">
                <div className="message-avatar">You</div>
                <div className="message-text">{message.content}</div>
              </div>
            ) : message.role === 'error' ? (
              <div className="message-content error-message">
                <div className="message-avatar">Error</div>
                <div className="message-text">{message.content}</div>
              </div>
            ) : (
              <div className="message-content assistant-message">
                <div className="message-avatar">AI</div>
                <div className="message-text">
                  <p className="advice">{message.content}</p>

                  {message.reasoning && (
                    <div className="reasoning-section">
                      <h4>Reasoning:</h4>
                      <p>{message.reasoning}</p>
                    </div>
                  )}

                  {message.actionItems && message.actionItems.length > 0 && (
                    <div className="action-items">
                      <h4>Action Items:</h4>
                      <ul>
                        {message.actionItems.map((item, i) => (
                          <li key={i}>{item}</li>
                        ))}
                      </ul>
                    </div>
                  )}

                  {message.timeline && (
                    <div className="timeline">
                      <strong>Timeline:</strong> {message.timeline}
                    </div>
                  )}

                  {message.confidence && (
                    <div className="confidence">
                      <strong>Confidence:</strong> {Math.round(message.confidence * 100)}%
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        ))}

        {loading && (
          <div className="message assistant">
            <div className="message-content assistant-message">
              <div className="message-avatar">AI</div>
              <div className="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        )}
      </div>

      <form onSubmit={handleSubmit} className="chat-input-form">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask me about your career development..."
          disabled={loading}
          className="chat-input"
        />
        <button type="submit" disabled={loading || !input.trim()} className="btn-send">
          Send
        </button>
      </form>

      <div className="chat-suggestions">
        <p>Suggested questions:</p>
        <div className="suggestion-chips">
          <button
            onClick={() => setInput("What should I focus on learning next?")}
            className="chip"
            disabled={loading}
          >
            What should I learn next?
          </button>
          <button
            onClick={() => setInput("How can I prepare for my target role?")}
            className="chip"
            disabled={loading}
          >
            How to prepare for my target role?
          </button>
          <button
            onClick={() => setInput("What's the fastest path to advance my career?")}
            className="chip"
            disabled={loading}
          >
            Fastest career advancement?
          </button>
        </div>
      </div>
    </div>
  )
}

export default AIChat
