<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { t, locale } from '@/i18n'

/**
 * 首页 Console 卡：仿终端输出（复刻源站的 "featured" 控制台卡片）。
 *
 * 打字机效果：问题（$ cmd）逐字打出 → 停顿 → 回答逐字打出 → 停顿，
 * 然后切换到下一组问答，无限循环。
 *
 * 窗口永远只有 2 行：第一行 = 问题，第二行 = 该问题的回答（成对出现，
 * 不会出现「上一个答案 + 下一个问题」的错位）。
 * 问答对随语言切换（命令保持英文，输出双语）。
 */
const pairs = computed(() => t('home.consolePairs'))

const CHAR_MS = 55        // 每个字符的打字间隔（毫秒），慢速打字感
const PAUSE_AFTER_CMD = 450   // 问题打完后的停顿
const PAUSE_AFTER_OUT = 1600  // 回答打完后的停顿（停留展示）

const pairIdx = ref(0)
const line1 = ref('')     // 第一行（问题）已打出的字符
const line2 = ref('')     // 第二行（回答）已打出的字符

/** 阶段：0=打问题 1=问题打完停顿 2=打回答 3=回答打完停顿 */
const stage = ref(0)
let tickCount = 0
let timer = null

/** 打字光标跟随当前输入位置：打问题时在第一行，其余在第二行 */
const caretOnFirst = computed(() => stage.value === 0)

function tick() {
  if (!pairs.value.length) return
  if (stage.value === 0) {
    const pair = pairs.value[pairIdx.value]
    if (!pair) return
    const target = pair.cmd
    if (line1.value.length < target.length) {
      line1.value = target.slice(0, line1.value.length + 1)
    } else {
      stage.value = 1
      tickCount = 0
    }
  } else if (stage.value === 1) {
    if (++tickCount >= Math.round(PAUSE_AFTER_CMD / CHAR_MS)) stage.value = 2
  } else if (stage.value === 2) {
    const pair = pairs.value[pairIdx.value]
    if (!pair) return
    const target = pair.out
    if (line2.value.length < target.length) {
      line2.value = target.slice(0, line2.value.length + 1)
    } else {
      stage.value = 3
      tickCount = 0
    }
  } else {
    if (++tickCount >= Math.round(PAUSE_AFTER_OUT / CHAR_MS)) {
      // 下一组问答，循环
      pairIdx.value = (pairIdx.value + 1) % pairs.value.length
      line1.value = ''
      line2.value = ''
      stage.value = 0
    }
  }
}

onMounted(() => {
  timer = setInterval(tick, CHAR_MS)
})
onBeforeUnmount(() => {
  clearInterval(timer)
})

/* 语言切换时重置打字循环（避免旧语言残留） */
watch(locale, () => {
  pairIdx.value = 0
  line1.value = ''
  line2.value = ''
  stage.value = 0
})
</script>

<template>
  <section class="console card" aria-label="Console">
    <div class="console__bar">
      <span class="console__dot" style="background: #ff5f57" />
      <span class="console__dot" style="background: #febc2e" />
      <span class="console__dot" style="background: #28c840" />
      <span class="console__title mono">~/Leo</span>
    </div>

    <div class="console__body mono">
      <!-- 第一行：问题（$ cmd） -->
      <p class="console__line">
        <span class="console__prompt">$</span>
        <span class="console__cmd">{{ line1 }}<span v-if="caretOnFirst" class="console__caret">█</span></span>
      </p>
      <!-- 第二行：回答 -->
      <p class="console__line">
        <span class="console__out">{{ line2 }}<span v-if="!caretOnFirst" class="console__caret">█</span></span>
      </p>
    </div>
  </section>
</template>

<style scoped>
.console {
  display: flex;
  flex-direction: column;
  min-height: 15rem;
  overflow: hidden;
  background: #0d0d0f;
  border-color: rgba(255, 255, 255, 0.08);
  color: #e6e6ea;
}

.console__bar {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.65rem 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
  background: rgba(255, 255, 255, 0.03);
}
.console__dot {
  width: 0.65rem;
  height: 0.65rem;
  border-radius: 999px;
}
.console__title {
  margin-left: auto;
  font-size: 0.72rem;
  color: rgba(230, 230, 234, 0.5);
}

.console__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.35rem;
  padding: 0.9rem 1rem 1.1rem;
  overflow: hidden;
  font-size: 0.8rem;
  line-height: 1.55;
}

.console__line {
  margin: 0;
  min-height: 1.55em;
  white-space: nowrap;
}
.console__prompt {
  color: var(--accent);
  margin-right: 0.5rem;
}
.console__cmd { color: #7dd3fc; }
.console__out { color: rgba(230, 230, 234, 0.72); }

/* 打字光标（方块闪烁，跟随输入位置） */
.console__caret {
  display: inline-block;
  margin-left: 0.08em;
  width: 0.5em;
  height: 1.05em;
  vertical-align: -0.15em;
  background: var(--accent);
  animation: blink 0.9s step-start infinite;
}
</style>
