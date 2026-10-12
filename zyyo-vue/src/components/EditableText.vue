<script setup>
import { ref, watch } from 'vue'
import { useEditMode } from '@/composables/useEditMode'

/**
 * 可编辑文字组件：
 *  - 无论是否编辑模式，都优先显示后端已保存值（homeContent[cfgKey]），
 *    保存后退出编辑模式页面即同步为新文案；fallback 仅在值缺失/未加载时兜底。
 *  - 编辑模式：contenteditable，虚线高亮，blur 时把新值写入 draft
 *
 * props.cfgKey  对应后端 home_content JSON 的键
 * props.fallback  默认文案（后端无值时兜底）
 * props.tag       渲染的标签（默认 span）
 */
const props = defineProps({
  cfgKey: { type: String, required: true },
  fallback: { type: String, default: '' },
  tag: { type: String, default: 'span' },
})

const { isEditMode, homeContent, setDraft } = useEditMode()

const el = ref(null)
const text = ref(props.fallback)

/** 显示值：优先后端已存值（编辑/非编辑模式一致），缺失时回退 fallback。 */
watch(
  () => [isEditMode.value, homeContent.value[props.cfgKey], props.fallback],
  () => {
    const saved = homeContent.value[props.cfgKey]
    text.value = saved != null ? saved : props.fallback
  },
  { immediate: true },
)

function onBlur(e) {
  const val = e.target.textContent.trim()
  if (val) setDraft(props.cfgKey, val)
  else setDraft(props.cfgKey, props.fallback)
}
</script>

<template>
  <component
    :is="tag"
    ref="el"
    class="editable-text"
    :class="{ 'editable-text--on': isEditMode }"
    :contenteditable="isEditMode ? 'plaintext-only' : undefined"
    :spellcheck="false"
    @blur="onBlur"
  >{{ text }}</component>
</template>

<style scoped>
.editable-text--on {
  outline: 1px dashed var(--accent);
  outline-offset: 3px;
  border-radius: 3px;
  cursor: text;
  transition: outline-color 0.2s ease, background-color 0.2s ease;
}
.editable-text--on:hover {
  background: var(--accent-soft);
}
.editable-text--on:focus {
  outline-style: solid;
  outline-color: var(--accent);
  background: transparent;
}
</style>
