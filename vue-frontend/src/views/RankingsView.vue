<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client'

const score = ref([])
const hot = ref([])
const latest = ref([])
const active = ref('score')

const rankMeta = {
  score: { title: '评分榜', value: (i) => `${Number(i.avg_rating || 0).toFixed(1)} 分`, list: score },
  hot: { title: '热度榜', value: (i) => `${i.review_count || 0} 条评价`, list: hot },
  latest: { title: '最新评价', value: (i) => i.latest_review_time || '-', list: latest },
}

const currentList = () => rankMeta[active.value].list.value || []

onMounted(async () => {
  const [s, h, l] = await Promise.all([
    api.scoreRank({ limit: 10 }),
    api.hotRank({ limit: 10 }),
    api.latestRank({ limit: 10 }),
  ])
  score.value = s.data?.list || []
  hot.value = h.data?.list || []
  latest.value = l.data?.list || []
})
</script>

<template>
  <h2>排行榜</h2>
  <div class="tabs">
    <button type="button" :class="{ active: active === 'score' }" @click="active = 'score'">评分榜</button>
    <button type="button" :class="{ active: active === 'hot' }" @click="active = 'hot'">热度榜</button>
    <button type="button" :class="{ active: active === 'latest' }" @click="active = 'latest'">最新评价</button>
  </div>

  <section class="rank-podium">
    <RouterLink
      v-for="(item, idx) in currentList().slice(0, 3)"
      :key="item.stall_id"
      class="podium-card"
      :to="`/stall/${item.stall_id}`"
    >
      <span class="podium-index">TOP {{ idx + 1 }}</span>
      <strong>{{ item.stall_name }}</strong>
      <small>{{ item.canteen_name }}</small>
      <span class="score">{{ rankMeta[active].value(item) }}</span>
    </RouterLink>
  </section>

  <section class="panel">
    <h3>{{ rankMeta[active].title }}</h3>
    <div v-for="(item, idx) in currentList()" :key="item.stall_id" class="rank-row">
      <span>{{ idx + 1 }}</span>
      <RouterLink :to="`/stall/${item.stall_id}`">{{ item.stall_name }}</RouterLink>
      <small class="muted">{{ item.canteen_name }}</small>
      <strong>{{ rankMeta[active].value(item) }}</strong>
    </div>
    <div v-if="!currentList().length" class="empty">暂无排行数据。</div>
  </section>
</template>
