import axios from 'axios'

const client = axios.create({ baseURL: '/api', timeout: 15000 })

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('canteen_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

client.interceptors.response.use((res) => {
  if (res.data?.code === 4001) {
    localStorage.removeItem('canteen_token')
    localStorage.removeItem('canteen_user')
  }
  return res.data
}, (error) => {
  const data = error.response?.data
  if (data?.code === 4001) {
    localStorage.removeItem('canteen_token')
    localStorage.removeItem('canteen_user')
  }
  if (data) return Promise.resolve(data)
  return Promise.reject(error)
})

const q = (params = {}) => new URLSearchParams(params).toString()

export const api = {
  register: (body) => client.post('/auth/register', body),
  login: (body) => client.post('/auth/login', body),
  me: () => client.get('/auth/me'),
  logout: () => client.post('/auth/logout'),
  changePassword: (body) => client.put('/auth/password', body),
  updateProfile: (body) => client.put('/users/me/profile', body),
  canteens: () => client.get('/canteens'),
  canteenDetail: (id) => client.get(`/canteens/${id}`),
  categories: () => client.get('/categories'),
  tags: () => client.get('/tags'),
  stalls: (params) => client.get(`/stalls?${q(params)}`),
  stallDetail: (id) => client.get(`/stalls/${id}`),
  stallReviews: (id, params) => client.get(`/stalls/${id}/reviews?${q(params)}`),
  submitReview: (body) => client.post('/reviews', body),
  myReviews: (params) => client.get(`/users/me/reviews?${q(params)}`),
  updateMyReview: (id, body) => client.put(`/users/me/reviews/${id}`, body),
  deleteMyReview: (id) => client.delete(`/users/me/reviews/${id}`),
  scoreRank: (params) => client.get(`/rankings/score?${q(params)}`),
  hotRank: (params) => client.get(`/rankings/hot?${q(params)}`),
  latestRank: (params) => client.get(`/rankings/latest?${q(params)}`),
  favorites: (params) => client.get(`/users/me/favorites?${q(params)}`),
  addFavorite: (body) => client.post('/users/me/favorites', body),
  deleteFavorite: (id) => client.delete(`/users/me/favorites/${id}`),
  blacklist: (params) => client.get(`/users/me/blacklist?${q(params)}`),
  addBlacklist: (body) => client.post('/users/me/blacklist', body),
  deleteBlacklist: (id) => client.delete(`/users/me/blacklist/${id}`),
  history: (params) => client.get(`/users/me/history?${q(params)}`),
  addHistory: (body) => client.post('/users/me/history', body),
  recommendToday: (params) => client.get(`/recommendations/today?${q(params)}`),
  recommendPersonalized: (body) => client.post('/recommendations/personalized', body),
  recommendFeed: (body) => client.post('/recommendations/feed', body),
  adminUsers: () => client.get('/admin/users'),
  adminUpdateUserRole: (id, body) => client.put(`/admin/users/${id}/role`, body),
  adminTags: () => client.get('/admin/tags'),
  adminCreateTag: (body) => client.post('/admin/tags', body),
  adminUpdateTag: (id, body) => client.put(`/admin/tags/${id}`, body),
  adminCreateCanteen: (body) => client.post('/admin/canteens', body),
  adminUpdateCanteen: (id, body) => client.put(`/admin/canteens/${id}`, body),
  adminCreateStall: (body) => client.post('/admin/stalls', body),
  adminUpdateStall: (id, body) => client.put(`/admin/stalls/${id}`, body),
  adminDeleteStall: (id) => client.delete(`/admin/stalls/${id}`),
  adminDeleteReview: (id) => client.delete(`/admin/reviews/${id}`),
}
