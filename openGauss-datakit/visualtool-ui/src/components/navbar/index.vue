<template>
  <div class="navbar">
    <div class="left-side">
      <a-space>
        <img src="@/assets/logo.png" alt="logo"/>
        <a-typography-title :style="{ margin: 0, fontSize: '14px', color: '#fff' }" :heading="5">
          {{ $t('navbar.index.5mpib9iig6o0') }}
        </a-typography-title>
      </a-space>
    </div>
    <ul class="right-side">
      <el-tooltip
        popper-class='robotClass'
        placement="bottom-end"
        :effect="theme"
        :show-arrow="false"
      >
        <template #default>
          <img class="robotImg" :src="robot" alt="robot icon" />
        </template>
        <template #content><div class="divClass" >{{$t('navbar.index.5nu4bvtnuyk0')}}<a href="mailto:ogpilot@public.opengauss.org">ogpilot@public.opengauss.org</a>{{$t('navbar.index.5nu5bvtnuyk0')}}</div>
          <div class="buttonClass">
          <el-button type="primary" tag="a" size="small" href="mailto:ogpilot@public.opengauss.org?subject=oGPilot试用申请&body=请在此填写您的公司/学校名和公网IP" target="_blank" rel="noopener noreferrer" >{{$t('navbar.index.5nu6bvtnuyk0')}}</el-button>
          <el-button type="primary" tag="a" size="small" href="https://opengauss-copilot.test.osinfra.cn/" target="_blank" rel="noopener noreferrer" >{{$t('navbar.index.5nu7bvtnuyk0')}}</el-button></div>
        </template>
      </el-tooltip>
      <li>
        <a-dropdown trigger="hover" @select="changeLang">
          <div class="nav-con">
            <span class="txt">{{ currentLocale === 'zh-CN' ? '简体中文' : 'English' }}</span>
            <img src="@/assets/images/common/down-arrow.png" alt="">
          </div>
          <template #content>
            <a-doption v-for="item in locales" :key="item.value" :value="item.value">
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
                <icon-user/>
                <span>
                  {{ $t('navbar.index.5m6nsk34ra40') }}
                </span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="handleCode">
                <icon-lock/>
                <span>
                  {{ $t('navbar.index.5m6nsk34rd00') }}
                </span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="handleSysSetting">
                <icon-settings/>
                <span>
                  {{ $t('navbar.index.5m6nsk34rd01') }}
                </span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="handleLogout">
                <icon-export/>
                <span>
                  {{ $t('navbar.index.5m6nsk34rg40') }}
                </span>
              </a-space>
            </a-doption>
          </template>
        </a-dropdown>
      </li>
      <li>
        <a-tooltip :content="theme === 'light' ? $t('navbar.index.5nu3bvtnu5o0') : $t('navbar.index.5nu3bvtnuyk0')" position="br">
          <div class="nav-btn" @click="handleToggleTheme">
            <icon-moon-fill v-if="theme === 'dark'"/>
            <icon-sun-fill v-else/>
          </div>
        </a-tooltip>
      </li>
    </ul>
  </div>
  <!-- reset -->
  <update-code v-model:open="editCodeVisible" :options="currentEditUser"/>
  <system-setting v-model:open="sysSettingVisible" :options="currentEditUser"/>
</template>

<script lang="ts" setup>
import { ref, watch, computed, onMounted } from 'vue'
import { Modal } from '@arco-design/web-vue'
import useUser from '@/hooks/user'
import useLocale from '@/hooks/locale'
import { LOCALE_OPTIONS } from '@/locale'
import { useUserStore, useAppStore, useTabBarStore } from '@/store'
import { destroyPluginApp } from '@/utils/pluginApp'
import UpdateCode from '@/views/security/user/components/UpdateCode.vue'
import WujieVue from 'wujie-vue3'
import { useI18n } from 'vue-i18n'
import SystemSetting from '@/views/security/systemSetting/SystemSetting.vue'
import robot from '@/assets/images/ops/robot.png'

const { t } = useI18n()
const { bus } = WujieVue

const { logout } = useUser()
const { changeLocale, currentLocale } = useLocale()
const locales = [...LOCALE_OPTIONS]
const userStore = useUserStore()
const appStore = useAppStore()
const tabBarStore = useTabBarStore()

const currentEditUser = ref({})
const editCodeVisible = ref<boolean>(false)
const sysSettingVisible = ref<boolean>(false)

const userName = computed(() => userStore.userName)

const theme = computed(() => {
  return appStore.theme
})
watch(() => appStore.menuCollapse, (val) => {
  if (val) {
    bus.$emit('opengauss-menu-collapse', '1')
  } else {
    bus.$emit('opengauss-menu-collapse', '0')
  }
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

const handleSysSetting = () => {
  currentEditUser.value = {
    userId: userStore.userId
  }
  sysSettingVisible.value = true
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
  bus.$on('opengauss-login-expired', () => {
    Modal.error({
      title: t('components.login-form.5o61dvpc2o00'),
      content: t('components.login-form.5o61dvpc5cs0'),
      okText: t('components.login-form.5o61dvpc5o80'),
      width: 'auto',
      maskClosable: false,
      async onOk () {
        const userStore = useUserStore()

        await userStore.logout()
        window.location.reload()
      }
    })
  })
  bus.$on('opengauss-close-tab', (route: any) => {
    tabBarStore.deleteTags(route)
    destroyPluginApp(route.fullPath)
  })
  bus.$on('opengauss-close-special-tab', (route: any) => {
    tabBarStore.deleteTagByFullPath(route)
    destroyPluginApp(route.fullPath)
  })
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
  .robotImg {
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    transition: 0.2s;
    height: 38px;
    top: 6px;
    right: 212px;
    position: absolute;
  }
  .robotImg:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
  }

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
.robotClass{
  max-width: 282px !important;
  background: var(--el-text-color-primary) !important;
  color:var(--el-bg-color) !important;
  border: 1px solid var(--el-text-color-primary) !important;
  .buttonClass {
    display: flex;
    justify-content: space-between;
    margin: 8px 4px 4px 4px;
    .el-button--primary{
      color: var(--color-bg-2);
      background-color: rgb(var(--primary-6));
      border: 1px solid transparent;
      &:hover{
        background-color: rgb(var(--primary-5));
        border-color: transparent;
      }
    }
  }
  .divClass{
    padding:4px;
    width: 282px;
  }
  a {
    color: white;
    text-decoration: none;
    &:hover{
      color: rgb(var(--primary-6));
    }
  }
}
</style>
