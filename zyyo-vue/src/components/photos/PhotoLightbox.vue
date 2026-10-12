<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { photosApi } from '@/api'
import { useEditMode } from '@/composables/useEditMode'
import { t } from '@/i18n'

/**
 * 大图弹窗（参照 QQ 相册）：
 * - 左侧大图：左右箭头 / 键盘 ← → 翻页，Esc 关闭
 * - 顶部工具：下载到本地（a[download]）、上传到当前相册（编辑模式）、关闭
 * - 右侧评论栏：作者描述（说明 + 拍摄时间）+ 访客评论（昵称 + 内容，公开可评）
 */
const props = defineProps({
  photos: { type: Array, required: true },
  index: { type: Number, required: true },
  category: { type: String, required: true },
})
const emit = defineEmits(['close', 'uploaded', 'updated'])

const { isEditMode } = useEditMode()

const current = computed(() => props.photos[props.index] || null)
const pageLabel = computed(() => `${props.index + 1} / ${props.photos.length}`)
const fileName = computed(() => {
  const u = current.value?.url || ''
  return u.substring(u.lastIndexOf('/') + 1)
})

function prev() {
  if (props.photos.length < 2) return
  emitIndex((props.index - 1 + props.photos.length) % props.photos.length)
}
function next() {
  if (props.photos.length < 2) return
  emitIndex((props.index + 1) % props.photos.length)
}
function emitIndex(i) {
  // 翻页后父组件需要更新 index —— 通过 update:index 回写
  emit('update:index', i)
}

/* ---------- 键盘 ---------- */
function onKeydown(e) {
  if (e.key === 'Escape') emit('close')
  else if (e.key === 'ArrowLeft') prev()
  else if (e.key === 'ArrowRight') next()
}

/* ---------- 评论 ---------- */
const comments = ref([])
const loadingComments = ref(false)
const author = ref('')
const content = ref('')
const posting = ref(false)
const posted = ref(false)

async function loadComments() {
  if (!current.value) return
  loadingComments.value = true
  try {
    comments.value = await photosApi.getComments(current.value.id)
  } catch (e) {
    console.error('Load comments failed:', e)
  } finally {
    loadingComments.value = false
  }
}

async function postComment() {
  if (!author.value.trim()) {
    alert(t('photos.nicknameRequired'))
    return
  }
  if (!content.value.trim()) {
    alert(t('photos.contentRequired'))
    return
  }
  posting.value = true
  try {
    const c = await photosApi.addComment(current.value.id, {
      author: author.value.trim(),
      content: content.value.trim(),
    })
    comments.value.push(c)
    content.value = ''
    posted.value = true
    setTimeout(() => (posted.value = false), 1800)
  } catch (e) {
    alert(e.message || t('photos.commentFailed'))
  } finally {
    posting.value = false
  }
}

/* ---------- 上传（上传到当前相册，编辑模式） ---------- */
const uploading = ref(false)
const fileInput = ref(null)
const upTakenDate = ref('')
const upCaption = ref('')

function nowMonth() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}
function openUpload() {
  upTakenDate.value = nowMonth()
}
function chooseFile(e) {
  const f = e.target.files[0]
  if (!f) return
  doUpload(f)
}
async function doUpload(file) {
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('category', props.category)
    if (upTakenDate.value) fd.append('takenDate', upTakenDate.value)
    if (upCaption.value.trim()) fd.append('caption', upCaption.value.trim())
    await photosApi.uploadPhoto(fd)
    if (fileInput.value) fileInput.value.value = ''
    upCaption.value = ''
    emit('uploaded')
  } catch (e) {
    alert(e.message || t('photos.uploadFailed'))
  } finally {
    uploading.value = false
  }
}

/* ---------- 作者描述编辑（编辑模式） ---------- */
const editingDesc = ref(false)
const descTaken = ref('')
const descCaption = ref('')
const savingDesc = ref(false)

function startEditDesc() {
  descTaken.value = current.value?.takenDate || ''
  descCaption.value = current.value?.caption || ''
  editingDesc.value = true
}
function cancelDesc() {
  editingDesc.value = false
}
async function saveDesc() {
  savingDesc.value = true
  try {
    await photosApi.updatePhoto(current.value.id, {
      takenDate: descTaken.value.trim(),
      caption: descCaption.value.trim(),
    })
    editingDesc.value = false
    emit('updated')
  } catch (e) {
    alert(e.message || t('photos.saveFailed'))
  } finally {
    savingDesc.value = false
  }
}

/* ---------- 切图时刷新评论 ---------- */
watch(
  () => props.index,
  () => loadComments(),
)

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
  loadComments()
})
onBeforeUnmount(() => window.removeEventListener('keydown', onKeydown))
</script>

<template>
  <Teleport to="body">
    <div class="lightbox" role="dialog" aria-modal="true">
      <!-- 顶部栏 -->
      <div class="lb-top">
        <span class="lb-title">
          <span class="lb-cat">{{ category }}</span>
          <span class="lb-page mono">{{ pageLabel }}</span>
        </span>
        <span class="lb-tools">
          <a
            v-if="current"
            class="lb-btn mono"
            :href="current.url"
            :download="fileName"
            :title="t('photos.downloadTitle')"
          >
            {{ t('photos.download') }}
          </a>
          <button
            v-if="isEditMode"
            class="lb-btn"
            :title="t('photos.uploadToAlbumTitle')"
            :disabled="uploading"
            @click="openUpload"
          >
            {{ uploading ? t('photos.uploading') : t('photos.uploadToAlbum') }}
          </button>
          <button class="lb-btn lb-btn--icon" :title="t('photos.closeEsc')" @click="emit('close')">
            ✕
          </button>
        </span>
      </div>

      <!-- 隐藏的上传文件选择（上传到当前相册） -->
      <input
        v-if="isEditMode"
        ref="fileInput"
        type="file"
        accept="image/*"
        class="lb-file"
        @change="chooseFile"
      />

      <!-- 主区：左图右评论 -->
      <div class="lb-body">
        <!-- 大图区 -->
        <div class="lb-stage">
          <img
            v-if="current"
            :key="current.id"
            :src="current.url"
            :alt="current.caption || current.category"
            class="lb-img"
          />
          <button
            v-if="photos.length > 1"
            class="lb-arrow lb-arrow--left"
            :aria-label="t('photos.prev')"
            @click="prev"
          >
            ‹
          </button>
          <button
            v-if="photos.length > 1"
            class="lb-arrow lb-arrow--right"
            :aria-label="t('photos.next')"
            @click="next"
          >
            ›
          </button>
        </div>

        <!-- 右侧评论栏 -->
        <aside class="lb-side">
          <!-- 作者描述 -->
          <div class="lb-desc">
            <div class="lb-desc__head">
              <span class="lb-desc__label">{{ t('photos.authorDesc') }}</span>
              <button
                v-if="isEditMode && !editingDesc"
                class="lb-desc__edit"
                :title="t('photos.editDescTitle')"
                @click="startEditDesc"
              >
                {{ t('photos.edit') }}
              </button>
              <span v-else-if="!editingDesc && current?.takenDate" class="lb-desc__date mono">
                {{ current.takenDate }}
              </span>
            </div>

            <!-- 编辑表单 -->
            <div v-if="isEditMode && editingDesc" class="lb-desc__form">
              <input
                v-model="descTaken"
                type="month"
                class="lb-desc__input mono"
                :aria-label="t('photos.takenDate')"
                :placeholder="t('photos.takenMonth')"
              />
              <textarea
                v-model="descCaption"
                class="lb-desc__input lb-desc__area"
                :placeholder="t('photos.descPlaceholder')"
                rows="3"
                maxlength="200"
              />
              <div class="lb-desc__actions">
                <button class="lb-desc__ok" :disabled="savingDesc" @click="saveDesc">
                  {{ savingDesc ? t('photos.uploading') : t('blog.save') }}
                </button>
                <button class="lb-desc__cancel" @click="cancelDesc">{{ t('photos.cancel') }}</button>
              </div>
            </div>

            <!-- 展示 -->
            <template v-else>
              <p class="lb-desc__text">
                {{ current?.caption || t('photos.noDesc') }}
              </p>
              <p v-if="isEditMode && !current?.caption" class="lb-desc__hint faint">
                {{ t('photos.editHint') }}
              </p>
            </template>
          </div>

          <!-- 评论区 -->
          <div class="lb-comments">
            <h4 class="lb-comments__title">
              {{ t('photos.commentsTitle') }} <span v-if="comments.length" class="mono">· {{ comments.length }}</span>
            </h4>
            <div class="lb-comments__list">
              <p v-if="loadingComments" class="lb-comments__empty faint">{{ t('photos.loadingComments') }}</p>
              <p v-else-if="!comments.length" class="lb-comments__empty faint">
                {{ t('photos.noComments') }}
              </p>
              <div v-for="c in comments" :key="c.id" class="cmt">
                <div class="cmt__head">
                  <span class="cmt__author">{{ c.author }}</span>
                  <span class="cmt__time mono">{{ c.createdAt }}</span>
                </div>
                <p class="cmt__text">{{ c.content }}</p>
              </div>
            </div>

            <!-- 评论输入 -->
            <div class="lb-comments__form">
              <input
                v-model="author"
                class="cmt-input"
                :placeholder="t('photos.nicknamePlaceholder')"
                maxlength="32"
                spellcheck="false"
              />
              <textarea
                v-model="content"
                class="cmt-input cmt-input--area"
                :placeholder="t('photos.commentPlaceholder')"
                maxlength="500"
                rows="2"
              />
              <button
                class="cmt-submit"
                :disabled="posting"
                @click="postComment"
              >
                {{ posting ? t('photos.posting') : t('photos.postComment') }}
              </button>
              <span v-if="posted" class="cmt-ok">{{ t('photos.posted') }}</span>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.lightbox {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  flex-direction: column;
  background: rgba(8, 8, 10, 0.92);
  color: #f5f5f5;
  backdrop-filter: blur(6px);
  animation: lb-in 0.22s ease;
}
@keyframes lb-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* ---------- 顶部栏 ---------- */
.lb-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.8rem;
  padding: 0.75rem 1.1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.lb-title {
  display: flex;
  align-items: baseline;
  gap: 0.7rem;
  min-width: 0;
}
.lb-cat {
  font-size: 0.92rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.lb-page {
  font-size: 0.8rem;
  color: rgba(245, 245, 245, 0.55);
  white-space: nowrap;
}
.lb-tools {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.lb-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.38rem 0.85rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.06);
  color: #f5f5f5;
  font-size: 0.8rem;
  text-decoration: none;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}
.lb-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(255, 255, 255, 0.4);
}
.lb-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.lb-btn--icon {
  padding: 0.38rem 0.65rem;
}
.lb-file { display: none; }

/* ---------- 主区 ---------- */
.lb-body {
  flex: 1;
  display: flex;
  min-height: 0;
}

/* 大图区 */
.lb-stage {
  position: relative;
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}
.lb-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 6px;
  box-shadow: 0 18px 60px rgba(0, 0, 0, 0.5);
  user-select: none;
}
.lb-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 2.6rem;
  height: 3.4rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 0.6rem;
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  font-size: 1.8rem;
  line-height: 1;
  cursor: pointer;
  transition: background-color 0.2s ease;
}
.lb-arrow:hover { background: rgba(255, 255, 255, 0.2); }
.lb-arrow--left { left: 0.7rem; }
.lb-arrow--right { right: 0.7rem; }

/* 右侧评论栏 */
.lb-side {
  width: 19rem;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.03);
  min-height: 0;
}
.lb-desc {
  padding: 1rem 1.1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.lb-desc__head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 0.6rem;
}
.lb-desc__label {
  font-size: 0.78rem;
  color: rgba(245, 245, 245, 0.6);
}
.lb-desc__edit {
  margin-left: auto;
  padding: 0.18rem 0.6rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.06);
  color: #f5f5f5;
  font-size: 0.72rem;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}
.lb-desc__edit:hover {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(255, 255, 255, 0.4);
}
.lb-desc__date {
  font-size: 0.72rem;
  color: rgba(245, 245, 245, 0.4);
}
.lb-desc__text {
  margin: 0.5rem 0 0;
  font-size: 0.88rem;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.lb-desc__hint {
  margin: 0.4rem 0 0;
  font-size: 0.72rem;
}
.lb-desc__form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 0.6rem;
}
.lb-desc__input {
  width: 100%;
  box-sizing: border-box;
  padding: 0.42rem 0.6rem;
  border-radius: 0.5rem;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.06);
  color: #f5f5f5;
  font-size: 0.8rem;
  font-family: inherit;
  outline: none;
  resize: none;
}
.lb-desc__input::placeholder { color: rgba(245, 245, 245, 0.4); }
.lb-desc__input:focus { border-color: var(--accent); }
.lb-desc__area { min-height: 4rem; line-height: 1.5; }
.lb-desc__actions {
  display: flex;
  gap: 0.5rem;
}
.lb-desc__ok {
  padding: 0.32rem 0.9rem;
  border-radius: 999px;
  border: none;
  background: var(--accent);
  color: #fff;
  font-size: 0.76rem;
  cursor: pointer;
  transition: opacity 0.2s ease;
}
.lb-desc__ok:hover:not(:disabled) { opacity: 0.88; }
.lb-desc__ok:disabled { opacity: 0.5; cursor: not-allowed; }
.lb-desc__cancel {
  padding: 0.32rem 0.9rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: transparent;
  color: rgba(245, 245, 245, 0.7);
  font-size: 0.76rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}
.lb-desc__cancel:hover { background: rgba(255, 255, 255, 0.1); }

.lb-comments {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 1rem 1.1rem;
}
.lb-comments__title {
  margin: 0 0 0.6rem;
  font-size: 0.9rem;
  font-weight: 600;
}
.lb-comments__list {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}
.lb-comments__empty {
  font-size: 0.8rem;
  padding: 0.6rem 0;
}
.cmt {
  padding: 0.55rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}
.cmt:last-child { border-bottom: none; }
.cmt__head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 0.6rem;
}
.cmt__author {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--accent);
}
.cmt__time {
  font-size: 0.68rem;
  color: rgba(245, 245, 245, 0.35);
}
.cmt__text {
  margin: 0.25rem 0 0;
  font-size: 0.84rem;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

/* 评论输入 */
.lb-comments__form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 0.7rem;
  padding-top: 0.7rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}
.cmt-input {
  width: 100%;
  box-sizing: border-box;
  padding: 0.45rem 0.65rem;
  border-radius: 0.5rem;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.06);
  color: #f5f5f5;
  font-size: 0.82rem;
  font-family: inherit;
  outline: none;
  resize: none;
}
.cmt-input::placeholder { color: rgba(245, 245, 245, 0.4); }
.cmt-input:focus { border-color: var(--accent); }
.cmt-input--area { min-height: 3.2rem; }
.cmt-submit {
  align-self: flex-end;
  padding: 0.4rem 1rem;
  border-radius: 999px;
  border: none;
  background: var(--accent);
  color: #fff;
  font-size: 0.8rem;
  cursor: pointer;
  transition: opacity 0.2s ease;
}
.cmt-submit:hover:not(:disabled) { opacity: 0.88; }
.cmt-submit:disabled { opacity: 0.5; cursor: not-allowed; }
.cmt-ok {
  font-size: 0.78rem;
  color: #34d399;
}

/* ---------- 移动端：评论栏放下方 ---------- */
@media (max-width: 720px) {
  .lb-body { flex-direction: column; }
  .lb-stage { min-height: 42vh; }
  .lb-side {
    width: 100%;
    border-left: none;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    max-height: 40vh;
  }
  .lb-desc { padding: 0.7rem 0.9rem; }
  .lb-comments { padding: 0.7rem 0.9rem; }
}
</style>
