<template>
  <a-config-provider :locale="locale" :updateAtScroll="true">
    <router-view v-slot="{ Component, route }">
      <keep-alive>
        <div class="app-container" v-if="route.meta && route.meta.keepAlive">
          <div class="main-bd">
            <component :is="Component" :key="route.meta.usePathKey ? route.fullPath : undefined" />
          </div>
        </div>
      </keep-alive>
      <div class="app-container" v-if="!(route.meta && route.meta.keepAlive)">
        <div class="main-bd">
          <component :is="Component" :key="route.meta.usePathKey ? route.fullPath : undefined" />
        </div>
      </div>
    </router-view>
  </a-config-provider>
</template>

<script lang="ts" setup>
import { computed, onMounted } from 'vue'
import enUS from '@arco-design/web-vue/es/locale/lang/en-us'
import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'
import useLocale from '@/hooks/locale'
const { changeLocale, currentLocale } = useLocale()
const locale = computed(() => {
  switch (currentLocale.value) {
    case 'zh-CN':
      return zhCN
    case 'en-US':
      return enUS
    default:
      return zhCN
  }
})

onMounted(() => {
  const theme = localStorage.getItem('opengauss-theme')
  if (theme === 'dark') {
    document.body.setAttribute('arco-theme', 'dark')
  } else {
    document.body.removeAttribute('arco-theme')
  }
  const localeVal = localStorage.getItem('locale')
  if (localeVal) {
    changeLocale(localeVal)
  }

  window.$wujie?.bus.$on('opengauss-theme-change', (val: string) => {
    if (val === 'dark') {
      document.body.setAttribute('arco-theme', 'dark')
    } else {
      document.body.removeAttribute('arco-theme')
    }
  })

  window.$wujie?.bus.$on('opengauss-locale-change', (val: string) => {
    if (val) {
      changeLocale(val)
    }
  })

  const htmlstyle = document.getElementsByTagName('html')[0].style
  window.$wujie?.bus.$on('opengauss-menu-collapse', (val: string) => {
    if (val === '1') {
      htmlstyle.setProperty('padding-left', '64px', 'important')
    } else {
      htmlstyle.setProperty('padding-left', '236px', 'important')
    }
  })
})
</script>
<style lang="less" scoped>
.app-container {
  .main-bd {}
}
</style>
