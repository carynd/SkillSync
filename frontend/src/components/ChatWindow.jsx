import { useState, useEffect, useRef } from 'react'
import aiService from '../services/aiService'
import '../styles/ChatWindow.css'

const ChatWindow = ({ conversationId, onClose }) => {
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [isTyping, setIsTyping] = useState(false)
  const [pageIndex, setPageIndex] = useState(0)
  const messagesEndRef = useRef(null)

  // Load messages when conversation changes
  useEffect(() => {
    if (conversationId) {
      loadMessages()
    }
  }, [conversationId])

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  const loadMessages = async () => {
    try {
      setLoading(true)
      const response = await aiService.getConversationMessages(conversationId, pageIndex, 20)
      setMessages(response.messages || [])
      setError(null)
    } catch (err) {
      console.error('Error loading messages:', err)
      setError('Failed to load conversation')
      setMessages([])
    } finally {
      setLoading(false)
    }
  }

  const handleSendMessage = async (e) => {
    e.preventDefault()

    if (!input.trim()) {
      return
    }

    const userMessage = input.trim()
    setInput('')

    try {
      // Add user message to UI immediately
      const newUserMessage = {
        id: `temp-${Date.now()}`,
        message: userMessage,
        role: 'USER',
        created_at: new Date().toISOString(),
        is_cached: false,
        token_count: 0,
        finish_reason: 'USER_INPUT'
      }

      setMessages(prev => [...prev, newUserMessage])
      setIsTyping(true)
      setError(null)

      // Send message and get response
      const response = await aiService.sendMessage(conversationId, userMessage)

      // Add AI response to messages
      if (response && response.assistant_message) {
        setMessages(prev => [...prev, {
          id: response.assistant_message.message_id,
          message: response.assistant_message.content,
          role: 'ASSISTANT',
          created_at: response.assistant_message.created_at,
          is_cached: response.is_cached || false,
          token_count: response.assistant_message.token_count || 0,
          finish_reason: response.finish_reason || 'STOP'
        }])
      }
    } catch (err) {
      console.error('Error sending message:', err)
      setError('Failed to send message')
    } finally {
      setIsTyping(false)
    }
  }

  const formatTime = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  const formatMessage = (text) => {
    // Convert markdown-style formatting to basic HTML
    return text
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.+?)\*/g, '<em>$1</em>')
      .replace(/\n/g, '<br/>')
  }

  if (!conversationId) {
    return (
      <div className="chat-window empty-chat">
        <div className="empty-state">
          <p>Select a conversation to start chatting</p>
        </div>
      </div>
    )
  }

  return (
    <div className="chat-window">
      <div className="chat-header">
        <button className="btn-close" onClick={onClose}>← Back</button>
        <h3>Chat</h3>
        <div className="header-spacer"></div>
      </div>

      <div className="chat-messages">
        {loading ? (
          <div className="loading">Loading conversation...</div>
        ) : error ? (
          <div className="error-message">
            {error}
            <button onClick={loadMessages} className="btn-retry">Retry</button>
          </div>
        ) : messages.length === 0 ? (
          <div className="empty-state">
            <p>Start a new conversation</p>
          </div>
        ) : (
          messages.map(msg => (
            <div
              key={msg.id}
              className={`message ${msg.role.toLowerCase()}`}
            >
              <div className="message-content">
                <div className="message-text">
                  {msg.role === 'USER' ? (
                    msg.message
                  ) : (
                    <div dangerouslySetInnerHTML={{ __html: formatMessage(msg.message) }} />
                  )}
                </div>
                <div className="message-meta">
                  <span className="time">{formatTime(msg.created_at)}</span>
                  {msg.is_cached && (
                    <span className="cached-badge">cached</span>
                  )}
                  {msg.token_count > 0 && (
                    <span className="token-count">{msg.token_count} tokens</span>
                  )}
                  {msg.finish_reason && msg.finish_reason !== 'STOP' && msg.role === 'ASSISTANT' && (
                    <span className="finish-reason">{msg.finish_reason}</span>
                  )}
                </div>
              </div>
            </div>
          ))
        )}

        {isTyping && (
          <div className="message assistant">
            <div className="message-content">
              <div className="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      <form className="chat-input-form" onSubmit={handleSendMessage}>
        <input
          type="text"
          className="chat-input"
          placeholder="Type your message..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          disabled={isTyping}
        />
        <button
          type="submit"
          className="btn-send"
          disabled={!input.trim() || isTyping}
        >
          Send
        </button>
      </form>
    </div>
  )
}

export default ChatWindow
