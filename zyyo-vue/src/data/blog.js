/**
 * 博客文章 Mock 数据（内容摘自 Leo.net 公开页面，用于前端先行复刻）。
 * 对接 Spring Boot 后由 src/api/modules/blog.js 请求真实接口替换。
 * 字段约定：
 *   id       唯一标识
 *   date     YYYY.MM.DD
 *   title    标题
 *   excerpt  摘要（复刻中取原文开头）
 *   tags     标签数组
 *   minutes  阅读时长（min）
 */
export default [
  {
    id: 1,
    date: '2026.09.16',
    title: '更新日志：9 月 15 日晚 20:15 → 9 月 16 日凌晨 01:21',
    excerpt:
      '过去 6 小时全量改动，一句一条：圆形动态流、评论数据迁移、机器人雪碧图、页面重构、朋友页、字体全量自托管。',
    tags: ['本站'],
    minutes: 5,
  },
  {
    id: 2,
    date: '2026.09.14',
    title: '从 Typecho 迁移到 Astro：一个更轻的站点',
    excerpt:
      'Typecho 时代结束了：内容全部迁到 Astro 静态站，评论尚未迁移；旧主题 Zyang 与新项目都将开源。聊聊新主页的设计理念。',
    tags: ['建站', 'Astro', 'Typecho'],
    minutes: 6,
  },
  {
    id: 3,
    date: '2026.08.14',
    title: 'Markdown 魔法块：34 种花式组件渲染全展示',
    excerpt:
      '本站 Markdown 插件升级：代码块即组件。折叠菜单、标签页、按钮、时间线、定价表……零 JS 依赖（除复制按钮），一篇文章全部演示。',
    tags: ['本站', 'Markdown', '实用分享'],
    minutes: 13,
  },
  {
    id: 4,
    date: '2026.06.30',
    title: '我对 AI 的思考',
    excerpt:
      '与 AI 深度交互后的思考碎片：为什么 Skills 能决定 AI 的导向，而训练数据不能？',
    tags: ['灵感相关'],
    minutes: 5,
  },
  {
    id: 5,
    date: '2026.06.30',
    title: 'Pi Agent Core：极简 AI Coding Agent 核心解析',
    excerpt: 'Pi：只有四个工具的极简 AI Coding Agent，拆开核心看看它强在哪。',
    tags: ['实用分享'],
    minutes: 5,
  },
  {
    id: 6,
    date: '2025.10.29',
    title: '锐捷校园网认证工具',
    excerpt: '基于 Electron 的锐捷校园网自动认证工具：掉线自动重连的校园网神器。',
    tags: ['实用分享'],
    minutes: 5,
  },
  {
    id: 7,
    date: '2025.07.09',
    title: '一个综合树维教务系统的选课排课客户端基于 nodejs',
    excerpt: '基于 Node.js 的树维教务选课排课客户端：从排课算法到 WebUI 的完整设计。',
    tags: ['实用分享'],
    minutes: 5,
  },
  {
    id: 8,
    date: '2025.04.01',
    title: '使用 javascript 手写 json-parse',
    excerpt: '花了一下午加一晚上，用 JavaScript 从零手写一个 JSON.parse。',
    tags: ['实用分享'],
    minutes: 10,
  },
]

/** 全站标签云（含文章数）。复刻自 /blog 页 Tags 侧栏。 */
export const tags = [
  { name: '实用分享', count: 12 },
  { name: '记录点滴', count: 7 },
  { name: '闲言碎语', count: 4 },
  { name: 'Typecho', count: 3 },
  { name: '开源', count: 3 },
  { name: '灵感相关', count: 3 },
  { name: '主机', count: 2 },
  { name: '本站', count: 2 },
  { name: '阿里云', count: 2 },
  { name: '高中', count: 2 },
  { name: '高考', count: 2 },
  { name: 'Astro', count: 1 },
  { name: 'css', count: 1 },
  { name: 'html', count: 1 },
  { name: 'Markdown', count: 1 },
  { name: 'PHP', count: 1 },
  { name: '主页', count: 1 },
  { name: '主题', count: 1 },
  { name: '主题开发', count: 1 },
  { name: '事故', count: 1 },
  { name: '制作', count: 1 },
  { name: '实践', count: 1 },
  { name: '建站', count: 1 },
  { name: '摔跤', count: 1 },
  { name: '旅行', count: 1 },
  { name: '朋友圈', count: 1 },
  { name: '毕业', count: 1 },
  { name: '组装', count: 1 },
  { name: '网站', count: 1 },
  { name: '西安', count: 1 },
  { name: '迁移', count: 1 },
]
