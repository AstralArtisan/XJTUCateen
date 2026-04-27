<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'

const form = reactive({ student_id: '', username: '', password: '' })
const router = useRouter()

const submit = async () => {
  const r = await api.register(form)
  if (r.code !== 0) return alert(r.message || '注册失败')
  alert('注册成功，请登录')
  router.push('/login')
}
</script>

<template>
  <h2>注册</h2>
  <form class="panel stack" style="max-width:420px;" @submit.prevent="submit">
    <input v-model="form.student_id" placeholder="学号" required />
    <input v-model="form.username" placeholder="昵称" required />
    <input v-model="form.password" placeholder="密码" type="password" required />
    <button type="submit">注册</button>
    <RouterLink to="/login">已有账号？去登录</RouterLink>
  </form>
</template>
