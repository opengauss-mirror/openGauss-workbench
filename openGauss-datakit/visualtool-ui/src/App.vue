<template>
  <a-config-provider :locale="locale">
    <router-view />
  </a-config-provider>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useTitle } from '@vueuse/core'
import enUS from '@arco-design/web-vue/es/locale/lang/en-us'
import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn'
import useLocale from '@/hooks/locale'

const { currentLocale } = useLocale()
const title = computed(() => {
  return currentLocale.value === 'en-US' ? 'openGauss DataKit' : 'openGauss DataKit'
})
useTitle(title)
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
