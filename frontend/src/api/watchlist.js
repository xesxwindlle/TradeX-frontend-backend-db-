import client from './client'

export const getWatchlist = () =>
  client.get('/api/watchlist')

export const addToWatchlist = (symbol) =>
  client.post('/api/watchlist', { symbol })

export const removeFromWatchlist = (symbol) =>
  client.delete('/api/watchlist', { data: { symbol } })
