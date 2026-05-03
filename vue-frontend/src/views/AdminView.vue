<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'
import { useToast } from '../composables/toast'
import { useUserStore } from '../stores/user'

const router = useRouter()
const user = useUserStore()
const toast = useToast()
const activeTab = ref('dashboard')
const users = ref([])
const tags = ref([])
const canteens = ref([])
const categories = ref([])
const stalls = ref([])
const dashboard = ref(null)
const adminReviews = ref([])
const reviewTotal = ref(0)
const reviewFilters = reactive({ keyword: '', stall_id: '', status: '' })
const form = reactive({
  tag: '',
  tagDesc: '',
  canteenName: '',
  canteenLoc: '',
  canteenDesc: '',
  stallCanteenId: '',
  stallName: '',
  stallCat: '',
  stallDesc: '',
  stallTags: '',
  reviewId: '',
})

const load = async () => {
  await user.refreshMe()
  if (!user.isAdmin) {
    router.push('/profile')
    return
  }
  const [u, t, c, g, s, d] = await Promise.all([
    api.adminUsers(),
    api.adminTags(),
    api.canteens(),
    api.categories(),
    api.stalls({ page: 1, page_size: 100 }),
    api.adminDashboard(),
  ])
  users.value = u.data?.list || []
  tags.value = t.data?.list || []
  canteens.value = c.data?.list || []
  categories.value = g.data?.list || []
  stalls.value = s.data?.list || []
  dashboard.value = d.data || null
  await loadAdminReviews()
}

const cleanReviewFilters = () => Object.fromEntries(Object.entries(reviewFilters).filter(([, v]) => v !== ''))

const loadAdminReviews = async () => {
  const r = await api.adminReviews({ page: 1, page_size: 50, ...cleanReviewFilters() })
  adminReviews.value = r.data?.list || []
  reviewTotal.value = r.data?.total || 0
}

const createTag = async () => {
  const r = await api.adminCreateTag({ name: form.tag, description: form.tagDesc })
  if (r.code !== 0) return toast.error(r.message || '创建失败')
  form.tag = ''
  form.tagDesc = ''
  toast.success('标签已创建')
  await load()
}

const createCanteen = async () => {
  const r = await api.adminCreateCanteen({ name: form.canteenName, location: form.canteenLoc, description: form.canteenDesc })
  if (r.code !== 0) return toast.error(r.message || '创建失败')
  form.canteenName = ''
  form.canteenLoc = ''
  form.canteenDesc = ''
  toast.success('食堂已创建')
  await load()
}

const createStall = async () => {
  const payload = {
    canteen_id: Number(form.stallCanteenId),
    name: form.stallName,
    category: form.stallCat,
    description: form.stallDesc,
    tags: form.stallTags.split(/[,，]/).map((s) => s.trim()).filter(Boolean),
  }
  const r = await api.adminCreateStall(payload)
  if (r.code !== 0) return toast.error(r.message || '创建失败')
  form.stallName = ''
  form.stallDesc = ''
  form.stallTags = ''
  toast.success('窗口已创建')
  await load()
}

const disableStall = async (id) => {
  const r = await api.adminDeleteStall(id)
  if (r.code !== 0) return toast.error(r.message || '停用失败')
  toast.success('窗口已停用')
  await load()
}

const deleteReview = async () => {
  const r = await api.adminDeleteReview(form.reviewId)
  if (r.code !== 0) return toast.error(r.message || '删除失败')
  form.reviewId = ''
  toast.success('评论已删除')
  const [, dashboardData] = await Promise.all([
    loadAdminReviews(),
    api.adminDashboard(),
  ])
  dashboard.value = dashboardData.data || null
}

const deleteReviewById = async (id) => {
  form.reviewId = id
  await deleteReview()
}

const setRole = async (item, role) => {
  const r = await api.adminUpdateUserRole(item.id, { role })
  if (r.code !== 0) return toast.error(r.message || '更新失败')
  toast.success('角色已更新')
  await load()
}

onMounted(load)
</script>

<template>
  <h2>管理后台</h2>
  <div class="tabs">
    <button type="button" :class="{ active: activeTab === 'dashboard' }" @click="activeTab = 'dashboard'">看板</button>
    <button type="button" :class="{ active: activeTab === 'tags' }" @click="activeTab = 'tags'">标签</button>
    <button type="button" :class="{ active: activeTab === 'canteens' }" @click="activeTab = 'canteens'">食堂</button>
    <button type="button" :class="{ active: activeTab === 'stalls' }" @click="activeTab = 'stalls'">窗口</button>
    <button type="button" :class="{ active: activeTab === 'reviews' }" @click="activeTab = 'reviews'">评论</button>
    <button type="button" :class="{ active: activeTab === 'users' }" @click="activeTab = 'users'">用户</button>
  </div>

  <section v-if="activeTab === 'dashboard' && dashboard" class="stack">
    <div class="grid">
      <article class="card"><h3>用户</h3><p class="score">{{ dashboard.user_count }}</p></article>
      <article class="card"><h3>营业窗口</h3><p class="score">{{ dashboard.stall_count }}</p></article>
      <article class="card"><h3>有效评价</h3><p class="score">{{ dashboard.review_count }}</p></article>
      <article class="card"><h3>待处理举报</h3><p class="score">{{ dashboard.pending_report_count }}</p></article>
    </div>
    <section class="panel">
      <h3>热门窗口</h3>
      <div v-for="s in dashboard.top_stalls || []" :key="s.stall_id" class="rank-row">
        <span>#</span>
        <RouterLink :to="`/stall/${s.stall_id}`">{{ s.stall_name }}</RouterLink>
        <small class="muted">{{ s.canteen_name }}</small>
        <strong>{{ s.review_count }} 条</strong>
      </div>
    </section>
    <section class="panel">
      <h3>低分关注</h3>
      <div v-for="s in dashboard.low_score_stalls || []" :key="s.stall_id" class="rank-row">
        <span>#</span>
        <RouterLink :to="`/stall/${s.stall_id}`">{{ s.stall_name }}</RouterLink>
        <small class="muted">{{ s.canteen_name }}</small>
        <strong>{{ Number(s.avg_rating || 0).toFixed(1) }} 分</strong>
      </div>
      <div v-if="!dashboard.low_score_stalls?.length" class="empty">暂无需要关注的低分窗口。</div>
    </section>
  </section>

  <section v-if="activeTab === 'tags'" class="panel stack">
    <form class="row" @submit.prevent="createTag">
      <input v-model="form.tag" placeholder="标签名称" required />
      <input v-model="form.tagDesc" placeholder="描述" />
      <button type="submit">新增标签</button>
    </form>
    <div class="row">
      <span v-for="t in tags" :key="t.id" class="user-pill">{{ t.name }}</span>
    </div>
  </section>

  <section v-if="activeTab === 'canteens'" class="panel stack">
    <form class="toolbar" @submit.prevent="createCanteen">
      <input v-model="form.canteenName" placeholder="食堂名称" required />
      <input v-model="form.canteenLoc" placeholder="位置" />
      <input v-model="form.canteenDesc" placeholder="描述" />
      <button type="submit">新增食堂</button>
    </form>
    <div class="table-wrap">
      <table>
        <thead><tr><th>ID</th><th>名称</th><th>位置</th><th>描述</th></tr></thead>
        <tbody>
          <tr v-for="c in canteens" :key="c.id">
            <td>{{ c.id }}</td><td>{{ c.name }}</td><td>{{ c.location || '-' }}</td><td>{{ c.description || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>

  <section v-if="activeTab === 'stalls'" class="panel stack">
    <form class="toolbar" @submit.prevent="createStall">
      <select v-model="form.stallCanteenId" required>
        <option value="">选择食堂</option>
        <option v-for="c in canteens" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      <input v-model="form.stallName" placeholder="窗口名称" required />
      <select v-model="form.stallCat">
        <option value="">选择分类</option>
        <option v-for="c in categories" :key="c" :value="c">{{ c }}</option>
      </select>
      <input v-model="form.stallTags" placeholder="标签，用逗号分隔" />
      <input v-model="form.stallDesc" placeholder="描述" />
      <button type="submit">新增窗口</button>
    </form>
    <div class="table-wrap">
      <table>
        <thead><tr><th>ID</th><th>名称</th><th>食堂</th><th>分类</th><th>评分</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="s in stalls" :key="s.id">
            <td>{{ s.id }}</td>
            <td>{{ s.name }}</td>
            <td>{{ s.canteen_name }}</td>
            <td>{{ s.category || '-' }}</td>
            <td>{{ Number(s.avg_rating || 0).toFixed(1) }}</td>
            <td><button class="danger" type="button" @click="disableStall(s.id)">停用</button></td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>

  <section v-if="activeTab === 'reviews'" class="panel stack">
    <form class="toolbar" @submit.prevent="loadAdminReviews">
      <input v-model="reviewFilters.keyword" placeholder="搜索评论、用户或窗口" />
      <select v-model="reviewFilters.stall_id">
        <option value="">全部窗口</option>
        <option v-for="s in stalls" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>
      <select v-model="reviewFilters.status">
        <option value="">正常评论</option>
        <option value="deleted">已删除</option>
      </select>
      <button type="submit">筛选</button>
    </form>
    <p class="muted">共 {{ reviewTotal }} 条评论，举报数高的会排在前面。</p>
    <div class="table-wrap">
      <table>
        <thead><tr><th>ID</th><th>窗口</th><th>用户</th><th>评分</th><th>内容</th><th>有用</th><th>举报</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="r in adminReviews" :key="r.id">
            <td>{{ r.id }}</td>
            <td>{{ r.stall_name }}</td>
            <td>{{ r.username }}</td>
            <td>{{ r.rating }}</td>
            <td>{{ r.content || '-' }}</td>
            <td>{{ r.like_count || 0 }}</td>
            <td>{{ r.report_count || 0 }}</td>
            <td><button v-if="!r.is_deleted" class="danger" type="button" @click="deleteReviewById(r.id)">删除</button></td>
          </tr>
        </tbody>
      </table>
    </div>
    <form class="row" @submit.prevent="deleteReview">
      <input v-model="form.reviewId" placeholder="评论 ID" required />
      <button class="danger" type="submit">删除评论</button>
    </form>
  </section>

  <section v-if="activeTab === 'users'" class="panel table-wrap">
    <table>
      <thead><tr><th>ID</th><th>学号</th><th>昵称</th><th>角色</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.student_id }}</td>
          <td>{{ u.username }}</td>
          <td>{{ u.role >= 1 ? '管理员' : '普通用户' }}</td>
          <td>
            <button v-if="u.id !== user.user?.id && u.role === 0" type="button" @click="setRole(u, 1)">设为管理员</button>
            <button v-if="u.id !== user.user?.id && u.role >= 1" class="secondary" type="button" @click="setRole(u, 0)">设为普通用户</button>
            <span v-if="u.id === user.user?.id" class="muted">当前账号</span>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
