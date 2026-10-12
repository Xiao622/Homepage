<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { blogApi } from '@/api'
import { useEditMode } from '@/composables/useEditMode'
import { t } from '@/i18n'

/**
 * Blog 列表页：文章列表 + 搜索 + 分页 + 标签云侧栏。
 * 搜索框对标题做模糊查询（300ms 防抖），可与标签筛选叠加；URL query (?tag=xxx) 保留标签。
 * 每篇文章卡右侧的右箭头进入详情页（/blog/:slug）——修改与删除在详情页进行；
 * 编辑模式（管理员登录后）「＋ New post」跳转新建页（/blog/new），空模板页面内编辑。
 * Home 页的 Blog 区仅为只读展示近 3 条。
 */
const route = useRoute()
const router = useRouter()
const { isEditMode } = useEditMode()

const posts = ref([])
const tags = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = 6
const loading = ref(true)

const currentTag = ref('')
const keywordInput = ref('')
const keyword = ref('')
let searchTimer = null

function onSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    keyword.value = keywordInput.value.trim()
    page.value = 1
    load()
  }, 300)
}

/** 重新加载列表 + 标签云。 */
async function refreshAll() {
  await Promise.all([load(), loadTags()])
}

async function load() {
  loading.value = true
  try {
    const data = await blogApi.getPosts({
      page: page.value,
      pageSize,
      tag: currentTag.value,
      keyword: keyword.value,
    })
    posts.value = data.list
    total.value = data.total
  } catch (e) {
    console.error('Load posts failed:', e)
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  try {
    tags.value = await blogApi.getTags()
  } catch {
    /* 标签失败不阻塞 */
  }
}

function pickTag(name) {
  currentTag.value = name
  page.value = 1
  router.replace({ query: name ? { tag: name } : {} })
  load()
}

function changePage(p) {
  page.value = p
  load()
}

onMounted(async () => {
  // 同步 URL 中的标签
  const q = route.query.tag
  currentTag.value = typeof q === 'string' ? q : ''
  watch(
    () => route.query.tag,
    (v) => {
      if (typeof v === 'string' && v !== currentTag.value) {
        currentTag.value = v
        page.value = 1
        load()
      }
    },
  )
  await loadTags()
  load()
})

onBeforeUnmount(() => clearTimeout(searchTimer))

const totalPages = () => Math.max(1, Math.ceil(total.value / pageSize))
</script>

<template>
  <div class="container page">
    <header class="page-header">
      <div class="page-header__main">
        <h1>Blog</h1>
        <p class="sub">{{ t('blog.postsCount', { n: total }) }}</p>
      </div>
      <div class="page-header__tools">
        <div class="search">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true">
            <circle cx="11" cy="11" r="7" />
            <path d="m21 21-4.3-4.3" />
          </svg>
          <input
            v-model="keywordInput"
            type="search"
            class="search__input"
            :placeholder="t('blog.searchPlaceholder')"
            :aria-label="t('blog.searchPlaceholder')"
            spellcheck="false"
            @input="onSearchInput"
          />
        </div>
        <RouterLink
          v-if="isEditMode"
          to="/blog/new"
          class="btn btn--accent"
        >
          {{ t('blog.newPost') }}
        </RouterLink>
      </div>
    </header>

    <div class="blog-layout">
      <!-- 文章列表 -->
      <div class="blog-main">
        <div v-if="loading" class="skeleton-list">
          <div v-for="i in 6" :key="i" class="skeleton skeleton--row" />
        </div>

        <template v-else>
          <article
            v-for="post in posts"
            :key="post.id"
            class="post card card--hover card-pad"
          >
            <div class="post__top">
              <time class="post__date mono">{{ post.date }}</time>
              <RouterLink
                :to="`/blog/${post.slug}`"
                class="post__arrow"
                :title="t('blog.readMore')"
                :aria-label="t('blog.readMore')"
              >
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <path d="M5 12h14m-6-6 6 6-6 6" />
                </svg>
              </RouterLink>
            </div>

            <h2 class="post__title serif">{{ post.title }}</h2>
            <p class="post__excerpt">{{ post.excerpt }}</p>
            <div class="post__tags">
              <button
                v-for="t in post.tags"
                :key="t"
                class="tag"
                @click.stop="pickTag(t)"
              >
                {{ t }}
              </button>
            </div>
          </article>

          <p v-if="!posts.length" class="empty">
            {{ keyword ? t('blog.emptyWithKeyword', { kw: keyword }) : t('blog.empty') }}
          </p>

          <!-- 分页 -->
          <nav v-if="totalPages() > 1" class="pager" aria-label="Pagination">
            <button
              class="btn btn--ghost"
              :disabled="page <= 1"
              @click="changePage(page - 1)"
            >
              {{ t('blog.prev') }}
            </button>
            <span class="pager__info mono">{{ page }} / {{ totalPages() }}</span>
            <button
              class="btn btn--ghost"
              :disabled="page >= totalPages()"
              @click="changePage(page + 1)"
            >
              {{ t('blog.next') }}
            </button>
          </nav>
        </template>
      </div>

      <!-- 标签云侧栏 -->
      <aside class="blog-side">
        <div class="card card-pad">
          <h3 class="side-title serif">{{ t('blog.tags') }}</h3>
          <div class="tag-cloud">
            <button
              class="tag"
              :class="{ 'tag--active': !currentTag }"
              @click="pickTag('')"
            >
              {{ t('blog.all') }}
            </button>
            <button
              v-for="t in tags"
              :key="t.name"
              class="tag"
              :class="{ 'tag--active': currentTag === t.name }"
              @click="pickTag(t.name)"
            >
              {{ t.name }}
              <span class="tag__count">{{ t.count }}</span>
            </button>
          </div>
          <RouterLink to="/blog" class="side-all">{{ t('home.viewAll') }}</RouterLink>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.blog-layout {
  display: grid;
  gap: 1.6rem;
}

@media (min-width: 900px) {
  .blog-layout {
    grid-template-columns: 1fr 15rem;
  }
}

.blog-main {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-width: 0;
}

/* ---------- 文章卡 ---------- */
.post__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}
.post__date {
  font-size: 0.8rem;
  color: var(--faint);
}
/* 右侧展开箭头：进入文章详情页（对所有访客可见） */
.post__arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.9rem;
  height: 1.9rem;
  border-radius: 999px;
  color: var(--faint);
  transition: color 0.2s ease, background-color 0.2s ease, transform 0.2s ease;
}
.post__arrow:hover {
  color: var(--accent);
  background: var(--accent-soft);
  transform: translateX(2px);
}

/* ---------- 页头：标题 + 搜索 + 新建 ---------- */
.page-header {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.6rem 1rem;
}
.page-header__tools {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}
.search {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.38rem 0.7rem;
  border-radius: 999px;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  color: var(--faint);
  transition: border-color 0.2s ease;
}
.search:focus-within { border-color: var(--accent); }
.search__input {
  min-width: 0;
  border: none;
  background: transparent;
  color: var(--text);
  font-size: 0.85rem;
  font-family: inherit;
  outline: none;
  width: 9.5rem;
}
.search__input::placeholder { color: var(--faint); }

.post__title {
  margin: 0 0 0.4rem;
  font-size: 1.18rem;
  font-weight: 600;
  line-height: 1.45;
  transition: color 0.2s ease;
}
.post:hover .post__title { color: var(--accent); }
.post__excerpt {
  margin: 0 0 0.8rem;
  font-size: 0.9rem;
  color: var(--muted);
  line-height: 1.75;
}
.post__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
}
.tag__count {
  margin-left: 0.25rem;
  font-size: 0.7rem;
  opacity: 0.7;
}

/* ---------- 分页 ---------- */
.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.2rem;
  padding: 1rem 0 0.5rem;
}
.pager .btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
.pager__info {
  font-size: 0.82rem;
  color: var(--faint);
}

/* ---------- 侧栏 ---------- */
.blog-side .card {
  position: sticky;
  top: 5.5rem;
}
.side-title {
  margin: 0 0 0.8rem;
  font-size: 1.05rem;
}
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  margin-bottom: 1rem;
}
.side-all {
  font-size: 0.85rem;
  color: var(--muted);
  transition: color 0.2s ease;
}
.side-all:hover { color: var(--accent); }

/* ---------- 状态 ---------- */
.empty {
  text-align: center;
  color: var(--faint);
  padding: 3rem 0;
}
.skeleton-list { display: flex; flex-direction: column; gap: 0.8rem; }
.skeleton {
  border-radius: 0.7rem;
  background: var(--card-border);
  opacity: 0.5;
  animation: blink 1.4s ease-in-out infinite;
}
.skeleton--row { height: 7.5rem; }
</style>
