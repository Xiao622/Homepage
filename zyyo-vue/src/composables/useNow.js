import { ref, onMounted, onBeforeUnmount } from 'vue'
import { locale } from '@/i18n'

/**
 * 实时时钟（首页 Now 卡片）。日期文案随语言（zh-CN / en-US）。
 */
export function useNow() {
  const now = ref(new Date())

  let timer = null
  onMounted(() => {
    timer = setInterval(() => {
      now.value = new Date()
    }, 1000)
  })
  onBeforeUnmount(() => clearInterval(timer))

  function pad(n) {
    return String(n).padStart(2, '0')
  }

  return {
    now,
    hhmm: () => `${pad(now.value.getHours())}:${pad(now.value.getMinutes())}`,
    seconds: () => pad(now.value.getSeconds()),
    dateText: () =>
      now.value.toLocaleDateString(locale.value === 'zh' ? 'zh-CN' : 'en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long',
      }),
  }
}
