<script setup>
import { t } from '@/i18n'

/**
 * 首页站点信息卡：固定页脚文字（随语言切换）+ 快捷入口。
 */
</script>

<template>
  <section class="siteinfo card" aria-label="Site info">
    <div class="siteinfo__row">
      <span class="siteinfo__dot" />
      <span class="siteinfo__text">{{ t('footer.text') }}</span>
    </div>

    <div class="siteinfo__links">
      <a href="https://github.com/Leo666" target="_blank" rel="noopener" title="GitHub" aria-label="GitHub">
        <svg viewBox="0 0 24 24" width="17" height="17" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.58 2 12.25c0 4.53 2.87 8.37 6.84 9.73.5.1.68-.22.68-.49v-1.7c-2.78.62-3.37-1.37-3.37-1.37-.45-1.18-1.11-1.5-1.11-1.5-.91-.63.07-.62.07-.62 1 .07 1.53 1.06 1.53 1.06.9 1.57 2.36 1.12 2.94.86.09-.67.35-1.12.63-1.38-2.22-.26-4.56-1.14-4.56-5.06 0-1.12.39-2.03 1.03-2.75-.1-.26-.45-1.3.1-2.7 0 0 .84-.28 2.75 1.05a9.36 9.36 0 0 1 5 0c1.91-1.33 2.75-1.05 2.75-1.05.55 1.4.2 2.44.1 2.7.64.72 1.03 1.63 1.03 2.75 0 3.93-2.34 4.79-4.57 5.05.36.32.68.94.68 1.9v2.82c0 .27.18.6.69.49A10.25 10.25 0 0 0 22 12.25C22 6.58 17.52 2 12 2Z" />
        </svg>
      </a>
      <a href="mailto:2468132707@qq.com" title="Email" aria-label="Email">
        <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="5" width="18" height="14" rx="2" />
          <path d="m3 7 9 6 9-6" />
        </svg>
      </a>
      <a href="#" target="_blank" rel="noopener" title="RSS" aria-label="RSS feed">
        <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 11a9 9 0 0 1 9 9" />
          <path d="M4 4a16 16 0 0 1 16 16" />
          <circle cx="5" cy="19" r="1.6" fill="currentColor" stroke="none" />
        </svg>
      </a>
    </div>
  </section>
</template>

<style scoped>
.siteinfo {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.8rem;
  padding: 1.1rem 1.25rem;
}

.siteinfo__row {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  min-width: 0;
}
.siteinfo__dot {
  flex: none;
  width: 0.55rem;
  height: 0.55rem;
  border-radius: 999px;
  background: #34d399;
  box-shadow: 0 0 8px rgba(52, 211, 153, 0.7);
  animation: blink 2.4s ease-in-out infinite;
}
.siteinfo__text {
  font-size: 0.8rem;
  color: var(--muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.siteinfo__links {
  display: flex;
  gap: 0.75rem;
  color: var(--faint);
}
.siteinfo__links a { transition: color 0.2s ease; }
.siteinfo__links a:hover { color: var(--accent); }
</style>
