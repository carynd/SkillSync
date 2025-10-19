import api from './api'

const aiService = {
  async getCareerAdvice(userId, question) {
    const response = await api.post(`/ai/advice/${userId}`, { question })
    return response.data
  }
}

export default aiService
