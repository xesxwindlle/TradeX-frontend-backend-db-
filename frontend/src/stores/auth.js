import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('tx_token'))
  const user = ref(JSON.parse(localStorage.getItem('tx_user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'Admin')

  function login(data) {
    token.value = data.token
    user.value = data
    localStorage.setItem('tx_token', data.token)
    localStorage.setItem('tx_user', JSON.stringify(data))
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('tx_token')
    localStorage.removeItem('tx_user')
  }

  return { token, user, isLoggedIn, isAdmin, login, logout }
})
