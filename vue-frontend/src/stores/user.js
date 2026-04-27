import { defineStore } from 'pinia'
import { api } from '../api/client'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('canteen_token') || '',
    user: JSON.parse(localStorage.getItem('canteen_user') || 'null'),
  }),
  getters: {
    isAdmin: (s) => s.user && s.user.role >= 1,
  },
  actions: {
    setSession(token, user) {
      this.token = token || ''
      this.user = user || null
      if (token) localStorage.setItem('canteen_token', token)
      if (user) localStorage.setItem('canteen_user', JSON.stringify(user))
    },
    clearSession() {
      this.token = ''
      this.user = null
      localStorage.removeItem('canteen_token')
      localStorage.removeItem('canteen_user')
    },
    async refreshMe() {
      if (!this.token) return
      const r = await api.me()
      if (r.code === 0) this.setSession(this.token, r.data)
      else this.clearSession()
    },
  },
})
