<template>
  <a-dropdown trigger="contextMenu" @select="actionSelect">
    <span
      class="arco-tag arco-tag-size-medium arco-tag-checked"
      :class="{ 'link-activated': itemData.fullPath === route.fullPath }"
      @click="goto(itemData)"
    >
      <span class="tag-link">
        {{ itemData.title }}
      </span>
      <span
        class="arco-icon-hover arco-tag-icon-hover arco-icon-hover-size-medium arco-tag-close-btn"
        @click.stop="tagClose(itemData, index)"
      >
        <icon-close />
      </span>
    </span>
    <template #content>
      <a-doption :disabled="disabledCurrent" :value="Eaction.current">
        <icon-close />
        <span>{{ $t('tab-bar.tab-item.5m6ntgxo4tg0') }}</span>
      </a-doption>
      <a-doption :disabled="disabledLeft" :value="Eaction.left">
        <icon-to-left />
        <span>{{ $t('tab-bar.tab-item.5m6ntgxo9vc0') }}</span>
      </a-doption>
      <a-doption :disabled="disabledRight" :value="Eaction.right">
        <icon-to-right />
        <span>{{ $t('tab-bar.tab-item.5m6ntgxoa240') }}</span>
      </a-doption>
      <a-doption :value="Eaction.others">
        <icon-swap />
        <span>{{ $t('tab-bar.tab-item.5m6ntgxoa540') }}</span>
      </a-doption>
      <a-doption :value="Eaction.all">
        <icon-folder-delete />
        <span>{{ $t('tab-bar.tab-item.5m6ntgxoadc0') }}</span>
      </a-doption>
    </template>
  </a-dropdown>
</template>

<script lang="ts" setup>
import { PropType, computed, toRaw } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTabBarStore } from '@/store'
import type { TagProps } from '@/store/modules/tab-bar/types'
import { DEFAULT_ROUTE_NAME } from '@/router/routes/index'
import { destroyPluginApp } from '@/utils/pluginApp'
import { Message } from '@arco-design/web-vue'
import { KeyValue } from '@/types/global'

// eslint-disable-next-line no-shadow
enum Eaction {
  current = 'current',
  left = 'left',
  right = 'right',
  others = 'others',
  all = 'all'
}

const props = defineProps({
  itemData: {
    type: Object as PropType<TagProps>,
    default () {
      return []
    }
  },
  index: {
    type: Number,
    default: 0
  }
})

const router = useRouter()
const route = useRoute()
const tabBarStore = useTabBarStore()

const goto = (tag: TagProps) => {
  router.push({ path: tag.fullPath, query: tag.query })
}
const tagList = computed(() => {
  return tabBarStore.getTabList
})

const disabledCurrent = computed(() => {
  return props.index === 0
})

const disabledLeft = computed(() => {
  return [0, 1].includes(props.index)
})

const disabledRight = computed(() => {
  return props.index === tagList.value.length - 1
})

const tagClose = (tag: TagProps, idx: number) => {
  if (localStorage.getItem(tag.name)) {
    Message.warning('Performing execution, do not close')
    return
  }
  tabBarStore.deleteTag(idx, tag)
  destroyPluginApp(tag.fullPath)
  if (props.itemData.fullPath === route.fullPath) {
    const latest = tagList.value[idx - 1] // Get the previous tab of the queue
    router.push({ name: latest.name, query: latest.query })
  }
}

const findCurrentRouteIndex = () => {
  return tagList.value.findIndex((el) => el.fullPath === route.fullPath)
}
const actionSelect = (value: any) => {
  const { itemData, index } = props
  const copyTagList = [...tagList.value]
  if (value === Eaction.current) {
    tagClose(itemData, index)
  } else if (value === Eaction.left) {
    const currentRouteIdx = findCurrentRouteIndex()
    const removeTagList = copyTagList.splice(1, props.index - 1)
    if (hasSpecialTag(removeTagList)) {
      return
    }
    tabBarStore.freshTabList(copyTagList)
    if (currentRouteIdx < index) {
      router.push({ path: itemData.fullPath, query: itemData.query })
    }
  } else if (value === Eaction.right) {
    const currentRouteIdx = findCurrentRouteIndex()
    const removeTagList = copyTagList.splice(props.index + 1)
    if (hasSpecialTag(removeTagList)) {
      return
    }
    tabBarStore.freshTabList(copyTagList)
    if (currentRouteIdx > index) {
      router.push({ path: itemData.fullPath, query: itemData.query })
    }
  } else if (value === Eaction.others) {
    const filterList = tagList.value.filter((el, idx) => {
      return idx === 0 || idx === props.index
    })
    const removeTagList = tagList.value.filter((el, idx) => {
      return idx !== 0 && idx !== props.index
    })
    if (hasSpecialTag(removeTagList)) {
      return
    }
    tabBarStore.freshTabList(filterList)
    router.push({ path: itemData.fullPath, query: itemData.query })
  } else {
    const removeTagList = tagList.value.filter((el, idx) => {
      return idx !== 0
    })
    if (hasSpecialTag(removeTagList)) {
      return
    }
    tabBarStore.resetTabList()
    router.push({ name: DEFAULT_ROUTE_NAME })
  }
}
const hasSpecialTag = (removeTags: TagProps[]) => {
  const specialTag = removeTags.filter((item: KeyValue) => {
    return localStorage.getItem(toRaw(item).name)
  })
  if (specialTag && specialTag.length) {
    Message.warning('Performing installation, do not close')
    return true
  }
  return false
}
</script>

<style scoped lang="less">
.arco-tag-size-medium {
  height: 30px;
  padding: 0 10px;
  font-size: 14px;
}

.tag-link {
  color: var(--color-text-2);
  text-decoration: none;
}

.link-activated {
  color: rgb(var(--link-6));

  .tag-link {
    color: rgb(var(--link-6));
  }

  & + .arco-tag-close-btn {
    color: rgb(var(--link-6));
  }
}

:deep(.arco-dropdown-option-content) {
  span {
    margin-left: 10px;
  }
}

.arco-dropdown-open {
  .tag-link {
    color: rgb(var(--danger-6));
  }

  .arco-tag-close-btn {
    color: rgb(var(--danger-6));
  }
}
</style>
