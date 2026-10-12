import http from '@/api/http'
import { mockDelay } from '@/api/mock'
import tracks from '@/data/tracks'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'
// 豆包浏览器对同一 host 连接池存在挂起 bug：页面内第 3 个及以后的 XHR/fetch 会永久 pending。
// 开发环境直连后端 8080（走 CORS）绕过；未配置 VITE_API_HOST 时退回相对路径（经 Vite proxy）。
const API_HOST = import.meta.env.VITE_API_HOST || ''

/** JSONP 请求：走 script 标签资源通道，绕过豆包浏览器对 /api/music 的 XHR/fetch 挂起 bug */
function jsonpGet(url, timeout = 8000) {
  return new Promise((resolve, reject) => {
    const cb = '__leoMusic' + Date.now() + '_' + Math.floor(Math.random() * 1e6)
    const s = document.createElement('script')
    const timer = setTimeout(() => {
      cleanup()
      reject(new Error('jsonp timeout'))
    }, timeout)
    function cleanup() {
      try {
        delete window[cb]
      } catch {
        /* noop */
      }
      if (s.parentNode) s.parentNode.removeChild(s)
      clearTimeout(timer)
    }
    window[cb] = (payload) => {
      cleanup()
      if (payload && payload.code === 0) resolve(payload.data)
      else reject(new Error((payload && payload.message) || 'request failed'))
    }
    s.src = url + (url.indexOf('?') >= 0 ? '&' : '?') + 'callback=' + cb
    s.onerror = () => {
      cleanup()
      reject(new Error('jsonp load error'))
    }
    document.head.appendChild(s)
  })
}

/**
 * 获取播放列表。
 * 真实后端：GET /api/music/tracks
 * 曲目带 url（真实音频）或 cover（真实封面）时，前端直接使用。
 * 说明：豆包浏览器对经代理转发的 /api/music GET 响应存在挂起 bug（XHR/fetch/JSONP
 * 均无法稳定接收，服务端已响应但浏览器收不到）；改用 Vite public 静态快照
 * music-tracks.json（静态文件 fetch 实测稳定）。上传/删除后由后端同步更新该快照。
 */
export async function getTracks() {
  if (USE_MOCK) return mockDelay().then(() => tracks)
  const res = await fetch('/music-tracks.json')
  if (!res.ok) throw new Error('HTTP ' + res.status)
  const body = await res.json()
  if (body.code === 0) return body.data
  throw new Error(body.message || 'request failed')
}

/** 上传曲目（需登录；multipart：file 音频 + cover 封面 + title + enTitle + artist + duration） */
export function uploadTrack(formData) {
  return http.post('/music/tracks', formData, { headers: { 'Content-Type': undefined } })
}

/** 删除曲目（需登录，按 id；同时删除磁盘上的音频与封面文件） */
export function deleteTrack(id) {
  return http.delete(`/music/tracks/${id}`)
}
