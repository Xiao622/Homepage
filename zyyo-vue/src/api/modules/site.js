import http from '@/api/http'
import { mockDelay } from '@/api/mock'
import { siteInfo, about, marqueeTags } from '@/data/site'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

/** 站点基础信息（页脚、关于页共用）。真实后端：GET /api/site */
export function getSiteInfo() {
  if (USE_MOCK) return mockDelay().then(() => siteInfo)
  return http.get('/site')
}

/** 关于页完整数据。真实后端：GET /api/about */
export function getAbout() {
  if (USE_MOCK) return mockDelay().then(() => about)
  return http.get('/about')
}

/** 保存关于页数据（需登录 token，整体覆盖）。真实后端：PUT /api/about */
export function updateAbout(content) {
  if (USE_MOCK) return mockDelay().then(() => content)
  return http.put('/about', content)
}

/** 首页标签跑马灯。真实后端：GET /api/site/marquee-tags */
export function getMarqueeTags() {
  if (USE_MOCK) return mockDelay().then(() => marqueeTags)
  return http.get('/site/marquee-tags')
}

/** Home 页可编辑文字。真实后端：GET /api/site/home-content */
export function getHomeContent() {
  if (USE_MOCK) return mockDelay().then(() => ({}))
  return http.get('/site/home-content')
}

/** 保存 Home 页可编辑文字（需登录 token）。真实后端：PUT /api/site/home-content */
export function updateHomeContent(content) {
  if (USE_MOCK) return mockDelay().then(() => content)
  return http.put('/site/home-content', content)
}
