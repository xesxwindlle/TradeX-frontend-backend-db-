import client from './client'

export const getUsers = () =>
  client.get('/api/admin/users')

export const resetUserPassword = (accountNumber) =>
  client.post(`/api/admin/users/${accountNumber}/reset`)

export const deleteUser = (accountNumber) =>
  client.delete(`/api/admin/users/${accountNumber}`)

export const getAdminOrders = (from, to) =>
  client.get('/api/admin/orders', { params: { from, to } })

export const getAdminInstruments = (limit) =>
  client.get('/api/admin/instruments', { params: { limit } })

export const addSymbol = (symbol) =>
  client.post('/api/admin/symbols', { symbol })

export const getMostTraded = (limit) =>
  client.get('/api/admin/stats/most-traded', { params: { limit } })

export const getMostHeld = (limit) =>
  client.get('/api/admin/stats/most-held', { params: { limit } })

export const getMostWatched = (limit) =>
  client.get('/api/admin/stats/most-watched', { params: { limit } })
