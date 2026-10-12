<script setup>
import { useMusicPlayer } from '@/composables/useMusicPlayer'
import { t } from '@/i18n'

/**
 * 首页 Now Playing 卡：显示当前曲目 / 未播放状态，点击打开音乐抽屉。
 * 标题与未播放文案为固定文案（随语言切换）。
 */
const { currentTrack, playing, toggleDrawer } = useMusicPlayer()
</script>

<template>
  <section
    class="nowplaying card card--hover"
    aria-label="Now playing"
    role="button"
    tabindex="0"
    @click="toggleDrawer"
    @keydown.enter="toggleDrawer"
  >
    <div class="nowplaying__icon" :class="{ 'nowplaying__icon--spin': playing }" aria-hidden="true">
      <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
        <path d="M9 18V6l10-2v12" />
        <circle cx="6.5" cy="18" r="2.5" />
        <circle cx="16.5" cy="16" r="2.5" />
      </svg>
    </div>
    <div class="nowplaying__body">
      <span class="nowplaying__label">{{ t('home.nowPlaying') }}</span>
      <template v-if="currentTrack">
        <p class="nowplaying__title serif">{{ currentTrack.title }}</p>
        <p class="nowplaying__artist">{{ currentTrack.artist }}</p>
      </template>
      <p v-else class="nowplaying__empty">{{ t('home.notPlaying') }}</p>
    </div>
  </section>
</template>

<style scoped>
.nowplaying {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem;
  cursor: pointer;
  min-height: 15rem;
}

.nowplaying__icon {
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 3rem;
  height: 3rem;
  border-radius: 999px;
  background: var(--accent-soft);
  color: var(--accent);
  transition: transform 0.3s ease;
}
.nowplaying__icon--spin { animation: nowplaying-spin 6s linear infinite; }

@keyframes nowplaying-spin {
  to { transform: rotate(360deg); }
}

.nowplaying__body { min-width: 0; }
.nowplaying__label {
  display: block;
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  color: var(--faint);
  margin-bottom: 0.35rem;
}
.nowplaying__title {
  margin: 0 0 0.1rem;
  font-size: 1.1rem;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.nowplaying__artist,
.nowplaying__empty {
  margin: 0;
  font-size: 0.82rem;
  color: var(--faint);
}
</style>
