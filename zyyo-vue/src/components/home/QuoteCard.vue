<script setup>
import { t } from '@/i18n'
/**
 * 首页名言卡：固定文案，随语言切换。
 */
</script>

<template>
  <section class="quotecard card" aria-label="Quote">
    <svg class="quotecard__mark" viewBox="0 0 24 24" width="26" height="26" fill="currentColor" aria-hidden="true">
      <path d="M10 7H6a2 2 0 0 0-2 2v3a2 2 0 0 0 2 2h2v1a3 3 0 0 1-3 3 1 1 0 0 0 0 2 5 5 0 0 0 5-5V9a2 2 0 0 0-2-2Zm10 0h-4a2 2 0 0 0-2 2v3a2 2 0 0 0 2 2h2v1a3 3 0 0 1-3 3 1 1 0 0 0 0 2 5 5 0 0 0 5-5V9a2 2 0 0 0-2-2Z" />
    </svg>
    <p class="quotecard__text serif">
      {{ t('home.quote') }}
    </p>
    <p class="quotecard__from">{{ t('home.quoteFrom') }}</p>
  </section>
</template>

<style scoped>
.quotecard {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 0.9rem;
  padding: 1.25rem;
  min-height: 15rem;
  background: linear-gradient(180deg, var(--card-bg), var(--card-bg));
}

.quotecard__mark {
  color: var(--accent);
  opacity: 0.85;
}

.quotecard__text {
  margin: 0;
  font-size: clamp(1.05rem, 2.4vw, 1.3rem);
  font-weight: 500;
  line-height: 1.7;
  color: var(--card-text);
}

.quotecard__from {
  margin: 0;
  font-size: 0.85rem;
  color: var(--faint);
}
</style>
