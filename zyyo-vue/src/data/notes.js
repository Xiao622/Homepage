/**
 * 随记（Notes）Mock 数据 —— 一句话式生活记录，摘自 Leo.net/notes。
 * expandable: true 表示原始卡片可展开查看更多，这里给出全文。
 */
export default [
  { id: 1, date: '2026-9-17', text: '哈哈，也正好拦住了访问不坚定的人', expandable: false },
  {
    id: 2,
    date: '2026-9-16',
    text: '部署到 cf 网站变得好慢',
    expandable: false,
  },
  {
    id: 3,
    date: '2026-9-14',
    text: '新的博客上线到 Leo.net，内部开发了很久终于上线了',
    expandable: false,
  },
  {
    id: 4,
    date: '09-11',
    title: '播放器水合闪烁',
    text: '彻底修好了进首页“卡片飞一下”的问题：不是 CSS transition 的锅，是原生 View Transition 抓了重排前的快照。把测量都挪到绘制前跑完，浏览器再也抓不到中间态。',
    expandable: true,
  },
  {
    id: 5,
    date: '09-10',
    text: '音乐抽屉在手机上改成了底部弹层，桌面还是右侧悬浮卡。歌词能滚动了，非当前行做虚化，鼠标一悬或一滚就清晰，松手即回——不糊脸。',
    expandable: false,
  },
  {
    id: 6,
    date: '09-08',
    title: '清晨',
    text: '五点半出门，桂花整排整排地香。跑到桥上停下来喘气，天边是那种将亮未亮的青灰色，很安静，只有江风和水声。',
    expandable: false,
  },
  {
    id: 7,
    date: '09-05',
    text: '把首页那张地图从 d3 矢量一路换成 MapLibre，最后发现公共 OSM 瓦片在自己网络里根本连不上，改高德的源才稳。折腾一整天，值。',
    expandable: false,
  },
  {
    id: 8,
    date: '09-02',
    title: '换个键盘手感',
    text: '试了 68 配列，方向键不用低头找，节奏舒服很多。外壳选了雾蓝，低调耐看，比之前那个花哨的 RGB 灯效适合坐一天。',
    expandable: false,
  },
  {
    id: 9,
    date: '08-30',
    text: '首页卡片的拖拽终于不“粘鼠标”了。之前松手后幽灵卡片还赖在光标上，是因为个别孤岛自己捕获了指针。补了 window 级兜底 + 统一收尾，稳了。',
    expandable: true,
  },
  {
    id: 10,
    date: '08-28',
    text: '今晚的风很软，把阳台的衣服吹得鼓起来，像有人在里面跳舞。收衣服的时候发现被太阳晒得温热，抱着上楼，有种很小的幸福感。',
    expandable: false,
  },
  {
    id: 11,
    date: '08-25',
    title: '卡片忽大忽小',
    text: '拖拽时卡片会莫名改变大小，根因是布局用 CSS 变量算的、和 React 内联 style 打架。改成每张卡固定 span、绝对定位只动 transform，彻底告别“变形”。卡片尺寸恒定，才谈得上高级感。',
    expandable: true,
  },
  {
    id: 12,
    date: '08-22',
    title: '读《设计的设计》',
    text: '原研哉讲“emptiness”不是空，而是留出容纳的余地。做界面时总想塞满，其实克制更难。首页背景后来被我调成近乎纯白，眼睛一下子松了。',
    expandable: false,
  },
  {
    id: 13,
    date: '08-19',
    text: '导航的 logo 和黑夜开关从药丸里拆出来，做成两颗圆钮一左一右，像灵动岛。favicon 那个圆在移动端要求填满无留白、桌面留白，来回调了好几轮才顺眼。',
    expandable: false,
  },
  {
    id: 14,
    date: '08-16',
    title: '加载动画',
    text: '那个“Leo”loader 之前字母右边会一闪一闪缺角，其实是 clip-path 把每个字母上下裁没了又放回来。去掉裁切只留一条红色扫描线扫过，干净了。字体不再是“一抽一抽”地加载。',
    expandable: true,
  },
  {
    id: 15,
    date: '08-12',
    text: '给音乐播放器接了公共镜像的网易云歌单，能整首播放还有实时歌词。缺点是首次点击“随机播放”常被浏览器自动播放策略拦一下，得再点一次——这属于平台限制，认了。',
    expandable: false,
  },
  {
    id: 16,
    date: '08-09',
    text: '把三个社交按钮做成“左上细亮边、右下粗暗边”的伪 3D，按下去会真的塌一截。白天又嫌它扎眼，调成几乎融进背景的灰，悬停才亮一点。审美这件事，来回改七八次也不为过。',
    expandable: false,
  },
  {
    id: 17,
    date: '08-05',
    text: '修了一下午的 bug，最后发现是少写了一个 await。气笑了，顺手把咖啡续上，重新泡了一杯。',
    expandable: false,
  },
  {
    id: 18,
    date: '08-01',
    text: '给博客加了“随记”这个页面，像朋友圈一样零散记点东西：跑过的路、看过的书、修过的 bug。以后不必每件事都攒成一篇长文，一句话也值得留下。',
    expandable: false,
  },
]
