<template>
  <a-config-provider :locale="locale">
    <router-view v-slot="{ Component, route }">
      <keep-alive>
        <component :is="Component" v-if="route.meta && route.meta.keepAlive" :key="route.meta.usePathKey ? route.fullPath : undefined" />
      </keep-alive>
      <component :is="Component" v-if="!(route.meta && route.meta.keepAlive)" :key="route.meta.usePathKey ? route.fullPath : undefined" />
    </router-view>
  </a-config-provider>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import enUS from '@arco-design/web-vue/es/locale/lang/en-us'
import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn'
import useLocale from '@/hooks/locale'

const { currentLocale, changeLocale } = useLocale()

onMounted(() => {
  // theme change
  const theme = localStorage.getItem('opengauss-theme')
  if (theme === 'dark') {
    document.body.setAttribute('arco-theme', 'dark')
  } else {
    document.body.removeAttribute('arco-theme')
  }

  window.$wujie?.bus.$on('opengauss-theme-change', val => {
    if (val === 'dark') {
      document.body.setAttribute('arco-theme', 'dark')
    } else {
      document.body.removeAttribute('arco-theme')
    }
  })

  // locale change
  const locale = localStorage.getItem('locale')
  if (locale === 'en-US') {
    changeLocale('en-US')
  } else {
    changeLocale('zh-CN')
  }

  window.$wujie?.bus.$on('opengauss-locale-change', val => {
    if (val === 'en-US') {
      changeLocale('en-US')
    } else {
      changeLocale('zh-CN')
    }
  })

  // menu collapse
  const htmlStyle = document.getElementsByTagName('html')[0].style
  window.$wujie?.bus.$on('opengauss-menu-collapse', val => {
    if (val === '1') {
      htmlStyle.setProperty('padding-left', '64px', 'important')
    } else {
      htmlStyle.setProperty('padding-left', '236px', 'important')
    }
  })
})

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
</script>
