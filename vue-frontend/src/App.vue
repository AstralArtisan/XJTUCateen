<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from './api/client'
import { useUserStore } from './stores/user'

const user = useUserStore()
const router = useRouter()
const roleLabel = computed(() => (user.user?.role >= 1 ? '管理员' : '普通用户'))

onMounted(() => {
  user.refreshMe()
})

const logout = async () => {
  if (!user.user) return
  await api.logout()
  user.clearSession()
  router.push('/login')
}
</script>

<template>
  <header class="topbar">
    <div class="container topbar-inner">
      <RouterLink class="brand" to="/">西交食堂评价系统</RouterLink>
      <nav class="nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/rankings">排行榜</RouterLink>
        <RouterLink to="/profile">个人中心</RouterLink>
        <RouterLink v-if="user.isAdmin" to="/admin">管理后台</RouterLink>
        <template v-if="user.user">
          <span class="user-pill">{{ user.user.username }} · {{ roleLabel }}</span>
          <button class="secondary" type="button" @click="logout">退出</button>
        </template>
        <template v-else>
          <RouterLink to="/login">登录</RouterLink>
          <RouterLink to="/register">注册</RouterLink>
        </template>
      </nav>
    </div>
  </header>
  <main class="container page">
    <RouterView />
  </main>
</template>
