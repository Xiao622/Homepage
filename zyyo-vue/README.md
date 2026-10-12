# Leo-vue

[Leo.net](https://Leo.net/) 个人站点的前端复刻 —— **Vue 3 + Vite** 项目。

复刻内容包括：首页卡片画板（Hero / Now Playing / Now 时钟 / 主题切换 / 名言 / 终端 Console / 站点信息）、标签跑马灯、Blog 列表、Notes 随记时间线、About 关于页、Links 友链页、Photos 相册页，以及音乐播放器抽屉与明暗主题切换。

## 快速开始

```bash
npm install
npm run dev        # 开发服务器 → http://localhost:5173
npm run build      # 生产构建（输出 dist/）
npm run preview    # 预览构建产物
```

## 项目结构

```
Leo-vue/
├── index.html                 # HTML 入口（字体、meta）
├── vite.config.js             # Vite 配置 + /api 代理（指向 Spring Boot）
├── .env.development           # 开发环境变量（VITE_USE_MOCK=false，已切真实后端接口）
├── .env.production            # 生产环境变量（默认真实接口）
└── src/
    ├── main.js                # 应用入口
    ├── App.vue                # 根组件（Loader + 布局 + 路由出口）
    ├── router/index.js        # 路由表（与页面目录一一对应）
    ├── api/                   # ★ 接口层（Spring Boot 对接入口）
    │   ├── http.js            #   axios 实例：拦截器、统一返回体解包
    │   ├── mock.js            #   Mock 延迟适配
    │   └── modules/           #   按业务分模块：blog/notes/links/photos/site/music
    ├── data/                  # Mock 数据（源站公开内容，接口就绪后可移除）
    ├── assets/styles/main.css # 设计系统：主题变量、卡片、按钮、动效
    ├── composables/           # 组合式函数：useTheme / useNow / useMusicPlayer
    ├── components/
    │   ├── layout/            # TopBar（导航/主题开关）· SiteFooter
    │   ├── home/              # 首页卡片：Hero / NowPlaying / Now / Theme / Quote / Console / SiteInfo / TagMarquee
    │   ├── common/            # PlaceholderPhoto 等通用组件
    │   └── music/             # MusicPlayer 音乐抽屉
    └── views/                 # 页面：Home / Blog / Notes / About / Links / Photos
```

## 对接 Spring Boot（三步）

1. **后端实现统一返回体**（`http.js` 已按此约定解包）：
   ```json
   { "code": 0, "message": "ok", "data": { } }
   ```
2. **关闭 Mock、指向后端**：`.env.development` 中
   `VITE_USE_MOCK=false`，`VITE_API_BASE_URL=/api`。
   `vite.config.js` 已配置 `/api` 代理到 `http://localhost:8080`（可按实际端口修改）。
3. **接口对照表**（`src/api/modules/` 每个函数都标注了对应 REST 路径）：

   | 模块 | 方法 | 真实接口 |
   | --- | --- | --- |
   | blog | `getPosts` / `getTags` / `getPost` | `GET /posts` · `GET /posts/tags` · `GET /posts/{id}` |
   | notes | `getNotes` | `GET /notes?order=time\|random` |
   | links | `getFriends` / `getUpdates` | `GET /links` · `GET /links/updates` |
   | photos | `getCategories` / `getPhotos` | `GET /photos/categories` · `GET /photos?category=` |
   | site | `getSiteInfo` / `getAbout` / `getMarqueeTags` | `GET /site` · `GET /about` · `GET /site/marquee-tags` |
   | music | `getTracks` | `GET /music/tracks` |

   页面组件只依赖 `@/api` 模块，切换 Mock / 真实接口无需改动组件代码。

## 复刻说明与版权

- 页面文案、文章/随记/友链等数据均摘自 Leo.net 公开页面，仅用于前端结构复刻与学习；
- 站点真实相片、音频属于原作者，复刻中以 SVG 占位图 / 模拟播放进度代替，接口就绪后替换；
- 设计上复刻了源站的核心视觉语言：Noto Serif SC 衬线字体、卡片 + 毛玻璃、暗色主题、品牌红 `#ff8282`、金色时钟、红色扫描线 Loader 等。
