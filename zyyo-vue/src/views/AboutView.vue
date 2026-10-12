<script setup>
import { computed } from 'vue'
import { t } from '@/i18n'

/**
 * 关于页：个人介绍、联系方式、数据、博客历史、技术栈、个人特点、开源项目、关于小站。
 * 桌面端右侧提供目录（TOC）锚点跳转。
 * 所有文字为前端固定文案，随右上角语言切换（zh / en）。
 */
const toc = computed(() => t('about.toc'))
const about = computed(() => t('about'))
const statusText = (s) => {
  const map = t('about.statusText')
  return (map && map[s]) || s
}
</script>

<template>
  <div class="container page">
    <header class="page-header">
      <h1>{{ about.title }}</h1>
      <p class="sub">{{ about.sub }}</p>
    </header>

    <div class="about-layout">
      <!-- 主体 -->
      <div class="about-main">
        <!-- 简介 -->
        <section id="intro" class="about-intro card card-pad">
          <p class="serif">{{ about.intro }}</p>
          <div class="contact-line">
            <a
              v-for="(c, i) in about.contacts"
              :key="c.label + i"
              class="contact-chip"
              :href="c.href"
              target="_blank"
              rel="noopener"
            >
              <span>{{ c.label }}</span>
              <span class="mono">{{ c.value }}</span>
            </a>
          </div>
        </section>

        <!-- 一点数据 -->
        <section id="stats" class="about-sec">
          <h2 class="about-h2 serif">{{ about.statsTitle }}</h2>
          <div class="stats-grid">
            <div v-for="(s, i) in about.stats" :key="s.label + i" class="stat card card-pad">
              <span class="stat__value serif">{{ s.value }}</span>
              <span class="stat__label">{{ s.label }}</span>
            </div>
          </div>
        </section>

        <!-- 博客历史 -->
        <section id="history" class="about-sec">
          <h2 class="about-h2 serif">{{ about.historyTitle }}</h2>
          <p class="about-desc">{{ about.historyDesc }}</p>
          <div class="history">
            <div
              v-for="(h, i) in about.history"
              :key="h.year + i"
              class="history__item"
            >
              <span class="history__year mono">{{ h.year }}</span>
              <div class="history__card card card-pad">
                <h3 class="history__title serif">{{ i + 1 }} · {{ h.title }}</h3>
                <p class="history__text">{{ h.text }}</p>
              </div>
            </div>
          </div>
        </section>

        <!-- 技术栈 -->
        <section id="skills" class="about-sec">
          <h2 class="about-h2 serif">{{ about.skillsTitle }}</h2>
          <p class="about-desc">{{ about.skillsDesc }}</p>
          <div class="skills card card-pad">
            <div v-for="(s, i) in about.skills" :key="s.name + i" class="skill">
              <div class="skill__head">
                <span>{{ s.name }}</span>
                <span class="mono skill__pct">{{ s.level }}</span>
              </div>
              <div class="skill__bar">
                <div class="skill__fill" :style="{ width: s.level + '%' }" />
              </div>
            </div>
          </div>
        </section>

        <!-- 个人特点 -->
        <section id="traits" class="about-sec">
          <h2 class="about-h2 serif">{{ about.traitsTitle }}</h2>
          <div class="traits-grid">
            <div
              v-for="(trait, i) in about.traits"
              :key="trait.title + i"
              class="trait card card-pad"
            >
              <span class="trait__num mono">{{ i + 1 }}</span>
              <h3 class="trait__title serif">{{ trait.title }}</h3>
              <p class="trait__text">{{ trait.text }}</p>
            </div>
          </div>
        </section>

        <!-- 开源项目 -->
        <section id="projects" class="about-sec">
          <h2 class="about-h2 serif">{{ about.projectsTitle }}</h2>
          <p class="about-desc">{{ about.projectsDesc }}</p>
          <div class="project-list">
            <a
              v-for="(p, i) in about.projects"
              :key="p.name + i"
              class="project card card-pad"
              :href="about.contacts[0].href"
              target="_blank"
              rel="noopener"
            >
              <div class="project__head">
                <span class="project__name mono">{{ p.name }}</span>
                <span class="project__status" :class="`project__status--${p.status}`">
                  {{ statusText(p.status) }}
                </span>
              </div>
              <p class="project__desc">{{ p.desc }}</p>
              <div class="project__tags">
                <span v-for="tag in p.tags" :key="tag" class="tag">{{ tag }}</span>
              </div>
            </a>
          </div>
        </section>

        <!-- 关于小站 -->
        <section id="features" class="about-sec">
          <h2 class="about-h2 serif">{{ about.featuresTitle }}</h2>
          <div class="feature-grid">
            <div
              v-for="(f, i) in about.features"
              :key="f.title + i"
              class="feature card card-pad"
            >
              <span class="feature__icon" aria-hidden="true">{{ f.icon }}</span>
              <h3 class="feature__title serif">{{ f.title }}</h3>
              <p class="feature__text">{{ f.text }}</p>
            </div>
          </div>
        </section>

        <!-- 一句话 -->
        <section id="closing" class="about-sec">
          <blockquote class="closing card card-pad">
            <p class="closing__text serif">「{{ about.closing }}」</p>
            <p class="closing__motto serif">{{ about.motto }}</p>
            <cite>{{ about.mottoOrigin }}</cite>
          </blockquote>
        </section>
      </div>

      <!-- 目录（桌面端） -->
      <aside class="about-toc">
        <nav class="card card-pad toc">
          <p class="toc__title serif">{{ t('blog.onThisPage') }}</p>
          <a
            v-for="entry in toc"
            :key="entry.id"
            :href="`#${entry.id}`"
            class="toc__link"
          >
            {{ entry.label }}
          </a>
        </nav>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.about-layout {
  display: grid;
  gap: 2rem;
}
@media (min-width: 960px) {
  .about-layout {
    grid-template-columns: minmax(0, 1fr) 12.5rem;
  }
}

.about-main {
  display: flex;
  flex-direction: column;
  gap: 2.4rem;
  min-width: 0;
}

/* ---------- 简介 ---------- */
.about-intro p {
  margin: 0 0 1.1rem;
  font-size: 1.05rem;
  line-height: 1.9;
  color: var(--text);
}
.contact-line {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}
.contact-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.42rem 0.9rem;
  border-radius: 999px;
  border: 1px solid var(--card-border);
  background: var(--card-bg);
  font-size: 0.85rem;
  color: var(--muted);
  transition: border-color 0.2s ease, color 0.2s ease;
}
.contact-chip:hover { border-color: var(--accent); color: var(--accent); }

/* ---------- 通用小节 ---------- */
.about-sec { scroll-margin-top: 5.5rem; }
.about-h2 {
  margin: 0 0 0.7rem;
  font-size: 1.4rem;
  font-weight: 600;
}
.about-desc {
  color: var(--muted);
  font-size: 0.93rem;
  line-height: 1.8;
  margin-bottom: 1.1rem;
}

/* ---------- 数据 ---------- */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.8rem;
}
@media (min-width: 640px) {
  .stats-grid { grid-template-columns: repeat(4, 1fr); }
}
.stat { text-align: center; }
.stat__value {
  display: block;
  font-size: 1.7rem;
  font-weight: 600;
  color: var(--accent);
}
.stat__label {
  font-size: 0.82rem;
  color: var(--faint);
}

/* ---------- 历史 ---------- */
.history {
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
}
.history__item {
  display: flex;
  align-items: flex-start;
  gap: 1.1rem;
}
.history__year {
  flex: none;
  width: 3.4rem;
  padding-top: 0.95rem;
  font-size: 0.8rem;
  color: var(--faint);
  text-align: right;
}
.history__card { flex: 1; }
.history__title {
  margin: 0 0 0.3rem;
  font-size: 1.02rem;
}
.history__text {
  margin: 0;
  font-size: 0.9rem;
  color: var(--muted);
  line-height: 1.75;
}

/* ---------- 技术栈 ---------- */
.skills {
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}
.skill__head {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  margin-bottom: 0.35rem;
}
.skill__pct { color: var(--faint); font-size: 0.8rem; }
.skill__bar {
  height: 0.5rem;
  border-radius: 999px;
  background: var(--card-border);
  overflow: hidden;
}
.skill__fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--accent), #c9a882);
  transition: width 1s cubic-bezier(0.22, 1, 0.36, 1);
}

/* ---------- 个人特点 ---------- */
.traits-grid {
  display: grid;
  gap: 0.8rem;
  grid-template-columns: 1fr;
}
@media (min-width: 700px) {
  .traits-grid { grid-template-columns: repeat(3, 1fr); }
}
.trait__num {
  font-size: 0.78rem;
  color: var(--faint);
}
.trait__title {
  margin: 0.3rem 0 0.35rem;
  font-size: 1.05rem;
}
.trait__text {
  margin: 0;
  font-size: 0.88rem;
  color: var(--muted);
  line-height: 1.7;
}

/* ---------- 项目 ---------- */
.project-list {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}
.project {
  display: block;
  transition: border-color 0.2s ease, transform 0.2s ease;
}
.project:hover { border-color: var(--card-border-hover); transform: translateY(-1px); }
.project__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.6rem;
  margin-bottom: 0.3rem;
}
.project__name {
  font-size: 0.92rem;
  color: var(--text);
}
.project__status {
  flex: none;
  font-size: 0.72rem;
  padding: 0.12rem 0.55rem;
  border-radius: 999px;
  border: 1px solid var(--card-border);
  color: var(--faint);
}
.project__status--stable { color: #34d399; border-color: rgba(52, 211, 153, 0.4); }
.project__status--active { color: #fbbf24; border-color: rgba(251, 191, 36, 0.4); }
.project__status--demo { color: var(--faint); }
.project__desc {
  margin: 0 0 0.6rem;
  font-size: 0.88rem;
  color: var(--muted);
  line-height: 1.7;
}
.project__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
}

/* ---------- 功能点 ---------- */
.feature-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.8rem;
}
@media (min-width: 640px) {
  .feature-grid { grid-template-columns: repeat(2, 1fr); }
}
.feature__icon { font-size: 1.2rem; }
.feature__title { margin: 0.3rem 0 0.3rem; font-size: 1rem; }
.feature__text { margin: 0; font-size: 0.85rem; color: var(--muted); }

/* ---------- 一句话 ---------- */
.closing {
  background: linear-gradient(160deg, var(--accent-soft), transparent 70%);
  border-color: var(--accent-soft);
}
.closing__text {
  margin: 0 0 0.8rem;
  font-size: 1.1rem;
  line-height: 1.8;
}
.closing__motto {
  margin: 0 0 0.4rem;
  font-size: 0.95rem;
  color: var(--muted);
}
.closing cite {
  font-style: normal;
  font-size: 0.82rem;
  color: var(--faint);
}

/* ---------- 目录 ---------- */
.about-toc { display: none; }
@media (min-width: 960px) {
  .about-toc { display: block; }
  .toc {
    position: sticky;
    top: 5.5rem;
    display: flex;
    flex-direction: column;
    gap: 0.35rem;
  }
}
.toc__title {
  margin: 0 0 0.4rem;
  font-size: 0.95rem;
  font-weight: 600;
}
.toc__link {
  font-size: 0.84rem;
  color: var(--muted);
  padding: 0.15rem 0;
  transition: color 0.2s ease;
}
.toc__link:hover { color: var(--accent); }
</style>
