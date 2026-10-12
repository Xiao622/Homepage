import http from '@/api/http'
import { mockDelay } from '@/api/mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

/**
 * 获取照片列表（分类名模糊 + 年月筛选 + limit 可叠加）。
 * 真实后端：GET /api/photos?category=街&month=2026-09&limit=4
 */
export function getPhotos({ category = '', month = '', limit = 0 } = {}) {
  if (USE_MOCK) {
    return mockDelay().then(() => ({ list: [], total: 0 }))
  }
  const params = { category, month }
  if (limit > 0) params.limit = limit
  return http.get('/photos', { params })
}

/** 获取相册列表（名称 / 照片数 / 封面）。真实后端：GET /api/photos/albums */
export function getAlbums() {
  if (USE_MOCK) return mockDelay().then(() => [])
  return http.get('/photos/albums')
}

/** 获取分类统计（按照片数倒序）。真实后端：GET /api/photos/categories */
export function getCategories() {
  if (USE_MOCK) return mockDelay().then(() => [])
  return http.get('/photos/categories')
}

/** 获取照片评论。真实后端：GET /api/photos/{id}/comments */
export function getComments(id) {
  return http.get(`/photos/${id}/comments`)
}

/** 发表照片评论（访客开放）。真实后端：POST /api/photos/{id}/comments */
export function addComment(id, data) {
  return http.post(`/photos/${id}/comments`, data)
}

/** 上传照片（需登录；multipart：file + category + takenDate + caption） */
export function uploadPhoto(formData) {
  return http.post('/photos', formData, { headers: { 'Content-Type': undefined } })
}

/** 更新照片信息（需登录，按 id） */
export function updatePhoto(id, data) {
  return http.put(`/photos/${id}`, data)
}

/** 删除照片（需登录，按 id） */
export function deletePhoto(id) {
  return http.delete(`/photos/${id}`)
}
