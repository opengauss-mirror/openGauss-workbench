<template>
  <div class="navbar">
    <div class="left-side">
      <a-space>
        <img src="@/assets/logo.png" alt="logo" />
        <a-typography-title
          :style="{ margin: 0, fontSize: '14px', color: '#fff' }"
          :heading="5"
        >
          {{$t('navbar.index.5mpib9iig6o0')}}
        </a-typography-title>
      </a-space>
    </div>
    <ul class="right-side">
      <li>
        <a-dropdown trigger="hover" @select="changeLang">
          <div class="nav-con">
            <span class="txt">{{ currentLocale === 'zh-CN' ? '简体中文' : 'English' }}</span>
            <img src="@/assets/images/common/down-arrow.png" alt="">
          </div>
          <template #content>
            <a-doption
              v-for="item in locales"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-doption>
          </template>
        </a-dropdown>
      </li>
      <li>
        <a-dropdown trigger="hover">
          <div class="nav-con">
            <img src="@/assets/images/common/avatar.png" alt="">
            <span class="txt">{{ userName }}</span>
            <img src="@/assets/images/common/down-arrow.png" alt="">
          </div>
          <template #content>
            <a-doption>
              <a-space @click="$router.push({ name: 'SecurityUsercenter' })">
                <icon-user />
                <span>
                  {{$t('navbar.index.5m6nsk34ra40')}}
                </span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="handleCode">
                <icon-lock />
                <span>
                  {{$t('navbar.index.5m6nsk34rd00')}}
                </span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="handleLogout">
                <icon-export />
                <span>
                  {{$t('navbar.index.5m6nsk34rg40')}}
                </span>
              </a-space>
            </a-doption>
          </template>
        </a-dropdown>
      </li>
      <li>
        <div class="nav-btn" @click="handleToggleTheme">
          <icon-moon-fill v-if="theme === 'dark'" />
          <icon-sun-fill v-else />
        </div>
      </li>
    </ul>
  </div>
  <!-- reset -->
  <update-code v-model:open="editCodeVisible" :options="currentEditUser" />
</template>

<script lang="ts" setup>
  import { ref, computed, onMounted } from 'vue'
  import useUser from '@/hooks/user'
  import useLocale from '@/hooks/locale'
  import { LOCALE_OPTIONS } from '@/locale'
  import { useUserStore, useAppStore } from '@/store'
  import UpdateCode from '@/views/security/user/components/UpdateCode.vue'
  import WujieVue from 'wujie-vue3'
  const { bus } = WujieVue

  const { logout } = useUser()
  const { changeLocale, currentLocale } = useLocale()
  const locales = [...LOCALE_OPTIONS]
  const userStore = useUserStore()
  const appStore = useAppStore()

  const currentEditUser = ref({})
  const editCodeVisible = ref<boolean>(false)

  const userName = computed(() => userStore.userName)

  const theme = computed(() => {
    return appStore.theme
  })

  const handleToggleTheme = () => {
    if (theme.value === 'light') {
      bus.$emit('opengauss-theme-change', 'dark')
      appStore.toggleTheme(true)
    } else {
      bus.$emit('opengauss-theme-change', 'light')
      appStore.toggleTheme(false)
    }
  }

  const changeLang = (value: any) => {
    bus.$emit('opengauss-locale-change', value)
    changeLocale(value)
    appStore.fetchServerMenuConfig()
  }

  const handleCode = () => {
    currentEditUser.value = {
      userId: userStore.userId
    }
    editCodeVisible.value = true
  }

  const handleLogout = () => {
    logout()
  }

  onMounted(() => {
    const opengaussTheme = localStorage.getItem('opengauss-theme')
    if (opengaussTheme === 'dark') {
      appStore.toggleTheme(true)
    } else {
      appStore.toggleTheme(false)
    }
  })
</script>

<style scoped lang="less">
  .navbar {
    display: flex;
    justify-content: space-between;
    height: 100%;
    background-color: #282B33;
    border-bottom: 1px solid var(--color-border);
  }

  .left-side {
    display: flex;
    align-items: center;
    padding-left: 20px;
  }

  .right-side {
    display: flex;
    padding-right: 16px;
    list-style: none;
    :deep(.locale-select) {
      border-radius: 20px;
    }
    li {
      display: flex;
      align-items: center;
      padding: 0 0 0 10px;
    }
    .nav-con {
      display: flex;
      align-items: center;
      cursor: pointer;
      .txt {
        font-size: 12px;
        color: #C9CDD4;
        margin-left: 3px;
        margin-right: 8px;
      }
    }
    a {
      color: var(--color-text-1);
      text-decoration: none;
    }
    .nav-btn {
      cursor: pointer;
      display: flex;
      align-items: center;
      color: #C9CDD4;
      font-size: 20px;
    }
    .trigger-btn,
    .ref-btn {
      position: absolute;
      bottom: 14px;
    }
    .trigger-btn {
      margin-left: 14px;
    }
  }

  .space-item {
    img {
      width: 14px;
      height: 14px;
      position: relative;
      top: -30px;
      filter: drop-shadow(#1D212A 0 30px);
      border-bottom: 0 solid transparent;
    }
    &:hover {
      img {
        position: relative;
        top: -30px;
        filter: drop-shadow(#E41D1D 0 30px);
        border-bottom: 0 solid transparent;
      }
      .txt {
        color: rgb(var(--primary-6));
      }
    }
  }
</style>

<style lang="less">
  .message-popover {
    .arco-popover-content {
      margin-top: 0;
    }
  }
</style>
