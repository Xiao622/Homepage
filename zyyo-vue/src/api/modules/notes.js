import http from '@/api/http'
import { mockDelay } from '@/api/mock'
import notes from '@/data/notes'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

/**
 * 获取随记列表。
 * 真实后端：GET /api/notes?order=time|random&keyword=xxx（keyword 模糊匹配标题/正文）
 */
export function getNotes({ order = 'time', keyword = '' } = {}) {
  if (USE_MOCK) {
    return mockDelay().then(() => {
      let list = [...notes]
      if (keyword) {
        const kw = keyword.trim().toLowerCase()
        list = list.filter(
          (n) =>
            (n.title || '').toLowerCase().includes(kw) ||
            (n.text || '').toLowerCase().includes(kw),
        )
      }
      if (order === 'random') list.sort(() => Math.random() - 0.5)
      return { list, total: list.length }
    })
  }
  return http.get('/notes', { params: { order, keyword } })
}

/** 新建随记（需登录） */
export function createNote(data) {
  return http.post('/notes', data)
}

/** 更新随记（需登录，按 id） */
export function updateNote(id, data) {
  return http.put(`/notes/${id}`, data)
}

/** 删除随记（需登录，按 id） */
export function deleteNote(id) {
  return http.delete(`/notes/${id}`)
}
