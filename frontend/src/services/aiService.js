import api from './api'

const aiService = {
  // Conversations
  async createConversation(title = 'New Chat') {
    const response = await api.post('/chat/conversations', { title })
    return response.data
  },

  async getUserConversations(page = 0, size = 10) {
    const response = await api.get('/chat/conversations', {
      params: { page, size }
    })
    return response.data
  },

  async getRecentConversations() {
    const response = await api.get('/chat/conversations/recent')
    return response.data
  },

  async getConversation(conversationId) {
    const response = await api.get(`/chat/conversations/${conversationId}`)
    return response.data
  },

  async updateConversationTitle(conversationId, title) {
    const response = await api.patch(`/chat/conversations/${conversationId}`, { title })
    return response.data
  },

  async archiveConversation(conversationId) {
    const response = await api.post(`/chat/conversations/${conversationId}/archive`)
    return response.data
  },

  async deleteConversation(conversationId) {
    const response = await api.delete(`/chat/conversations/${conversationId}`)
    return response.data
  },

  // Messages
  async sendMessage(conversationId, content) {
    const response = await api.post(
      `/chat/conversations/${conversationId}/messages`,
      { content }
    )
    return response.data
  },

  async getConversationMessages(conversationId, page = 0, size = 20) {
    const response = await api.get(
      `/chat/conversations/${conversationId}/messages`,
      { params: { page, size } }
    )
    return response.data
  },

  // Preferences
  async getUserPreferences() {
    const response = await api.get('/preferences')
    return response.data
  },

  async savePreferences(preferences) {
    const response = await api.post('/preferences', preferences)
    return response.data
  },

  async applyPreset(presetName) {
    const response = await api.post(`/preferences/preset/${presetName}`)
    return response.data
  },

  async getAvailablePresets() {
    const response = await api.get('/preferences/presets')
    return response.data
  },

  async resetPreferences() {
    const response = await api.post('/preferences/reset')
    return response.data
  },

  // Quota
  async getConversationQuota() {
    const response = await api.get('/chat/quota')
    return response.data
  },

  // Backward compatibility - legacy method
  async getCareerAdvice(userId, question) {
    try {
      // Create or get recent conversation
      const conversations = await this.getRecentConversations()
      let conversationId

      if (conversations && conversations.length > 0) {
        conversationId = conversations[0].conversation_id
      } else {
        const newConv = await this.createConversation('Career Chat')
        conversationId = newConv.conversation_id
      }

      // Send message and get AI response
      const response = await this.sendMessage(conversationId, question)
      return {
        advice: response.assistant_message?.content,
        advice_text: response.assistant_message?.content,
        success: response.success,
        is_cached: response.is_cached
      }
    } catch (error) {
      console.error('Error getting career advice:', error)
      throw error
    }
  }
}

export default aiService
