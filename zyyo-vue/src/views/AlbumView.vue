<script setup>
import { ref, computed, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { photosApi } from '@/api'
import { useEditMode } from '@/composables/useEditMode'
import PhotoLightbox from '@/components/photos/PhotoLightbox.vue'
import { t } from '@/i18n'

/**
 * 相册详情页（/photos/:category）：该相册内的照片网格。
 * - 点击照片打开大图弹窗（翻页 / 下载 / 上传 / 评论栏 / 描述编辑）
 * - 编辑模式：上传到当前相册、hover 删除、卡片信息原地编辑 + 保存条
 * - 编辑模式：相册名（标题）可原地改名 → 批量更新该相册全部照片的分类
 * - 年月筛选保留（在该相册内过滤）
 */
const route = useRoute()
const router = useRouter()
const category = computed(() => route.params.category)
const { isEditMode } = useEditMode()

const photos = ref([])
const total = ref(0)
const loading = ref(true)
const month = ref('')

/* ---------- 大图弹窗 ---------- */
const lightboxIndex = ref(-1)

function openLightbox(i) {
  lightboxIndex.value = i
}
function closeLightbox() {
  lightboxIndex.value = -1
}
async function onLightboxUploaded() {
  // 上传成功：刷新列表，新照片位于列表头部（id 倒序），弹窗跳到第一张
  await load()
  lightboxIndex.value = 0
}

/* ---------- 年月筛选 ---------- */
function onMonthChange() {
  load()
}
function clearMonth() {
  month.value = ''
  load()
}

/* ---------- 上传面板（分类固定为当前相册） ---------- */
const showUpload = ref(false)
const uploading = ref(false)
const fileInput = ref(null)
const uploadFile = ref(null)
const upTakenDate = ref('')
const upCaption = ref('')

function nowMonth() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}
function openUpload() {
  showUpload.value = true
  upTakenDate.value = nowMonth()
}
function chooseFile(e) {
  uploadFile.value = e.target.files[0] || null
}
async function doUpload() {
  if (!uploadFile.value) {
    alert(t('photos.uploadFileFirst'))
    return
  }
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', uploadFile.value)
    fd.append('category', category.value)
    if (upTakenDate.value) fd.append('takenDate', upTakenDate.value)
    if (upCaption.value.trim()) fd.append('caption', upCaption.value.trim())
    await photosApi.uploadPhoto(fd)
    uploadFile.value = null
    if (fileInput.value) fileInput.value.value = ''
    upTakenDate.value = nowMonth()
    upCaption.value = ''
    showUpload.value = false
    await load()
  } catch (e) {
    alert(e.message || t('photos.uploadFailed'))
  } finally {
    uploading.value = false
  }
}

/* ---------- 删除 ---------- */
const deletingId = ref(null)
async function removePhoto(photo) {
  if (!window.confirm(t('photos.deletePhotoConfirm'))) return
  deletingId.value = photo.id
  try {
    await photosApi.deletePhoto(photo.id)
    if (lightboxIndex.value >= 0) closeLightbox()
    await load()
  } catch (e) {
    alert(e.message || t('photos.deleteFailed'))
  } finally {
    deletingId.value = null
  }
}

/* ---------- 卡片信息原地编辑 + 保存条 ---------- */
const hasChanges = ref(false)
const saving = ref(false)
const saved = ref(false)
const gridEl = ref(null)

const vFill = {
  mounted(el, binding) {
    el.textContent = binding.value ?? ''
  },
  updated(el, binding) {
    if (binding.value !== binding.oldValue) {
      el.textContent = binding.value ?? ''
    }
  },
}

function fillDom() {
  if (!gridEl.value) return
  gridEl.value.querySelectorAll('[data-path^="photo."]').forEach((el) => {
    const parts = el.dataset.path.split('.')
    const photo = photos.value.find((p) => String(p.id) === parts[1])
    if (!photo) return
    el.textContent = photo[parts[2]] || ''
  })
}

function onEdit() {
  hasChanges.value = true
}

function collectDrafts() {
  const drafts = {}
  gridEl.value?.querySelectorAll('[data-path^="photo."]').forEach((el) => {
    const parts = el.dataset.path.split('.')
    const id = parts[1]
    const field = parts[2]
    if (!drafts[id]) drafts[id] = {}
    drafts[id][field] = (el.textContent || '').trim()
  })
  for (const id of Object.keys(drafts)) {
    const photo = photos.value.find((p) => String(p.id) === id)
    if (!photo) {
      delete drafts[id]
      continue
    }
    const d = drafts[id]
    if (
      (d.category ?? photo.category) === photo.category &&
      (d.takenDate ?? '') === (photo.takenDate || '') &&
      (d.caption ?? '') === (photo.caption || '')
    ) {
      delete drafts[id]
    }
  }
  return drafts
}

async function save() {
  const drafts = collectDrafts()
  const ids = Object.keys(drafts)
  // 相册重命名：标题被改成新名字 → 批量更新该相册全部照片的分类
  const nameEl = document.querySelector('[data-album-name]')
  const newName = nameEl?.textContent?.trim() || ''
  const renaming = newName !== category.value
  if (renaming && !newName) {
    alert(t('photos.albumNameRequired2'))
    return
  }
  saving.value = true
  try {
    if (renaming) {
      for (const p of photos.value) {
        await photosApi.updatePhoto(p.id, { category: newName })
      }
    }
    for (const id of ids) {
      await photosApi.updatePhoto(id, drafts[id])
    }
    hasChanges.value = false
    if (renaming) {
      await router.replace({ name: 'photo-album', params: { category: newName } })
      await load()
    } else {
      await load()
    }
    saved.value = true
    setTimeout(() => (saved.value = false), 2200)
  } catch (e) {
    alert(e.message || t('photos.saveFailed'))
  } finally {
    saving.value = false
  }
}

const draftCount = () => Object.keys(collectDrafts()).length

/* ---------- 数据加载 ---------- */
async function load() {
  loading.value = true
  try {
    const data = await photosApi.getPhotos({ category: category.value, month: month.value })
    photos.value = data.list
    total.value = data.total
  } catch (e) {
    console.error('Load photos failed:', e)
  } finally {
    loading.value = false
    await nextTick()
    fillDom()
  }
}

function onImgError(e) {
  e.target.closest('figure')?.classList.add('photo--broken')
}

watch(isEditMode, () => {
  hasChanges.value = false
  fillDom()
})

// 相册改名后路由参数变化：清空筛选、关闭弹窗并重新加载
watch(category, () => {
  hasChanges.value = false
  month.value = ''
  closeLightbox()
  load()
})

onMounted(load)
onBeforeUnmount(() => closeLightbox())
</script>

<template>
  <div class="container page">
    <header class="page-header">
      <RouterLink to="/photos" class="back mono">← Photos</RouterLink>
      <div class="head-main">
        <h1
          :contenteditable="isEditMode ? 'true' : 'false'"
          v-fill="category"
          data-album-name
          spellcheck="false"
          @input="onEdit"
        />
        <p class="sub">{{ t('photos.photoCount', { n: total }) }}</p>
      </div>
      <button v-if="isEditMode" class="btn btn--accent" @click="openUpload">
        {{ t('photos.upload') }}
      </button>
    </header>

    <!-- 年月筛选 -->
    <div class="filter">
      <input
        v-model="month"
        type="month"
        class="month-input mono"
        :aria-label="t('photos.monthFilter')"
        @change="onMonthChange"
      />
      <button v-if="month" class="clear-btn" @click="clearMonth">{{ t('photos.clearMonth') }}</button>
    </div>

    <!-- 上传面板（分类固定为当前相册） -->
    <div v-if="isEditMode && showUpload" class="upload-panel card card-pad">
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        class="upload-file"
        @change="chooseFile"
      />
      <div class="upload-fields">
        <input
          v-model="upTakenDate"
          type="month"
          class="upload-input mono"
          :aria-label="t('photos.takenMonth')"
        />
        <input
          v-model="upCaption"
          class="upload-input"
          :placeholder="t('photos.captionOptional')"
          :aria-label="t('photos.captionLabel')"
        />
      </div>
      <div class="upload-actions">
        <button class="btn btn--accent" :disabled="uploading" @click="doUpload">
          {{ uploading ? t('photos.uploading') : t('photos.uploadBtn') }}
        </button>
        <button class="btn btn--ghost" @click="showUpload = false">{{ t('photos.cancel') }}</button>
      </div>
    </div>

    <div v-if="loading" class="grid">
      <div v-for="i in 6" :key="i" class="skeleton" />
    </div>

    <div v-else ref="gridEl" class="grid">
      <figure
        v-for="(p, i) in photos"
        :key="p.id"
        class="photo card"
        :style="{ '--img-broken': JSON.stringify(t('photos.imgBroken')) }"
      >
        <div class="photo__imgwrap" @click="openLightbox(i)">
          <img
            :src="p.url"
            :alt="p.caption || p.category"
            loading="lazy"
            @error="onImgError"
          />
          <button
            v-if="isEditMode"
            class="photo__del"
            :title="t('photos.deletePhotoTitle')"
            :disabled="deletingId === p.id"
            @click.stop="removePhoto(p)"
          >
            ✕
          </button>
        </div>
        <figcaption class="photo__meta">
          <div class="photo__row">
            <span
              class="photo__field mono"
              :contenteditable="isEditMode ? 'true' : 'false'"
              v-fill="p.takenDate"
              :data-path="`photo.${p.id}.takenDate`"
              :data-placeholder="t('photos.addTimePlaceholder')"
              spellcheck="false"
              @input="onEdit"
            />
            <span
              class="photo__caption"
              :contenteditable="isEditMode ? 'true' : 'false'"
              v-fill="p.caption"
              :data-path="`photo.${p.id}.caption`"
              :data-placeholder="t('photos.addCaptionPlaceholder')"
              spellcheck="false"
              @input="onEdit"
            />
          </div>
        </figcaption>
      </figure>

      <p v-if="!photos.length" class="empty">
        {{ month ? t('photos.emptyInMonth') : t('photos.emptyAlbum') }}
      </p>
    </div>

    <!-- 大图弹窗 -->
    <PhotoLightbox
      v-if="lightboxIndex >= 0"
      :photos="photos"
      :index="lightboxIndex"
      :category="category.value"
      @close="closeLightbox"
      @update:index="(i) => (lightboxIndex = i)"
      @uploaded="onLightboxUploaded"
      @updated="load"
    />

    <!-- 编辑模式浮动保存条 -->
    <Transition name="fade">
      <div v-if="isEditMode && hasChanges" class="edit-bar">
        <span class="edit-bar__hint">
          <span class="edit-bar__dot" /> {{ t('photos.editMode') }}
          <template v-if="draftCount()"> {{ t('photos.pendingPhotos', { n: draftCount() }) }}</template>
        </span>
        <button class="edit-bar__save" :disabled="saving" @click="save">
          {{ saving ? t('photos.uploading') : t('blog.save') }}
        </button>
        <span v-if="saved" class="edit-bar__saved">{{ t('blog.saved') }}</span>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.page-header {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 1rem;
}
.back {
  justify-self: start;
  font-size: 0.85rem;
  color: var(--muted);
  text-decoration: none;
  padding: 0.4rem 0.8rem;
  border-radius: 999px;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  transition: color 0.2s ease, border-color 0.2s ease;
  white-space: nowrap;
}
.back:hover { color: var(--text); border-color: var(--card-border-hover); }
/* 中间标题组居中（Photos / Upload 保持在两端） */
.head-main {
  justify-self: center;
  display: flex;
  align-items: center;
  gap: 0.8rem;
  min-width: 0;
  max-width: 100%;
}
.page-header > .btn { justify-self: end; }
.head-main h1 {
  margin: 0;
  font-size: 1.5rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  outline: none;
  border-radius: 4px;
  transition: box-shadow 0.15s ease;
  cursor: text;
}
.head-main h1:hover { box-shadow: 0 0 0 1px var(--accent-soft); }
.head-main h1:focus { box-shadow: 0 0 0 1.5px var(--accent); }
.head-main h1:empty::before {
  content: attr(data-placeholder);
  color: var(--faint);
  font-style: italic;
}
.head-main .sub { white-space: nowrap; margin: 0; }
@media (max-width: 560px) {
  .page-header { grid-template-columns: 1fr auto; }
  .head-main {
    grid-column: 1 / -1;
    grid-row: 2;
    justify-content: center;
  }
}

.filter {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.6rem;
  margin: 1rem 0 1.5rem;
}
.month-input {
  padding: 0.42rem 0.7rem;
  border-radius: 999px;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  color: var(--muted);
  font-size: 0.82rem;
  outline: none;
  /* 原生控件（含日历图标）跟随主题：浅色=深色图标，深色=白色图标 */
  color-scheme: light;
}
[data-theme='dark'] .month-input {
  color-scheme: dark;
}
.month-input:focus { border-color: var(--accent); }
.month-input::-webkit-calendar-picker-indicator { cursor: pointer; }
.clear-btn {
  border: none;
  background: transparent;
  color: var(--accent);
  font-size: 0.8rem;
  cursor: pointer;
  padding: 0.2rem 0.4rem;
}

/* ---------- 上传面板 ---------- */
.upload-panel {
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
  margin-bottom: 1.5rem;
}
.upload-file {
  font-size: 0.85rem;
  color: var(--muted);
}
.upload-file::file-selector-button {
  margin-right: 0.7rem;
  padding: 0.4rem 0.9rem;
  border-radius: 999px;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  color: var(--text);
  font-size: 0.8rem;
  cursor: pointer;
}
.upload-fields {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}
.upload-input {
  flex: 1;
  min-width: 8rem;
  padding: 0.42rem 0.7rem;
  border-radius: 0.6rem;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  color: var(--text);
  font-size: 0.85rem;
  outline: none;
}
.upload-input:focus { border-color: var(--accent); }
.upload-actions {
  display: flex;
  gap: 0.5rem;
}

/* ---------- 网格 ---------- */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(13.5rem, 1fr));
  gap: 0.9rem;
}
.skeleton {
  aspect-ratio: 4 / 3.6;
  border-radius: var(--card-radius-sm);
  background: var(--card-border);
  opacity: 0.5;
  animation: blink 1.4s ease-in-out infinite;
}

.photo {
  margin: 0;
  padding: 0;
  overflow: hidden;
  border-radius: var(--card-radius-sm);
  cursor: zoom-in;
  transition: transform 0.25s ease, border-color 0.25s ease;
}
.photo:hover { transform: translateY(-3px); border-color: var(--card-border-hover); }

.photo__imgwrap {
  position: relative;
  aspect-ratio: 4 / 3;
  background: var(--card-border);
  overflow: hidden;
}
.photo__imgwrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.photo--broken .photo__imgwrap::after {
  content: var(--img-broken, 'load failed');
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  color: var(--faint);
  background: var(--card-border);
}
.photo__del {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  width: 1.7rem;
  height: 1.7rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 0.74rem;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s ease, background-color 0.2s ease;
}
.photo:hover .photo__del { opacity: 1; }
.photo__del:hover { background: rgba(255, 90, 90, 0.85); }
.photo__del:disabled { opacity: 0.35; cursor: not-allowed; }

.photo__meta {
  margin: 0;
  padding: 0.5rem 0.7rem 0.55rem;
}
.photo__row {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: baseline;
  gap: 0.5rem;
}
.photo__field {
  font-size: 0.78rem;
  color: var(--muted);
}
.photo__caption {
  font-size: 0.78rem;
  color: var(--text);
  line-height: 1.5;
}

.photo__meta [contenteditable='true'] {
  outline: none;
  border-radius: 3px;
  transition: box-shadow 0.15s ease;
  cursor: text;
}
.photo__meta [contenteditable='true']:hover { box-shadow: 0 0 0 1px var(--accent-soft); }
.photo__meta [contenteditable='true']:focus { box-shadow: 0 0 0 1.5px var(--accent); }
/* 编辑模式下的空值占位提示 */
.photo__meta [contenteditable='true']:empty::before {
  content: attr(data-placeholder);
  color: var(--faint);
  font-style: italic;
}

.empty {
  grid-column: 1 / -1;
  text-align: center;
  color: var(--faint);
  padding: 3rem 0;
}

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
