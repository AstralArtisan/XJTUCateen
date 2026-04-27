<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api/client'
import { useUserStore } from '../stores/user'

const user = useUserStore()
const stalls = ref([])
const canteens = ref([])
const categories = ref([])
const tags = ref([])
const today = ref(null)
const aiCards = ref([])
const aiSummary = ref('')
const aiPrompt = ref('')
const loading = ref(false)
const filters = reactive({ canteen_id: '', category: '', tag_name: '', keyword: '', sort_by: '' })

const cleanParams = () => Object.fromEntries(Object.entries(filters).filter(([, v]) => v !== ''))

const loadBase = async () => {
  const [c, g, t] = await Promise.all([api.canteens(), api.categories(), api.tags()])
  canteens.value = c.data?.list || []
  categories.value = g.data?.list || []
  tags.value = t.data?.list || []
}

const loadStalls = async () => {
  const r = await api.stalls({ page: 1, page_size: 30, ...cleanParams() })
  stalls.value = r.data?.list || []
}

const pickToday = async () => {
  const r = await api.recommendToday({ limit: 1, exclude_blacklist: true, seed: Date.now() % 1000 })
  today.value = r.data?.list?.[0] || null
}

const askAi = async () => {
  if (!aiPrompt.value.trim()) return
  loading.value = true
  try {
    const r = await api.recommendFeed({ preference_text: aiPrompt.value, limit: 3, exclude_blacklist: true, seed: Date.now() % 1000 })
    aiCards.value = r.data?.list || []
    aiSummary.value = r.data?.ai_summary || '根据你的偏好，找到以下推荐。'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadBase()
  await loadStalls()
})
</script>

<template>
  <div class="row" style="justify-content:space-between;margin-bottom:14px;">
    <h2 style="margin:0;">窗口列表</h2>
    <button type="button" @click="pickToday">今天吃什么？</button>
  </div>

  <section v-if="today" class="panel stack" style="margin-bottom:16px;">
    <strong>今日推荐：{{ today.stall_name }}</strong>
    <span class="muted">{{ today.canteen_name }} · {{ today.category || '未分类' }}</span>
    <span>{{ today.reason || '根据评分和热度为你推荐。' }}</span>
    <RouterLink :to="`/stall/${today.stall_id}`">查看详情</RouterLink>
  </section>

  <section class="panel" style="margin-bottom:16px;">
    <div class="toolbar">
      <select v-model="filters.canteen_id" @change="loadStalls">
        <option value="">全部食堂</option>
        <option v-for="c in canteens" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      <select v-model="filters.category" @change="loadStalls">
        <option value="">全部分类</option>
        <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
      </select>
      <select v-model="filters.tag_name" @change="loadStalls">
        <option value="">全部标签</option>
        <option v-for="t in tags" :key="t.id" :value="t.name">{{ t.name }}</option>
      </select>
      <input v-model="filters.keyword" placeholder="搜索窗口名称" @keydown.enter="loadStalls" />
      <select v-model="filters.sort_by" @change="loadStalls">
        <option value="">默认排序</option>
        <option value="score">评分优先</option>
        <option value="hot">热度优先</option>
      </select>
      <button type="button" @click="loadStalls">查询</button>
    </div>
  </section>

  <section v-if="user.user" class="panel stack" style="margin-bottom:16px;">
    <strong>AI 美食助手</strong>
    <div class="row">
      <input v-model="aiPrompt" style="flex:1 1 260px;" placeholder="想吃什么？例如：清淡、辣、预算15以内" @keydown.enter="askAi" />
      <button type="button" :disabled="loading" @click="askAi">{{ loading ? '推荐中...' : '获取推荐' }}</button>
    </div>
    <p v-if="aiSummary" class="muted">{{ aiSummary }}</p>
    <div v-if="aiCards.length" class="grid">
      <article v-for="item in aiCards" :key="item.stall_id" class="card">
        <h3>{{ item.stall_name }}</h3>
        <p>{{ item.canteen_name }} · {{ item.category || '未分类' }}</p>
        <p>{{ item.reason }}</p>
        <RouterLink :to="`/stall/${item.stall_id}`">查看详情</RouterLink>
      </article>
    </div>
  </section>

  <div v-if="stalls.length" class="grid">
    <article v-for="s in stalls" :key="s.id" class="card">
      <h3>{{ s.name }}</h3>
      <p class="muted">{{ s.canteen_name }} · {{ s.category || '未分类' }}</p>
      <p><span class="score">{{ Number(s.avg_rating || 0).toFixed(1) }}</span> 分 · {{ s.review_count || 0 }} 条评价</p>
      <p v-if="s.description">{{ s.description }}</p>
      <RouterLink :to="`/stall/${s.id}`">查看详情</RouterLink>
    </article>
  </div>
  <div v-else class="empty panel">没有找到符合条件的窗口。</div>
</template>
