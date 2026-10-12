import { ref } from 'vue'

/**
 * 主题状态（模块级单例）：dark / light。
 * 持久化到 localStorage，并同步 <html data-theme>。
 * 顶栏开关为唯一切换入口。
 */
const THEME_KEY = 'Leo-theme'

const theme = ref(localStorage.getItem(THEME_KEY) || 'dark')

function apply() {
  document.documentElement.dataset.theme = theme.value
  localStorage.setItem(THEME_KEY, theme.value)
}
apply()

export function useTheme() {
  function toggle() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
    apply()
  }
  function set(value) {
    theme.value = value
    apply()
  }
  return { theme, toggle, set }
}
