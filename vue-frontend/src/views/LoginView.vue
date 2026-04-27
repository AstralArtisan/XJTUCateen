<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'
import { useUserStore } from '../stores/user'

const form = reactive({ student_id: '', password: '' })
const router = useRouter()
const user = useUserStore()

const submit = async () => {
  const r = await api.login(form)
  if (r.code !== 0) return alert(r.message || '登录失败')
  user.setSession(r.data.token, r.data.user)
  router.push('/')
}
</script>

<template>
  <h2>登录</h2>
  <form class="panel stack" style="max-width:420px;" @submit.prevent="submit">
    <input v-model="form.student_id" placeholder="账号 / 学号" required />
    <input v-model="form.password" placeholder="密码" type="password" required />
    <button type="submit">登录</button>
    <RouterLink to="/register">没有账号？去注册</RouterLink>
  </form>
</template>
