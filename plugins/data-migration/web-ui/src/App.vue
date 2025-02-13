<template>
  <a-config-provider :locale="locale">
    <el-config-provider :locale="elLocale">
      <router-view v-slot="{ Component, route }">
        <keep-alive>
          <component :is="Component" v-if="route.meta && route.meta.keepAlive"
            :key="route.meta.usePathKey ? route.fullPath : undefined" />
        </keep-alive>
        <component :is="Component" v-if="!(route.meta && route.meta.keepAlive)"
          :key="route.meta.usePathKey ? route.fullPath : undefined" />
      </router-view>
    </el-config-provider>
  </a-config-provider>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import enUS from '@arco-design/web-vue/es/locale/lang/en-us'
import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'
import useLocale from '@/hooks/locale'
import useTheme from '@/hooks/theme'

const { currentLocale, changeLocale } = useLocale()
const { changeTheme } = useTheme()

onMounted(() => {
  // theme change
  const theme = localStorage.getItem('opengauss-theme')
  if (theme === 'dark') {
    document.body.setAttribute('arco-theme', 'dark')
    changeTheme('dark')
  } else {
    document.body.removeAttribute('arco-theme')
    changeTheme('')
  }

  window.$wujie?.bus.$on('opengauss-theme-change', val => {
    if (val === 'dark') {
      document.body.setAttribute('arco-theme', 'dark')
      changeTheme('dark')
    } else {
      document.body.removeAttribute('arco-theme')
      changeTheme('')
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

const elLocale = computed(() => {
  return currentLocale.value === 'zh-CN' ? zhCn : en
})
</script>

<style lang="less">
@import '@/assets/style/openGlobal.less';
</style>