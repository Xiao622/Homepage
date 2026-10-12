<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { t } from '@/i18n'
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'

/**
 * Hero 卡右侧 3D 模型：
 *  - 加载 public/models/hero-model.glb
 *  - 鼠标拖拽旋转（水平为主，垂直小幅限制，避免翻过头）
 *  - 空闲时缓慢自转（"转台"效果），拖拽后从当前位置继续
 *  - 透明背景融入卡片，容器自适应
 */
const container = ref(null)
const loading = ref(true)
const failed = ref(false)

let renderer = null
let scene = null
let camera = null
let group = null
let rafId = 0
let ro = null
let dragging = false
let lastX = 0
let lastY = 0
let idleRot = 0

function setupScene(el) {
  const width = el.clientWidth
  const height = el.clientHeight

  scene = new THREE.Scene()

  camera = new THREE.PerspectiveCamera(35, width / height, 0.1, 2000)
  camera.position.set(0, 0.35, 3.6)
  camera.lookAt(0, 0, 0)

  renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.setSize(width, height)
  el.appendChild(renderer.domElement)

  // 灯光：环境 + 双方向光，让 GLB 材质有层次
  scene.add(new THREE.HemisphereLight(0xffffff, 0x222233, 1.15))
  const key = new THREE.DirectionalLight(0xffffff, 2.1)
  key.position.set(3, 5, 4)
  scene.add(key)
  const rim = new THREE.DirectionalLight(0x88aaff, 0.9)
  rim.position.set(-4, 2, -3)
  scene.add(rim)

  // 旋转容器（group 只负责转动，wrapper 负责居中+缩放）
  group = new THREE.Group()
  scene.add(group)

  const loader = new GLTFLoader()
  loader.load(
    import.meta.env.VITE_MODEL_URL || '/models/hero-model.glb',
    (gltf) => {
      const model = gltf.scene
      const box = new THREE.Box3().setFromObject(model)
      const center = box.getCenter(new THREE.Vector3())
      const size = box.getSize(new THREE.Vector3())
      const maxDim = Math.max(size.x, size.y, size.z)
      const scale = 2.2 / maxDim // 模型目标高度约 2.2 单位

      const wrapper = new THREE.Group()
      model.position.sub(center) // 模型中心移到 wrapper 原点
      wrapper.add(model)
      wrapper.scale.setScalar(scale)
      group.add(wrapper)

      loading.value = false
    },
    undefined,
    (err) => {
      console.error('Hero model load failed:', err)
      loading.value = false
      failed.value = true
    },
  )

  // ---- 鼠标拖拽旋转 ----
  const canvas = renderer.domElement
  canvas.style.cursor = 'grab'
  canvas.addEventListener('pointerdown', onDown)
  window.addEventListener('pointermove', onMove)
  window.addEventListener('pointerup', onUp)

  // ---- 容器尺寸自适应 ----
  ro = new ResizeObserver(() => {
    const w = el.clientWidth
    const h = el.clientHeight
    if (!w || !h) return
    camera.aspect = w / h
    camera.updateProjectionMatrix()
    renderer.setSize(w, h)
  })
  ro.observe(el)

  // ---- 渲染循环 ----
  function animate() {
    rafId = requestAnimationFrame(animate)
    if (group && !dragging) {
      idleRot += 0.004 // 缓慢自转（转台效果）
      group.rotation.y = idleRot
    }
    renderer.render(scene, camera)
  }
  animate()

  // dev 调试钩子（仅开发环境，验证拖拽旋转用）
  if (import.meta.env.DEV) {
    window.__heroModel = {
      getY: () => group.rotation.y,
      getX: () => group.rotation.x,
    }
  }
}

function onDown(e) {
  dragging = true
  lastX = e.clientX
  lastY = e.clientY
  renderer.domElement.style.cursor = 'grabbing'
}

function onMove(e) {
  if (!dragging || !group) return
  const dx = e.clientX - lastX
  const dy = e.clientY - lastY
  lastX = e.clientX
  lastY = e.clientY
  group.rotation.y += dx * 0.008 // 水平旋转（左右晃动）
  group.rotation.x = Math.max(-0.6, Math.min(0.6, group.rotation.x + dy * 0.005))
}

function onUp() {
  dragging = false
  idleRot = group ? group.rotation.y : idleRot // 从当前位置继续自转
  renderer.domElement.style.cursor = 'grab'
}

onMounted(() => {
  if (container.value) setupScene(container.value)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(rafId)
  if (ro) ro.disconnect()
  window.removeEventListener('pointermove', onMove)
  window.removeEventListener('pointerup', onUp)
  if (renderer) {
    renderer.dispose()
    if (renderer.domElement && renderer.domElement.parentNode) {
      renderer.domElement.parentNode.removeChild(renderer.domElement)
    }
  }
})
</script>

<template>
  <div ref="container" class="hero-model" role="img" :aria-label="'3D model — ' + t('home.dragToRotate')">
    <span v-if="loading" class="hero-model__hint">{{ t('home.modelLoading') }}</span>
    <span v-else-if="failed" class="hero-model__hint">{{ t('home.modelUnavailable') }}</span>
    <span v-else class="hero-model__tip">{{ t('home.dragToRotate') }}</span>
  </div>
</template>

<style scoped>
.hero-model {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 14rem;
  overflow: hidden;
}
.hero-model canvas {
  display: block;
  width: 100%;
  height: 100%;
}
.hero-model__hint {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  color: var(--faint);
}
.hero-model__tip {
  position: absolute;
  right: 0.4rem;
  bottom: 0.4rem;
  font-size: 0.68rem;
  letter-spacing: 0.04em;
  color: var(--faint);
  opacity: 0.7;
  pointer-events: none;
}
</style>
