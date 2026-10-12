import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import http from 'node:http'

// 豆包内置浏览器对「经代理转发的 /api/music 列表 GET 响应」存在挂起 bug（永久 pending），
// 但同一后端响应用 curl 秒回、静态 JSON 却能正常 fetch。
// 因此对 GET /api/music/* 用本中间件在 Node 侧取完整响应后：
//  - 普通请求：以完整响应头回给浏览器（常规 JSON）
//  - jsonp 请求（tracks/jsonp?callback=xxx）：包装成 callback(json) 返回，
//    前端用 script 标签加载（资源通道），彻底绕开 XHR/fetch 挂起。
// 上传（POST multipart）与删除（DELETE）走原 proxy（实测正常），不受影响。
function musicBypass() {
  const BACKEND = 'http://localhost:8080'
  return {
    name: 'music-bypass',
    configureServer(server) {
      server.middlewares.use('/api/music', (req, res, next) => {
        if (req.method !== 'GET') {
          next()
          return
        }
        const isJsonp = req.url.indexOf('jsonp') >= 0
        const cbMatch = req.url.match(/callback=([A-Za-z0-9_]+)/)
        // jsonp 请求打普通 tracks 端点（后端可能未重启、暂无 jsonp 端点），由本中间件负责包装
        const backendPath = isJsonp ? '/tracks' : req.url
        // connect 中间件挂载路径会把 req.url 剥离为挂载点之后的部分（如 /tracks），需手动补回
        const backendUrl = BACKEND + '/api/music' + backendPath
        const proxyReq = http.request(
          backendUrl,
          { method: 'GET' },
          (r) => {
            let body = ''
            r.setEncoding('utf8')
            r.on('data', (c) => {
              body += c
            })
            r.on('end', () => {
              res.statusCode = r.statusCode || 200
              if (isJsonp && cbMatch) {
                body = cbMatch[1] + '(' + body + ')'
                res.setHeader('Content-Type', 'application/javascript; charset=utf-8')
              } else {
                res.setHeader('Content-Type', 'application/json; charset=utf-8')
              }
              // 静态文件风格的完整响应头，与 Vite public 静态 JSON 的响应保持一致
              res.setHeader('Cache-Control', 'no-cache')
              res.setHeader('ETag', '"' + Date.now().toString(36) + '"')
              res.setHeader('Access-Control-Allow-Origin', '*')
              res.setHeader('X-Content-Type-Options', 'nosniff')
              res.setHeader('Content-Length', Buffer.byteLength(body))
              res.end(body)
            })
          },
        )
        proxyReq.on('error', (err) => {
          console.error('[music-bypass] backend request failed:', err.message)
          next()
        })
        proxyReq.end()
      })
    },
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), musicBypass()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    host: true,
    port: 5173,
    // 开发代理：/api 前缀的请求转发到本地 Spring Boot 后端
    // 后端启动后按需调整 target；开启代理后前端 axios 无需关心跨域
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 若后端本身不带 /api 前缀，可放开下面这行去掉前缀
        // rewrite: (path) => path.replace(/^\/api/, ''),
      },
      // 照片静态访问：/uploads 前缀转发到后端，由 PhotoController 回放文件
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
