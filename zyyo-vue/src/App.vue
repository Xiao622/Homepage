<script setup>
import { ref, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import TopBar from '@/components/layout/TopBar.vue'
import SiteFooter from '@/components/layout/SiteFooter.vue'
import MusicPlayer from '@/components/music/MusicPlayer.vue'

/**
 * 首屏加载动画（复刻源站）：深色遮罩 + “Leo” 大字 + 红色扫描线，
 * 动画播完一次后淡出。
 */
const loading = ref(true)

onMounted(() => {
  const t = setTimeout(() => {
    loading.value = false
  }, 1400)
  // 组件卸载时清理
  return () => clearTimeout(t)
})
</script>

<template>
  <!-- 首屏 Loader -->
  <Transition name="loader-fade">
    <div v-if="loading" class="loader" aria-hidden="true">
      <div class="loader__word">
        <span v-for="ch in ['L', 'e', 'o']" :key="ch" class="loader__letter">
          {{ ch }}
        </span>
        <span class="loader__scan" />
      </div>
    </div>
  </Transition>

  <TopBar />

  <!-- 页面主体：路由出口 -->
  <main class="app-main">
    <RouterView v-slot="{ Component }">
      <Transition name="fade" mode="out-in">
        <component :is="Component" />
      </Transition>
    </RouterView>
  </main>

  <SiteFooter />

  <!-- 音乐播放器抽屉（桌面右滑 / 移动端底部弹层） -->
  <MusicPlayer />
</template>

<style scoped>
.app-main {
  min-height: calc(100vh - 20rem);
}

/* ---------- Loader ---------- */
.loader {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #111111;
}

.loader__word {
  position: relative;
  display: flex;
  font-family: var(--font-serif);
  font-size: clamp(2.6rem, 9vw, 4.4rem);
  font-weight: 600;
  font-style: italic;
  color: #f2fff0;
}

.loader__letter {
  display: inline-block;
  animation: loader-letter 1s cubic-bezier(0.175, 0.885, 0.32, 1.275) both;
}

.loader__letter:nth-child(2) { animation-delay: 0.08s; }
.loader__letter:nth-child(3) { animation-delay: 0.16s; }
.loader__letter:nth-child(4) { animation-delay: 0.24s; }

@keyframes loader-letter {
  from { opacity: 0; transform: translateY(14px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 红色扫描线 */
.loader__scan {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 1;
  height: 5px;
  width: 100%;
  border-radius: 4px;
  background: #ff8282;
  opacity: 0.9;
  box-shadow: 0 0 18px 4px rgba(255, 130, 130, 0.5);
  animation: scan 1.2s ease-in-out 0.35s 2;
}

.loader-fade-leave-active {
  transition: opacity 0.6s ease;
}
.loader-fade-leave-to {
  opacity: 0;
}
</style>
