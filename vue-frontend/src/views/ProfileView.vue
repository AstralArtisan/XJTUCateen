<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client'
import { useToast } from '../composables/toast'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const toast = useToast()
const profile = reactive({ student_id: '', username: '', signature: '', preference_text: '', avatar_url: '' })
const password = reactive({ old_password: '', new_password: '' })
const editReview = reactive({ id: null, rating: 5, content: '' })
const activeTab = ref('info')
const reviews = ref([])
const favorites = ref([])
const blacklist = ref([])
const history = ref([])

const load = async () => {
  await userStore.refreshMe()
  if (!userStore.user) {
    router.push('/login')
    return
  }
  Object.assign(profile, {
    student_id: userStore.user.student_id || '',
    username: userStore.user.username || '',
    signature: userStore.user.signature || '',
    preference_text: userStore.user.preference_text || '',
    avatar_url: userStore.user.avatar_url || '',
  })
  const [r, f, b, h] = await Promise.all([
    api.myReviews({ page: 1, page_size: 50 }),
    api.favorites({ page: 1, page_size: 50 }),
    api.blacklist({ page: 1, page_size: 50 }),
    api.history({ page: 1, page_size: 50 }),
  ])
  reviews.value = r.data?.list || []
  favorites.value = f.data?.list || []
  blacklist.value = b.data?.list || []
  history.value = h.data?.list || []
}

const readAvatar = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = () => { profile.avatar_url = String(reader.result || '') }
  reader.readAsDataURL(file)
}

const saveProfile = async () => {
  const r = await api.updateProfile(profile)
  if (r.code !== 0) return toast.error(r.message || '保存失败')
  userStore.setSession(userStore.token, r.data)
  toast.success('资料已保存')
  await load()
}

const savePassword = async () => {
  const r = await api.changePassword(password)
  if (r.code !== 0) return toast.error(r.message || '修改失败')
  password.old_password = ''
  password.new_password = ''
  toast.success('密码已修改')
}

const startEditReview = (item) => {
  editReview.id = item.id
  editReview.rating = item.rating
  editReview.content = item.content || ''
}

const saveReview = async () => {
  const r = await api.updateMyReview(editReview.id, { rating: Number(editReview.rating), content: editReview.content })
  if (r.code !== 0) return toast.error(r.message || '保存失败')
  editReview.id = null
  toast.success('评价已保存')
  await load()
}

const deleteReview = async (id) => {
  const r = await api.deleteMyReview(id)
  if (r.code !== 0) return toast.error(r.message || '删除失败')
  toast.success('评价已删除')
  await load()
}

const removeFavorite = async (id) => {
  const r = await api.deleteFavorite(id)
  if (r.code !== 0) return toast.error(r.message || '取消失败')
  toast.success('已取消收藏')
  await load()
}

const removeBlacklist = async (id) => {
  const r = await api.deleteBlacklist(id)
  if (r.code !== 0) return toast.error(r.message || '移除失败')
  toast.success('已移出黑名单')
  await load()
}

onMounted(load)
</script>

<template>
  <h2>个人中心</h2>
  <section class="panel row" style="align-items:flex-start;margin-bottom:16px;">
    <img class="avatar" :src="profile.avatar_url || `https://ui-avatars.com/api/?name=${encodeURIComponent(profile.username || 'U')}&background=145D52&color=fff&size=128`" alt="avatar" />
    <div>
      <h3 style="margin:0 0 6px;">{{ profile.username }}</h3>
      <p class="muted">{{ profile.student_id }} · {{ userStore.isAdmin ? '管理员' : '普通用户' }}</p>
      <p>{{ profile.signature || '还没有签名。' }}</p>
    </div>
  </section>

  <div class="tabs">
    <button type="button" :class="{ active: activeTab === 'info' }" @click="activeTab = 'info'">资料</button>
    <button type="button" :class="{ active: activeTab === 'reviews' }" @click="activeTab = 'reviews'">我的评价</button>
    <button type="button" :class="{ active: activeTab === 'favorites' }" @click="activeTab = 'favorites'">收藏</button>
    <button type="button" :class="{ active: activeTab === 'blacklist' }" @click="activeTab = 'blacklist'">黑名单</button>
    <button type="button" :class="{ active: activeTab === 'history' }" @click="activeTab = 'history'">浏览历史</button>
  </div>

  <section v-if="activeTab === 'info'" class="panel stack">
    <input v-model="profile.student_id" placeholder="学号" />
    <input v-model="profile.username" placeholder="昵称" />
    <input v-model="profile.signature" placeholder="个性签名" />
    <textarea v-model="profile.preference_text" placeholder="口味偏好，例如：喜欢辣的、不吃香菜、预算15以内"></textarea>
    <input type="file" accept="image/*" @change="readAvatar" />
    <div class="row">
      <button type="button" @click="saveProfile">保存资料</button>
    </div>

    <form class="stack" style="margin-top:16px;" @submit.prevent="savePassword">
      <h3>修改密码</h3>
      <input v-model="password.old_password" type="password" placeholder="原密码" required />
      <input v-model="password.new_password" type="password" placeholder="新密码" required />
      <button type="submit">确认修改</button>
    </form>
  </section>

  <section v-if="activeTab === 'reviews'" class="stack">
    <article v-for="r in reviews" :key="r.id" class="card">
      <template v-if="editReview.id === r.id">
        <select v-model="editReview.rating">
          <option v-for="i in [5, 4, 3, 2, 1]" :key="i" :value="i">{{ i }} 分</option>
        </select>
        <textarea v-model="editReview.content"></textarea>
        <div class="row">
          <button type="button" @click="saveReview">保存</button>
          <button class="secondary" type="button" @click="editReview.id = null">取消</button>
        </div>
      </template>
      <template v-else>
        <h3>{{ r.stall_name }}</h3>
        <p><span class="score">{{ r.rating }} 分</span> · {{ r.content || '没有文字评价' }}</p>
        <div class="row">
          <button class="secondary" type="button" @click="startEditReview(r)">编辑</button>
          <button class="danger" type="button" @click="deleteReview(r.id)">删除</button>
        </div>
      </template>
    </article>
    <div v-if="!reviews.length" class="empty panel">还没有写过评价。</div>
  </section>

  <section v-if="activeTab === 'favorites'" class="grid">
    <article v-for="f in favorites" :key="f.stall_id" class="card">
      <h3>{{ f.stall_name }}</h3>
      <p class="muted">{{ f.canteen_name }}</p>
      <div class="row">
        <RouterLink :to="`/stall/${f.stall_id}`">查看详情</RouterLink>
        <button class="secondary" type="button" @click="removeFavorite(f.stall_id)">取消收藏</button>
      </div>
    </article>
  </section>

  <section v-if="activeTab === 'blacklist'" class="stack">
    <article v-for="b in blacklist" :key="b.stall_id" class="card row" style="justify-content:space-between;">
      <span>{{ b.stall_name }} <small class="muted">{{ b.canteen_name }}</small></span>
      <button class="secondary" type="button" @click="removeBlacklist(b.stall_id)">移除</button>
    </article>
  </section>

  <section v-if="activeTab === 'history'" class="stack">
    <article v-for="h in history" :key="`${h.stall_id}-${h.visited_at}`" class="card row" style="justify-content:space-between;">
      <RouterLink :to="`/stall/${h.stall_id}`">{{ h.stall_name }}</RouterLink>
      <small class="muted">{{ h.visited_at }}</small>
    </article>
  </section>
</template>
