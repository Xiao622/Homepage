<script setup>
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { blogApi } from '@/api'
import { useEditMode } from '@/composables/useEditMode'
import EditorToolbar from '@/components/blog/EditorToolbar.vue'
import { t } from '@/i18n'

/**
 * 文章详情页（/blog/:slug）：
 *  - 展示：标题 / 日期 / 标签 / 正文（HTML 富文本）+ 右侧「On this page」目录列（完全还原 zyyo）
 *  - 编辑模式（管理员登录后）：直接在页面内编辑（与 Home 页编辑模式一致），
 *    正文选中文字时出现 Word 同款样式栏；底部浮动保存条，改动后保存。
 *  - 删除仍在详情页（仅编辑模式显示）。
 */
const route = useRoute()
const router = useRouter()
const { isEditMode } = useEditMode()

const post = ref(null)
const notFound = ref(false)
const loading = ref(true)

const titleEl = ref(null)
const dateEl = ref(null)
const tagsEl = ref(null)
const excerptEl = ref(null)
const bodyEl = ref(null)
const viewBodyEl = ref(null) // 展示模式的正文容器（TOC 提取用）

const toc = ref([])   // [{ id, text, level }]
const activeId = ref('')

const dirty = ref(false)
const saved = ref(false)
const saving = ref(false)
const deleting = ref(false)
const pastingImage = ref(false) // 正文正在上传粘贴的图片

/* ================= 正文粘贴图片（Ctrl+V） =================
 * 编辑模式下，在正文中粘贴图片（截图等）→ 自动上传到 uploads/blog/ →
 * 把返回的图片地址以 <img> 插入光标处。纯文本粘贴行为不受影响。 */
async function onBodyPaste(e) {
  if (!isEditMode.value || pastingImage.value) return
  const items = e.clipboardData?.items
  if (!items) return
  const imageItem = Array.from(items).find(
    (it) => it.kind === 'file' && it.type.startsWith('image/'),
  )
  if (!imageItem) return // 不是图片粘贴（纯文本 / 富文本）→ 走默认行为

  e.preventDefault()
  const file = imageItem.getAsFile()
  if (!file) return
  pastingImage.value = true
  try {
    const url = await blogApi.uploadImage(file)
    if (bodyEl.value) {
      bodyEl.value.focus()
      document.execCommand(
        'insertHTML',
        false,
        `<img src="${url}" alt="" style="max-width:100%;border-radius:10px;margin:0.6rem 0;">`,
      )
      markDirty()
    }
  } catch (err) {
    alert(err.message || t('blog.uploadFailed'))
  } finally {
    pastingImage.value = false
  }
}

/* ================= 加载 ================= */
async function load(slug) {
  loading.value = true
  notFound.value = false
  try {
    post.value = await blogApi.getPost(slug)
    if (!post.value) {
      notFound.value = true
    } else {
      document.title = `${post.value.title} — Leo`
    }
  } catch {
    notFound.value = true
  } finally {
    loading.value = false
    await nextTick()
    if (post.value) buildToc()
  }
}

watch(
  () => route.params.slug,
  (slug) => {
    if (typeof slug === 'string') load(slug)
  },
  { immediate: true },
)

/* ================= 编辑模式：回填 contenteditable ================= */
watch(
  [isEditMode, () => post.value],
  async () => {
    if (!post.value) return
    await nextTick()
    if (!isEditMode.value) {
      // 切回展示模式：正文 v-html 渲染完成后重建目录
      buildToc()
      return
    }
    if (titleEl.value && document.activeElement !== titleEl.value) {
      titleEl.value.textContent = post.value.title || ''
    }
    if (dateEl.value && document.activeElement !== dateEl.value) {
      dateEl.value.textContent = post.value.date || ''
    }
    if (tagsEl.value && document.activeElement !== tagsEl.value) {
      tagsEl.value.textContent = (post.value.tags || []).join(', ')
    }
    if (excerptEl.value && document.activeElement !== excerptEl.value) {
      excerptEl.value.textContent = post.value.excerpt || ''
    }
    if (bodyEl.value && document.activeElement !== bodyEl.value) {
      bodyEl.value.innerHTML = post.value.content || ''
    }
  },
)

function markDirty() {
  dirty.value = true
  // 正文变动时刷新目录
  if (isEditMode.value) scheduleToc()
}

/* ================= 目录（On this page）================= */
let tocTimer = null
function scheduleToc() {
  clearTimeout(tocTimer)
  tocTimer = setTimeout(buildToc, 250)
}

function buildToc() {
  const host = isEditMode.value ? bodyEl.value : viewBodyEl.value
  if (!host) { toc.value = []; return }
  const heads = host.querySelectorAll('h1, h2, h3')
  const list = []
  heads.forEach((h, i) => {
    if (!h.id) h.id = `sec-${i + 1}`
    list.push({ id: h.id, text: h.textContent.trim(), level: Number(h.tagName.slice(1)) })
  })
  toc.value = list
}

function scrollTo(id) {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

/* 滚动高亮当前章节 */
function onScroll() {
  let current = ''
  for (const item of toc.value) {
    const el = document.getElementById(item.id)
    if (el && el.getBoundingClientRect().top <= 130) current = item.id
  }
  activeId.value = current
}

/* ================= 保存 / 删除 ================= */
async function save() {
  if (!post.value) return
  const title = titleEl.value?.textContent.trim() || ''
  const date = dateEl.value?.textContent.trim() || ''
  if (!title) { alert(t('blog.titleRequired')); return }
  if (!date) { alert(t('blog.dateRequired')); return }

  saving.value = true
  try {
    post.value = await blogApi.updatePost(post.value.slug, {
      title,
      date,
      minutes: post.value.minutes ?? 5,
      excerpt: excerptEl.value?.textContent.trim() || '',
      content: bodyEl.value?.innerHTML || '',
      tags: (tagsEl.value?.textContent || '')
        .split(/[,，]/)
        .map((s) => s.trim())
        .filter(Boolean),
    })
    dirty.value = false
    saved.value = true
    setTimeout(() => (saved.value = false), 2200)
    scheduleToc()
  } catch (e) {
    alert(e.message || t('blog.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function removePost() {
  if (!window.confirm(t('blog.deleteConfirm', { title: post.value.title }))) return
  deleting.value = true
  try {
    await blogApi.deletePost(post.value.slug)
    router.replace('/blog')
  } catch (e) {
    alert(e.message || t('blog.deleteFailed'))
  } finally {
    deleting.value = false
  }
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
})
onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  clearTimeout(tocTimer)
})

const hasBody = computed(() => !!post.value?.content)

/** 摘要回退展示（HTML 转义，防止特殊字符破坏布局） */
const excerptHtml = computed(() => {
  const raw = post.value?.excerpt || ''
  const esc = raw
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
  return `<p class="post-page__content--excerpt">${esc}</p>`
})
</script>

<template>
  <div class="container page">
    <article v-if="post" class="post-layout">
      <!-- 主列 -->
      <div class="post-main">
        <!-- 顶栏：返回 + 编辑操作 -->
        <div class="post-page__bar">
          <RouterLink to="/blog" class="post-page__back">{{ t('blog.back') }}</RouterLink>
          <div v-if="isEditMode" class="post-page__actions">
            <button
              class="post-page__action post-page__action--danger"
              :disabled="deleting"
              @click="removePost"
            >
              {{ t('blog.delete') }}
            </button>
          </div>
        </div>

        <!-- 文章头部 -->
        <header class="post-page__head">
          <!-- 展示模式：标题纯文本 -->
          <h1 v-if="!isEditMode" class="post-page__title serif">{{ post.title }}</h1>
          <!-- 编辑模式：标题可直接编辑 -->
          <div
            v-else
            ref="titleEl"
            class="post-page__title serif editable"
            contenteditable="true"
            spellcheck="false"
            @input="markDirty"
          />

          <div class="post-page__meta mono">
            <time v-if="!isEditMode">{{ post.date }}</time>
            <span
              v-else
              ref="dateEl"
              class="editable"
              contenteditable="true"
              spellcheck="false"
              @input="markDirty"
            />
          </div>

          <div v-if="isEditMode" class="post-page__tags-edit">
            <span
              ref="tagsEl"
              class="editable"
              contenteditable="true"
              spellcheck="false"
              @input="markDirty"
            />
          </div>
          <div v-else class="post-page__tags">
            <span v-for="t in post.tags" :key="t" class="tag">{{ t }}</span>
          </div>

          <!-- 编辑模式：摘要（列表卡片文案）也可直接改 -->
          <div
            v-if="isEditMode"
            ref="excerptEl"
            class="post-page__excerpt-edit editable"
            contenteditable="true"
            spellcheck="false"
            @input="markDirty"
          />
        </header>

        <!-- 正文 -->
        <div class="post-page__body">
          <!-- 展示模式：HTML 渲染（供 TOC 提取标题） -->
          <div
            v-if="!isEditMode"
            ref="viewBodyEl"
            class="post-page__content"
            v-html="post.content || excerptHtml"
          />
          <!-- 编辑模式：contenteditable 富文本 + Word 样式栏（Ctrl+V 可直接粘贴图片） -->
          <div
            v-else
            ref="bodyEl"
            class="post-page__content post-page__content--edit editable"
            contenteditable="true"
            spellcheck="false"
            @input="markDirty"
            @paste="onBodyPaste"
          />
          <EditorToolbar v-if="isEditMode" :container="bodyEl" />
          <p v-if="pastingImage" class="post-page__pasting">{{ t('blog.pastingImage') }}</p>
        </div>
      </div>

      <!-- 右侧目录列：On this page（完全还原 zyyo） -->
      <aside v-if="toc.length" class="post-toc">
        <h4 class="post-toc__title">{{ t('blog.onThisPage') }}</h4>
        <nav class="post-toc__nav">
          <button
            v-for="item in toc"
            :key="item.id"
            class="post-toc__item"
            :class="{ 'post-toc__item--on': activeId === item.id, [`post-toc__item--l${item.level}`]: true }"
            @click="scrollTo(item.id)"
          >
            {{ item.text }}
          </button>
        </nav>
      </aside>
    </article>

    <!-- 骨架屏 -->
    <div v-else-if="loading" class="post-page">
      <div class="skeleton skeleton--row" />
      <div class="skeleton skeleton--row" />
      <div class="skeleton skeleton--row" />
    </div>

    <!-- 404 -->
    <div v-else class="post-page post-page--empty">
      <h1 class="post-page__title serif">{{ t('blog.notFoundTitle') }}</h1>
      <p class="post-page__empty-sub">{{ t('blog.notFoundSub') }}</p>
      <RouterLink to="/blog" class="post-page__back">{{ t('blog.back') }}</RouterLink>
    </div>

    <!-- 编辑模式浮动保存条（有未保存修改时出现） -->
    <Transition name="fade">
      <div v-if="isEditMode && dirty" class="edit-bar">
        <span class="edit-bar__hint">
          <span class="edit-bar__dot" /> {{ t('blog.editMode') }}
          <template v-if="dirty">{{ t('blog.hasUnsaved') }}</template>
        </span>
        <button class="edit-bar__save" :disabled="saving" @click="save">
          {{ saving ? t('blog.saving') : t('blog.save') }}
        </button>
        <span v-if="saved" class="edit-bar__saved">{{ t('blog.saved') }}</span>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
/* ============ 布局：主列 + 右侧目录 ============ */
.post-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 13rem;
  gap: 2.5rem;
  align-items: start;
}
.post-main {
  min-width: 0;
}

/* ============ 顶栏 ============ */
.post-page__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 2.2rem;
}
.post-page__back {
  font-size: 0.85rem;
  color: var(--muted);
  transition: color 0.2s ease;
}
.post-page__back:hover { color: var(--accent); }
.post-page__actions { display: flex; gap: 0.45rem; }
.post-page__action {
  padding: 0.32rem 0.7rem;
  border-radius: 999px;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  color: var(--muted);
  font-size: 0.78rem;
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease, opacity 0.2s ease;
}
.post-page__action--danger:hover {
  color: #ff8282;
  border-color: #ff8282;
}
.post-page__action:disabled { opacity: 0.4; cursor: not-allowed; }

/* ============ 文章头部 ============ */
.post-page__head { margin-bottom: 2rem; }
.post-page__title {
  margin: 0 0 0.8rem;
  font-size: clamp(1.7rem, 4vw, 2.4rem);
  font-weight: 600;
  line-height: 1.35;
}
.post-page__meta {
  font-size: 0.82rem;
  color: var(--faint);
}
.post-page__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  margin-top: 0.9rem;
}
.post-page__tags-edit {
  margin-top: 0.9rem;
  font-size: 0.82rem;
  color: var(--muted);
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}
.post-page__excerpt-edit {
  margin-top: 1.1rem;
  font-size: 0.92rem;
  line-height: 1.7;
  color: var(--muted);
  font-style: italic;
}

/* ============ 正文 ============ */
.post-page__body {
  border-top: 1px solid var(--card-border);
  padding-top: 1.6rem;
}
.post-page__content {
  margin: 0;
  font-size: 0.98rem;
  line-height: 1.9;
  color: var(--text);
  white-space: pre-wrap;
}
.post-page__content :deep(h1),
.post-page__content :deep(h2),
.post-page__content :deep(h3) {
  margin: 1.6em 0 0.6em;
  line-height: 1.4;
  scroll-margin-top: 5.5rem;
}
.post-page__content :deep(h1) { font-size: 1.5rem; }
.post-page__content :deep(h2) { font-size: 1.3rem; }
.post-page__content :deep(h3) { font-size: 1.12rem; }
.post-page__content :deep(p) { margin: 0 0 0.8em; }
.post-page__content :deep(ul),
.post-page__content :deep(ol) {
  padding-left: 1.4em;
  margin: 0 0 0.8em;
}
.post-page__content :deep(blockquote) {
  margin: 0.8em 0;
  padding: 0.35em 0.9em;
  border-left: 3px solid var(--accent);
  color: var(--muted);
  font-style: italic;
}
.post-page__content :deep(a) { color: var(--accent); }
.post-page__content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 10px;
  margin: 0.6rem 0;
  display: block;
  box-shadow: 0 6px 20px -8px var(--shadow-color, rgba(0, 0, 0, 0.3));
}
.post-page__pasting {
  margin: 0.4rem 0 0;
  font-size: 0.78rem;
  color: var(--accent);
}
.post-page__content--edit {
  outline: none;
  min-height: 12rem;
  white-space: normal;
}
.post-page__content--excerpt {
  color: var(--muted);
  font-style: italic;
}

/* 展示模式：清除正文内联背景色。
 * 从参考站复制内容时会带入硬编码深色背景（如 rgb(10,10,10)），
 * 浅色主题下黑底白字不可读；这里强制背景透明、跟随主题，
 * 文字颜色（用户样式栏设置的彩色文字）仍保留。 */
.post-page__content:not(.post-page__content--edit) :deep(p),
.post-page__content:not(.post-page__content--edit) :deep(span),
.post-page__content:not(.post-page__content--edit) :deep(div),
.post-page__content:not(.post-page__content--edit) :deep(li),
.post-page__content:not(.post-page__content--edit) :deep(blockquote) {
  background-color: transparent !important;
}

/* ============ contenteditable 编辑提示 ============ */
.editable {
  outline: none;
  border-radius: 4px;
  transition: box-shadow 0.15s ease;
}
.editable:hover { box-shadow: 0 0 0 1px var(--accent-soft); }
.editable:focus { box-shadow: 0 0 0 1.5px var(--accent); }

/* ============ 右侧目录列（On this page）============ */
.post-toc {
  position: sticky;
  top: 5.5rem;
  max-height: calc(100vh - 7rem);
  overflow-y: auto;
  padding-left: 0.9rem;
  border-left: 1px solid var(--card-border);
}
.post-toc__title {
  margin: 0 0 0.7rem;
  font-size: 0.78rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--faint);
}
.post-toc__nav {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}
.post-toc__item {
  display: block;
  width: 100%;
  padding: 0.28rem 0.4rem;
  border: none;
  background: transparent;
  text-align: left;
  font-size: 0.8rem;
  line-height: 1.45;
  color: var(--muted);
  cursor: pointer;
  border-radius: 5px;
  transition: color 0.15s ease, background 0.15s ease;
}
.post-toc__item:hover {
  color: var(--text);
  background: var(--accent-soft);
}
.post-toc__item--on {
  color: var(--accent);
  background: var(--accent-soft);
}
.post-toc__item--l1 { padding-left: 0.4rem; }
.post-toc__item--l2 { padding-left: 0.9rem; }
.post-toc__item--l3 { padding-left: 1.4rem; }

/* ============ 响应式：窄屏隐藏目录列 ============ */
@media (max-width: 860px) {
  .post-layout {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  .post-toc { display: none; }
}

/* ============ 骨架 / 空态 ============ */
.skeleton {
  border-radius: 0.7rem;
  background: var(--card-border);
  opacity: 0.5;
  animation: blink 1.4s ease-in-out infinite;
}
.skeleton--row { height: 7.5rem; margin-bottom: 1rem; }
.post-page--empty { text-align: center; padding: 4rem 0; }
.post-page__empty-sub { margin: 0.6rem 0 1.6rem; color: var(--faint); font-size: 0.9rem; }

/* ============ 浮动保存条 ============ */
.edit-bar {
  position: fixed;
  right: 1.2rem;
  bottom: 1.2rem;
  z-index: 90;
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.6rem 0.9rem 0.6rem 1rem;
  border-radius: 999px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.28);
}
.edit-bar__hint {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  font-size: 0.8rem;
  color: var(--muted);
  white-space: nowrap;
}
.edit-bar__dot {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 999px;
  background: var(--accent);
  box-shadow: 0 0 8px var(--accent-soft);
  animation: blink 2s ease-in-out infinite;
}
.edit-bar__save {
  padding: 0.42rem 1rem;
  border-radius: 999px;
  border: none;
  background: var(--accent);
  color: #fff;
  font-size: 0.82rem;
  cursor: pointer;
  transition: opacity 0.2s ease;
}
.edit-bar__save:hover:not(:disabled) { opacity: 0.88; }
.edit-bar__save:disabled { opacity: 0.5; cursor: not-allowed; }
.edit-bar__saved {
  font-size: 0.8rem;
  color: #34d399;
  white-space: nowrap;
}
</style>
