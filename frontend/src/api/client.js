import axios from 'axios'

const client = axios.create({ baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080/TradeX' })

client.interceptors.request.use(config => {
  const token = localStorage.getItem('tx_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

client.interceptors.response.use(
  r => r,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('tx_token')
      localStorage.removeItem('tx_user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default client
