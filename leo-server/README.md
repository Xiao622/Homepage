# leo-server — Leo 个人站点后端

Spring Boot 3.2.5 + MyBatis-Plus 3.5.7 + MySQL 8 的个人站点接口（查询 + 管理员登录 + Home 内容更新）。

## 功能范围

| 页面 | 接口 | 说明 |
| --- | --- | --- |
| Home | `GET /api/site` | 站点基础信息（name/motto/footer/github/email…） |
| Home | `GET /api/site/marquee-tags` | 首页技术栈跑马灯标签 |
| Home | `GET /api/site/home-content` | Home 页可编辑文字（无需登录） |
| Home | `PUT /api/site/home-content` | 保存 Home 页可编辑文字（需 `Authorization: Bearer <token>`；整体覆盖，footer 同步写 t_site_config.footer） |
| 登录 | `POST /api/auth/login` | 管理员登录 `{username, password}` → `{token}`（凭证存于 t_site_config：优先 `admin_accounts` 多账户 JSON 数组，缺失时回退 `admin_user` / `admin_password` 单账户） |
| 登录 | `POST /api/auth/logout` | 注销当前 token（需 Authorization 头） |
| Blog | `GET /api/posts?page=&pageSize=&tag=&keyword=` | 文章分页（支持按标签筛选 + 标题模糊搜索，时间倒序） |
| Blog | `GET /api/posts/tags` | 标签云（按文章数倒序） |
| Blog | `GET /api/posts/{slug}` | 文章详情（公开；按路由编号，含 content 正文，不存在返回 404） |
| Blog | `POST /api/posts` | 新建文章（需登录；slug 自动生成 = 年月日时分 yyyyMMddHHmm，同分钟冲突递增后缀；标签云计数自动 +1） |
| Blog | `PUT /api/posts/{slug}` | 更新文章（需登录；按 slug 定位；标签变化自动同步标签云） |
| Blog | `DELETE /api/posts/{slug}` | 删除文章（需登录；按 slug 定位；标签计数 -1，归零标签自动移除） |
| Notes | `GET /api/notes?order=time\|random&keyword=` | 随记列表（时间语义倒序 / 随机；keyword 模糊匹配标题或正文；时间排序按「年份补全 + 月日补零」语义键，新建随记自然置顶） |
| Notes | `POST /api/notes` | 新建随记（需登录；时间与内容必填，否则 400） |
| Notes | `PUT /api/notes/{id}` | 更新随记（需登录；时间与内容必填，不存在 404） |
| Notes | `DELETE /api/notes/{id}` | 删除随记（需登录） |
| Music | `GET /api/music/tracks` | 曲目列表（公开；播放器 / Music 页共用，封面为渐变种子，接入真实音频后补 url 字段） |
| About | `GET /api/about` | 关于页完整数据（t_site_config 中 JSON） |
| About | `PUT /api/about` | 保存关于页数据（需登录 token；整体覆盖 t_site_config.about） |
| Photos | `GET /api/photos?category=&month=` | 照片列表（公开；category 分类名模糊、month 匹配拍摄时间前缀如 2026-09，可叠加，时间倒序） |
| Photos | `GET /api/photos/albums` | 相册列表（公开；按分类分组：名称 / 照片数 / 封面=最新一张） |
| Photos | `GET /api/photos/categories` | 分类统计（按照片数倒序）`[{name, count}]` |
| Photos | `POST /api/photos` | 上传照片（需登录；multipart：file + category + takenDate + caption；格式白名单 jpg/jpeg/png/gif/webp/bmp/avif/svg，非白名单 400） |
| Photos | `PUT /api/photos/{id}` | 更新照片信息（需登录；category/takenDate/caption，不存在 404） |
| Photos | `DELETE /api/photos/{id}` | 删除照片（需登录；同时删除磁盘文件与该照片评论） |
| Photos | `GET /api/photos/{id}/comments` | 照片评论列表（公开，时间正序） |
| Photos | `POST /api/photos/{id}/comments` | 发表照片评论（公开；body：{author, content}；照片不存在 404、昵称/内容为空 400） |
| Photos | `GET /uploads/{filename}` | 照片文件流（公开；从 uploads/ 目录回放，前端经 Vite 代理 `/uploads` 访问） |

统一返回体：`{ "code": 0, "message": "ok", "data": ... }`（code=0 成功；
登录失败 code=400，未登录/过期 code=401）。

> 登录令牌：内存 Map 存储、30 分钟过期（`common/TokenStore.java`），演示用；
> 生产环境建议替换为 JWT / Redis 会话。管理员密码在 t_site_config 中为明文，演示项目约定。

## 启动方式

```bash
# 前置：MySQL 8 运行于 127.0.0.1:3306，root 密码默认 888888（可用环境变量 MYSQL_PASSWORD 覆盖）
# 1. 初始化数据库（已执行过可跳过；脚本幂等，重跑会重建表与种子数据）
mysql --user=root --password=888888 --host=127.0.0.1 --port=3306 < src/main/resources/sql/init.sql

# 2. 启动后端（默认端口 8080）
mvn spring-boot:run
```

> 注意：项目内置 `.mvn/settings.xml`（通过 `.mvn/maven.config` 自动生效），
> 将 Maven 镜像指向阿里云 HTTPS 仓库。原因是本机全局 settings.xml 中配置的
> 阿里云镜像为已失效的 HTTP 地址且被 Maven 3.8+ 默认拦截。此配置仅对本项目生效。

## 目录结构

```
leo-server/
├── pom.xml
├── .mvn/                         # 项目级 Maven 镜像配置
└── src/main/
    ├── java/com/leo/blog/
    │   ├── LeoBlogApplication.java    # 入口（@MapperScan）
    │   ├── common/                    # Result / PageResult 统一返回体
    │   ├── config/                    # MyBatis-Plus 分页 / CORS / uploads 静态映射
    │   ├── controller/                # Post / Note / Site / Photo / Music 五个控制器
    │   ├── service/                   # 查询服务
    │   ├── mapper/                    # 8 个 BaseMapper
    │   ├── entity/                    # Post / Tag / Note / SiteConfig / MarqueeTag / Photo / PhotoComment / Track
    │   └── dto/                       # PostVO / NoteVO / SiteInfoVO / PhotoVO / TrackVO
    └── resources/
        ├── application.yml            # 数据源、端口、SQL 日志
        └── sql/init.sql               # 建库建表 + 种子数据（leo_blog）
```

## 数据库

库名 `leo_blog`，8 张表：

- `t_post` — 文章（slug 路由编号唯一键 / post_date / title / excerpt / content 正文 / minutes / tags CSV）
- `t_tag` — 标签云（name 主键 + count）
- `t_note` — 随记（note_date/title/text/expandable）
- `t_photo` — 相册照片（url 相对路径 / category 相册名 / taken_date 拍摄时间 / caption 作者描述；文件存于 `uploads/` 目录）
- `t_photo_comment` — 照片评论（photo_id / author / content）
- `t_track` — 音乐曲目（title/en_title/artist/duration/cover_seed；播放器与 Music 页共用）
- `t_site_config` — 站点 key-value（含 about 整段 JSON）
- `t_marquee_tag` — 首页跑马灯标签

## 与前端对接

前端 `zyyo-vue`（Leo 站点）经 Vite 代理 `/api` → `localhost:8080`。
前端 `.env.development` 中 `VITE_USE_MOCK=false` 即走真实接口。
接口字段名与前端 `src/api/modules/*.js` 的期望结构一一对齐，前端代码零改动。

## 后续扩展（预留）

- 评论、访问统计、站内搜索
- Links 页面的写操作（友链管理）
- Photos 云存储 / 图床接入（当前为本地 uploads 目录）
