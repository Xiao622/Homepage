<script setup>
import { computed, onMounted, ref } from 'vue'
import { useMusicPlayer } from '@/composables/useMusicPlayer'
import { useEditMode } from '@/composables/useEditMode'
import { musicApi } from '@/api'
import { t } from '@/i18n'

/**
 * Music 页：曲目墙（布局参考 re.bluepoch 官网 MUSIC 板块）。
 *  - 顶部 MUSIC 大标题 + 副文案
 *  - 曲目区：最新 3 首大卡一行展示（封面 / 播放按钮 / 中文 / 演唱者 / 英文名 / 时长）
 *  - 与全局播放器（useMusicPlayer 单例）联动：点击即播放
 *  - 编辑模式：上传曲目（音频 + 封面 + 歌名 + 演唱者）、删除曲目
 *  - 封面优先使用真实封面图，未上传时按 coverSeed 生成渐变占位
 */
const { tracks, currentIndex, playing, progress, select, togglePlay } = useMusicPlayer()
const { isEditMode } = useEditMode()

/** 页面本地曲目列表：直接驱动本页模板渲染（不依赖单例引用，规避豆包浏览器环境下的
 *  模块双实例/响应式断链问题）；同时同步到播放器单例 tracks，保证全局播放联动。 */
const localTracks = ref([])
const featured = computed(() => localTracks.value)

/** 封面渐变：按 seed 偏移色相，深浅主题均清晰 */
function coverStyle(seed) {
  const hue = ((seed % 8) * 45 + 210) % 360
  return {
    background: `linear-gradient(135deg, hsl(${hue}, 68%, 56%), hsl(${(hue + 42) % 360}, 76%, 32%))`,
  }
}

/** 有真实封面图时覆盖渐变 */
function coverBg(t) {
  return t.cover ? { backgroundImage: `url(${t.cover})`, backgroundSize: 'cover', backgroundPosition: 'center' } : coverStyle(t.coverSeed)
}

function isCurrent(i) {
  return currentIndex.value === i
}

/** 点击卡片：当前播放 → 暂停/继续；其他 → 切到该曲并播放 */
function onCardClick(i) {
  if (isCurrent(i)) togglePlay()
  else select(i)
}

function fmtDuration(sec) {
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

/* ===================== 编辑模式：上传 / 删除 ===================== */
const showUpload = ref(false)
const uploading = ref(false)
const uploadMsg = ref('')
const form = ref({
  file: null,
  cover: null,
  title: '',
  artist: '',
})

function pickFile(e, key) {
  form.value[key] = e.target.files[0] || null
}

async function refreshTracks() {
  try {
    const data = await musicApi.getTracks()
    if (data) {
      localTracks.value = data
      tracks.value = data
    }
  } catch {
    /* 保持现状 */
  }
}

async function submitUpload() {
  uploadMsg.value = ''
  if (!form.value.file) {
    uploadMsg.value = t('music.selectAudio')
    return
  }
  if (!form.value.title.trim()) {
    uploadMsg.value = t('music.titleRequired')
    return
  }
  if (!form.value.artist.trim()) {
    uploadMsg.value = t('music.artistRequired')
    return
  }
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', form.value.file)
    if (form.value.cover) fd.append('cover', form.value.cover)
    fd.append('title', form.value.title.trim())
    fd.append('artist', form.value.artist.trim())
    await musicApi.uploadTrack(fd)
    form.value = { file: null, cover: null, title: '', artist: '' }
    showUpload.value = false
    await refreshTracks()
  } catch (e) {
    uploadMsg.value = e.message || t('music.uploadFailed')
  } finally {
    uploading.value = false
  }
}

async function removeTrack(t, i) {
  if (!window.confirm(t('music.deleteConfirm', { title: t.title }))) return
  try {
    await musicApi.deleteTrack(t.id)
    await refreshTracks()
    // 调整播放索引：被删曲目在播或在其后时，指向同一首歌
    if (currentIndex.value >= tracks.value.length) {
      select(Math.max(0, tracks.value.length - 1)).catch(() => {})
    } else if (currentIndex.value >= i) {
      select(Math.max(0, currentIndex.value - 1)).catch(() => {})
    }
  } catch (e) {
    window.alert(e.message || t('music.deleteFailed'))
  }
}

/** 从静态快照加载曲目（上传/删除后由后端同步更新 music-tracks.json） */
async function loadTracks() {
  try {
    const data = await musicApi.getTracks()
    if (data && data.length) {
      localTracks.value = data
      tracks.value = data
      return true
    }
  } catch {
    /* 本轮失败，交由重试 */
  }
  return false
}

/** 播放器曲目为空时加载（与抽屉/首页共用同一份列表） */
onMounted(async () => {
  if (localTracks.value.length) return
  for (let i = 0; i < 3; i++) {
    if (await loadTracks()) return
    await new Promise((r) => setTimeout(r, 1200))
  }
})
</script>

<template>
  <div class="container page">
    <header class="page-header">
      <h1>Music</h1>
      <p class="sub">{{ t('music.sub', { n: localTracks.length }) }}</p>
      <button v-if="isEditMode" class="music-upload-btn" @click="showUpload = true">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <path d="M12 5v14M5 12h14" />
        </svg>
        {{ t('music.upload') }}
      </button>
    </header>

    <!-- 主推 3 卡 -->
    <section v-if="featured.length" class="music-featured" aria-label="Featured tracks">
      <article
        v-for="(tr, i) in featured"
        :key="tr.id"
        class="music-feat card"
        :class="{ 'music-card--playing': isCurrent(i) }"
        role="button"
        tabindex="0"
        @click="onCardClick(i)"
        @keydown.enter="onCardClick(i)"
      >
        <button
          v-if="isEditMode"
          class="music-del"
          :aria-label="t('music.deleteTrackAria', { title: tr.title })"
          :title="t('music.deleteTrackTitle')"
          @click.stop="removeTrack(tr, i)"
        >
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M18 6 6 18M6 6l12 12" />
          </svg>
        </button>

        <div class="music-feat__cover" :style="coverBg(tr)">
          <svg class="music-feat__glyph" viewBox="0 0 24 24" width="34" height="34" fill="none" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 18V6l10-2v12" />
            <circle cx="6.5" cy="18" r="2.5" />
            <circle cx="16.5" cy="16" r="2.5" />
          </svg>
          <button
            class="music-play"
            :aria-label="isCurrent(i) && playing ? t('music.pause') : t('music.play')"
            @click.stop="onCardClick(i)"
          >
            <svg v-if="isCurrent(i) && playing" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M7 5h4v14H7zM13 5h4v14h-4z" />
            </svg>
            <svg v-else viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M8 5.5v13l11-6.5z" />
            </svg>
          </button>
        </div>

        <div class="music-feat__meta">
          <span class="music-tag">EP</span>
          <h2 class="music-feat__title">{{ tr.title }}</h2>
          <p class="music-feat__en">{{ tr.artist }}{{ tr.enTitle ? ' · ' + tr.enTitle : '' }}</p>
          <p class="music-feat__dur">{{ fmtDuration(tr.duration) }}</p>
          <span v-if="isCurrent(i)" class="music-playing">PLAYING</span>
        </div>

        <!-- 播放中进度条 -->
        <div v-if="isCurrent(i)" class="music-bar" aria-hidden="true">
          <div class="music-bar__fill" :style="{ width: progress + '%' }" />
        </div>
      </article>
    </section>



    <!-- 空态 -->
    <p v-if="!localTracks.length" class="music-empty">
      {{ t('music.empty') }} <template v-if="isEditMode">{{ t('music.emptyEditHint') }}</template>
    </p>

    <!-- 上传弹窗 -->
    <div v-if="showUpload" class="upload-mask" @click.self="showUpload = false">
      <form class="upload-panel card" @submit.prevent="submitUpload">
        <h3 class="upload-title">{{ t('music.uploadTitle') }}</h3>

        <label class="upload-row">
          <span class="upload-label">{{ t('music.audioFile') }}</span>
          <input class="upload-input" type="file" accept="audio/*" @change="pickFile($event, 'file')" />
          <span v-if="form.file" class="upload-file-name" :title="form.file.name">{{ form.file.name }}</span>
        </label>

        <label class="upload-row">
          <span class="upload-label">{{ t('music.coverImage') }}</span>
          <input class="upload-input" type="file" accept="image/*" @change="pickFile($event, 'cover')" />
          <span v-if="form.cover" class="upload-file-name" :title="form.cover.name">{{ form.cover.name }}</span>
        </label>

        <label class="upload-row">
          <span class="upload-label">{{ t('music.songTitle') }}</span>
          <input v-model="form.title" class="upload-input" type="text" :placeholder="t('music.titlePlaceholder')" />
        </label>

        <label class="upload-row">
          <span class="upload-label">{{ t('music.artist') }}</span>
          <input v-model="form.artist" class="upload-input" type="text" :placeholder="t('music.artistPlaceholder')" />
        </label>

        <p v-if="uploadMsg" class="upload-msg">{{ uploadMsg }}</p>

        <div class="upload-actions">
          <button type="button" class="upload-cancel" @click="showUpload = false">{{ t('music.cancel') }}</button>
          <button type="submit" class="upload-submit" :disabled="uploading">
            {{ uploading ? t('music.uploading') : t('music.upload') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
/* ---------- 页头上传按钮 ---------- */
.music-upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  margin-top: 0.7rem;
  padding: 0.45rem 0.95rem;
  border-radius: 999px;
  border: 1px solid var(--accent-soft);
  background: transparent;
  color: var(--accent);
  font-size: 0.82rem;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}
.music-upload-btn:hover {
  background: var(--accent);
  color: #fff;
}

/* ---------- 删除按钮 ---------- */
.music-del {
  position: absolute;
  top: 0.6rem;
  left: 0.6rem;
  z-index: 3;
  width: 1.9rem;
  height: 1.9rem;
  border-radius: 999px;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 15, 15, 0.55);
  color: #fff;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s ease, background-color 0.2s ease;
}
.music-feat:hover .music-del,
.music-del:focus-visible {
  opacity: 1;
}
.music-del:hover { background: #e5484d; }

/* ---------- 上传弹窗 ---------- */
.upload-mask {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(3px);
}
.upload-panel {
  width: min(430px, 100%);
  max-height: 88vh;
  overflow: auto;
  padding: 1.3rem 1.4rem 1.1rem;
}
.upload-title {
  font-family: var(--font-serif);
  font-size: 1.15rem;
  margin: 0 0 0.9rem;
}
.upload-row {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  margin-bottom: 0.65rem;
}
.upload-label {
  flex: none;
  width: 5.6rem;
  font-size: 0.8rem;
  color: var(--muted);
}
.upload-input {
  flex: 1;
  min-width: 0;
  padding: 0.42rem 0.6rem;
  border-radius: 0.5rem;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  color: var(--text);
  font-size: 0.82rem;
}
.upload-input[type='file'] {
  padding: 0.3rem 0.5rem;
  font-size: 0.75rem;
}
.upload-file-name {
  max-width: 38%;
  font-size: 0.72rem;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.upload-msg {
  margin: 0.2rem 0 0;
  font-size: 0.78rem;
  color: #e5484d;
}
.upload-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.6rem;
  margin-top: 1rem;
}
.upload-cancel {
  padding: 0.42rem 0.95rem;
  border-radius: 999px;
  border: 1px solid var(--card-border);
  background: transparent;
  color: var(--muted);
  font-size: 0.82rem;
  cursor: pointer;
}
.upload-submit {
  padding: 0.42rem 1.1rem;
  border-radius: 999px;
  border: none;
  background: var(--accent);
  color: #fff;
  font-size: 0.82rem;
  cursor: pointer;
}
.upload-submit:disabled { opacity: 0.55; cursor: not-allowed; }

/* ---------- 主推 3 卡 ---------- */
.music-featured {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 2.6rem;
}
.music-feat {
  position: relative;
  overflow: hidden;
  padding: 0;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}
.music-feat:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 26px -14px var(--shadow-color, rgba(0, 0, 0, 0.35));
}
.music-feat__cover {
  position: relative;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.92);
}
.music-feat__glyph {
  opacity: 0.82;
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.28));
}
.music-feat__meta {
  position: relative;
  padding: 0.85rem 1rem 1rem;
}
.music-tag {
  display: inline-block;
  font-size: 0.68rem;
  font-weight: 700;
  letter-spacing: 0.14em;
  color: var(--accent);
  border: 1px solid var(--accent-soft);
  border-radius: 999px;
  padding: 0.1rem 0.5rem;
  margin-bottom: 0.45rem;
}
.music-feat__title {
  font-family: var(--font-serif);
  font-size: 1.18rem;
  font-weight: 600;
  margin: 0 0 0.15rem;
}
.music-feat__en {
  margin: 0 0 0.3rem;
  font-size: 0.82rem;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.music-feat__dur {
  margin: 0;
  font-size: 0.78rem;
  color: var(--muted);
  font-variant-numeric: tabular-nums;
}

/* ---------- 播放按钮 ---------- */
.music-play {
  position: absolute;
  right: 0.9rem;
  bottom: 0.9rem;
  width: 2.6rem;
  height: 2.6rem;
  border-radius: 999px;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.92);
  color: #111;
  cursor: pointer;
  opacity: 0;
  transform: translateY(6px);
  transition: opacity 0.22s ease, transform 0.22s ease, background-color 0.2s ease;
}
.music-feat:hover .music-play,
.music-play:focus-visible {
  opacity: 1;
  transform: translateY(0);
}
.music-play:hover { background: #fff; }
.music-play--sm {
  width: 2.15rem;
  height: 2.15rem;
}

/* ---------- 当前播放态 ---------- */
.music-card--playing {
  border-color: var(--accent);
  box-shadow: 0 0 0 1px var(--accent-soft), 0 10px 26px -14px var(--shadow-color, rgba(0, 0, 0, 0.35));
}
.music-card--playing .music-play {
  opacity: 1;
  transform: translateY(0);
}
.music-playing {
  position: absolute;
  top: 0.75rem;
  right: 0.85rem;
  font-size: 0.66rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: var(--accent);
}
.music-bar {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  background: var(--card-border);
}
.music-bar__fill {
  height: 100%;
  background: var(--accent);
  transition: width 0.6s linear;
}

.music-empty {
  color: var(--muted);
  padding: 2rem 0;
}

/* ---------- 响应式 ---------- */
@media (max-width: 768px) {
  .music-featured {
    grid-template-columns: 1fr;
  }
}
</style>
