import api from './api'

const jobService = {
  async getAvailableRoles() {
    const response = await api.get('/jobs/roles')
    return response.data
  },

  async syncJobData(jobSyncRequest) {
    const response = await api.post('/jobs/sync', jobSyncRequest)
    return response.data
  },

  async getSkillsForRole(role) {
    const response = await api.get(`/jobs/${encodeURIComponent(role)}/skills`)
    return response.data
  },

  async getRecommendations(userId) {
    const response = await api.get(`/recommendations/${userId}`)
    return response.data
  },

  async generateRecommendations(userId) {
    const response = await api.post(`/recommendations/generate/${userId}`)
    return response.data
  }
}

export default jobService
