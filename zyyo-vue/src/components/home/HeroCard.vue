<script setup>
import HeroModel from '@/components/home/HeroModel.vue'
import { t } from '@/i18n'
/**
 * 首页 Hero 卡：自我介绍（固定文案，随语言切换）+ 右侧 3D 模型（可拖拽旋转）。
 */
</script>

<template>
  <section class="hero card" aria-label="About me">
    <div class="hero__main">
      <p class="hero__hi serif">{{ t('home.heroHi') }} <em>Leo.</em></p>
      <p class="hero__intro">
        {{ t('home.heroIntro') }}
      </p>
      <RouterLink to="/about" class="hero__about">
        {{ t('home.aboutLink') }}
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M5 12h14m-6-6 6 6-6 6" />
        </svg>
      </RouterLink>
    </div>

    <div class="hero__model">
      <HeroModel />
    </div>
  </section>
</template>

<style scoped>
.hero {
  display: flex;
  align-items: stretch;
  gap: 1.5rem;
  padding: 1.5rem;
  min-height: 15rem;
}

/* 左栏：文字（占剩余宽度） */
.hero__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 1.2rem;
}

/* 右栏：3D 模型 */
.hero__model {
  flex: 0 0 300px;
  border-left: 1px solid var(--card-border);
  padding-left: 1.5rem;
}

.hero__hi {
  margin: 0;
  font-size: clamp(1.7rem, 3.4vw, 2.3rem);
  font-weight: 600;
  color: var(--text);
}
.hero__hi em {
  font-style: italic;
  color: var(--accent);
}

.hero__intro {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.8;
  color: var(--muted);
  max-width: 34em;
}

.hero__about {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.9rem;
  color: var(--accent);
  border-bottom: 1px dashed var(--accent);
  padding-bottom: 0.1rem;
  transition: border-bottom-style 0.15s ease;
}
.hero__about:hover { border-bottom-style: solid; }

/* 手机（<640px）：模型隐藏，避免挤压文字 */
@media (max-width: 639px) {
  .hero {
    flex-direction: column;
  }
  .hero__model {
    display: none;
  }
}
</style>
