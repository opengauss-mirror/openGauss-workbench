<template>
  <div class="login-form-wrapper">
    <div class="login-form-title">{{$t('components.login-form.5m6noeaox740')}}</div>
    <div class="login-form-title">{{$t('components.login-form.5mpi8serosw0')}}</div>
    <a-form
      ref="loginForm"
      :model="userInfo"
      class="login-form"
      layout="vertical"
      size="large"
      @submit="handleSubmit"
    >
      <a-form-item
        field="username"
        class="login-form-input"
        :rules="[{ required: true, message: $t('components.login-form.5m6noeaoxxg0') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input
          v-model="userInfo.username"
          :placeholder="$t('components.login-form.5m6noeaoy240')"
        ></a-input>
      </a-form-item>
      <a-form-item
        field="password"
        class="login-form-input"
        :rules="[{ required: true, message: $t('components.login-form.5m6noeaoy5c0') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input-password
          v-model="userInfo.password"
          :placeholder="$t('components.login-form.5m6noeaoy9c0')"
          allow-clear
        ></a-input-password>
      </a-form-item>
      <a-space :size="16" direction="vertical">
        <div class="login-form-password-actions">
          <a-checkbox
            checked="rememberPassword"
            v-model="loginConfig.rememberMe"
            @change="(setRememberPassword as any)"
          >
            {{$t('components.login-form.5m6noeap7a80')}}
          </a-checkbox>
        </div>
        <a-button type="outline" html-type="submit" long size="large" :loading="loading" style="height: 64px; font-size: 24px; margin-top: 50px;">
          {{$t('components.login-form.5m6noeap7n40')}}
        </a-button>
      </a-space>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue'
  import { useRouter } from 'vue-router'
  import { Message } from '@arco-design/web-vue'
  import { ValidatedError } from '@arco-design/web-vue/es/form/interface'
  import { useStorage } from '@vueuse/core'
  import { useUserStore } from '@/store'
  import useLoading from '@/hooks/loading'
  import type { LoginData } from '@/api/user'

  const router = useRouter()
  const errorMessage = ref('')
  const { loading, setLoading } = useLoading()
  const userStore = useUserStore()

  const loginConfig = useStorage('login-config', {
    username: '',
    password: '',
    rememberMe: false
  })
  const userInfo = reactive({
    username: loginConfig.value.rememberMe ? loginConfig.value.username : '',
    password: loginConfig.value.rememberMe ? loginConfig.value.password : ''
  })

  // Log in
  const handleSubmit = async ({
    errors,
    values
  }: {
    errors: Record<string, ValidatedError> | undefined;
    values: Record<string, any>;
  }) => {
    if (!errors) {
      setLoading(true)
      try {
        await userStore.login(values as LoginData)
        const { redirect, ...othersQuery } = router.currentRoute.value.query
        router.push({
          name: (redirect as string) || 'Dashboard',
          query: {
            ...othersQuery
          }
        })
        Message.success('Login success')
        const { rememberMe } = loginConfig.value
        const { username, password } = values
        // The actual production environment requires encrypted storage.
        loginConfig.value.username = rememberMe ? username : ''
        loginConfig.value.password = rememberMe ? password : ''
      } catch (err) {
        errorMessage.value = (err as Error).message
      } finally {
        setLoading(false)
      }
    }
  }
  // Set whether to remember the password
  const setRememberPassword = (value: boolean) => {
    loginConfig.value.rememberMe = value
  }
</script>

<style lang="less" scoped>
  .login-form {
    margin-top: 50px;
    &-wrapper {
      width: 500px;
    }

    &-title {
      color: #fff;
      font-size: 30px;
      line-height: 42px;
    }

    &-error-msg {
      height: 32px;
      color: rgb(var(--red-6));
      line-height: 32px;
    }

    &-password-actions {
      display: flex;
      justify-content: space-between;
      :deep(.arco-checkbox) {
        padding-left: 0;
      }
      :deep(.arco-checkbox-label) {
        color: #fff;
      }
      :deep(.arco-checkbox-checked) {
        .arco-checkbox-icon {
          background-color: transparent;
          border-color: #fff;
        }
      }
      :deep(.arco-icon-hover) {
        &::before {
          display: none !important;
        }
      }
    }

    &-register-btn {
      color: var(--color-text-3) !important;
    }

    &-input {
      margin-bottom: 36px;
      position: relative;
      .arco-input-wrapper {
        background: none !important;
        border: none;
        border-bottom: 1px solid rgba(255, 255, 255, .5) !important;
        padding-left: 0;
        :deep(.arco-input) {
          &::placeholder {
            color: #fff;
          }
          &.arco-input-size-large {
            font-size: 18px;
            color: #fff;
          }
        }
      }
      :deep(.arco-form-item-message) {
        position: absolute;
        top: 45px;
        font-size: 14px;
      }
    }
  }
</style>
