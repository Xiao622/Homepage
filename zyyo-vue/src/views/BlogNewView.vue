<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { blogApi } from '@/api'
import EditorToolbar from '@/components/blog/EditorToolbar.vue'
import { t } from '@/i18n'

/**
 * 新建文章页（/blog/new）：
 * 进入一个空模板，直接在页面内编辑（标题 / 日期 / 标签 / 摘要 / 正文富文本），
 * 正文选中文字时出现 Word 同款样式栏；点击「保存」创建文章并跳转详情页。
 */
const router = useRouter()

const titleEl = ref(null)
const dateEl = ref(null)
const tagsEl = ref(null)
const excerptEl = ref(null)
const bodyEl = ref(null)

const saving = ref(false)
const error = ref('')

/** 默认日期：今天（yyyy.MM.dd） */
function today() {
  const d = new Date()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}.${mm}.${dd}`
}

onMounted(() => {
  if (dateEl.value) dateEl.value.textContent = today()
  document.title = `${t('blog.newPost')} — Leo`
})

function readEditors() {
  return {
    title: titleEl.value?.textContent.trim() || '',
    date: dateEl.value?.textContent.trim() || '',
    excerpt: excerptEl.value?.textContent.trim() || '',
    content: bodyEl.value?.innerHTML || '',
    tags: (tagsEl.value?.textContent || '')
      .split(/[,，]/)
      .map((s) => s.trim())
      .filter(Boolean),
  }
}

async function save() {
  const data = readEditors()
  if (!data.title) { error.value = t('blog.titleRequired'); return }
  if (!data.date) { error.value = t('blog.dateRequired'); return }

  saving.value = true
  error.value = ''
  try {
    const created = await blogApi.createPost({
      title: data.title,
      date: data.date,
      minutes: 5,
      excerpt: data.excerpt,
      content: data.content,
      tags: data.tags,
    })
    router.replace(`/blog/${created.slug}`)
  } catch (e) {
    error.value = e.message || t('blog.publishFailed')
  } finally {
    saving.value = false
  }
}

function cancel() {
  router.push('/blog')
}
</script>

<template>
  <div class="container page">
    <div class="new-post">
      <!-- 顶栏：返回 + 操作 -->
      <div class="new-post__bar">
        <RouterLink to="/blog" class="new-post__back">{{ t('blog.back') }}</RouterLink>
        <div class="new-post__actions">
          <button class="new-post__action" @click="cancel">{{ t('blog.cancel') }}</button>
          <button class="new-post__action new-post__action--primary" :disabled="saving" @click="save">
            {{ saving ? t('blog.saving') : t('blog.save') }}
          </button>
        </div>
      </div>

      <p v-if="error" class="new-post__error">{{ error }}</p>

      <!-- 空模板：与详情页编辑态一致的页面内编辑 -->
      <article class="post-page">
        <header class="post-page__head">
          <div
            ref="titleEl"
            class="new-editor new-post__title serif"
            contenteditable="true"
            :data-placeholder="t('blog.postTitlePlaceholder')"
            spellcheck="false"
          />
          <div class="post-page__meta mono">
            <span
              ref="dateEl"
              class="new-editor"
              contenteditable="true"
              :data-placeholder="t('blog.datePlaceholder')"
              spellcheck="false"
            />
          </div>
          <div
            ref="tagsEl"
            class="new-editor post-page__tags-edit mono"
            contenteditable="true"
            :data-placeholder="t('blog.tagsPlaceholder')"
            spellcheck="false"
          />
          <div
            ref="excerptEl"
            class="new-editor new-post__excerpt"
            contenteditable="true"
            :data-placeholder="t('blog.excerptPlaceholder')"
            spellcheck="false"
          />
        </header>

        <div class="post-page__body">
          <div
            ref="bodyEl"
            class="new-editor post-page__content new-post__body"
            contenteditable="true"
            :data-placeholder="t('blog.bodyPlaceholder')"
            spellcheck="false"
          />
        </div>
      </article>

      <EditorToolbar :container="bodyEl" />
    </div>
  </div>
</template>

<style scoped>
.new-post {
  max-width: 46rem;
  margin: 0 auto;
}

/* ---------- 顶栏 ---------- */
.new-post__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.6rem;
}
.new-post__back {
  font-size: 0.85rem;
  color: var(--muted);
  transition: color 0.2s ease;
}
.new-post__back:hover { color: var(--accent); }
.new-post__actions {
  display: flex;
  gap: 0.45rem;
}
.new-post__action {
  padding: 0.34rem 0.85rem;
  border-radius: 999px;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  color: var(--muted);
  font-size: 0.8rem;
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease, opacity 0.2s ease;
}
.new-post__action:hover { color: var(--text); border-color: var(--card-border-hover); }
.new-post__action--primary {
  background: var(--accent);
  border-color: transparent;
  color: #fff;
}
.new-post__action--primary:hover { opacity: 0.88; color: #fff; }
.new-post__action:disabled { opacity: 0.55; cursor: not-allowed; }

.new-post__error {
  margin: 0 0 1rem;
  padding: 0.55rem 0.9rem;
  border-radius: 0.6rem;
  background: rgba(229, 72, 77, 0.12);
  border: 1px solid rgba(229, 72, 77, 0.35);
  color: #ff8282;
  font-size: 0.85rem;
}

/* ---------- 文章头部 ---------- */
.post-page__head {
  margin-bottom: 2rem;
}
.new-post__title {
  margin: 0 0 0.8rem;
  font-size: clamp(1.7rem, 4vw, 2.4rem);
  font-weight: 600;
  line-height: 1.35;
}
.post-page__meta {
  font-size: 0.82rem;
  color: var(--faint);
}
.post-page__tags-edit {
  margin-top: 0.9rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  font-size: 0.82rem;
  color: var(--muted);
}
.new-post__excerpt {
  margin-top: 1.1rem;
  font-size: 0.92rem;
  line-height: 1.7;
  color: var(--muted);
  font-style: italic;
}

/* ---------- 正文 ---------- */
.post-page__body {
  border-top: 1px solid var(--card-border);
  padding-top: 1.6rem;
}
.new-post__body {
  min-height: 24rem;
  font-size: 0.98rem;
  line-height: 1.9;
  color: var(--text);
  outline: none;
}

/* ---------- contenteditable 空态占位 ---------- */
.new-editor {
  outline: none;
}
.new-editor:empty::before {
  content: attr(data-placeholder);
  color: var(--faint);
  pointer-events: none;
}
.new-editor:focus {
  border-radius: 4px;
  box-shadow: 0 0 0 1px var(--accent-soft);
}
</style>
