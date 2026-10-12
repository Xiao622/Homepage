<script setup>
import { ref, onMounted } from 'vue'
import HeroCard from '@/components/home/HeroCard.vue'
import NowPlayingCard from '@/components/home/NowPlayingCard.vue'
import NowCard from '@/components/home/NowCard.vue'
import MapCard from '@/components/home/MapCard.vue'
import QuoteCard from '@/components/home/QuoteCard.vue'
import ConsoleCard from '@/components/home/ConsoleCard.vue'
import SiteInfoCard from '@/components/home/SiteInfoCard.vue'
import PhotosCard from '@/components/home/PhotosCard.vue'
import TagMarquee from '@/components/home/TagMarquee.vue'
import { blogApi, notesApi } from '@/api'
import { t } from '@/i18n'

/**
 * 首页：卡片画板（Hero + 右列三卡 + Map/Quote/Photos + SiteInfo）
 * + 标签跑马灯 + 最新文章 + 最新随记。
 *
 * 布局（桌面 ≥1024px，3 列）：
 *   第 1 行：Hero（跨 2 列）｜右列三卡（Now Playing / Now / Console，各 1/3 高）
 *   第 2 行：Map｜Quote｜Photos
 *   第 3 行：SiteInfo（跨满 3 列，1/3 高）
 */
const latestPosts = ref([])
const latestNotes = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [posts, notes] = await Promise.all([
      blogApi.getPosts({ page: 1, pageSize: 3 }),
      notesApi.getNotes({ order: 'time' }),
    ])
    latestPosts.value = posts.list
    latestNotes.value = notes.list.slice(0, 3)
  } catch (e) {
    console.error('Home data load failed:', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="container container--board home">
    <!-- 卡片画板 -->
    <div class="board">
      <HeroCard class="board__hero" />

      <!-- 第三列：Now Playing / Now / Console 三卡堆叠 -->
      <div class="board__right">
        <NowPlayingCard class="board__nowplaying" />
        <NowCard class="board__now" />
        <ConsoleCard class="board__console" />
      </div>

      <MapCard class="board__map" />
      <QuoteCard class="board__quote" />
      <PhotosCard class="board__photos" />
      <SiteInfoCard class="board__info" />
    </div>

    <!-- 标签跑马灯 -->
    <TagMarquee />

    <!-- 最新文章 -->
    <section class="section-title">
      <h2>{{ t('home.blogSection') }}</h2>
      <RouterLink to="/blog" class="more">{{ t('home.viewAll') }}</RouterLink>
    </section>

    <div v-if="loading" class="skeleton-list">
      <div v-for="i in 3" :key="i" class="skeleton skeleton--row" />
    </div>

    <div v-else class="post-list">
      <RouterLink v-for="post in latestPosts" :key="post.id" to="/blog" class="post-row">
        <span class="post-row__date mono">{{ post.date }}</span>
        <span class="post-row__title">{{ post.title }}</span>
      </RouterLink>
    </div>

    <!-- 最新随记 -->
    <section class="section-title">
      <h2>{{ t('home.notesSection') }}</h2>
      <RouterLink to="/notes" class="more">{{ t('home.viewAll') }}</RouterLink>
    </section>

    <div v-if="loading" class="skeleton-list">
      <div v-for="i in 3" :key="i" class="skeleton skeleton--row" />
    </div>

    <div v-else class="note-list">
      <article v-for="note in latestNotes" :key="note.id" class="note-row">
        <span class="note-row__date mono">{{ note.date }}</span>
        <p class="note-row__text">{{ note.text }}</p>
      </article>
    </div>
  </div>
</template>

<style scoped>
.home {
  padding-top: 1.4rem;
}

/* ============================================================
   画板网格：三行三列（桌面 ≥1024px）
   ============================================================ */
.board {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-areas:
    'hero  hero  right'
    'map   quote photos'
    'info  info  info';
  gap: 1rem;
  align-items: stretch;
}

.board__hero   { grid-area: hero;   min-height: 15rem; }
.board__right  { grid-area: right;  display: flex; flex-direction: column; gap: 1rem; }
.board__map    { grid-area: map; }
.board__quote  { grid-area: quote; }
.board__photos { grid-area: photos; }
.board__info   { grid-area: info; }

/* ---------- 右列三卡：高度为原卡片 1/3（312 → 104px） ---------- */
.board__right :deep(.nowplaying) {
  min-height: 104px;
  padding: 0.85rem 1.1rem;
}
.board__right :deep(.nowplaying__icon) {
  width: 2.4rem;
  height: 2.4rem;
}

.board__right :deep(.nowcard) {
  min-height: 104px;
  padding: 0.7rem 1.1rem;
  gap: 0.2rem;
}
.board__right :deep(.nowcard__time) { font-size: 1.6rem; }
.board__right :deep(.nowcard__sec)  { font-size: 0.72rem; }
.board__right :deep(.nowcard__date) { font-size: 0.78rem; }

.board__right :deep(.console) {
  height: 104px;
  min-height: 104px;
}
.board__right :deep(.console__bar) {
  padding: 0.45rem 1rem;
}
.board__right :deep(.console__body) {
  padding: 0.55rem 1rem 0.7rem;
  font-size: 0.74rem;
  line-height: 1.5;
  gap: 0.2rem;
}

/* ---------- 第三行 SiteInfo：跨满 3 列，高度 1/3（240 → 80px） ---------- */
.board__info {
  height: 80px;
  min-height: 80px;
  padding: 0.6rem 1.1rem;
  overflow: hidden;
}

/* ============================================================
   响应式
   ============================================================ */
/* 平板（640–1023px）：两列 */
@media (max-width: 1023px) and (min-width: 640px) {
  .board {
    grid-template-columns: 1fr 1fr;
    grid-template-areas:
      'hero   hero'
      'right  right'
      'map    quote'
      'photos photos'
      'info   info';
  }
}

/* 手机（<640px）：单列 */
@media (max-width: 639px) {
  .board {
    grid-template-columns: 1fr;
    grid-template-areas:
      'hero'
      'right'
      'map'
      'quote'
      'photos'
      'info';
  }
}

/* ---------- 文章 / 随记列表 ---------- */
.post-list {
  display: flex;
  flex-direction: column;
}
.post-row {
  display: flex;
  align-items: baseline;
  gap: 1.1rem;
  padding: 0.85rem 0.25rem;
  border-bottom: 1px solid var(--card-border);
  transition: padding-left 0.2s ease;
}
.post-row:last-child { border-bottom: none; }
.post-row:hover { padding-left: 0.6rem; }
.post-row:hover .post-row__title { color: var(--accent); }

.post-row__date {
  flex: none;
  font-size: 0.78rem;
  color: var(--faint);
}
.post-row__title {
  flex: 1;
  min-width: 0;
  font-size: 0.98rem;
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s ease;
}

/* ---------- 随记 ---------- */
.note-list {
  display: flex;
  flex-direction: column;
}
.note-row {
  display: flex;
  gap: 1.1rem;
  padding: 0.85rem 0.25rem;
  border-bottom: 1px solid var(--card-border);
}
.note-row:last-child { border-bottom: none; }
.note-row__date {
  flex: none;
  font-size: 0.78rem;
  color: var(--faint);
  font-family: var(--font-mono);
  padding-top: 0.15rem;
}
.note-row__text {
  margin: 0;
  font-size: 0.93rem;
  color: var(--muted);
  line-height: 1.7;
}

/* ---------- 骨架屏 ---------- */
.skeleton-list { display: flex; flex-direction: column; gap: 0.7rem; }
.skeleton {
  border-radius: 0.6rem;
  background: var(--card-border);
  opacity: 0.5;
  animation: blink 1.4s ease-in-out infinite;
}
.skeleton--row { height: 2.4rem; }
</style>
