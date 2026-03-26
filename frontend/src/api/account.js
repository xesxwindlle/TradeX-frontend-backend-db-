import client from './client'

export const getAccount = () =>
  client.get('/api/account')

export const addFunds = (amount) =>
  client.post('/api/account/funds', { amount })

export const changePassword = (currentPassword, newPassword) =>
  client.patch('/api/account/password', { currentPassword, newPassword })

export const sendMobileCode = (newMobile) =>
  client.post('/api/account/mobile/send-code', { newMobile })

export const changeMobile = (currentMobile, newMobile, verificationCode) =>
  client.patch('/api/account/mobile', { currentMobile, newMobile, verificationCode })

export const resetAccount = () =>
  client.post('/api/account/reset')

export const deleteAccount = () =>
  client.delete('/api/account')
