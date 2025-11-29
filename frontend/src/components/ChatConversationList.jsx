import { useState, useEffect } from 'react'
import aiService from '../services/aiService'
import '../styles/ChatConversationList.css'

const ChatConversationList = ({ selectedId, onSelectConversation, onNewChat }) => {
  const [conversations, setConversations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    loadConversations()
  }, [])

  const loadConversations = async () => {
    try {
      setLoading(true)
      const data = await aiService.getRecentConversations()
      setConversations(data || [])
      setError(null)
    } catch (err) {
      console.error('Error loading conversations:', err)
      setError('Failed to load conversations')
      setConversations([])
    } finally {
      setLoading(false)
    }
  }

  const handleNewChat = async () => {
    try {
      const newConversation = await aiService.createConversation('New Chat')
      setConversations(prev => [newConversation, ...prev])
      onNewChat(newConversation.conversation_id)
    } catch (err) {
      console.error('Error creating conversation:', err)
      setError('Failed to create conversation')
    }
  }

  const handleDeleteConversation = async (e, conversationId) => {
    e.stopPropagation()
    try {
      await aiService.deleteConversation(conversationId)
      setConversations(prev => prev.filter(c => c.conversation_id !== conversationId))
      if (selectedId === conversationId) {
        onSelectConversation(null)
      }
    } catch (err) {
      console.error('Error deleting conversation:', err)
      setError('Failed to delete conversation')
    }
  }

  const formatDate = (dateString) => {
    const date = new Date(dateString)
    const today = new Date()
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)

    if (date.toDateString() === today.toDateString()) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    } else if (date.toDateString() === yesterday.toDateString()) {
      return 'Yesterday'
    } else {
      return date.toLocaleDateString([], { month: 'short', day: 'numeric' })
    }
  }

  return (
    <div className="conversation-list">
      <div className="conversation-list-header">
        <h2>Conversations</h2>
        <button
          className="btn-new-chat"
          onClick={handleNewChat}
          title="Start a new conversation"
        >
          + New
        </button>
      </div>

      {error && (
        <div className="error-message">
          {error}
          <button onClick={loadConversations} className="btn-retry">Retry</button>
        </div>
      )}

      <div className="conversation-items">
        {loading ? (
          <div className="loading">Loading conversations...</div>
        ) : conversations.length === 0 ? (
          <div className="empty-state">
            <p>No conversations yet</p>
            <button onClick={handleNewChat} className="btn-start">
              Start a new conversation
            </button>
          </div>
        ) : (
          conversations.map(conv => (
            <div
              key={conv.conversation_id}
              className={`conversation-item ${selectedId === conv.conversation_id ? 'active' : ''}`}
              onClick={() => onSelectConversation(conv.conversation_id)}
            >
              <div className="conversation-info">
                <div className="conversation-title">
                  {conv.title || 'Untitled Conversation'}
                </div>
                <div className="conversation-meta">
                  {conv.message_count > 0 && (
                    <span className="message-count">{conv.message_count} messages</span>
                  )}
                  {conv.last_message_at && (
                    <span className="last-message">
                      {formatDate(conv.last_message_at)}
                    </span>
                  )}
                </div>
              </div>
              <button
                className="btn-delete"
                onClick={(e) => handleDeleteConversation(e, conv.conversation_id)}
                title="Delete conversation"
              >
                ×
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default ChatConversationList
