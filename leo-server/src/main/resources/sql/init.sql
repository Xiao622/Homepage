-- ============================================================
-- leo_blog 数据库初始化脚本
-- 用途：Leo 个人站点（复刻 zyyo.net）Home / Blog / Notes 数据
-- 字符集：utf8mb4，排序规则：utf8mb4_unicode_ci
-- ============================================================

CREATE DATABASE IF NOT EXISTS `leo_blog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `leo_blog`;

-- ---------- 站点基础信息（key-value） ----------
DROP TABLE IF EXISTS `t_site_config`;
CREATE TABLE `t_site_config` (
  `config_key`   VARCHAR(64)  NOT NULL COMMENT '配置键',
  `config_value` TEXT         NULL COMMENT '配置值',
  PRIMARY KEY (`config_key`)
) ENGINE = InnoDB COMMENT = '站点基础信息';

-- ---------- 首页标签跑马灯 ----------
DROP TABLE IF EXISTS `t_marquee_tag`;
CREATE TABLE `t_marquee_tag` (
  `id`   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64)     NOT NULL COMMENT '标签名',
  `sort` INT             NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '首页技术栈跑马灯标签';

-- ---------- 博客文章 ----------
CREATE TABLE `t_post` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `slug` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `post_date` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布日期，展示用（如 2026.09.16）',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `excerpt` text COLLATE utf8mb4_unicode_ci COMMENT '摘要',
  `content` mediumtext COLLATE utf8mb4_unicode_ci,
  `minutes` int NOT NULL DEFAULT '5' COMMENT '阅读时长（分钟）',
  `tags` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '标签，逗号分隔（FIND_IN_SET 查询）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slug` (`slug`),
  KEY `idx_date` (`post_date`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客文章';

-- ---------- 标签云 ----------
CREATE TABLE `t_tag` (
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `count` int NOT NULL DEFAULT '0' COMMENT '该标签下文章数',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客标签云';

-- 随记 ----------
DROP TABLE IF EXISTS `t_note`;
CREATE TABLE `t_note` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `note_date`  VARCHAR(20)     NOT NULL COMMENT '日期，展示用（如 09-11 / 2026-9-17）',
  `title`      VARCHAR(255)    NULL COMMENT '可选标题',
  `text`       TEXT            NOT NULL COMMENT '正文',
  `expandable` TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否可展开',
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '随记';

-- ---------- 相册照片 ----------
DROP TABLE IF EXISTS `t_photo`;
CREATE TABLE `t_photo` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `url`        VARCHAR(500)    NOT NULL COMMENT '访问路径（如 /uploads/photos/xxx.jpg）',
  `category`   VARCHAR(50)     NOT NULL DEFAULT '未分类' COMMENT '分类名（自由文本，即相册名）',
  `taken_date` VARCHAR(20)     NULL COMMENT '拍摄时间（如 2026-09 / 2026-09-12）',
  `caption`    VARCHAR(200)    NULL COMMENT '说明文字（作者描述）',
  `created_at` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_taken_date` (`taken_date`)
) ENGINE = InnoDB COMMENT = '相册照片';

-- ---------- 照片评论 ----------
DROP TABLE IF EXISTS `t_photo_comment`;
CREATE TABLE `t_photo_comment` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `photo_id`   BIGINT UNSIGNED NOT NULL COMMENT '所属照片 id',
  `author`     VARCHAR(64)     NOT NULL COMMENT '评论者昵称',
  `content`    VARCHAR(500)    NOT NULL COMMENT '评论内容',
  `created_at` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_photo` (`photo_id`)
) ENGINE = InnoDB COMMENT = '照片评论';

-- ---------- 音乐曲目 ----------
DROP TABLE IF EXISTS `t_track`;
CREATE TABLE `t_track` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title`      VARCHAR(100)    NOT NULL COMMENT '曲名（中文）',
  `en_title`   VARCHAR(120)    NULL COMMENT '英文曲名',
  `artist`     VARCHAR(100)    NULL COMMENT '艺术家',
  `duration`   INT             NOT NULL DEFAULT 0 COMMENT '时长（秒）',
  `cover_seed` INT             NOT NULL DEFAULT 0 COMMENT '封面渐变种子（前端按种子生成占位封面）',
  `url`        VARCHAR(500)    NULL COMMENT '真实音频地址（uploads/music/ 相对路径）',
  `cover`      VARCHAR(500)    NULL COMMENT '真实封面图片地址（uploads/music/covers/ 相对路径）',
  `created_at` TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT = '音乐曲目';

-- ============================================================
-- 种子数据
-- ============================================================

-- 站点信息
INSERT INTO `t_site_config` (`config_key`, `config_value`) VALUES
('name',         'Leo'),
('motto',        '好的工具是安静的。用完之后，它会自己走开。'),
('motto_origin', '— 写代码时的一个念头'),
('footer',       '© 2026 Leo · Built with Vue 3 + Vite'),
('rss',          '#'),
('github',       'https://github.com/Leo666'),
('email',        'mailto:i@Leo.net');

-- 关于页完整数据（整体 JSON 存储，后端解析返回）
INSERT INTO `t_site_config` (`config_key`, `config_value`) VALUES
('about', '{"intro":"我是 Leo，一个喜欢安静工具和干净画面的人。2020 年开始写博客，一直想把它做成「家」的样子——写写字，做点小工具，记录生活里的碎屑。","contacts":[{"label":"GitHub","value":"Leo666","href":"https://github.com/Leo666"},{"label":"Email","value":"i@Leo.net","href":"mailto:i@Leo.net"},{"label":"QQ","value":"3509679579","href":"#"},{"label":"微信","value":"Leoynet","href":"#"}],"stats":[{"label":"文章","value":31},{"label":"写作","value":"6 年"},{"label":"身高","value":"182cm"},{"label":"坐标","value":"河南"}],"history":[{"year":"2020","title":"开始写博客","text":"用 Typecho 搭了第一个站，写下了第一篇水文，从此开始“慢慢写字”。"},{"year":"2022","title":"折腾开源","text":"写了选课排课工具、Typecho 主题，在 GitHub 上认识了不少朋友。"},{"year":"2024","title":"重构为 Astro","text":"从动态博客迁移到静态生成，开始自己手搓想要的一切。"},{"year":"2026","title":"现在的样子","text":"一个安静的、可拖拽的、由几十种魔法块组成的家。"}],"skills":[{"name":"JavaScript","level":88},{"name":"Node.js","level":82},{"name":"Vue 3","level":78},{"name":"TypeScript","level":76},{"name":"PHP","level":60}],"traits":[{"title":"自主学习","text":"独立钻研，在技术细节上精益求精"},{"title":"记录生活","text":"除了技术，也分享生活的琐碎与心情"},{"title":"用代码偷懒","text":"重复 > 60s 的事，就写个脚本处理掉"}],"projects":[{"name":"typecho-theme-Leo","status":"stable","desc":"An elegant theme based on Typecho design","tags":["Typecho","PHP","主题"]},{"name":"homepage","status":"stable","desc":"我的个人主页，简约主页，个人导航","tags":["CSS","主页"]},{"name":"fuck-shuwei","status":"active","desc":"综合树维教务系统的选课排课客户端，基于 Node.js","tags":["JavaScript","Node.js","选课"]},{"name":"ruijie-electron","status":"active","desc":"锐捷校园网认证工具，Windows / Electron","tags":["Electron","JavaScript","工具"]},{"name":"fuckDemo","status":"demo","desc":"平时做的一些小 demo 合集","tags":["Vue","Demo"]},{"name":"Leo666","status":"stable","desc":"我的个人仓库","tags":["Profile"]}],"features":[{"icon":"⚡","title":"静态生成","text":"Astro 驱动，速度起飞"},{"icon":"🧲","title":"卡片可拖拽","text":"首页布局你说了算"},{"icon":"🪄","title":"魔法块系统","text":"十几类内容块，丰富内容呈现"},{"icon":"🌙","title":"深夜可读","text":"自带暗色模式"},{"icon":"💬","title":"评论在线","text":"Giscus 驱动"}],"closing":"如果你也是慢慢生活、慢慢写东西的人，欢迎来打个招呼。邮件回得慢，但我一定会回。"}');

-- 管理员凭证（演示项目明文存储；生产环境应改为哈希 + 盐）
-- admin_accounts 为多账户 JSON 数组（后端登录校验优先读取此键）；
-- admin_user / admin_password 保留为单账户兼容（admin_accounts 缺失时回退使用）。
INSERT INTO `t_site_config` (`config_key`, `config_value`) VALUES
('admin_user',     'fuxiaotian19'),
('admin_password', 'dilraba.8'),
('admin_accounts', '[{"username":"fuxiaotian19","password":"dilraba.8"},{"username":"123123","password":"123123"}]');

-- Home 页可编辑文字（整体 JSON，编辑模式保存后整体覆盖；footer 会同步写 t_site_config.footer。
-- 仅 hero_name / hero_intro / quote_text / footer 可编辑，其余首页文字为前端固定文案）
INSERT INTO `t_site_config` (`config_key`, `config_value`) VALUES
('home_content', '{"hero_name":"Leo.","hero_intro":"I build slowly — notes on code and design, sketches of daily life, and small tools that make ordinary days smoother. I''ve kept a blog since 2020. Outside the screen, I run, sketch, and photograph what catches my eye. Running taught me to finish what I start; writing taught me that some things are better kept quiet.","quote_text":"Good tools are quiet. When you''re done with them, they step away.","footer":"© 2026 Leo · Built with Vue 3 + Vite"}');

-- 跑马灯标签
INSERT INTO `t_marquee_tag` (`name`, `sort`) VALUES
('TypeScript', 1), ('React', 2), ('Astro', 3), ('Tailwind', 4), ('Node.js', 5),
('Git', 6), ('Docker', 7), ('REST', 8), ('Figma', 9), ('Typography', 10),
('Color', 11), ('Terminal', 12), ('Writing', 13), ('Running', 14);

-- 博客文章：14 篇开发记录（slug = 年月日时分编号）
INSERT INTO `t_post` (`id`, `slug`, `post_date`, `title`, `excerpt`, `minutes`, `tags`, `content`) VALUES
(1, '202609220900', '2026.09.22', 'Leo 站点开发记录 #01：项目启动，技术选型与工程搭建', '复刻 zyyo.net 的第一步：选定 Vue 3 + Vite 技术栈，搭好分层目录、axios 接口层与路由骨架，为后续十个阶段打地基。', 6, '本站,开发,Vue,Vite', '<h2>为什么是复刻练习</h2><p>想把 zyyo.net 的界面结构与视觉风格完整搬下来，再逐步改造成自己的样子。第一步不是写代码，而是把目标拆清楚：先做出「长得像」的静态页面，再接入真实数据，最后做成可编辑的完整站点。</p><h2>技术栈选型</h2><ul><li>前端：Vue 3（组合式 API + &lt;script setup&gt;）+ Vite 开发服务器，热更新开发效率高。</li><li>路由：vue-router（history 模式），页面组件统一放在 src/views。</li><li>请求：axios 统一封装，拦截器自动处理统一返回体 { code, message, data }，预留与 Spring Boot 对接的接口层。</li><li>主题：CSS 变量驱动的浅色 / 深色双主题。</li></ul><h2>工程结构</h2><p>src 下按职责分层：views（页面）、components（可复用组件）、composables（组合式逻辑）、api（接口模块统一出口）、data（Mock 数据）、router、styles。</p><h2>接口层预留</h2><p>api/index.js 作为统一出口，每个业务一个 modules 文件；http.js 内置 axios 实例与响应拦截器。开发环境 .env.development 设 VITE_USE_MOCK=false 即走真实接口，true 则回落到 Mock 数据——前端可以在后端就绪前独立开发。</p><h2>遇到的坑</h2><ul><li>SPA 路由刷新 404：开发阶段交给 Vite 的 history 回退，生产部署需要服务器把未匹配路由指回 index.html。</li><li>跨域：开发期用 Vite 代理 /api 与 /uploads 到后端 8080，绕开 CORS 配置问题。</li></ul>'),
(2, '202609231100', '2026.09.23', 'Leo 站点开发记录 #02：站点骨架，Home 布局与全局组件', '搭出第一个可见版本：顶栏导航、Home 3×3 网格（Hero / Now Playing / 时钟 / Console / Theme / Photos / SiteInfo）、音乐抽屉与加载动画。', 7, '本站,开发,前端,布局', '<h2>顶部导航</h2><p>Logo（悬停出现扫描线动画）+ 站点菜单 + 右侧操作区：音乐抽屉开关、主题开关、账户按钮；移动端收起为汉堡菜单。菜单顺序 Home · Blog · Notes · Photos · Music · About，About 固定在中间导航最右侧。</p><h2>Home 的 3×3 网格</h2><ul><li>Hero 自我介绍卡：左侧文案 + 右侧 GLB 3D 模型，支持鼠标左右拖动旋转（Three.js 加载本地 .glb 文件）。</li><li>Now Playing 音乐卡：展示当前播放曲目，与顶栏抽屉共用全局播放器状态。</li><li>Now 时钟卡：实时时钟与日期（JS 定时器驱动）。</li><li>Console 终端卡：打字机效果，问题永远在第一行、回答永远在第二行，播完轮回复位。</li><li>Theme 主题切换卡、Photos 相册卡、SiteInfo 站点信息卡（社交链接 + 跑马灯标签）。</li></ul><h2>全局状态</h2><p>音乐播放器用模块级单例 composable（useMusicPlayer）：顶栏抽屉、Home NowPlaying、Music 页三方共享同一份播放列表与播放状态，切换页面不中断。</p><h2>加载动画</h2><p>首屏展示 Leo 品牌加载动画，字母逐个浮现 + 扫描线划过。这个动画后来出了个著名的 bug，见下一篇。</p>'),
(3, '202609241200', '2026.09.24', 'Leo 站点开发记录 #03：品牌迁移，Zyyo → Leo 与「Zppo」谜案', '全站字段替换 Zyyo → Leo 后，加载动画仍显示 Zppo？排查发现品牌文案不止存在于页面，还藏在站点配置 JSON 里。', 4, '本站,开发,Bug', '<h2>批量替换</h2><p>把页面里所有 Zyyo 字段替换为 Leo：Logo、标题、页脚、文案。用全局搜索逐处核对，确保没有遗漏。</p><h2>加载动画残留 Zppo</h2><p>替换完成后，第一次进入站点仍看到加载动画显示 Zppo，而不是 Leo。页面代码里明明已经全部替换，为什么还有残留？</p><p>排查发现：加载动画的文案并不在页面代码里，而是从后端站点配置（t_site_config 的 name 字段）读取——数据层是另一个「页面」。</p><h2>修复</h2><p>把数据库里站点配置的 name 同步改为 Leo，并把这个教训记下来：全站品牌字段 = 前端文案 + 后端数据 + 第三方配置，三处都要查。</p><h2>经验</h2><blockquote>替换关键词时，永远要覆盖「代码」和「数据」两个层面；动画、标题这类看似前端的文案，来源可能是接口。</blockquote>'),
(4, '202609251000', '2026.09.25', 'Leo 站点开发记录 #04：后端与数据库，Spring Boot + MyBatis-Plus + MySQL', '建库建表 8 张，统一返回体与登录 Token；五个业务模块的查询接口全部就绪。顺带解决端口占用与中文乱码两个环境坑。', 8, 'Spring Boot,MyBatis-Plus,MySQL,后端', '<h2>数据库设计</h2><p>按参考图建好 leo_blog 数据库，共 8 张表：</p><ul><li>t_post 文章（slug 唯一键、日期、标题、摘要、正文、阅读时长、标签）</li><li>t_tag 标签云、t_note 随记、t_site_config 站点配置（JSON 存储 about 整段数据）</li><li>t_marquee_tag 跑马灯标签、t_photo 相册照片、t_photo_comment 照片评论、t_track 音乐曲目</li></ul><h2>后端结构</h2><ul><li>Spring Boot 3.2.5 + MyBatis-Plus（LambdaQueryWrapper 链式查询）+ Lombok。</li><li>统一返回体 Result { code, message, data }，登录校验失败返回 401。</li><li>登录：账号密码存 t_site_config（admin_accounts JSON 数组，支持多管理员），TokenStore 内存存储、30 分钟过期。</li><li>控制器：Post / Note / Site / Photo / Music 五个，查询接口全部公开。</li></ul><h2>接口一览</h2><p>文章列表 / 详情 / 标签云、随记列表（时间 / 随机 / 关键词搜索）、站点信息、照片列表（分类 + 年月筛选 + 相册分组）、曲目列表。所有写操作（增删改）都要求登录 token。</p><h2>遇到的坑</h2><ul><li>8080 端口被占用：后端启动失败提示 Web server failed to start，杀掉监听进程后重启解决。</li><li>中文写入乱码：PowerShell / MySQL CLI 编码不稳，统一改用 Python + pymysql 写入数据库，UTF-8 全程可控。</li><li>上传文件静态映射 404：WebConfig 静态资源映射不生效，改为 Controller 用 FileSystemResource 按文件名回放文件流，走 /uploads 代理访问。</li></ul>'),
(5, '202609272000', '2026.09.27', 'Leo 站点开发记录 #05：Home 编辑模式，管理员登录与原地编辑', '顶栏账户按钮登录后进入编辑模式：页面文字 contenteditable 原地编辑、浮动保存条整体提交；固定文案锁定不可改。修复了编辑不生效的经典 bug。', 6, '编辑模式,登录,本站', '<h2>管理员体系</h2><p>顶栏账户按钮打开登录弹窗，输入管理员账号密码（fuxiaotian19 / dilraba.8 与 123123 / 123123）进入编辑模式；再次点击铅笔图标退出。登录状态存 localStorage，请求自动附带 Bearer token。</p><h2>原地编辑</h2><p>编辑模式下页面文字变为 contenteditable，直接点文字修改；有改动时右下角浮动保存条提示「修改模式 · 有未保存修改」，点击保存整体 PUT /api/home 提交。</p><h2>固定文案锁定</h2><p>部分文案（Toggle theme、Broken thoughts、Now Playing、相片集等）属于设计的一部分，不参与编辑，保证布局与品牌一致性。</p><h2>经典 Bug：编辑了但没生效</h2><p>进入编辑模式修改自我介绍并保存后，退出编辑模式发现内容没变化；再进入编辑模式，又显示的是修改后的文字。</p><p>根因：展示模式从接口数据渲染，编辑模式的 contenteditable 是另一份副本；保存成功后展示层没有同步刷新。修复：保存接口返回最新数据，成功后回填展示层 DOM；并统一回填逻辑（v-fill 指令只回填未聚焦字段，不覆盖用户正在输入的内容）。</p><h2>经验</h2><blockquote>「编辑副本」与「展示视图」是两份数据时，保存成功后的同步回填是必须的一步，否则用户会看到编辑失效的假象。</blockquote>'),
(6, '202609281500', '2026.09.28', 'Leo 站点开发记录 #06：Blog 列表、详情页与目录列', 'Blog 页成型：列表卡片（日期 + 标题 + 摘要 + 标签）、详情页正文 + 右侧 On this page 目录列（滚动高亮），去掉列表卡片右上角的分钟数。', 6, '本站,Blog,前端', '<h2>列表页</h2><p>卡片式列表：日期 + 标题 + 摘要 + 标签，点击进入详情。按日期倒序，支持标签筛选。细节：列表卡片右上角原本显示「5 min / 6 min」阅读时长，被去掉——阅读时长只在详情页保留。</p><h2>详情页</h2><ul><li>文章头：标题、日期、标签；正文富文本 HTML 渲染。</li><li>右侧「On this page」目录列：自动提取正文中的 h1/h2/h3 生成目录，滚动时高亮当前章节，点击平滑跳转——完全还原参考站。</li><li>响应式：窄屏（&lt; 860px）隐藏目录列，正文占满。</li></ul><h2>正文渲染细节</h2><p>正文用 v-html 渲染后端返回的 HTML；代码、列表、引用块等样式通过 :deep() 选择器作用到内容上。摘要字段在正文为空时作为回退展示。</p><h2>遇到的坑</h2><ul><li>目录提取时机：v-html 渲染是异步的，必须在 nextTick 之后才能取到标题节点。</li><li>滚动高亮：监听 window scroll，按标题距顶部位置计算当前章节。</li></ul>'),
(7, '202609291600', '2026.09.29', 'Leo 站点开发记录 #07：Blog 页面内编辑与 Word 同款样式栏', '编辑模式升级：放弃弹窗，直接在详情页内编辑；选中文字弹出 Word 同款样式栏（加粗、字号、颜色）；新建文章走空模板页，slug 用年月日时分编号。', 8, 'Blog,编辑器,本站', '<h2>页面内编辑</h2><p>第一版编辑模式用弹窗，用户反馈不舒服，改为与 Home 页一致的页面内编辑：详情页直接点标题、日期、标签、正文修改，浮动保存条统一保存。</p><h2>Word 同款样式栏</h2><p>编辑正文时选中文字，出现悬浮样式栏：加粗、斜体、字号、文字颜色、清除格式。实现：document.execCommand + styleWithCSS，操作选中范围；样式栏跟随选区位置弹出，失焦隐藏。</p><h2>新建文章</h2><p>列表页「＋ New post」跳转空模板页（/blog/new），填写标题 / 日期 / 摘要 / 正文 / 标签后保存。保存成功跳转到新文章详情页。</p><h2>slug 编号方案</h2><p>参考站路由用标题拼音（typecho-to-astro），我们决定改用更简单的方案：年月日时分编号（如 202609292009），同分钟冲突自动加后缀，避免中文 / 拼音的歧义与重名问题。</p><h2>删除</h2><p>编辑模式下详情页显示「✕ 删除」，二次确认后删除并返回列表。删除时同步标签云计数（文章标签 count -1，归零自动移除）。</p><h2>遇到的坑</h2><ul><li>execCommand 已废弃但仍是 contenteditable 唯一可靠的富文本操作方式，注意 styleWithCSS 的兼容性。</li><li>样式栏的选区定位：用 getSelection + getBoundingClientRect 计算，滚动 / 输入变化时要重新定位。</li></ul>'),
(8, '202609301400', '2026.09.30', 'Leo 站点开发记录 #08：Notes 随记，当前页编辑与搜索框', '随记页时间线成型：当前页直接编辑时间与内容、可新建删除；Notes 与 Blog 页接入 300ms 防抖模糊搜索，标题与正文都能搜到。', 5, 'Notes,搜索,本站', '<h2>随记时间线</h2><p>Notes 页按日期倒序展示一条条随记：日期 + 内容，长文可展开。编辑模式直接在当前页修改日期与内容（contenteditable），「＋ New note」在顶部插入空卡片，卡片右上角 ✕ 删除，改动统一浮动保存条提交。</p><h2>搜索框</h2><p>Notes 与 Blog 页各加一个搜索框：输入 300ms 防抖后请求后端 keyword 参数，对标题与正文做 LIKE 模糊查询，空结果给出提示。</p><h2>排序逻辑</h2><p>种子随记的日期格式混杂（2026-9-17 与 09-11 并存），后端用「年份补全 + 月日补零」的语义键在内存排序，保证新建随记自然置顶，跨格式日期也能正确排序。</p><h2>遇到的坑</h2><ul><li>防抖：连续输入触发多次请求，setTimeout 300ms 合并，避免接口压力与列表闪烁。</li><li>contenteditable 回填覆盖用户输入：回填只在编辑模式切换 / 数据加载时执行，正在编辑的元素跳过（沿用 v-fill 的思路）。</li></ul>'),
(9, '202610011300', '2026.10.01', 'Leo 站点开发记录 #09：About 页 87 个字段的编辑模式', '关于页全部字段接入编辑：data-path 定位 + 自定义 v-fill 指令回填；保存时自动还原类型（tags 数组、status 中英文、数字字段）。', 6, 'About,编辑模式,本站', '<h2>87 个可编辑字段</h2><p>About 页数据是一整段 JSON（intro、contacts、stats、history、skills、traits、projects、features、closing、motto 等），共 87 个展示字段。全部接入编辑模式：每个文本节点带 data-path 属性定位到 JSON 里的具体路径。</p><h2>自定义指令 v-fill</h2><p>写了一个 v-fill 指令：进入编辑模式时，按 data-path 从后端数据回填 DOM；回填跳过正在聚焦（用户正在编辑）的元素，避免打断输入。展示模式则直接渲染接口数据。</p><h2>保存与类型还原</h2><p>保存时遍历 DOM 收集文本 → 按 data-path 写回 draft JSON → 整体 PUT /api/about。类型还原是关键：tags 拆回数组、status 中英文互转、数字字段 parseFloat 还原，避免「字符串数字」污染数据结构。</p><h2>遇到的坑</h2><ul><li>嵌套结构（projects 是对象数组）：data-path 用点路径如 projects.0.name，deepGet / deepSet 处理。</li><li>编辑模式切回展示模式必须重建 DOM，否则内容停留在编辑态。</li></ul>'),
(10, '202610031000', '2026.10.03', 'Leo 站点开发记录 #10：Photos 上传、删除、分类与年月筛选', '照片页基础功能：格式白名单上传、删除、自由分类、年月筛选；修复浅色主题下日历图标「消失」的 UA 控件颜色问题。', 6, 'Photos,相册,图片', '<h2>上传与删除</h2><p>编辑模式「＋ Upload」上传照片（multipart：文件 + 分类 + 拍摄时间 + 说明），后端做格式白名单校验（jpg/jpeg/png/gif/webp/bmp/avif/svg）；照片文件存 uploads/ 目录，数据库记录相对路径，Controller 按文件名回放文件流。</p><h2>分类与年月筛选</h2><p>每张照片可归入任意分类；列表页支持分类名模糊 + 年月（type=month 输入框）筛选，时间倒序。上传时可指定拍摄时间（精确到月或日）。</p><h2>编辑模式</h2><p>登录后每张照片卡片出现 ✕ 删除按钮与原地可编辑信息（分类 / 时间 / 说明），浮动保存条统一提交。</p><h2>Bug：浅色主题下日历图标不可见</h2><p>年月筛选的原生 date input 在白天模式下，右侧日历图标是白色，浅色背景上看不见。一开始给 .month-input::-webkit-calendar-picker-indicator 加 filter 想改图标颜色，无效——原生控件颜色只受 color-scheme 控制。</p><p>修复：input 的 color-scheme 跟随主题（浅色 light / 深色 dark），图标自动变成对应主题的颜色。</p><h2>经验</h2><blockquote>UA 控件（日历、滚动条等）的样式开关是 color-scheme，作者 CSS 改不动它；跟主题联动时直接切 color-scheme 而不是写 filter。</blockquote>'),
(11, '202610051000', '2026.10.05', 'Leo 站点开发记录 #11：Photos 相册化、大图弹窗与评论栏', '参照 QQ 相册升级为三级结构：相册列表 → 相册详情 → 大图弹窗（翻页、下载、弹窗内上传、作者描述 + 访客评论）。删除照片连带清评论。', 8, 'Photos,相册,评论', '<h2>三级相册结构</h2><ul><li>相册列表 /photos：按分类分组，封面取相册最新一张，显示张数，点击进入。</li><li>相册详情 /photos/:category：该相册照片网格 + 年月筛选 + 编辑模式上传 / 删除 / 原地改信息。</li><li>大图弹窗：点击任意照片弹出，QQ 相册风格。</li></ul><h2>大图弹窗功能</h2><ul><li>左右箭头 / 键盘 ← → 翻页，Esc 关闭，显示页码（如 2 / 3）。</li><li>「⭳ 下载」触发浏览器直接下载原图到本地。</li><li>编辑模式弹窗内「＋ 上传」：上传到当前相册，完成后自动跳到新照片。</li><li>右侧评论栏：上方作者描述（说明 + 拍摄时间），下方访客评论列表；访客无需登录即可评论（昵称 + 内容 + 时间戳）。</li></ul><h2>数据与接口</h2><p>新增 t_photo_comment 表；接口：GET /api/photos/albums（相册分组 + 封面）、GET/POST /api/photos/{id}/comments（评论读写）、DELETE /api/photos/{id}（删除照片与磁盘文件）。</p><h2>Bug：删除照片留下孤儿评论</h2><p>第一版删除照片只删照片行和文件，评论表里残留 photo_id 指向已删照片。修复：删除照片时连带删除该照片的全部评论（DELETE 前先按 photo_id 清理评论表）。</p><h2>响应式</h2><p>窄屏下弹窗评论栏自动移到图片下方，保证移动端可用。</p>'),
(12, '202610061400', '2026.10.06', 'Leo 站点开发记录 #12：Music 曲目墙与全局播放器联动', '新增 Music 页，布局参考深蓝互动官网 MUSIC 板块：3 张主推大卡 + 曲目网格；点击即播放，与顶栏抽屉、首页 Now Playing 三方联动。', 6, 'Music,音乐,播放器', '<h2>曲目墙布局</h2><p>参考 re.bluepoch.com 官网 MUSIC 板块，做成 Leo 风格：MUSIC 大标题 + 副文案，顶部 3 张主推大卡（封面渐变 + 音符图标 + 播放按钮 + 中文 / 英文曲名 + 时长），下方 All tracks 网格。</p><h2>封面占位方案</h2><p>曲目表 t_track 存 cover_seed（0~7），前端按 seed 生成不同色相的渐变封面，不存图片文件；接入真实封面 / 音频后替换 url 字段即可无缝切换。</p><h2>播放联动</h2><p>点击任意曲目卡 → 全局播放器单例 select(index) 播放：当前播放卡高亮（accent 边框 + PLAYING 角标 + 底部进度条），播放按钮变暂停；顶栏抽屉、首页 Now Playing 卡片实时同步。实测 Music 页点歌后回首页，Now Playing 显示同一曲目。</p><h2>数据</h2><p>后端 GET /api/music/tracks 返回曲目列表（公开），种子 8 首 Leo Tapes 风格的原创曲目；前端播放进度为模拟推进（无真实音频源），README 已注明接入真实音频的改法。</p><h2>导航</h2><p>菜单顺序调整：Home · Blog · Notes · Photos · Music · About。</p>'),
(13, '202610071500', '2026.10.07', 'Leo 站点开发记录 #13：Home Photos 联动与踩坑清单', 'Home 的 Photos 栏展示最近上传的 4 张照片（limit 参数），点击直达相册；修复 Blog 详情页白天模式黑底白字问题，汇总全部踩坑经验。', 6, '本站,开发,踩坑', '<h2>Home Photos 联动</h2><p>Home 页 Photos 卡从占位文案升级为真实数据：GET /api/photos?limit=4（后端新增可选 limit 参数）取最近上传的 4 张照片，缩略图网格渲染（object-fit: cover + hover 放大）；点击缩略图跳转到对应相册详情页，与 Photos 页实时联动；不足 4 张时自适应显示实际张数。</p><h2>Bug：Blog 详情页白天模式黑底白字</h2><p>浅色主题下打开 Blog 详情页，正文是黑底白字。排查发现正文里内联了硬编码深色背景样式（background-color: rgb(10,10,10)）——当初从参考站复制内容时带进来的，浅色主题下没有跟随主题。</p><p>修复：详情页展示模式统一清除正文内联背景色（背景强制透明跟随主题，用户样式栏设置的文字颜色、加粗、字号不受影响），以后粘贴深色内容也不会再出问题。</p><h2>踩坑清单（全站）</h2><ul><li>加载动画遗留 Zppo：品牌文案藏在站点配置 JSON 里，代码替换覆盖不到数据层。</li><li>浅色主题日历图标不可见：UA 控件颜色只受 color-scheme 控制。</li><li>Blog 黑底白字：复制内容带入的内联深色背景。</li><li>编辑不生效：编辑副本与展示视图未同步，保存后要回填。</li><li>8080 端口占用：先杀监听进程再启动。</li><li>后端重启 token 全失效：Token 存内存，重启即清，需重新登录。</li><li>中文写入乱码：PowerShell / MySQL CLI 编码不稳，改用 Python + pymysql。</li><li>上传文件静态映射 404：改为 Controller 文件流回放。</li></ul><h2>现状</h2><p>Home / Blog / Notes / About / Photos / Music 六个页面全部接入编辑模式与真实接口。下一步：接入真实音频源、Blog 评论功能、部署上线。</p>'),
(14, '202610081600', '2026.10.08', 'Leo 站点开发记录 #14：Music 音乐上传，音频 + 封面 + 歌名 + 演唱者', '音乐功能补齐上传闭环：曲目表加真实音频与封面字段，上传接口落盘 uploads/music，前端编辑模式弹窗上传、卡片删除、真实 &lt;audio&gt; 播放。', 7, 'Music,音乐,上传,文件存储', '<h2>需求</h2><p>Music 页此前只有 8 首种子曲目（封面为渐变色块、播放为模拟推进），缺少自建内容的能力。本次补齐上传：音频文件 + 封面图片 + 歌曲名称 + 演唱者四个要素，并让播放器真正出声。</p><h2>数据库</h2><p>t_track 新增两列：url VARCHAR(500)（真实音频地址）、cover VARCHAR(500)（封面图片地址），与原有 id / title / en_title / artist / duration / cover_seed / created_at 并存——无封面上传的曲目继续用 cover_seed 渐变占位。</p><h2>后端接口</h2><ul><li>POST /api/music/tracks（需登录，multipart）：音频必传 + 歌名 / 演唱者必填，封面可选；音频格式白名单（mp3 / wav / ogg / m4a / flac / aac / webm），图片 jpg / png / gif / webp / bmp / avif / svg。</li><li>文件落盘：音频存 uploads/music/，封面存 uploads/music/covers/，UUID 文件名防冲突；时长不填默认 180 秒。</li><li>DELETE /api/music/tracks/{id}（需登录）：删数据库行，并连带删除磁盘上的音频与封面文件，不留孤儿文件。</li><li>GET /api/music/tracks 仍公开，曲目列表带 url / cover 真实字段。</li></ul><h2>前端</h2><ul><li>Music 页编辑模式：页头 Upload 按钮 → 弹窗六行表单（音频* / 封面 / 歌曲名称* / 演唱者* / 英文曲名 / 时长），校验后 multipart 提交，成功后刷新列表。</li><li>每张曲目卡右上角 ✕ 删除（编辑模式出现），confirm 后调删除接口并刷新，播放索引自动校正。</li><li>封面：有 cover 直接显示真实图，否则按 cover_seed 生成渐变——上传带封面后立即呈现。</li><li>播放器重写为真实播放：模块级 new Audio() 单例，有 url 时 timeupdate 驱动进度、ended 自动切歌、切歌先 pause + 清 src；无 url 的旧曲目保持模拟推进兼容。</li></ul><h2>端到端验证</h2><p>浏览器自动化走完整流程：登录 → Upload → 上传测试 wav + 封面 → 列表出现新曲目（真实封面 / 演唱者行 / 时长 0:02）→ 真实点击播放，网络请求加载 wav、进度条实时走动、PLAYING 高亮 → 删除测试曲目，磁盘文件一并清理、列表回到 8 首。</p><h2>遇到的坑</h2><ul><li>JDK 23 + Lombok 1.18.32 编译失败：PhotoService 报 PhotoComment getter/setter 找不到，Lombok 版本不支持新 JDK 注解处理；改用 JDK 17（与 pom target 一致）打包成功。</li><li>浏览器自动化上传文件：CSS 选择器两次匹配失败，改回 ref 方式上传成功；属于验证工具层的坑，功能本身没问题。</li><li>真实播放「没声音」：程序化 click() 不携带用户手势，被浏览器自动播放策略拦截；真实鼠标事件点击后音频正常加载播放。</li><li>中文写库仍用 Python + pymysql，避免 PowerShell / MySQL CLI 乱码。</li></ul><h2>存储位置</h2><p>音乐与封面统一存在后端 leo-server/uploads/music/（封面在 covers/ 子目录），与照片 uploads/ 同根，目录由上传接口自动创建。</p>');

-- 标签云（由文章 tags 统计生成）
INSERT INTO `t_tag` (`name`, `count`) VALUES
('About', 1), ('Blog', 2), ('Bug', 1), ('Music', 2), ('MyBatis-Plus', 1), ('MySQL', 1), ('Notes', 1), ('Photos', 2), ('Spring Boot', 1), ('Vite', 1), ('Vue', 1), ('上传', 1), ('前端', 2), ('后端', 1), ('图片', 1), ('布局', 1), ('开发', 4), ('搜索', 1), ('播放器', 1), ('文件存储', 1), ('本站', 9), ('登录', 1), ('相册', 2), ('编辑器', 1), ('编辑模式', 2), ('评论', 1), ('踩坑', 1), ('音乐', 2);





-- 随记
INSERT INTO `t_note` (`id`, `note_date`, `title`, `text`, `expandable`) VALUES
(1,  '2026-9-17', NULL, '哈哈，也正好拦住了访问不坚定的人', 0),
(2,  '2026-9-16', NULL, '部署到 cf 网站变得好慢', 0),
(3,  '2026-9-14', NULL, '新的博客上线到 Leo.net，内部开发了很久终于上线了', 0),
(4,  '09-11', '播放器水合闪烁', '彻底修好了进首页“卡片飞一下”的问题：不是 CSS transition 的锅，是原生 View Transition 抓了重排前的快照。把测量都挪到绘制前跑完，浏览器再也抓不到中间态。', 1),
(5,  '09-10', NULL, '音乐抽屉在手机上改成了底部弹层，桌面还是右侧悬浮卡。歌词能滚动了，非当前行做虚化，鼠标一悬或一滚就清晰，松手即回——不糊脸。', 0),
(6,  '09-08', '清晨', '五点半出门，桂花整排整排地香。跑到桥上停下来喘气，天边是那种将亮未亮的青灰色，很安静，只有江风和水声。', 0),
(7,  '09-05', NULL, '把首页那张地图从 d3 矢量一路换成 MapLibre，最后发现公共 OSM 瓦片在自己网络里根本连不上，改高德的源才稳。折腾一整天，值。', 0),
(8,  '09-02', '换个键盘手感', '试了 68 配列，方向键不用低头找，节奏舒服很多。外壳选了雾蓝，低调耐看，比之前那个花哨的 RGB 灯效适合坐一天。', 0),
(9,  '08-30', NULL, '首页卡片的拖拽终于不“粘鼠标”了。之前松手后幽灵卡片还赖在光标上，是因为个别孤岛自己捕获了指针。补了 window 级兜底 + 统一收尾，稳了。', 1),
(10, '08-28', NULL, '今晚的风很软，把阳台的衣服吹得鼓起来，像有人在里面跳舞。收衣服的时候发现被太阳晒得温热，抱着上楼，有种很小的幸福感。', 0),
(11, '08-25', '卡片忽大忽小', '拖拽时卡片会莫名改变大小，根因是布局用 CSS 变量算的、和 React 内联 style 打架。改成每张卡固定 span、绝对定位只动 transform，彻底告别“变形”。卡片尺寸恒定，才谈得上高级感。', 1),
(12, '08-22', '读《设计的设计》', '原研哉讲“emptiness”不是空，而是留出容纳的余地。做界面时总想塞满，其实克制更难。首页背景后来被我调成近乎纯白，眼睛一下子松了。', 0),
(13, '08-19', NULL, '导航的 logo 和黑夜开关从药丸里拆出来，做成两颗圆钮一左一右，像灵动岛。favicon 那个圆在移动端要求填满无留白、桌面留白，来回调了好几轮才顺眼。', 0),
(14, '08-16', '加载动画', '那个“Leo”loader 之前字母右边会一闪一闪缺角，其实是 clip-path 把每个字母上下裁没了又放回来。去掉裁切只留一条红色扫描线扫过，干净了。字体不再是“一抽一抽”地加载。', 1),
(15, '08-12', NULL, '给音乐播放器接了公共镜像的网易云歌单，能整首播放还有实时歌词。缺点是首次点击“随机播放”常被浏览器自动播放策略拦一下，得再点一次——这属于平台限制，认了。', 0),
(16, '08-09', NULL, '把三个社交按钮做成“左上细亮边、右下粗暗边”的伪 3D，按下去会真的塌一截。白天又嫌它扎眼，调成几乎融进背景的灰，悬停才亮一点。审美这件事，来回改七八次也不为过。', 0),
(17, '08-05', NULL, '修了一下午的 bug，最后发现是少写了一个 await。气笑了，顺手把咖啡续上，重新泡了一杯。', 0),
(18, '08-01', NULL, '给博客加了“随记”这个页面，像朋友圈一样零散记点东西：跑过的路、看过的书、修过的 bug。以后不必每件事都攒成一篇长文，一句话也值得留下。', 0);

-- 音乐曲目（封面渐变种子 0~7；url/cover 为真实音频与封面，上传接口写入）
INSERT INTO `t_track` (`id`, `title`, `en_title`, `artist`, `duration`, `cover_seed`) VALUES
(1,  '漂游之人',   'Drifting',                'Leo Tapes', 258, 0),
(2,  '金色之线',   'Golden Thread',           'Leo Tapes', 245, 1),
(3,  '今夜星光灿烂', 'E lucevan le stelle',   'Leo Tapes', 276, 2),
(4,  '长夜鸣笛',   'The Midnight Whistle',    'Leo Tapes', 300, 3),
(5,  '雨中曲',     'Dust to dust',            'Leo Tapes', 232, 4),
(6,  '相别',       'Goodbye',                 'Leo Tapes', 214, 5),
(7,  '雨后窗台',   'Rainy Window',            'Leo Tapes', 198, 6),
(8,  '纸飞机',     'Paper Planes',            'Leo Tapes', 187, 7);

