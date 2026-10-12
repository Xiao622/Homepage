<script setup>
import { ref, onMounted } from 'vue'
import { photosApi } from '@/api'
import { t } from '@/i18n'

/**
 * 首页 Photos 卡：与 Photos 页关联，展示最近上传的 3 张照片缩略图。
 *  - 数据源：GET /api/photos?limit=3（时间倒序，取最近 3 张）
 *  - 点击缩略图 / "View all →" 跳转 /photos 相册列表
 *  - 标题与提示文案随语言切换
 */
const photos = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const data = await photosApi.getPhotos({ limit: 3 })
    photos.value = (data && data.list) || []
  } catch {
    photos.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="photoscard card" aria-label="Photos">
    <div class="photoscard__head">
      <span class="photoscard__label serif">{{ t('home.photosLabel') }}</span>
      <RouterLink to="/photos" class="photoscard__more">{{ t('home.viewAll') }}</RouterLink>
    </div>

    <!-- 最近 3 张照片 -->
    <div v-if="photos.length" class="photoscard__grid">
      <RouterLink
        v-for="p in photos"
        :key="p.id"
        :to="'/photos/' + encodeURIComponent(p.category)"
        class="photoscard__thumb"
        :title="p.caption || p.category"
      >
        <img :src="p.url" :alt="p.caption || p.category" loading="lazy" />
      </RouterLink>
    </div>
    <!-- 占位（无照片） -->
    <div v-else class="photoscard__grid">
      <span v-for="i in 3" :key="i" class="photoscard__thumb photoscard__thumb--ph" aria-hidden="true" />
    </div>
    <p class="photoscard__note">
      {{ photos.length ? t('home.photosNote', { n: photos.length }) : t('home.photosEmpty') }}
    </p>
  </section>
</template>

<style scoped>
.photoscard {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 0.9rem;
  padding: 1.25rem;
  min-height: 15rem;
}

.photoscard__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 0.6rem;
}
.photoscard__label {
  font-size: 1.05rem;
  font-weight: 600;
  letter-spacing: 0.01em;
  color: var(--card-text);
}
.photoscard__more {
  font-size: 0.78rem;
  color: var(--faint);
  transition: color 0.2s ease;
}
.photoscard__more:hover { color: var(--accent); }

.photoscard__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem;
}
.photoscard__thumb {
  position: relative;
  aspect-ratio: 4 / 3;
  border-radius: 0.45rem;
  overflow: hidden;
  background: linear-gradient(140deg, var(--card-border), color-mix(in srgb, var(--card-border) 55%, transparent));
  opacity: 0.92;
  transition: transform 0.25s ease, opacity 0.25s ease;
}
.photoscard__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
}
.photoscard:hover .photoscard__thumb { opacity: 1; transform: translateY(-2px); }
.photoscard__thumb:hover img { transform: scale(1.06); }

.photoscard__thumb--ph {
  display: block;
  background: linear-gradient(140deg, var(--card-border), color-mix(in srgb, var(--card-border) 55%, transparent));
  opacity: 0.9;
}
.photoscard__thumb--ph:nth-child(2n) { background: linear-gradient(140deg, var(--card-border), color-mix(in srgb, var(--card-border) 40%, transparent)); }

.photoscard__note {
  margin: 0;
  font-size: 0.78rem;
  color: var(--faint);
}
</style>
