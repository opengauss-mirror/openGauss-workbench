<template>
  <a-config-provider :locale="locale">
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
})
</script>
<style lang="less" scoped>
.app-container {
  .main-bd {}
}
</style>