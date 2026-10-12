import axios from 'axios'
import { t } from '@/i18n'

/**
 * axios 实例 —— 全站统一请求出口。
 *
 * 约定（与 Spring Boot 后端对齐）：
 *   - baseURL 来自 .env 的 VITE_API_BASE_URL，默认 /api，经 vite proxy 转发
 *   - 后端统一返回体：{ code: 0, message: 'ok', data: ... }
 *   - 响应拦截器自动解包 data，业务代码直接拿业务数据
 *
 * 对接真实后端时只需：
 *   1. .env.development 里 VITE_USE_MOCK=false
 *   2. 后端按上述返回体实现即可，前端代码零改动
 */
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

/* ---------- 请求拦截器：统一附加令牌 / 公共参数 ---------- */
http.interceptors.request.use(
  (config) => {
    // 预留：登录态令牌（如 localStorage 中的 token）
    const token = localStorage.getItem('Leo-token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  (error) => Promise.reject(error),
)

/* ---------- 响应拦截器：解包统一返回体 + 统一错误处理 ---------- */
http.interceptors.response.use(
  (response) => {
    const body = response.data
    // 非标准返回体（如文件流）直接放行
    if (body === null || typeof body !== 'object' || !('code' in body)) {
      return body
    }
    if (body.code === 0) return body.data
    return Promise.reject(new Error(body.message || t('api.requestFailed')))
  },
  (error) => {
    const status = error.response?.status
    let msg = t('api.networkError')
    if (status === 401) {
      msg = t('api.sessionExpired')
      // 预留：跳转登录页 / 刷新令牌
    } else if (status === 403) {
      msg = t('api.forbidden')
    } else if (status === 404) {
      msg = t('api.notFound')
    } else if (status >= 500) {
      msg = t('api.serverError')
    }
    error.message = msg
    return Promise.reject(error)
  },
)

export default http
