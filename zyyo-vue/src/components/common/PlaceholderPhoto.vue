<script setup>
/**
 * 占位照片组件：相片属于源站作者版权内容，复刻阶段用按分类着色的
 * SVG 占位图展示相册结构；接入真实接口后，用返回的 image URL 替换。
 */
const props = defineProps({
  category: { type: String, default: 'daily' },
  caption: { type: String, default: '' },
  index: { type: Number, default: 0 },
})

const art = {
  daily: {
    from: '#f6d7a8',
    to: '#d98e73',
    accent: '#8c4a32',
    icon: 'M12 4 4 20h16Z', // 山形
  },
  streets: {
    from: '#7fa8c9',
    to: '#3f5a7d',
    accent: '#c9dcec',
    icon: 'M4 20h16M7 20v-8m5 8v-8m5 8v-8M4 8l8-4 8 4',
  },
  nature: {
    from: '#a8d5a2',
    to: '#4c7a54',
    accent: '#eaf6e6',
    icon: 'M12 20v-7m0 0c0-3 2-5 5-5 0 3-2 5-5 5Zm0 0c0-3-2-5-5-5 0 3 2 5 5 5Z',
  },
  food: {
    from: '#f2b98c',
    to: '#b05c3c',
    accent: '#fff1e6',
    icon: 'M6 4v8a2 2 0 0 0 2 2h1m-3-2h2m0 0V4m4 0v6c0 1.5 1 2 2 2s2-.5 2-2V4m-2 0v6',
  },
}[props.category] || {
  from: '#8a8a9a',
  to: '#4a4a58',
  accent: '#e6e6ee',
  icon: '',
}
</script>

<template>
  <figure class="photo" :title="caption">
    <svg
      class="photo__svg"
      viewBox="0 0 400 300"
      preserveAspectRatio="xMidYMid slice"
      role="img"
      :aria-label="caption"
    >
      <defs>
        <linearGradient :id="`g-${category}-${index}`" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" :stop-color="art.from" />
          <stop offset="100%" :stop-color="art.to" />
        </linearGradient>
      </defs>
      <rect width="400" height="300" :fill="`url(#g-${category}-${index})`" />
      <!-- 柔和光斑 -->
      <circle cx="70" cy="50" r="90" fill="#fff" opacity="0.12" />
      <circle cx="340" cy="240" r="110" fill="#000" opacity="0.1" />
      <!-- 分类图形 -->
      <path
        v-if="art.icon"
        :d="art.icon"
        fill="none"
        :stroke="art.accent"
        stroke-width="3"
        stroke-linecap="round"
        stroke-linejoin="round"
        opacity="0.85"
        transform="translate(172 105) scale(2.4)"
      />
      <text
        x="200"
        y="272"
        text-anchor="middle"
        :fill="art.accent"
        font-size="17"
        font-family="Noto Serif SC, serif"
        opacity="0.9"
      >
        {{ caption }}
      </text>
    </svg>
    <figcaption class="photo__cap mono">{{ caption }}</figcaption>
  </figure>
</template>

<style scoped>
.photo {
  margin: 0;
  border-radius: var(--card-radius-sm);
  overflow: hidden;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  transition: transform 0.25s ease, border-color 0.25s ease;
}
.photo:hover {
  transform: translateY(-3px);
  border-color: var(--card-border-hover);
}
.photo__svg {
  width: 100%;
  aspect-ratio: 4 / 3;
}
.photo__cap {
  margin: 0;
  padding: 0.45rem 0.7rem;
  font-size: 0.72rem;
  color: var(--faint);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
