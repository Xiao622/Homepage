<script setup>
import { computed } from 'vue'
import { useMusicPlayer } from '@/composables/useMusicPlayer'
import { t } from '@/i18n'

/**
 * 音乐抽屉：桌面端右侧悬浮卡，移动端底部弹层。
 * 结构复刻自源站：状态行（Now Playing / Not Playing）→ 进度条与时间 → 曲目列表。
 */
const {
  tracks,
  currentTrack,
  currentIndex,
  playing,
  progress,
  duration,
  open,
  select,
  togglePlay,
  next,
  prev,
  close,
} = useMusicPlayer()

const progressText = computed(() => {
  const cur = Math.floor((progress.value / 100) * duration.value)
  return `${fmt(cur)} - ${fmt(duration.value)}`
})

function fmt(s) {
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${String(sec).padStart(2, '0')}`
}
</script>

<template>
  <Transition name="drawer">
    <aside v-if="open" class="music" :aria-label="t('music.playerAria')">
      <div class="music__head">
        <span class="music__status" :class="{ 'music__status--playing': playing }">
          {{ currentTrack ? (playing ? t('music.nowPlaying') : t('music.paused')) : t('music.notPlaying') }}
          <span v-if="tracks.length" class="music__count mono">{{ t('music.tracksCount', { n: tracks.length }) }}</span>
        </span>
        <button class="music__close" :aria-label="t('music.closePlayer')" @click="close">
          <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M6 6l12 12M18 6L6 18" />
          </svg>
        </button>
      </div>

      <!-- 当前曲目 + 进度 -->
      <div class="music__now">
        <template v-if="currentTrack">
          <p class="music__track serif">{{ currentTrack.title }}</p>
          <p class="music__artist">{{ currentTrack.artist }}</p>
        </template>
        <p v-else class="music__hint">{{ t('music.chooseSong') }}</p>

        <div class="music__progress">
          <div class="music__bar">
            <div class="music__bar-fill" :style="{ width: progress + '%' }" />
          </div>
          <span class="music__time mono">{{ progressText }}</span>
        </div>

        <div class="music__controls">
          <button class="ctl" :aria-label="t('music.prevTrack')" @click="prev">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M6 5h2v14H6zM20 5v14L9.5 12z" /></svg>
          </button>
          <button class="ctl ctl--main" :aria-label="t('music.playPause')" @click="togglePlay">
            <svg v-if="!playing" viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M8 5v14l11-7z" /></svg>
            <svg v-else viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M6 5h4v14H6zM14 5h4v14h-4z" /></svg>
          </button>
          <button class="ctl" :aria-label="t('music.nextTrack')" @click="next">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M16 5h2v14h-2zM4 5v14l10.5-7z" /></svg>
          </button>
        </div>
      </div>

      <!-- 曲目列表 -->
      <ul class="music__list">
        <li v-if="!tracks.length" class="music__empty mono">{{ t('music.noTracks') }}</li>
        <li
          v-for="(t, i) in tracks"
          :key="t.id"
          class="music__item"
          :class="{ 'music__item--active': i === currentIndex }"
          @click="select(i)"
        >
          <span class="music__idx mono">{{ i === currentIndex ? (playing ? '♪' : '‖') : String(i + 1).padStart(2, '0') }}</span>
          <span class="music__item-title">{{ t.title }}</span>
          <span class="music__item-artist">{{ t.artist }}</span>
        </li>
      </ul>
    </aside>
  </Transition>
</template>

<style scoped>
.music {
  position: fixed;
  inset-inline: 0.5rem;
  bottom: 0.5rem;
  z-index: 70;
  display: flex;
  flex-direction: column;
  max-height: 86dvh;
  overflow: hidden;
  border-radius: var(--card-radius);
  background: var(--card-bg-solid);
  border: 1px solid var(--card-border);
  box-shadow: 0 24px 60px -20px rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  color: var(--card-text);
}

/* 桌面端：右侧悬浮卡 */
@media (min-width: 768px) {
  .music {
    inset: auto 1.25rem 1.25rem auto;
    width: 21rem;
    max-height: min(30rem, 86dvh);
  }
}

.music__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.9rem 1.1rem 0.6rem;
  border-bottom: 1px solid var(--card-border);
}
.music__status {
  font-size: 0.8rem;
  color: var(--faint);
  letter-spacing: 0.04em;
}
.music__status--playing { color: var(--accent); }
.music__count {
  margin-left: 0.4rem;
  font-size: 0.68rem;
  color: var(--faint);
}
.music__close {
  display: inline-flex;
  padding: 0.2rem;
  border: none;
  background: transparent;
  color: var(--faint);
}
.music__close:hover { color: var(--text); }

.music__now {
  padding: 1.1rem 1.1rem 0.9rem;
}
.music__track {
  margin: 0 0 0.15rem;
  font-size: 1.15rem;
  font-weight: 600;
}
.music__artist,
.music__hint {
  margin: 0 0 1rem;
  font-size: 0.82rem;
  color: var(--faint);
}

.music__progress {
  display: flex;
  align-items: center;
  gap: 0.7rem;
}
.music__bar {
  flex: 1;
  height: 5px;
  border-radius: 999px;
  background: var(--card-border);
  overflow: hidden;
}
.music__bar-fill {
  height: 100%;
  border-radius: 999px;
  background: var(--accent);
  transition: width 0.6s linear;
}
.music__time {
  font-size: 0.72rem;
  color: var(--faint);
  white-space: nowrap;
}

.music__controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.1rem;
  margin-top: 0.9rem;
}
.ctl {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--muted);
  padding: 0.25rem;
  transition: color 0.2s ease, transform 0.1s ease;
}
.ctl:hover { color: var(--text); }
.ctl:active { transform: scale(0.92); }
.ctl--main {
  width: 2.7rem;
  height: 2.7rem;
  border-radius: 999px;
  background: var(--accent);
  color: #1a0e0e;
}
.ctl--main:hover { color: #1a0e0e; filter: brightness(1.08); }

/* 曲目列表 */
.music__list {
  list-style: none;
  margin: 0;
  padding: 0.4rem 0.5rem 0.8rem;
  overflow-y: auto;
}
.music__empty {
  padding: 1rem 0.6rem;
  text-align: center;
  color: var(--faint);
  font-size: 0.85rem;
}
.music__item {
  display: flex;
  align-items: center;
  gap: 0.7rem;
  padding: 0.62rem 0.6rem;
  border-radius: 0.7rem;
  cursor: pointer;
  transition: background-color 0.15s ease;
}
.music__item:hover { background: var(--accent-soft); }
.music__item--active { background: var(--accent-soft); }
.music__item--active .music__item-title { color: var(--accent); }

.music__idx {
  width: 1.6rem;
  flex: none;
  font-size: 0.78rem;
  color: var(--faint);
  text-align: center;
}
.music__item-title {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.9rem;
}
.music__item-artist {
  flex: none;
  font-size: 0.75rem;
  color: var(--faint);
}

/* 抽屉动画 */
.drawer-enter-active,
.drawer-leave-active {
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.25s ease;
}
.drawer-enter-from,
.drawer-leave-to {
  transform: translateY(16px);
  opacity: 0;
}
@media (min-width: 768px) {
  .drawer-enter-from,
  .drawer-leave-to {
    transform: translateX(24px);
  }
}
</style>
