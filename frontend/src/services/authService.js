import api from './api'

const authService = {
  async register(userData) {
    const response = await api.post('/users/register', userData)
    if (response.data.token) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify({
        userId: response.data.userId,
        email: response.data.email,
        name: response.data.name
      }))
    }
    return response.data
  },

  async login(credentials) {
    const response = await api.post('/users/login', credentials)
    if (response.data.token) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify({
        userId: response.data.userId,
        email: response.data.email,
        name: response.data.name
      }))
    }
    return response.data
  },

  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('user')
    return userStr ? JSON.parse(userStr) : null
  },

  getToken() {
    return localStorage.getItem('token')
  },

  isAuthenticated() {
    return !!this.getToken()
  }
}

export default authService
