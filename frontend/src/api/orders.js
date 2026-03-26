import client from './client'

export const getOrders = () =>
  client.get('/api/orders')

export const placeOrder = (data) =>
  client.post('/api/orders', data)
