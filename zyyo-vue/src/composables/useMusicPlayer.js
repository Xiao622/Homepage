import { ref, computed, watch } from 'vue'
import { musicApi } from '@/api'

/**
 * 音乐播放器状态（模块级单例）：顶栏、首页 NowPlaying 卡片、抽屉、Music 页共用。
 *
 * 播放策略：
 *  - 曲目带 url（真实音频）→ 用模块级 <audio> 播放，进度监听 timeupdate / ended
 *  - 曲目无 url（占位曲目）→ 保持模拟进度推进，播完自动切下一首
 */
const tracks = ref([])
const currentIndex = ref(-1)
const playing = ref(false)
const progress = ref(0) // 0 ~ 100
const duration = ref(0) // 秒
const open = ref(false)

let progressTimer = null
let audioEl = null

function getAudio() {
  if (!audioEl) {
    audioEl = new Audio()
    audioEl.addEventListener('timeupdate', () => {
      if (audioEl.duration > 0) {
        duration.value = audioEl.duration
        progress.value = (audioEl.currentTime / audioEl.duration) * 100
      }
    })
    audioEl.addEventListener('ended', () => {
      progress.value = 0
      next()
    })
    audioEl.addEventListener('play', () => (playing.value = true))
    audioEl.addEventListener('pause', () => (playing.value = false))
  }
  return audioEl
}

async function ensureTracks() {
  if (!tracks.value.length) {
    try {
      const data = await musicApi.getTracks()
      tracks.value = data
    } catch {
      /* 曲目加载失败时保持空列表 */
    }
  }
}

// 抽屉打开时加载播放列表
watch(open, (v) => {
  if (v) ensureTracks()
})

function stopProgress() {
  clearInterval(progressTimer)
  progressTimer = null
}

function play() {
  const track = currentTrack.value
  if (!track) return
  if (track.url) {
    // 真实音频：设置 src 后播放（重复选择同一首时从头播）
    const audio = getAudio()
    if (audio.src !== track.url) {
      audio.src = track.url
      audio.currentTime = 0
    } else if (audio.paused) {
      audio.currentTime = 0
    }
    audio.play().catch(() => {
      /* 自动播放被拦截时保持静默，用户可手动点击 */
    })
    return
  }
  // 占位曲目：模拟推进（演示 3 分钟）
  playing.value = true
  duration.value = track.duration || 180
  stopProgress()
  progressTimer = setInterval(() => {
    const step = 100 / ((track.duration || 180) / 0.6)
    progress.value += step
    if (progress.value >= 100) {
      progress.value = 0
      next()
    }
  }, 600)
}

function pause() {
  playing.value = false
  stopProgress()
  if (audioEl && !audioEl.paused) audioEl.pause()
}

async function select(index) {
  await ensureTracks()
  if (index < 0 || index >= tracks.value.length) return
  // 切歌时先停旧音频
  if (audioEl) {
    audioEl.pause()
    audioEl.removeAttribute('src')
  }
  stopProgress()
  currentIndex.value = index
  progress.value = 0
  play()
}

function togglePlay() {
  if (currentIndex.value < 0) {
    select(0)
    return
  }
  const track = currentTrack.value
  if (track && track.url && audioEl) {
    // 真实音频：交给 audio 自身的 paused 状态
    if (audioEl.paused) {
      audioEl.play().catch(() => {})
    } else {
      audioEl.pause()
    }
    return
  }
  playing.value ? pause() : play()
}

function next() {
  if (currentIndex.value < 0) return select(0)
  select((currentIndex.value + 1) % tracks.value.length)
}

function prev() {
  if (currentIndex.value < 0) return select(0)
  select((currentIndex.value - 1 + tracks.value.length) % tracks.value.length)
}

function close() {
  open.value = false
}

function toggleDrawer() {
  open.value = !open.value
}

const currentTrack = computed(() =>
  currentIndex.value >= 0 ? tracks.value[currentIndex.value] : null,
)

export function useMusicPlayer() {
  return {
    tracks,
    currentIndex,
    currentTrack,
    playing,
    progress,
    duration,
    open,
    ensureTracks,
    select,
    togglePlay,
    next,
    prev,
    close,
    toggleDrawer,
  }
}
