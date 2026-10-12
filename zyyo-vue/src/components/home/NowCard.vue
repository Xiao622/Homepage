<script setup>
import { useNow } from '@/composables/useNow'
import { t } from '@/i18n'

/**
 * 首页 Now 卡：实时时钟（复刻源站的金色数字时钟 + 柔和光晕）。
 */
const { now, hhmm, seconds, dateText } = useNow()
</script>

<template>
  <section class="nowcard card" aria-label="Now">
    <span class="nowcard__label">{{ t('home.nowLabel') }}</span>
    <div class="nowcard__clock">
      <span class="nowcard__time mono">{{ hhmm() }}</span>
      <span class="nowcard__sec mono">{{ seconds() }}</span>
    </div>
    <span class="nowcard__date">{{ dateText() }}</span>
  </section>
</template>

<style scoped>
.nowcard {
  --now-bg: #eef2f2;
  --now-digit: #5b423f;
  --now-digit-off: rgba(0, 0, 0, 0.05);
  --now-glow: rgba(91, 66, 63, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 0.8rem;
  padding: 1.25rem;
  background: var(--now-bg);
  border-color: transparent;
  min-height: 15rem;
}

[data-theme='dark'] .nowcard {
  --now-bg: #1e2121;
  --now-digit: #c9a882;
  --now-digit-off: rgba(255, 255, 255, 0.06);
  --now-glow: rgba(201, 168, 130, 0.4);
}

.nowcard__label {
  font-size: 0.78rem;
  letter-spacing: 0.1em;
  color: var(--now-digit);
  opacity: 0.75;
}

.nowcard__clock {
  display: flex;
  align-items: baseline;
  gap: 0.2rem;
  font-family: var(--font-mono);
  color: var(--now-digit);
  text-shadow: 0 0 26px var(--now-glow);
}
.nowcard__time {
  font-size: clamp(2.1rem, 5vw, 2.9rem);
  font-weight: 500;
  line-height: 1;
  letter-spacing: 0.01em;
}
.nowcard__sec {
  font-size: 0.95rem;
  opacity: 0.7;
  animation: blink 1s step-start infinite;
}

.nowcard__date {
  font-size: 0.85rem;
  color: var(--now-digit);
  opacity: 0.75;
}
</style>
