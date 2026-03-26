import client from './client'

export const login = (email, password) =>
  client.post('/api/auth/login', { email, password })

export const sendSignupCode = (email) =>
  client.post('/api/auth/signup/send-code', { email })

export const signup = (data) =>
  client.post('/api/auth/signup', data)

export const requestPasswordReset = (email) =>
  client.post('/api/auth/password/reset/request', { email })

export const confirmPasswordReset = (email, code, newPassword) =>
  client.post('/api/auth/password/reset/confirm', { email, code, newPassword })
