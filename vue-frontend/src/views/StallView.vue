<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api/client'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const detail = ref(null)
const reviews = ref([])
const form = reactive({ rating: 5, content: '' })

const load = async () => {
  const id = route.params.id
  const [d, r] = await Promise.all([
    api.stallDetail(id),
    api.stallReviews(id, { page: 1, page_size: 20 }),
  ])
  detail.value = d.code === 0 ? d.data : null
  reviews.value = r.data?.list || []
  if (user.user) api.addHistory({ stall_id: Number(id) }).catch(() => {})
}

const requireLogin = () => {
  if (user.user) return true
  router.push('/login')
  return false
}

const submit = async () => {
  if (!requireLogin()) return
  const r = await api.submitReview({ stall_id: Number(route.params.id), rating: Number(form.rating), content: form.content })
  if (r.code !== 0) return alert(r.message || '提交失败')
  form.content = ''
  await load()
}

const addFav = async () => {
  if (!requireLogin()) return
  const r = await api.addFavorite({ stall_id: Number(route.params.id) })
  alert(r.code === 0 ? '已收藏' : r.message)
}

const addBlack = async () => {
  if (!requireLogin()) return
  const r = await api.addBlacklist({ stall_id: Number(route.params.id) })
  alert(r.code === 0 ? '已加入黑名单' : r.message)
}

onMounted(load)
</script>

<template>
  <section v-if="detail" class="panel stack">
    <div class="row" style="justify-content:space-between;">
      <div>
        <h2 style="margin-bottom:4px;">{{ detail.name }}</h2>
        <p class="muted">{{ detail.canteen_name }} · {{ detail.category || '未分类' }}</p>
      </div>
      <div class="score">{{ Number(detail.avg_rating || 0).toFixed(1) }} 分</div>
    </div>
    <p>{{ detail.description || '暂无简介' }}</p>
    <p v-if="detail.tags?.length" class="muted">标签：{{ detail.tags.join('、') }}</p>
    <div class="row">
      <button type="button" @click="addFav">收藏</button>
      <button class="secondary" type="button" @click="addBlack">加入黑名单</button>
    </div>
  </section>
  <div v-else class="empty panel">窗口不存在。</div>

  <form class="panel stack" style="margin-top:16px;" @submit.prevent="submit">
    <h3>写评价</h3>
    <select v-model="form.rating">
      <option v-for="i in [5, 4, 3, 2, 1]" :key="i" :value="i">{{ i }} 分</option>
    </select>
    <textarea v-model="form.content" rows="3" placeholder="说说真实体验"></textarea>
    <button type="submit">提交评价</button>
  </form>

  <h3>评价列表</h3>
  <article v-for="r in reviews" :key="r.id" class="card">
    <div class="row" style="justify-content:space-between;">
      <strong>{{ r.username || '匿名用户' }}</strong>
      <span class="score">{{ r.rating }} 分</span>
    </div>
    <p>{{ r.content || '这位同学没有留下文字评价。' }}</p>
    <small class="muted">{{ r.updated_at || r.created_at }}</small>
  </article>
  <div v-if="!reviews.length" class="empty panel">暂无评价。</div>
</template>
