import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由表 —— 与 Leo.net 页面目录一一对应。
 * 新增页面：在 views 下建组件 → 在这里注册一条路由即可。
 * 页面组件统一按「目录结构」组织在 src/views/<Page>View.vue。
 */
const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: 'Leo — notes on code and design' },
  },
  {
    path: '/blog',
    name: 'blog',
    component: () => import('@/views/BlogView.vue'),
    meta: { title: 'Blog — Leo' },
  },
  {
    path: '/blog/new',
    name: 'post-new',
    component: () => import('@/views/BlogNewView.vue'),
    meta: { title: 'New Post — Leo' },
  },
  {
    path: '/blog/:slug',
    name: 'post',
    component: () => import('@/views/BlogPostView.vue'),
    meta: { title: 'Post — Leo' },
  },
  {
    path: '/notes',
    name: 'notes',
    component: () => import('@/views/NotesView.vue'),
    meta: { title: 'Notes — Leo' },
  },
  {
    path: '/music',
    name: 'music',
    component: () => import('@/views/MusicView.vue'),
    meta: { title: 'Music — Leo' },
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('@/views/AboutView.vue'),
    meta: { title: 'About — Leo' },
  },
  {
    path: '/photos',
    name: 'photos',
    component: () => import('@/views/PhotosView.vue'),
    meta: { title: 'Photos — Leo' },
  },
  {
    path: '/photos/:category',
    name: 'photo-album',
    component: () => import('@/views/AlbumView.vue'),
    meta: { title: 'Album — Leo' },
  },
  // 兜底：未匹配路由回到首页
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 详情页滚动位置还原；普通跳转回到顶部
    if (savedPosition) return savedPosition
    return { top: 0 }
  },
})

router.afterEach((to) => {
  document.title = to.meta?.title || 'Leo'
})

export default router
