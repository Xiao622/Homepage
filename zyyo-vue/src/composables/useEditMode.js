import { ref, reactive, computed } from 'vue'
import { authApi, siteApi } from '@/api'

/**
 * 全局编辑模式（单例，多个组件共享同一状态）：
 *  - 登录成功后进入编辑模式，token 持久化到 localStorage（key: Leo-token）
 *  - 编辑模式下可编辑 Home 页文字（EditableText 组件），改动进 drafts
 *  - save() 将 { 后端值 + drafts } 整体提交到 PUT /api/site/home-content
 */
const token = ref(localStorage.getItem('Leo-token') || '')
const isEditMode = ref(!!token.value)
const homeContent = ref({})   // 后端拉取的 Home 页可编辑文字
const drafts = reactive({})   // 本次会话的未保存修改（cfgKey -> 新值）
const loading = ref(false)

/** 登录：校验账号密码，成功进入编辑模式并拉取最新内容。 */
async function login(username, password) {
  loading.value = true
  try {
    const res = await authApi.login({ username, password })
    token.value = res.token
    localStorage.setItem('Leo-token', res.token)
    isEditMode.value = true
    await loadContent()
    return true
  } finally {
    loading.value = false
  }
}

/** 拉取 Home 页可编辑文字（覆盖本地 draft）。 */
async function loadContent() {
  try {
    homeContent.value = (await siteApi.getHomeContent()) || {}
  } catch {
    /* 网络异常时保持现状，不阻塞编辑 */
  }
}

/** 记录一次文字修改；与后端当前值一致时视为未修改（撤销）。 */
function setDraft(key, val) {
  if (val === homeContent.value[key]) {
    delete drafts[key]
  } else {
    drafts[key] = val
  }
}

/** 保存全部修改（整体覆盖）。 */
async function save() {
  loading.value = true
  try {
    const merged = { ...homeContent.value, ...drafts }
    await siteApi.updateHomeContent(merged)
    homeContent.value = merged
    Object.keys(drafts).forEach((k) => delete drafts[k])
    return true
  } finally {
    loading.value = false
  }
}

/** 退出编辑模式：注销 token 并清空本地编辑状态（保留已保存的后端值供页面展示）。 */
async function logout() {
  try {
    await authApi.logout()
  } catch {
    /* 网络异常也照常退出本地状态 */
  }
  token.value = ''
  localStorage.removeItem('Leo-token')
  isEditMode.value = false
  Object.keys(drafts).forEach((k) => delete drafts[k])
}

/** 模块加载时拉取 Home 页内容：
 *  无论是否登录都加载，保证非编辑模式也显示后端已保存的文案（而非写死占位）。 */
loadContent()

const hasDraft = computed(() => Object.keys(drafts).length > 0)

export function useEditMode() {
  return {
    isEditMode,
    homeContent,
    drafts,
    hasDraft,
    loading,
    login,
    logout,
    loadContent,
    setDraft,
    save,
  }
}
