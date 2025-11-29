import { useState, useEffect } from 'react'
import ChatConversationList from './ChatConversationList'
import ChatWindow from './ChatWindow'
import ChatPreferences from './ChatPreferences'
import aiService from '../services/aiService'
import '../styles/AIChat.css'

const AIChat = () => {
  const [selectedConversation, setSelectedConversation] = useState(null)
  const [showPreferences, setShowPreferences] = useState(false)
  const [quota, setQuota] = useState(null)
  const [quotaLoading, setQuotaLoading] = useState(true)

  // Load user's quota on mount
  useEffect(() => {
    loadQuota()
  }, [])

  const loadQuota = async () => {
    try {
      setQuotaLoading(true)
      const data = await aiService.getConversationQuota()
      setQuota(data)
    } catch (err) {
      console.error('Error loading quota:', err)
    } finally {
      setQuotaLoading(false)
    }
  }

  const handleNewChat = (conversationId) => {
    setSelectedConversation(conversationId)
  }

  const handleCloseChat = () => {
    setSelectedConversation(null)
  }

  return (
    <div className="ai-chat-layout">
      <div className="chat-sidebar">
        <ChatConversationList
          selectedId={selectedConversation}
          onSelectConversation={setSelectedConversation}
          onNewChat={handleNewChat}
        />

        {/* Quota Display */}
        {!quotaLoading && quota && (
          <div className="quota-info">
            <h4>Conversations</h4>
            <p className="quota-text">
              {quota.active_count} / {quota.limit}
            </p>
            <div className="quota-bar">
              <div
                className={`quota-fill ${
                  quota.active_count >= quota.limit * 0.8 ? 'warning' : ''
                }`}
                style={{
                  width: `${Math.min((quota.active_count / quota.limit) * 100, 100)}%`
                }}
              ></div>
            </div>
            <p className="quota-plan">{quota.plan_type} Plan</p>
          </div>
        )}

        {/* Settings Button */}
        <button
          className="btn-settings"
          onClick={() => setShowPreferences(!showPreferences)}
          title="AI Preferences"
        >
          Settings
        </button>
      </div>

      <div className="chat-main">
        {showPreferences ? (
          <ChatPreferences onClose={() => setShowPreferences(false)} />
        ) : (
          <ChatWindow
            conversationId={selectedConversation}
            onClose={handleCloseChat}
          />
        )}
      </div>
    </div>
  )
}

export default AIChat
