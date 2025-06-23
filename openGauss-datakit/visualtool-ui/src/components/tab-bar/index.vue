<template>
  <div class="tab-bar-container">
    <a-affix ref="affixRef" :offset-top="offsetTop">
      <div class="tab-bar-box">
        <div class="tab-bar-scroll">
          <div class="tags-wrap">
            <tab-item
              v-for="(tag, index) in tagList"
              :key="tag.fullPath"
              :index="index"
              :item-data="tag"
            />
          </div>
        </div>
      </div>
    </a-affix>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, watch } from 'vue'
  import type { RouteLocationNormalized } from 'vue-router'
  import { listenerRouteChange } from '@/utils/route-listener'
  import { useAppStore, useTabBarStore } from '@/store'
  import appClientMenus from '@/router/appMenus'
  import tabItem from './tab-item.vue'
  import { destroyPluginApp } from '@/utils/pluginApp'
  import showMessage from '@/hooks/showMessage'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()
  const appStore = useAppStore()
  const appRoute = computed(() => {
    if (appStore.menuFromServer) {
      return appStore.appAsyncMenus
    }
    return appClientMenus
  })
  const tabBarStore = useTabBarStore()

  const affixRef = ref()
  const tagList = computed(() => {
    const copyRouter = JSON.parse(JSON.stringify(appRoute.value)) || []
    const deepFlatten: any = (arr: any[]) => [].concat(...arr.map(v => (Array.isArray(v.children) ? deepFlatten(v.children) : v)))
    const flattenRouter = deepFlatten(copyRouter) || []
    const cacheList = tabBarStore.getTabList.map((item: any) => {
      const filterItem = flattenRouter.find((v: any) => v.name === item.name)
      return {
        ...item,
        title: filterItem ? filterItem.meta.title : item.title
      }
    })
    return cacheList
  })
  const offsetTop = computed(() => {
    return appStore.navbar ? 50 : 0
  })

  watch(
    () => appStore.navbar,
    () => {
      affixRef.value.updatePosition()
    }
  )
  listenerRouteChange((route: RouteLocationNormalized) => {
    if (
      !route.meta.noAffix &&
      !tagList.value.some((tag) => tag.fullPath === route.fullPath)
    ) {
      // Migration subtask details expand only the current one
      if (route.name === 'Static-pluginData-migrationSubTaskDetail') {
        // if there were previous subtask details
        const migrationSubDetailTag = tagList.value.find((tag) => tag.name === 'Static-pluginData-migrationSubTaskDetail')
        migrationSubDetailTag && tabBarStore.deleteTagByFullPath(migrationSubDetailTag);
        migrationSubDetailTag && destroyPluginApp(migrationSubDetailTag.fullPath)
        // The prompt shows only one subtask details
        migrationSubDetailTag && showMessage('warning', t('tab-bar.tab-item.subTaskMigraTab'))
      }
      tabBarStore.updateTabList(route)
    }
  }, true)
</script>

<style scoped lang="less">
  .tab-bar-container {
    position: relative;
    background-color: var(--color-bg-2);
    .tab-bar-box {
      padding: 0 20px;
      background-color: var(--color-bg-2);
      border-bottom: 1px solid var(--color-border);
      .tab-bar-scroll {
        height: 46px;
        flex: 1;
        overflow: hidden;
        .tags-wrap {
          padding: 8px 0;
          height: 48px;
          white-space: nowrap;
          overflow-x: auto;
          overflow-y: hidden;
          &::-webkit-scrollbar {
            width: 6px;
            height: 6px;
          }
          &::-webkit-scrollbar-track {
            background: rgb(239, 239, 239);
            border-radius: 2px;
          }
          &::-webkit-scrollbar-thumb{
            background: #ccc;
            border-radius: 1px;
            cursor: pointer;
          }
          &::-webkit-scrollbar-thumb:hover{
            background: #aaa;
          }
          :deep(.arco-tag) {
            display: inline-flex;
            align-items: center;
            margin-right: 8px;
            cursor: pointer;
            &:first-child {
              .arco-tag-close-btn {
                display: none;
              }
            }
          }
        }
      }
    }

    .tag-bar-operation {
      width: 100px;
      height: 32px;
    }
  }
</style>
