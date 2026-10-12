// Cloudflare Pages Function：把 /api/* 请求代理到后端（同源方案，前端零改动）
// 后端地址通过 Pages 环境变量 API_ORIGIN 配置，如 https://api.fuxiaotian.tech
export async function onRequest(context) {
  const origin = (context.env.API_ORIGIN || '').replace(/\/+$/, '')
  if (!origin) {
    return new Response('API_ORIGIN 未配置', { status: 500 })
  }
  const url = new URL(context.request.url)
  const target = origin + url.pathname + url.search

  const resp = await fetch(target, {
    method: context.request.method,
    headers: context.request.headers,
    body: ['GET', 'HEAD'].includes(context.request.method)
      ? undefined
      : context.request.body,
  })

  const headers = new Headers(resp.headers)
  headers.set('Access-Control-Allow-Origin', '*')
  if (resp.status >= 200 && resp.status < 300) {
    headers.set('Cache-Control', 'public, max-age=300')
  } else {
    headers.set('Cache-Control', 'no-store')
  }
  return new Response(resp.body, { status: resp.status, headers })
}
