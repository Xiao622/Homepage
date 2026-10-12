<script setup>
import { ref, onMounted } from 'vue'
import { siteApi } from '@/api'

/**
 * 首页标签跑马灯（复刻源站 Technology / Design 标签带）。
 * 内容复制两份实现无缝循环滚动。
 */
const tags = ref(['TypeScript', 'React', 'Astro', 'Tailwind', 'Node.js', 'Git', 'Docker', 'REST', 'Figma', 'Typography', 'Color', 'Terminal', 'Writing', 'Running'])

onMounted(async () => {
  try {
    tags.value = await siteApi.getMarqueeTags()
  } catch {
    /* 使用默认标签 */
  }
})
</script>

<template>
  <div class="marquee" aria-hidden="true">
    <div class="marquee__track">
      <span v-for="(t, i) in [...tags, ...tags]" :key="`${t}-${i}`" class="marquee__tag serif">
        {{ t }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.marquee {
  overflow: hidden;
  border-block: 1px solid var(--card-border);
  padding-block: 0.85rem;
  margin-block: 1.6rem 0.4rem;
  -webkit-mask-image: linear-gradient(90deg, transparent, #000 8%, #000 92%, transparent);
  mask-image: linear-gradient(90deg, transparent, #000 8%, #000 92%, transparent);
}

.marquee__track {
  display: flex;
  gap: 2.2rem;
  width: max-content;
  animation: marquee 26s linear infinite;
}
.marquee:hover .marquee__track {
  animation-play-state: paused;
}

.marquee__tag {
  font-size: 1.05rem;
  font-style: italic;
  color: var(--faint);
  white-space: nowrap;
  transition: color 0.2s ease;
}
.marquee__tag:hover { color: var(--accent); }
</style>
