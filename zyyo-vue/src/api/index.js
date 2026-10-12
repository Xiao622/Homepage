/**
 * API 模块统一出口：组件只从 @/api 引入，不直接依赖 axios 或 mock。
 * 页面新增业务时在此追加模块。
 */
export * as blogApi from './modules/blog'
export * as notesApi from './modules/notes'
export * as photosApi from './modules/photos'
export * as siteApi from './modules/site'
export * as musicApi from './modules/tracks'
export * as authApi from './modules/auth'
// musicApi.getTracks 已切原生 fetch（豆包浏览器 XHR 挂起问题），版本戳刷新
// eslint-disable-next-line
export const __musicRefresh = 20261012
