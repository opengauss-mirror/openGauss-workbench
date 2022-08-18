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

<script lang="ts" setup>
import { computed } from 'vue'
import enUS from '@arco-design/web-vue/es/locale/lang/en-us'
import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn'
import useLocale from '@/hooks/locale'

const { currentLocale } = useLocale()
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
