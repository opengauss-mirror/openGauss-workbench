<template>
  <div class="app-container">
    <div class="main-bd">
      <div class="iframe-con">
        <WujieVue class="iframe" :class="[appStore.menuCollapse && 'iframe-clp']" :name="route.fullPath" :url="pluginUrl" :props="props" :plugins="plugins" :sync="false"></WujieVue>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch, onBeforeMount } from 'vue'
  import { createPluginApp } from '@/utils/pluginApp'
  import { useRouter, useRoute } from 'vue-router'
  import { useAppStore } from '@/store'

  const appStore = useAppStore()
  const router = useRouter()
  const route = useRoute()
  const pluginUrl = ref(route.path)
  const props = ref<any>({})
  const methods = ref<any>({
    jump (location: any) {
      router.push(location)
    }
  })
  const plugins = ref<any>([])

  watch(() => appStore.menuCollapse, (val) => {
    if (val) {
      plugins.value = [
        {
          cssBeforeLoaders: [
            { content: 'html{padding-top: 113px !important;padding-left: 64px !important;height: 100%;view-transition-name: none;}' }
          ]
        }
      ]
    } else {
      plugins.value = [
        {
          cssBeforeLoaders: [
            { content: 'html{padding-top: 113px !important;padding-left: 228px !important;height: 100%;view-transition-name: none;}' }
          ]
        }
      ]
    }
  }, {
    immediate: true
  })

  props.value = {
    data: route.query,
    methods: methods.value
  }

  onBeforeMount(() => {
    if (route.fullPath) {
      createPluginApp(route.fullPath as string)
    }
  })
</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    .iframe-con {
      width: 100%;
      height: 100%;
      overflow: hidden;
      .iframe {
        width: calc(100vw - 16px);
        height: 100%;
        margin-top: -113px;
        margin-left: -228px;
        &.iframe-clp {
          margin-left: -64px;
        }
      }
    }
  }
}
</style>
