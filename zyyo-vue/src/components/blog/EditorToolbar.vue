<script setup>
/**
 * EditorToolbar — Word 同款浮动文字样式栏。
 * 在富文本编辑容器内选中文字时自动浮现，跟随选区定位；
 * 点击按钮对选区应用格式（加粗 / 斜体 / 下划线 / 删除线 / 标题 / 字号 / 颜色 / 高亮 / 列表 / 引用 / 清除）。
 * 按钮 mousedown 阻止默认行为以保持选区不丢失。
 */
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { t } from '@/i18n'

const props = defineProps({
  /** 富文本编辑容器（DOM 元素）；选区必须落在容器内才显示 */
  container: { type: Object, default: null },
})

const visible = ref(false)
const barStyle = ref({ left: '0px', top: '0px' })
const active = ref({ bold: false, italic: false, underline: false, strike: false })
const showFontSize = ref(false)
const showColor = ref(false)

const FONT_SIZES = [12, 14, 16, 18, 20, 24, 28, 32]
const COLORS = [
  ['#111111', '#5f5f66', '#e5484d', '#f76b15', '#ffb224', '#46a758', '#3e63dd', '#8e4ec6', '#e93d82'],
  ['#ffffff', '#c8c8cc', '#ffb4b0', '#ffd8a8', '#ffe08a', '#b5f0c8', '#b6c8ff', '#e3c6ff', '#ffc0dd'],
]

function selectionInContainer() {
  const sel = window.getSelection()
  if (!sel || sel.isCollapsed || sel.rangeCount === 0) return false
  const el = props.container
  if (!el) return false
  const range = sel.getRangeAt(0)
  return el.contains(range.commonAncestorContainer)
}

function updateToolbar() {
  showFontSize.value = false
  showColor.value = false
  if (!selectionInContainer()) { visible.value = false; return }

  const sel = window.getSelection()
  const rect = sel.getRangeAt(0).getBoundingClientRect()
  if (!rect || (rect.width === 0 && rect.height === 0)) { visible.value = false; return }

  const barW = 352
  let left = rect.left + rect.width / 2 - barW / 2
  left = Math.max(8, Math.min(left, window.innerWidth - barW - 8))
  let top = rect.top - 48
  if (top < 72) top = rect.bottom + 10
  barStyle.value = { left: `${left}px`, top: `${top}px` }

  try {
    active.value.bold = document.queryCommandState('bold')
    active.value.italic = document.queryCommandState('italic')
    active.value.underline = document.queryCommandState('underline')
    active.value.strike = document.queryCommandState('strikeThrough')
  } catch {
    /* ignore */
  }
  visible.value = true
}

/** execCommand 便捷执行（styleWithCSS 保证生成干净的 inline 样式） */
function exec(cmd, val = null) {
  document.execCommand('styleWithCSS', false, 'true')
  document.execCommand(cmd, false, val)
  requestAnimationFrame(updateToolbar)
}

/** 手动包 span（字号 / 颜色用，跨节点选区也可靠） */
function applySpanStyle(fn) {
  const sel = window.getSelection()
  if (!sel || sel.isCollapsed || sel.rangeCount === 0) return
  const range = sel.getRangeAt(0)
  try {
    const frag = range.extractContents()
    const span = document.createElement('span')
    fn(span)
    span.appendChild(frag)
    range.insertNode(span)
    sel.removeAllRanges()
    const r = document.createRange()
    r.selectNodeContents(span)
    sel.addRange(r)
  } catch {
    /* 复杂选区回退到 execCommand */
  }
  requestAnimationFrame(updateToolbar)
}

function setFontSize(px) {
  applySpanStyle((span) => { span.style.fontSize = `${px}px` })
}
function setColor(color) {
  applySpanStyle((span) => { span.style.color = color })
}

function onDocSelectionChange() { updateToolbar() }
function onScroll() { if (visible.value) updateToolbar() }
function onKeyUp() { requestAnimationFrame(updateToolbar) }

onMounted(() => {
  document.addEventListener('selectionchange', onDocSelectionChange)
  window.addEventListener('scroll', onScroll, true)
  document.addEventListener('keyup', onKeyUp, true)
})
onBeforeUnmount(() => {
  document.removeEventListener('selectionchange', onDocSelectionChange)
  window.removeEventListener('scroll', onScroll, true)
  document.removeEventListener('keyup', onKeyUp, true)
})
</script>

<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="etb"
      :style="barStyle"
      @mousedown.prevent
    >
      <!-- 第一组：字重 -->
      <button class="etb__btn" :class="{ 'etb__btn--on': active.bold }" :title="t('editor.bold')" @click="exec('bold')">
        <b>B</b>
      </button>
      <button class="etb__btn" :class="{ 'etb__btn--on': active.italic }" :title="t('editor.italic')" @click="exec('italic')">
        <i>I</i>
      </button>
      <button class="etb__btn" :class="{ 'etb__btn--on': active.underline }" :title="t('editor.underline')" @click="exec('underline')">
        <u>U</u>
      </button>
      <button class="etb__btn" :class="{ 'etb__btn--on': active.strike }" :title="t('editor.strike')" @click="exec('strikeThrough')">
        <s>S</s>
      </button>

      <span class="etb__sep" />

      <!-- 第二组：标题档 -->
      <button class="etb__btn etb__btn--block" :title="t('editor.h1')" @click="exec('formatBlock', 'h1')">H1</button>
      <button class="etb__btn etb__btn--block" :title="t('editor.h2')" @click="exec('formatBlock', 'h2')">H2</button>
      <button class="etb__btn etb__btn--block" :title="t('editor.h3')" @click="exec('formatBlock', 'h3')">H3</button>
      <button class="etb__btn etb__btn--block" :title="t('editor.paragraph')" @click="exec('formatBlock', 'p')">{{ t('editor.paragraph') }}</button>

      <span class="etb__sep" />

      <!-- 第三组：字号 -->
      <div class="etb__dd">
        <button class="etb__btn" :title="t('editor.fontSize')" @click="showFontSize = !showFontSize">
          {{ t('editor.fontSize') }} <span class="etb__caret">▾</span>
        </button>
        <div v-if="showFontSize" class="etb__menu">
          <button v-for="s in FONT_SIZES" :key="s" class="etb__menu-item" @click="setFontSize(s)">
            <span :style="{ fontSize: `${s}px` }">{{ s }}px</span>
          </button>
        </div>
      </div>

      <!-- 第四组：颜色 -->
      <div class="etb__dd">
        <button class="etb__btn" :title="t('editor.color')" @click="showColor = !showColor">
          <span class="etb__color-a">A</span> <span class="etb__caret">▾</span>
        </button>
        <div v-if="showColor" class="etb__menu etb__menu--colors">
          <button
            v-for="c in COLORS"
            :key="c"
            class="etb__swatch"
            :style="{ background: c }"
            :title="c"
            @click="setColor(c)"
          />
        </div>
      </div>

      <!-- 第五组：高亮 / 列表 / 引用 / 清除 -->
      <button class="etb__btn" :title="t('editor.highlight')" @click="exec('hiliteColor', '#ffe14d')">
        <span class="etb__hilite">{{ t('editor.highlightLabel') }}</span>
      </button>
      <span class="etb__sep" />
      <button class="etb__btn" :title="t('editor.ul')" @click="exec('insertUnorderedList')">•≡</button>
      <button class="etb__btn" :title="t('editor.ol')" @click="exec('insertOrderedList')">1≡</button>
      <button class="etb__btn" :title="t('editor.quote')" @click="exec('formatBlock', 'blockquote')">❝</button>
      <span class="etb__sep" />
      <button class="etb__btn" :title="t('editor.clear')" @click="exec('removeFormat')">⌫</button>
    </div>
  </Teleport>
</template>

<style scoped>
.etb {
  position: fixed;
  z-index: 120;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 4px 6px;
  border-radius: 10px;
  background: rgba(24, 24, 27, 0.92);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 10px 32px rgba(0, 0, 0, 0.45), 0 0 0 1px rgba(255, 255, 255, 0.08);
  transform: translateX(-50%);
  white-space: nowrap;
}

.etb__btn {
  min-width: 1.9rem;
  height: 1.9rem;
  padding: 0 0.3rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border: none;
  border-radius: 7px;
  background: transparent;
  color: #e8e8ea;
  font-size: 0.82rem;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.15s ease, color 0.15s ease;
}
.etb__btn:hover { background: rgba(255, 255, 255, 0.14); }
.etb__btn--on {
  background: rgba(255, 130, 130, 0.28);
  color: #fff;
}
.etb__btn--block { font-weight: 600; }
.etb__caret { font-size: 0.6rem; opacity: 0.7; }
.etb__color-a {
  width: 1.05rem;
  height: 1.05rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  background: linear-gradient(135deg, #e5484d 0 25%, #ffb224 25% 50%, #46a758 50% 75%, #3e63dd 75%);
  color: #fff;
  font-size: 0.62rem;
  font-weight: 700;
}
.etb__hilite {
  padding: 0.05rem 0.28rem;
  border-radius: 3px;
  background: #ffe14d;
  color: #3a3000;
  font-size: 0.72rem;
}
.etb__sep {
  width: 1px;
  height: 1.15rem;
  margin: 0 5px;
  background: rgba(255, 255, 255, 0.18);
}

/* 下拉菜单 */
.etb__dd { position: relative; }
.etb__menu {
  position: absolute;
  top: calc(100% + 6px);
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 5px;
  border-radius: 9px;
  background: rgba(24, 24, 27, 0.95);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.45), 0 0 0 1px rgba(255, 255, 255, 0.08);
}
.etb__menu-item {
  padding: 0.32rem 0.7rem;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #e8e8ea;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s ease;
}
.etb__menu-item:hover { background: rgba(255, 255, 255, 0.14); }
.etb__menu--colors {
  display: grid;
  grid-template-columns: repeat(9, 1fr);
  gap: 5px;
  padding: 7px;
}
.etb__swatch {
  width: 1.25rem;
  height: 1.25rem;
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.15s ease;
}
.etb__swatch:hover { transform: scale(1.18); }
</style>
