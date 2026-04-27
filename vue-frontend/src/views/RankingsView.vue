<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client'

const score = ref([])
const hot = ref([])
const latest = ref([])

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
  <div class="grid">
    <section class="panel">
      <h3>评分榜</h3>
      <p v-for="(i, idx) in score" :key="i.stall_id">
        {{ idx + 1 }}. <RouterLink :to="`/stall/${i.stall_id}`">{{ i.stall_name }}</RouterLink>
        <span class="score">{{ Number(i.avg_rating || 0).toFixed(1) }}</span>
      </p>
    </section>
    <section class="panel">
      <h3>热度榜</h3>
      <p v-for="(i, idx) in hot" :key="i.stall_id">
        {{ idx + 1 }}. <RouterLink :to="`/stall/${i.stall_id}`">{{ i.stall_name }}</RouterLink>
        <span class="muted">{{ i.review_count || 0 }} 条评价</span>
      </p>
    </section>
    <section class="panel">
      <h3>最新评价</h3>
      <p v-for="(i, idx) in latest" :key="i.stall_id">
        {{ idx + 1 }}. <RouterLink :to="`/stall/${i.stall_id}`">{{ i.stall_name }}</RouterLink>
        <span class="muted">{{ i.latest_review_time || '-' }}</span>
      </p>
    </section>
  </div>
</template>
