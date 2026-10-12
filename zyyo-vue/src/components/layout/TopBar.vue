<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import { useMusicPlayer } from '@/composables/useMusicPlayer'
import { useEditMode } from '@/composables/useEditMode'
import { t, locale, toggleLang } from '@/i18n'
import LoginDialog from '@/components/layout/LoginDialog.vue'

/**
 * 顶部导航：Logo + 站点导航（含 About，位于最右） + 语言切换 + 主题开关 + 音乐抽屉开关 + 账户（编辑模式）。
 * 移动端收起为汉堡菜单。
 */
const { theme, toggle } = useTheme()
const { open, toggleDrawer } = useMusicPlayer()
const { isEditMode, logout } = useEditMode()
const route = useRoute()

const navItems = [
  { label: 'nav.home', to: '/' },
  { label: 'nav.blog', to: '/blog' },
  { label: 'nav.notes', to: '/notes' },
  { label: 'nav.photos', to: '/photos' },
  { label: 'nav.music', to: '/music' },
  { label: 'nav.about', to: '/about' },
]

const menuOpen = ref(false)
const loginOpen = ref(false)

function isActive(to) {
  if (to === '/') return route.path === '/'
  return route.path.startsWith(to)
}

function onAccountClick() {
  if (isEditMode.value) {
    logout()
  } else {
    loginOpen.value = true
  }
}
</script>

<template>
  <header class="topbar">
    <div class="container topbar__inner">
      <!-- Logo -->
      <RouterLink to="/" class="logo" aria-label="Leo home">
        <span class="logo__word">Leo</span>
        <span class="logo__scan" aria-hidden="true" />
      </RouterLink>

      <!-- 桌面导航 -->
      <nav class="nav" aria-label="Main navigation">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav__link"
          :class="{ 'nav__link--active': isActive(item.to) }"
        >
          {{ t(item.label) }}
        </RouterLink>
      </nav>

      <!-- 右侧操作 -->
      <div class="actions">
        <button
          class="icon-btn"
          :class="{ 'icon-btn--active': open }"
          :title="open ? t('topbar.closeMusic') : t('topbar.openMusic')"
          aria-label="Toggle music player"
          @click="toggleDrawer"
        >
          <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 18V6l10-2v12" />
            <circle cx="6.5" cy="18" r="2.5" />
            <circle cx="16.5" cy="16" r="2.5" />
          </svg>
        </button>

        <button
          class="icon-btn"
          :title="theme === 'dark' ? t('topbar.toLight') : t('topbar.toDark')"
          aria-label="Toggle theme"
          @click="toggle"
        >
          <!-- 月亮 / 太阳 -->
          <svg v-if="theme === 'dark'" viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z" />
          </svg>
          <svg v-else viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="4" />
            <path d="M12 2v2m0 16v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M2 12h2m16 0h2M4.9 19.1l1.4-1.4m11.4-11.4 1.4-1.4" />
          </svg>
        </button>

        <!-- 账户 / 编辑模式开关 -->
        <button
          class="icon-btn"
          :class="{ 'icon-btn--active': isEditMode }"
          :title="isEditMode ? t('topbar.exitEdit') : t('topbar.adminLogin')"
          :aria-label="isEditMode ? t('topbar.exitEdit') : t('topbar.adminLogin')"
          @click="onAccountClick"
        >
          <!-- 未登录：人形图标；编辑模式：铅笔图标 -->
          <svg v-if="!isEditMode" viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="8" r="4" />
            <path d="M4 21c0-4 3.6-6.5 8-6.5s8 2.5 8 6.5" />
          </svg>
          <svg v-else viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 20h9" />
            <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z" />
          </svg>
        </button>

        <!-- 语言切换（右上角）：中文模式显示 EN，英文模式显示 中 -->
        <button
          class="icon-btn lang-btn"
          :title="t('topbar.switchLang')"
          :aria-label="t('topbar.switchLang')"
          @click="toggleLang"
        >
          {{ locale === 'zh' ? 'EN' : '中' }}
        </button>

        <!-- 移动端菜单开关 -->
        <button
          class="icon-btn menu-btn"
          aria-label="Toggle menu"
          @click="menuOpen = !menuOpen"
        >
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
            <path v-if="!menuOpen" d="M4 7h16M4 12h16M4 17h16" />
            <path v-else d="M6 6l12 12M18 6L6 18" />
          </svg>
        </button>
      </div>
    </div>

    <!-- 移动端下拉菜单 -->
    <Transition name="fade">
      <nav v-if="menuOpen" class="mobile-nav container" aria-label="Mobile navigation">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="mobile-nav__link"
          :class="{ 'mobile-nav__link--active': isActive(item.to) }"
          @click="menuOpen = false"
        >
          {{ t(item.label) }}
        </RouterLink>
      </nav>
    </Transition>

    <!-- 管理员登录弹窗 -->
    <LoginDialog v-if="loginOpen" @close="loginOpen = false" />
  </header>
</template>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 50;
  background: var(--topbar-bg);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1px solid transparent;
}

.topbar__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding-top: 1.1rem;
  padding-bottom: 1rem;
}

/* ---------- Logo ---------- */
.logo {
  position: relative;
  display: inline-block;
  font-family: var(--font-serif);
  font-weight: 600;
  font-style: italic;
  font-size: 1.45rem;
  color: var(--text);
  letter-spacing: 0.02em;
  transition: color 0.25s ease;
}
.logo:hover { color: var(--accent); }

.logo__scan {
  position: absolute;
  left: 0;
  top: -2px;
  height: 3px;
  width: 100%;
  border-radius: 3px;
  background: var(--accent);
  opacity: 0;
  box-shadow: 0 0 12px 2px var(--accent-soft);
  transition: opacity 0.2s ease;
}
.logo:hover .logo__scan {
  opacity: 0.9;
  animation: scan 1.4s ease-in-out infinite;
}

/* ---------- 导航 ---------- */
.nav {
  display: none;
  align-items: center;
  gap: 0.35rem;
}
.nav__link {
  padding: 0.4rem 0.8rem;
  border-radius: 999px;
  font-size: 0.9rem;
  color: var(--muted);
  transition: color 0.2s ease, background-color 0.2s ease;
}
.nav__link:hover { color: var(--text); background: var(--accent-soft); }
.nav__link--active {
  color: var(--accent);
  background: var(--accent-soft);
}

/* ---------- 右侧按钮 ---------- */
.actions {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}
.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.1rem;
  height: 2.1rem;
  border-radius: 999px;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  color: var(--muted);
  transition: color 0.2s ease, border-color 0.2s ease, background-color 0.2s ease;
}
.icon-btn:hover { color: var(--text); border-color: var(--card-border-hover); }
.icon-btn--active { color: var(--accent); border-color: var(--accent); }

/* 语言切换按钮：宽一点以容纳 中 / EN */
.lang-btn {
  width: auto;
  min-width: 2.1rem;
  padding: 0 0.45rem;
  font-size: 0.72rem;
  font-weight: 600;
  letter-spacing: 0.03em;
}

.menu-btn { display: inline-flex; }

/* ---------- 移动端菜单 ---------- */
.mobile-nav {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
  padding-bottom: 1rem;
  animation: fade-in-up 0.2s ease;
}
.mobile-nav__link {
  padding: 0.7rem 0.6rem;
  border-radius: 0.75rem;
  color: var(--muted);
  font-size: 1rem;
}
.mobile-nav__link--active { color: var(--accent); background: var(--accent-soft); }

@media (min-width: 768px) {
  .nav { display: flex; }
  .menu-btn { display: none; }
}
</style>
