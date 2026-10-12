import http from '@/api/http'

/**
 * 管理员登录模块。
 * 真实后端：POST /api/auth/login / logout
 */
export function login(data) {
  return http.post('/auth/login', data)
}

export function logout() {
  return http.post('/auth/logout')
}
