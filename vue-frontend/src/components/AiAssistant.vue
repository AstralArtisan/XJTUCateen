<script setup>
import { nextTick, ref } from 'vue'
import { api } from '../api/client'
import { useToast } from '../composables/toast'
import { useUserStore } from '../stores/user'

const user = useUserStore()
const toast = useToast()
const open = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref([])
const panelRef = ref(null)

const openPanel = () => {
  open.value = true
  if (!messages.value.length) {
    const pref = user.user?.preference_text
    messages.value.push({
      role: 'assistant',
      text: pref ? `我会参考你的偏好：${pref}` : '告诉我你想吃什么，我来帮你推荐窗口。',
      items: [],
    })
  }
}

const scrollBottom = async () => {
  await nextTick()
  const box = panelRef.value?.querySelector('.ai-messages')
  if (box) box.scrollTop = box.scrollHeight
}

const send = async () => {
  const text = input.value.trim()
  if (!text || loading.value) return
  messages.value.push({ role: 'user', text, items: [] })
  input.value = ''
  loading.value = true
  await scrollBottom()
  try {
    const r = await api.recommendFeed({
      preference_text: text,
      limit: 3,
      exclude_blacklist: true,
      seed: Date.now() % 1000,
    })
    if (r.code !== 0) {
      toast.error(r.message || '推荐失败')
      return
    }
    messages.value.push({
      role: 'assistant',
      text: r.data?.ai_summary || '根据你的偏好，找到以下推荐。',
      items: r.data?.list || [],
    })
  } catch (error) {
    toast.error('推荐服务暂时不可用')
  } finally {
    loading.value = false
    await scrollBottom()
  }
}
</script>

<template>
  <button v-if="user.user && !open" class="ai-fab" type="button" @click="openPanel">AI</button>

  <aside v-if="user.user && open" ref="panelRef" class="ai-panel">
    <div class="ai-head">
      <strong>AI 美食助手</strong>
      <button class="secondary" type="button" @click="open = false">收起</button>
    </div>
    <div class="ai-messages">
      <div v-for="(message, index) in messages" :key="index" class="ai-message" :class="message.role">
        <p>{{ message.text }}</p>
        <RouterLink
          v-for="item in message.items"
          :key="item.stall_id"
          class="ai-rec"
          :to="`/stall/${item.stall_id}`"
          @click="open = false"
        >
          <strong>{{ item.stall_name }}</strong>
          <span>{{ item.canteen_name }} · {{ item.category || '未分类' }}</span>
          <small>{{ item.reason }}</small>
        </RouterLink>
      </div>
      <div v-if="loading" class="ai-message assistant"><p>正在推荐...</p></div>
    </div>
    <form class="ai-input" @submit.prevent="send">
      <input v-model="input" placeholder="例如：清淡、想吃面、预算15以内" />
      <button type="submit" :disabled="loading">发送</button>
    </form>
  </aside>
</template>
