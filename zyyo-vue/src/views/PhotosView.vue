<script setup>
import { ref, onMounted } from 'vue'
import { photosApi } from '@/api'
import { useEditMode } from '@/composables/useEditMode'
import { t } from '@/i18n'

/**
 * 相册列表页：以「相册」为单位展示（封面 + 名称 + 照片数），
 * 点击相册进入 /photos/:category 查看该相册内的照片。
 * 编辑模式：＋ Upload 上传（分类名即相册名），新相册上传后立即可见。
 */
const { isEditMode } = useEditMode()
const albums = ref([])
const loading = ref(true)

/* ---------- 上传面板（新建相册 = 上传首图并命名） ---------- */
const showUpload = ref(false)
const uploading = ref(false)
const fileInput = ref(null)
const uploadFile = ref(null)
const upCategory = ref('')
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
  if (!upCategory.value.trim()) {
    alert(t('photos.albumNameRequired'))
    return
  }
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', uploadFile.value)
    fd.append('category', upCategory.value.trim())
    if (upTakenDate.value) fd.append('takenDate', upTakenDate.value)
    if (upCaption.value.trim()) fd.append('caption', upCaption.value.trim())
    await photosApi.uploadPhoto(fd)
    uploadFile.value = null
    if (fileInput.value) fileInput.value.value = ''
    upCategory.value = ''
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

async function load() {
  loading.value = true
  try {
    albums.value = await photosApi.getAlbums()
  } catch (e) {
    console.error('Load albums failed:', e)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="container page">
    <header class="page-header">
      <h1>Photos</h1>
      <p class="sub">{{ t('photos.sub', { n: albums.length }) }}</p>
      <button v-if="isEditMode" class="btn btn--accent page-header__btn" @click="openUpload">
        {{ t('photos.upload') }}
      </button>
    </header>

    <!-- 上传面板（新建相册：分类名即相册名） -->
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
          v-model="upCategory"
          class="upload-input"
          :placeholder="t('photos.albumPlaceholder')"
          :aria-label="t('photos.albumLabel')"
        />
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

    <div v-else class="grid">
      <RouterLink
        v-for="a in albums"
        :key="a.name"
        :to="{ name: 'photo-album', params: { category: a.name } }"
        class="album card"
      >
        <div class="album__cover">
          <img :src="a.cover" :alt="a.name" loading="lazy" />
        </div>
        <div class="album__meta">
          <span class="album__name">{{ a.name }}</span>
          <span class="album__count mono">{{ t('photos.albumCount', { n: a.count }) }}</span>
        </div>
      </RouterLink>

      <p v-if="!albums.length" class="empty">
        {{ t('photos.empty') }}
      </p>
    </div>

    <p class="note faint">{{ t('photos.note', { n: albums.length }) }}</p>
  </div>
</template>

<style scoped>
.page-header { display: flex; align-items: center; gap: 1rem; flex-wrap: wrap; }
.page-header h1 { margin: 0; }
.page-header__btn { margin-left: auto; }

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

.album {
  margin: 0;
  padding: 0;
  overflow: hidden;
  border-radius: var(--card-radius-sm);
  text-decoration: none;
  transition: transform 0.25s ease, border-color 0.25s ease;
}
.album:hover { transform: translateY(-3px); border-color: var(--card-border-hover); }

.album__cover {
  aspect-ratio: 4 / 3;
  background: var(--card-border);
  overflow: hidden;
}
.album__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}
.album:hover .album__cover img { transform: scale(1.04); }

.album__meta {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 0.5rem;
  padding: 0.55rem 0.7rem 0.6rem;
}
.album__name {
  font-size: 0.88rem;
  color: var(--text);
  font-weight: 600;
}
.album__count {
  font-size: 0.72rem;
  color: var(--faint);
}

.empty {
  grid-column: 1 / -1;
  text-align: center;
  color: var(--faint);
  padding: 3rem 0;
}
.note {
  margin-top: 1.6rem;
  font-size: 0.8rem;
  text-align: center;
}
</style>
