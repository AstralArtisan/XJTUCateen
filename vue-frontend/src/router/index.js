import { createRouter, createWebHashHistory } from 'vue-router'

import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import StallView from '../views/StallView.vue'
import RankingsView from '../views/RankingsView.vue'
import ProfileView from '../views/ProfileView.vue'
import AdminView from '../views/AdminView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/stall-list', redirect: { path: '/', hash: '#stall-list' } },
    { path: '/login', component: LoginView },
    { path: '/register', component: RegisterView },
    { path: '/stall/:id', component: StallView },
    { path: '/rankings', component: RankingsView },
    { path: '/profile', component: ProfileView },
    { path: '/admin', component: AdminView },
  ],
})

export default router
