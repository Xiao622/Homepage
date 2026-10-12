<script setup>
import { ref } from 'vue'
import { useEditMode } from '@/composables/useEditMode'
import { t } from '@/i18n'

/**
 * 管理员登录弹窗：账号 + 密码 → 进入编辑模式。
 * 凭证校验由后端 POST /api/auth/login 完成。
 */
const emit = defineEmits(['close'])

const { login, loading } = useEditMode()

const username = ref('')
const password = ref('')
const error = ref('')

async function submit() {
  error.value = ''
  if (!username.value.trim() || !password.value) {
    error.value = t('login.required')
    return
  }
  try {
    const ok = await login(username.value.trim(), password.value)
    if (ok) emit('close')
  } catch (e) {
    error.value = e.message || t('login.failed')
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div class="login-overlay" @click.self="emit('close')">
        <form class="login-dialog" @submit.prevent="submit">
          <h3 class="login-dialog__title serif">{{ t('login.title') }}</h3>
          <p class="login-dialog__sub">{{ t('login.sub') }}</p>

          <label class="login-dialog__field">
            <span>{{ t('login.account') }}</span>
            <input
              v-model="username"
              type="text"
              autocomplete="username"
              :placeholder="t('login.accountPlaceholder')"
              spellcheck="false"
            />
          </label>

          <label class="login-dialog__field">
            <span>{{ t('login.password') }}</span>
            <input
              v-model="password"
              type="password"
              autocomplete="current-password"
              :placeholder="t('login.passwordPlaceholder')"
            />
          </label>

          <p v-if="error" class="login-dialog__error">{{ error }}</p>

          <div class="login-dialog__actions">
            <button type="button" class="login-dialog__cancel" @click="emit('close')">{{ t('login.cancel') }}</button>
            <button type="submit" class="login-dialog__submit" :disabled="loading">
              {{ loading ? t('login.loggingIn') : t('login.login') }}
            </button>
          </div>
        </form>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.login-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.login-dialog {
  width: min(360px, 100%);
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
  padding: 1.6rem 1.5rem 1.4rem;
  border-radius: 1rem;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.35);
  color: var(--text);
}

.login-dialog__title {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 600;
}
.login-dialog__sub {
  margin: 0 0 0.3rem;
  font-size: 0.82rem;
  color: var(--faint);
}

.login-dialog__field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  font-size: 0.82rem;
  color: var(--muted);
}
.login-dialog__field input {
  padding: 0.6rem 0.75rem;
  border-radius: 0.6rem;
  border: 1px solid var(--card-border);
  background: var(--input-bg, rgba(128, 128, 128, 0.08));
  color: var(--text);
  font-size: 0.92rem;
  outline: none;
  transition: border-color 0.2s ease;
}
.login-dialog__field input:focus {
  border-color: var(--accent);
}

.login-dialog__error {
  margin: 0;
  font-size: 0.8rem;
  color: #ff8282;
}

.login-dialog__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.6rem;
  margin-top: 0.3rem;
}
.login-dialog__cancel {
  padding: 0.5rem 1rem;
  border-radius: 999px;
  border: 1px solid var(--card-border);
  background: transparent;
  color: var(--muted);
  font-size: 0.85rem;
  cursor: pointer;
  transition: color 0.2s ease, border-color 0.2s ease;
}
.login-dialog__cancel:hover {
  color: var(--text);
  border-color: var(--card-border-hover);
}
.login-dialog__submit {
  padding: 0.5rem 1.15rem;
  border-radius: 999px;
  border: none;
  background: var(--accent);
  color: #fff;
  font-size: 0.85rem;
  cursor: pointer;
  transition: opacity 0.2s ease;
}
.login-dialog__submit:hover { opacity: 0.88; }
.login-dialog__submit:disabled { opacity: 0.55; cursor: not-allowed; }

.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.2s ease;
}
.dialog-enter-active .login-dialog,
.dialog-leave-active .login-dialog {
  transition: transform 0.2s ease;
}
.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}
.dialog-enter-from .login-dialog,
.dialog-leave-to .login-dialog {
  transform: scale(0.96) translateY(8px);
}
</style>
