import client from './client'

export const getHoldings = () =>
  client.get('/api/holdings')

export const getPnl = () =>
  client.get('/api/holdings/pnl')
