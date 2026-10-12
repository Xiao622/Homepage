<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useTheme } from '@/composables/useTheme'
import { t, locale } from '@/i18n'

/**
 * 地图卡：展示「辽宁省沈阳市沈北新区 · 辽宁大学（蒲河校区）」。
 * Leaflet + 高德矢量瓦片（无 key），红色标记 + 弹窗，支持缩放拖动。
 * Leaflet 动态加载，避免拖慢首页首屏。深色主题下轻微压暗地图。
 * 标题 / 地址 / 弹窗名称随语言切换。
 */
const LON = 123.38671
const LAT = 41.926588
const mapName = computed(() => t('home.mapName'))

const { theme } = useTheme()
const isDark = computed(() => theme.value === 'dark')

const mapEl = ref(null)
let map = null
let marker = null

/* 红色水滴标记（纯 SVG，避免 Leaflet 默认图标图片在打包后的 404 问题） */
function buildPin(L) {
  return L.divIcon({
    className: 'map-pin',
    html: '<svg viewBox="0 0 24 24" width="26" height="34" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 0C6.5 0 2 4.5 2 10c0 7.2 10 14 10 14s10-6.8 10-14C22 4.5 17.5 0 12 0Z" fill="#e5484d"/><circle cx="12" cy="10" r="4.5" fill="#fff"/></svg>',
    iconSize: [26, 34],
    iconAnchor: [13, 32],
    popupAnchor: [0, -30],
  })
}

/* 高德瓦片（style=7 标准街道图；webrd01~04 轮询） */
function buildLayer(L) {
  return L.tileLayer(
    'https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=7&x={x}&y={y}&z={z}',
    {
      subdomains: ['1', '2', '3', '4'],
      maxZoom: 18,
      attribution: locale.value === 'en' ? '© AMap' : '© 高德地图',
    }
  )
}

async function initMap() {
  if (!mapEl.value || map) return
  const { default: L } = await import('leaflet')
  await import('leaflet/dist/leaflet.css')
  map = L.map(mapEl.value, {
    center: [LAT, LON],
    zoom: 15,
    scrollWheelZoom: false, // 避免与页面滚动冲突
    zoomControl: true,
  })
  buildLayer(L).addTo(map)
  marker = L.marker([LAT, LON], { icon: buildPin(L) }).addTo(map).bindPopup(mapName.value)
  marker.openPopup()
}

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map) {
    map.remove()
    map = null
  }
})

/* 主题变化时重绘瓦片，让深色滤镜即时生效（Leaflet 容器不变） */
watch(isDark, () => {
  if (map) map.invalidateSize()
})

/* 语言切换时更新弹窗名称 */
watch(mapName, (name) => {
  if (marker) marker.setPopupContent(name)
})
</script>

<template>
  <section class="mapcard card" :class="{ 'mapcard--dark': isDark }">
    <div class="mapcard__head">
      <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z" />
        <circle cx="12" cy="10" r="3" />
      </svg>
      <span class="mapcard__title">{{ t('home.mapTitle') }}</span>
    </div>

    <div ref="mapEl" class="mapcard__frame" />

    <div class="mapcard__foot">
      <span class="mapcard__addr">{{ t('home.mapAddr') }}</span>
      <a
        class="mapcard__link"
        :href="'https://uri.amap.com/marker?position=' + LON + ',' + LAT + '&name=' + encodeURIComponent(mapName) + '&callnative=0'"
        target="_blank"
        rel="noreferrer"
      >{{ t('home.mapOpen') }}</a>
    </div>
  </section>
</template>

<style scoped>
.mapcard {
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
  padding: 0.9rem 0.9rem 0.7rem;
  min-height: 15rem;
  overflow: hidden;
}

.mapcard__head {
  display: inline-flex;
  align-items: center;
  gap: 0.42rem;
  padding: 0 0.2rem;
  font-size: 0.82rem;
  letter-spacing: 0.02em;
  color: var(--muted);
}

.mapcard__frame {
  flex: 1;
  width: 100%;
  min-height: 0;
  border-radius: 0.55rem;
  overflow: hidden;
  background: var(--card-border);
}

/* 深色主题下轻微压暗地图，避免过亮抢眼 */
.mapcard--dark .mapcard__frame {
  filter: brightness(0.82) saturate(0.88);
}

.mapcard__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.6rem;
  padding: 0.05rem 0.2rem 0;
}

.mapcard__addr {
  font-size: 0.72rem;
  color: var(--faint);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mapcard__link {
  flex: none;
  font-size: 0.74rem;
  color: var(--accent);
  text-decoration: none;
}
.mapcard__link:hover { text-decoration: underline; }
</style>

<style>
/* Leaflet 控件跟随站点主题 */
.mapcard__frame .leaflet-container {
  width: 100%;
  height: 100%;
  font-family: inherit;
}
.mapcard__frame .leaflet-popup-content-wrapper {
  border-radius: 0.5rem;
}
.mapcard__frame .leaflet-popup-content {
  margin: 0.55rem 0.75rem;
  font-size: 0.8rem;
}
.map-pin {
  background: none;
  border: none;
}
.map-pin svg {
  display: block;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.35));
}
</style>
