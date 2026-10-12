<script setup>
import { computed } from 'vue'
import { useTheme } from '@/composables/useTheme'

/**
 * 主题切换卡：亮色 = 白昼天空，暗色 = 星夜 + 地球光晕。
 * 点击整卡切换主题。文字为固定文案（不参与编辑模式）。
 */
const { theme, toggle } = useTheme()
const isDark = computed(() => theme.value === 'dark')

const stars = Array.from({ length: 26 }, (_, i) => ({
  id: i,
  left: (i * 37 + 13) % 100,
  top: (i * 53 + 7) % 72,
  size: 1 + ((i * 7) % 3),
  delay: (i % 6) * 0.35,
}))
</script>

<template>
  <section
    class="themecard card"
    :class="{ 'themecard--dark': isDark }"
    aria-label="Toggle theme"
    role="button"
    tabindex="0"
    title="切换主题"
    @click="toggle"
    @keydown.enter="toggle"
  >
    <!-- 星夜 / 白昼背景 -->
    <span
      v-for="s in stars"
      :key="s.id"
      class="themecard__star"
      :style="{ left: s.left + '%', top: s.top + '%', width: s.size + 'px', height: s.size + 'px', animationDelay: s.delay + 's' }"
    />

    <span class="themecard__glow" />

    <span class="themecard__sun" aria-hidden="true" />

    <div class="themecard__label">
      <svg v-if="isDark" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
        <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z" />
      </svg>
      <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
        <circle cx="12" cy="12" r="4" />
        <path d="M12 2v2m0 16v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M2 12h2m16 0h2M4.9 19.1l1.4-1.4m11.4-11.4 1.4-1.4" />
      </svg>
      <span>Toggle theme</span>
    </div>
  </section>
</template>

<style scoped>
.themecard {
  display: flex;
  align-items: flex-end;
  padding: 1.25rem;
  min-height: 15rem;
  cursor: pointer;
  overflow: hidden;
  background: linear-gradient(180deg, #eef7ff 0%, #f8fafc 55%, #e2e8f0 100%);
  border-color: rgba(15, 23, 42, 0.08);
  color: #334155;
}

/* 暗色：星夜 */
.themecard--dark {
  background: linear-gradient(180deg, #07070a 0%, #0d0d12 60%, #14141b 100%);
  color: #d8d8de;
  border-color: rgba(255, 255, 255, 0.07);
}

.themecard__star {
  position: absolute;
  border-radius: 999px;
  background: #fff;
  opacity: 0.35;
  animation: twinkle 2.4s ease-in-out infinite;
}

/* 地球光晕 */
.themecard__glow {
  position: absolute;
  right: -30%;
  bottom: -45%;
  width: 90%;
  aspect-ratio: 1;
  border-radius: 999px;
  background: radial-gradient(circle at 35% 35%, rgba(148, 163, 184, 0.5), rgba(100, 116, 139, 0.12) 60%, transparent 72%);
}

/* 亮色：小太阳 */
.themecard__sun {
  position: absolute;
  right: 14%;
  top: 18%;
  width: 2.6rem;
  height: 2.6rem;
  border-radius: 999px;
  background: radial-gradient(circle, #fbbf24, #f59e0b);
  box-shadow: 0 0 34px 10px rgba(251, 191, 36, 0.35);
  opacity: 0;
  transition: opacity 0.4s ease;
}
.themecard:not(.themecard--dark) .themecard__sun { opacity: 1; }

.themecard__label {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  font-size: 0.85rem;
  letter-spacing: 0.02em;
  opacity: 0.85;
}
</style>
