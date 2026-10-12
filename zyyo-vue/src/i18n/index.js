import { ref, computed } from 'vue'
import zh from './zh'
import en from './en'

/**
 * 轻量 i18n：中英文切换。
 * - locale：reactive 语言（localStorage 持久化，默认 zh）
 * - t(key, params)：取当前语言文案；支持点路径（如 'nav.home'）与 {name} 插值
 * - toggleLang / setLang：切换语言
 *
 * 用法（模板）：{{ t('nav.home') }} / :placeholder="t('blog.searchPlaceholder')"
 * 用法（脚本）：t('music.deleteConfirm', { title: t.title })
 */
const STORAGE_KEY = 'leo-lang'

const locale = ref(localStorage.getItem(STORAGE_KEY) || 'zh')

const messages = { zh, en }

function resolve(obj, path) {
  return path.split('.').reduce((o, k) => (o == null ? undefined : o[k]), obj)
}

function t(key, params) {
  const dict = messages[locale.value] || zh
  let text = resolve(dict, key)
  if (text == null) {
    // 回退中文，避免出现 undefined
    text = resolve(zh, key)
  }
  if (typeof text !== 'string') return text
  if (params) {
    for (const [k, v] of Object.entries(params)) {
      text = text.replace(new RegExp(`\\{${k}\\}`, 'g'), String(v))
    }
  }
  return text
}

const isZh = computed(() => locale.value === 'zh')

function setLang(lang) {
  locale.value = lang === 'en' ? 'en' : 'zh'
  localStorage.setItem(STORAGE_KEY, locale.value)
  document.documentElement.lang = locale.value
}

function toggleLang() {
  setLang(locale.value === 'zh' ? 'en' : 'zh')
}

// 初始化 <html lang>
document.documentElement.lang = locale.value

export { locale, isZh, t, setLang, toggleLang }
