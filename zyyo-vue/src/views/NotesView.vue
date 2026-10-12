<script setup>
import { ref, reactive, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { notesApi } from '@/api'
import { useEditMode } from '@/composables/useEditMode'
import { t } from '@/i18n'

/**
 * 随记页：一句话式生活记录时间线。
 *  - 排序：By Time（最新在前）/ Random；搜索框对标题与正文做模糊查询（300ms 防抖）
 *  - 编辑模式（管理员登录后）：在当前页直接编辑每条随记的时间 / 标题 / 内容，
 *    可新建（＋ New note 插入顶部空卡片）与删除（卡片右上 ✕）；
 *    有修改时右下角浮动保存条，统一保存（新建 POST / 修改 PUT）后刷新列表。
 */
const { isEditMode } = useEditMode()

const notes = ref([])
const total = ref(0)
const order = ref('time')
const expanded = ref(new Set())
const loading = ref(true)

const keywordInput = ref('')
const keyword = ref('')
let searchTimer = null
function onSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    keyword.value = keywordInput.value.trim()
    load()
  }, 300)
}

/* ---------- 编辑模式状态 ---------- */
const drafts = reactive({})            // id -> { date, title, text }
const deletingId = ref(null)
const saving = ref(false)
const saved = ref(false)
const noteRefs = new Map()             // id -> { date, title, text } DOM refs

function setNoteRef(field, id) {
  return (el) => {
    if (!el) return
    let entry = noteRefs.get(id)
    if (!entry) {
      entry = { date: null, title: null, text: null }
      noteRefs.set(id, entry)
    }
    entry[field] = el
  }
}

/** 当前日期 yyyy.M.d（与种子格式一致，如 2026-9-30） */
function today() {
  const d = new Date()
  return `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`
}

/** 回填所有 contenteditable（仅在 notes / 编辑模式切换时调用，避免覆盖用户输入） */
function fillEditors() {
  if (!isEditMode.value) return
  for (const note of notes.value) {
    const refs = noteRefs.get(note.id)
    if (!refs) continue
    if (refs.date) refs.date.textContent = note.date || ''
    if (refs.title) refs.title.textContent = note.title || ''
    if (refs.text) refs.text.textContent = note.text || ''
  }
}

/** 输入事件：记录草稿（与后端原值一致时视为未修改） */
function markDraft(note, field) {
  const refs = noteRefs.get(note.id)
  if (!refs) return
  const val = field === 'date' ? refs.date.textContent.trim()
    : field === 'title' ? refs.title.textContent.trim()
    : refs.text.textContent.trim()
  const base = { date: note.date, title: note.title || '', text: note.text }
  const cur = drafts[note.id] || { ...base }
  cur[field] = val
  if (cur.date === base.date && cur.title === base.title && cur.text === base.text) {
    delete drafts[note.id]
  } else {
    drafts[note.id] = cur
  }
}

/** 新建：在列表顶部插入一张空卡片（本地 temp id，保存时 POST） */
async function addNote() {
  const id = `temp-${Date.now()}`
  notes.value.unshift({ id, date: today(), title: '', text: '', expandable: false, isNew: true })
  // 新条目默认视为有内容待写，直接进草稿
  drafts[id] = { date: today(), title: '', text: '' }
  // 渲染后回填日期（内容由用户输入）
  await nextTick()
  const refs = noteRefs.get(id)
  if (refs && refs.date) refs.date.textContent = today()
}

async function removeNote(note) {
  if (!window.confirm(t('notes.deleteConfirm'))) return
  deletingId.value = note.id
  try {
    await notesApi.deleteNote(note.id)
    notes.value = notes.value.filter((n) => n.id !== note.id)
    delete drafts[note.id]
  } catch (e) {
    alert(e.message || t('notes.deleteFailed'))
  } finally {
    deletingId.value = null
  }
}

/** 保存全部修改：temp 条目新建，已有条目更新；成功后刷新列表 */
async function save() {
  if (!Object.keys(drafts).length) return
  saving.value = true
  try {
    for (const [id, d] of Object.entries(drafts)) {
      const payload = { date: d.date, title: d.title, text: d.text }
      if (typeof id === 'string' && id.startsWith('temp-')) {
        await notesApi.createNote(payload)
      } else {
        await notesApi.updateNote(id, payload)
      }
    }
    Object.keys(drafts).forEach((k) => delete drafts[k])
    await load()
    saved.value = true
    setTimeout(() => (saved.value = false), 2200)
  } catch (e) {
    alert(e.message || t('notes.saveFailed'))
  } finally {
    saving.value = false
  }
}

/* ---------- 数据加载 ---------- */
async function load(o = order.value, kw = keyword.value) {
  loading.value = true
  try {
    const data = await notesApi.getNotes({ order: o, keyword: kw })
    notes.value = data.list
    total.value = data.total
    noteRefs.clear()
  } catch (e) {
    console.error('Load notes failed:', e)
  } finally {
    loading.value = false
    // 等列表真正渲染（skeleton 切换到 timeline / 编辑分支）后回填编辑内容
    await nextTick()
    fillEditors()
  }
}

function switchOrder(o) {
  if (order.value === o) return
  order.value = o
  expanded.value = new Set()
  load(o)
}

function toggleExpand(id) {
  const next = new Set(expanded.value)
  next.has(id) ? next.delete(id) : next.add(id)
  expanded.value = next
}

/* 编辑模式进入/退出时回填 */
watch(isEditMode, async (v) => {
  if (v) {
    await nextTick()
    fillEditors()
  }
})

onMounted(() => load())
onBeforeUnmount(() => clearTimeout(searchTimer))

const hasDraft = () => Object.keys(drafts).length > 0
</script>

<template>
  <div class="container page">
    <header class="page-header">
      <h1>Notes</h1>
      <p class="sub">{{ t('notes.sub', { n: total }) }}</p>

      <!-- 搜索框 + 新建按钮 -->
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
            :placeholder="t('notes.searchPlaceholder')"
            :aria-label="t('notes.searchPlaceholder')"
            spellcheck="false"
            @input="onSearchInput"
          />
        </div>
        <button v-if="isEditMode" class="btn btn--accent" @click="addNote">{{ t('notes.newNote') }}</button>
      </div>
    </header>

    <!-- 排序切换 -->
    <div class="seg" role="tablist" :aria-label="t('notes.sortLabel')">
      <button
        class="seg__btn"
        :class="{ 'seg__btn--active': order === 'time' }"
        @click="switchOrder('time')"
      >
        {{ t('notes.byTime') }}
      </button>
      <button
        class="seg__btn"
        :class="{ 'seg__btn--active': order === 'random' }"
        @click="switchOrder('random')"
      >
        {{ t('notes.random') }}
      </button>
    </div>

    <div v-if="loading" class="skeleton-list">
      <div v-for="i in 6" :key="i" class="skeleton skeleton--row" />
    </div>

    <div v-else class="timeline">
      <article
        v-for="note in notes"
        :key="note.id"
        class="timeline__item"
        :class="{ 'timeline__item--expandable': note.expandable && !isEditMode }"
      >
        <div class="timeline__rail" aria-hidden="true">
          <span class="timeline__dot" :class="{ 'timeline__dot--open': expanded.has(note.id) }" />
        </div>

        <div class="timeline__body card card--hover card-pad">
          <!-- 编辑模式：删除按钮 -->
          <button
            v-if="isEditMode"
            class="timeline__del"
            :title="t('notes.deleteTitle')"
            :disabled="deletingId === note.id"
            @click="removeNote(note)"
          >
            ✕
          </button>

          <!-- 时间（年月日） -->
          <time v-if="!isEditMode" class="timeline__date mono">{{ note.date }}</time>
          <div
            v-else
            :ref="setNoteRef('date', note.id)"
            class="timeline__date mono editable"
            contenteditable="true"
            spellcheck="false"
            @input="markDraft(note, 'date')"
          />

          <!-- 可选标题 -->
          <h2 v-if="note.title && !isEditMode" class="timeline__title serif">{{ note.title }}</h2>
          <div
            v-else-if="isEditMode"
            :ref="setNoteRef('title', note.id)"
            class="timeline__title serif editable"
            :class="{ 'timeline__title--empty': isEditMode }"
            contenteditable="true"
            spellcheck="false"
            @input="markDraft(note, 'title')"
          />

          <!-- 正文 -->
          <p
            v-if="!isEditMode"
            class="timeline__text"
            :class="{ 'timeline__text--clamped': note.expandable && !expanded.has(note.id) }"
          >
            {{ note.text }}
          </p>
          <div
            v-else
            :ref="setNoteRef('text', note.id)"
            class="timeline__text editable"
            contenteditable="true"
            spellcheck="false"
            @input="markDraft(note, 'text')"
          />

          <button
            v-if="note.expandable && !isEditMode"
            class="timeline__more"
            @click="toggleExpand(note.id)"
          >
            {{ expanded.has(note.id) ? t('notes.collapse') : t('notes.expand') }}
            <svg
              viewBox="0 0 24 24"
              width="13"
              height="13"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
              :style="expanded.has(note.id) ? 'transform: rotate(180deg)' : ''"
            >
              <path d="m6 9 6 6 6-6" />
            </svg>
          </button>
        </div>
      </article>

      <p v-if="!notes.length" class="empty">
        {{ keyword ? t('notes.emptyWithKeyword', { kw: keyword }) : t('notes.empty') }}
      </p>
    </div>

    <!-- 编辑模式浮动保存条（有未保存修改时出现） -->
    <Transition name="fade">
      <div v-if="isEditMode && hasDraft()" class="edit-bar">
        <span class="edit-bar__hint">
          <span class="edit-bar__dot" /> {{ t('notes.editMode') }}
          <template v-if="hasDraft()"> {{ t('notes.pendingCount', { n: Object.keys(drafts).length }) }}</template>
        </span>
        <button class="edit-bar__save" :disabled="saving" @click="save">
          {{ saving ? t('notes.saving') : t('notes.save') }}
        </button>
        <span v-if="saved" class="edit-bar__saved">{{ t('notes.saved') }}</span>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
/* ---------- 页头工具：搜索框 + 新建 ---------- */
.page-header { position: relative; }
.page-header__tools {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-top: 0.6rem;
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
  flex: 1;
  max-width: 16rem;
}
.search:focus-within { border-color: var(--accent); }
.search__input {
  flex: 1;
  min-width: 0;
  border: none;
  background: transparent;
  color: var(--text);
  font-size: 0.85rem;
  font-family: inherit;
  outline: none;
}
.search__input::placeholder { color: var(--faint); }

/* ---------- 分段切换 ---------- */
.seg {
  display: inline-flex;
  gap: 0.3rem;
  padding: 0.25rem;
  border-radius: 999px;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  margin: 1.4rem 0 1.8rem;
}
.seg__btn {
  border: none;
  background: transparent;
  padding: 0.4rem 1rem;
  border-radius: 999px;
  font-size: 0.85rem;
  color: var(--muted);
  transition: color 0.2s ease, background-color 0.2s ease;
}
.seg__btn:hover { color: var(--text); }
.seg__btn--active {
  color: var(--accent);
  background: var(--accent-soft);
}

/* ---------- 时间线 ---------- */
.timeline {
  display: flex;
  flex-direction: column;
}
.timeline__item {
  display: flex;
  gap: 1rem;
}
.timeline__rail {
  position: relative;
  flex: none;
  width: 1.2rem;
  display: flex;
  justify-content: center;
}
/* 竖向连线 */
.timeline__rail::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  width: 1px;
  transform: translateX(-50%);
  background: var(--card-border);
}
.timeline__item:first-child .timeline__rail::before { top: 0.6rem; }
.timeline__item:last-child .timeline__rail::before { bottom: auto; height: 0.6rem; }

.timeline__dot {
  position: relative;
  z-index: 1;
  margin-top: 1.9rem;
  width: 0.55rem;
  height: 0.55rem;
  border-radius: 999px;
  background: var(--faint);
  transition: background-color 0.25s ease, box-shadow 0.25s ease;
}
.timeline__dot--open {
  background: var(--accent);
  box-shadow: 0 0 10px var(--accent-soft);
}

.timeline__body {
  position: relative;
  flex: 1;
  min-width: 0;
  margin-bottom: 1.1rem;
}
.timeline__del {
  position: absolute;
  top: 0.6rem;
  right: 0.6rem;
  width: 1.6rem;
  height: 1.6rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--card-border);
  border-radius: 999px;
  background: var(--card-bg);
  color: var(--muted);
  font-size: 0.72rem;
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease, opacity 0.2s ease;
  z-index: 2;
}
.timeline__del:hover { color: #ff8282; border-color: #ff8282; }
.timeline__del:disabled { opacity: 0.4; cursor: not-allowed; }

.timeline__date {
  display: block;
  font-size: 0.76rem;
  color: var(--faint);
  margin-bottom: 0.3rem;
}
.timeline__title {
  margin: 0 0 0.35rem;
  font-size: 1.08rem;
  font-weight: 600;
}
.timeline__title--empty { margin-top: 0.4rem; }
.timeline__text {
  margin: 0;
  font-size: 0.92rem;
  color: var(--muted);
  line-height: 1.75;
}
.timeline__text--clamped {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.timeline__more {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  margin-top: 0.6rem;
  padding: 0;
  border: none;
  background: transparent;
  font-size: 0.82rem;
  color: var(--accent);
}
.timeline__more svg { transition: transform 0.25s ease; }

/* ---------- contenteditable 编辑提示 ---------- */
.editable {
  outline: none;
  border-radius: 4px;
  transition: box-shadow 0.15s ease;
}
.editable:hover { box-shadow: 0 0 0 1px var(--accent-soft); }
.editable:focus { box-shadow: 0 0 0 1.5px var(--accent); }

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
.skeleton--row { height: 6rem; }

/* ---------- 浮动保存条 ---------- */
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
