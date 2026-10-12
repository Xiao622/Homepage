/**
 * 站点信息 / 关于页 Mock 数据 —— 摘自 Leo.net/about。
 */

export const siteInfo = {
  name: 'Leo',
  motto: '做自己，感受每个年纪的美好和烦恼',
  mottoOrigin: '— 写代码时的一个念头',
  footer: '© 2026 Leo · Built with Vue 3 + Vite',
  rss: '#',
  github: 'https://github.com/Xiao622',
  email: 'mailto:i@Leo.net',
}

export const about = {
  intro:
    '我是 Leo，一个喜欢安静工具和干净画面的人。2020 年开始写博客，一直想把它做成「家」的样子——写写字，做点小工具，记录生活里的碎屑。',
  contacts: [
    { label: 'GitHub', value: 'Xiao622', href: 'https://github.com/Xiao622' },
    { label: 'Email', value: 'i@Leo.net', href: 'mailto:i@Leo.net' },
    { label: 'QQ', value: '3509679579', href: '#' },
    { label: '微信', value: 'Leoynet', href: '#' },
  ],
  stats: [
    { label: '文章', value: 31 },
    { label: '写作', value: '6 年' },
    { label: '身高', value: '182cm' },
    { label: '坐标', value: '河南' },
  ],
  history: [
    {
      year: '2020',
      title: '开始写博客',
      text: '用 Typecho 搭了第一个站，写下了第一篇水文，从此开始“慢慢写字”。',
    },
    {
      year: '2022',
      title: '折腾开源',
      text: '写了选课排课工具、Typecho 主题，在 GitHub 上认识了不少朋友。',
    },
    {
      year: '2024',
      title: '重构为 Astro',
      text: '从动态博客迁移到静态生成，开始自己手搓想要的一切。',
    },
    {
      year: '2026',
      title: '现在的样子',
      text: '一个安静的、可拖拽的、由几十种魔法块组成的家。',
    },
  ],
  skills: [
    { name: 'JavaScript', level: 88 },
    { name: 'Node.js', level: 82 },
    { name: 'Vue 3', level: 78 },
    { name: 'TypeScript', level: 76 },
    { name: 'PHP', level: 60 },
  ],
  traits: [
    { title: '自主学习', text: '独立钻研，在技术细节上精益求精' },
    { title: '记录生活', text: '除了技术，也分享生活的琐碎与心情' },
    { title: '用代码偷懒', text: '重复 > 60s 的事，就写个脚本处理掉' },
  ],
  projects: [
    { name: 'typecho-theme-Leo', status: 'stable', desc: 'An elegant theme based on Typecho design', tags: ['Typecho', 'PHP', '主题'] },
    { name: 'homepage', status: 'stable', desc: '我的个人主页，简约主页，个人导航', tags: ['CSS', '主页'] },
    { name: 'fuck-shuwei', status: 'active', desc: '综合树维教务系统的选课排课客户端，基于 Node.js', tags: ['JavaScript', 'Node.js', '选课'] },
    { name: 'ruijie-electron', status: 'active', desc: '锐捷校园网认证工具，Windows / Electron', tags: ['Electron', 'JavaScript', '工具'] },
    { name: 'fuckDemo', status: 'demo', desc: '平时做的一些小 demo 合集', tags: ['Vue', 'Demo'] },
    { name: 'Xiao622', status: 'stable', desc: '我的个人仓库', tags: ['Profile'] },
  ],
  features: [
    { icon: '⚡', title: '静态生成', text: 'Astro 驱动，速度起飞' },
    { icon: '🧲', title: '卡片可拖拽', text: '首页布局你说了算' },
    { icon: '🪄', title: '魔法块系统', text: '十几类内容块，丰富内容呈现' },
    { icon: '🌙', title: '深夜可读', text: '自带暗色模式' },
    { icon: '💬', title: '评论在线', text: 'Giscus 驱动' },
  ],
  closing:
    '人独自行过生命，蒙受玷污，承担罪过，痛饮苦酒，寻觅出路。',
}

/** 首页技术栈标签跑马灯 */
export const marqueeTags = [
  'TypeScript',
  'React',
  'Astro',
  'Tailwind',
  'Node.js',
  'Git',
  'Docker',
  'REST',
  'Figma',
  'Typography',
  'Color',
  'Terminal',
  'Writing',
  'Running',
]
