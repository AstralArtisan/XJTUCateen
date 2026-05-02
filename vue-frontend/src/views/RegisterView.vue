<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'
import { useToast } from '../composables/toast'

const form = reactive({ student_id: '', username: '', password: '' })
const router = useRouter()
const toast = useToast()

const submit = async () => {
  const r = await api.register(form)
  if (r.code !== 0) return toast.error(r.message || '注册失败')
  toast.success('注册成功，请登录')
  router.push('/login')
}
</script>

<template>
  <section class="auth-shell">
    <div>
      <p class="eyebrow">JOIN THE TABLE</p>
      <h2>注册</h2>
      <p class="muted">创建账号后即可评价窗口、保存偏好并使用推荐能力。</p>
    </div>
    <form class="panel stack auth-card" @submit.prevent="submit">
    <input v-model="form.student_id" placeholder="学号" required />
    <input v-model="form.username" placeholder="昵称" required />
    <input v-model="form.password" placeholder="密码" type="password" required />
    <button type="submit">注册</button>
    <RouterLink to="/login">已有账号？去登录</RouterLink>
  </form>
  </section>
</template>
