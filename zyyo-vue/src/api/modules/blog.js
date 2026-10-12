import http from '@/api/http'
import { mockDelay } from '@/api/mock'
import posts, { tags } from '@/data/blog'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

/**
 * 获取文章列表（分页 / 标签筛选 / 标题模糊搜索）。
 * 真实后端：GET /api/posts?page=&pageSize=&tag=&keyword=
 */
export function getPosts({ page = 1, pageSize = 6, tag = '', keyword = '' } = {}) {
  if (USE_MOCK) {
    return mockDelay().then(() => {
      let filtered = tag
        ? posts.filter((p) => p.tags.includes(tag))
        : posts
      if (keyword) {
        const kw = keyword.trim().toLowerCase()
        filtered = filtered.filter((p) => (p.title || '').toLowerCase().includes(kw))
      }
      const start = (page - 1) * pageSize
      return {
        list: filtered.slice(start, start + pageSize),
        total: filtered.length,
        page,
        pageSize,
      }
    })
  }
  return http.get('/posts', { params: { page, pageSize, tag, keyword } })
}

/** 获取文章详情（按路由编号 slug，公开） */
export function getPost(slug) {
  if (USE_MOCK) {
    return mockDelay().then(() => posts.find((p) => p.slug === slug) || null)
  }
  return http.get(`/posts/${slug}`)
}

/** 获取标签云 */
export function getTags() {
  if (USE_MOCK) return mockDelay().then(() => tags)
  return http.get('/posts/tags')
}

/** 新建文章（需登录；slug 由后端自动生成） */
export function createPost(data) {
  return http.post('/posts', data)
}

/** 更新文章（需登录，按 slug） */
export function updatePost(slug, data) {
  return http.put(`/posts/${slug}`, data)
}

/** 删除文章（需登录，按 slug） */
export function deletePost(slug) {
  return http.delete(`/posts/${slug}`)
}

/** 上传正文图片（需登录；multipart file，返回图片访问路径） */
export function uploadImage(file) {
  const fd = new FormData()
  fd.append('file', file)
  return http.post('/posts/images', fd, { headers: { 'Content-Type': undefined } })
}
