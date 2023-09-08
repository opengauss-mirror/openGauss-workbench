<template>
  <div style="margin-top: 6px">
    <IndexBar :tabId="props.tabId"></IndexBar>
    <el-tabs v-model="resourceMonitorTabIndex" type="card" class="card-tabs">
      <el-tab-pane label="CPU" :name="tabKeys.ResourceMonitorCPU">
        <CPU
          :tabId="props.tabId"
          v-if="
            tabKeyLoaded.indexOf(tabKeys.ResourceMonitorCPU) >= 0 ||
            resourceMonitorTabIndex === tabKeys.ResourceMonitorCPU
          "
          @goto="goto"
        >
        </CPU>
      </el-tab-pane>
      <el-tab-pane :label="$t('resourceMonitor.memoryTab')" :name="tabKeys.ResourceMonitorMemory">
        <Memory
          @changeCluster="toChangeCluster"
          :tabId="props.tabId"
          v-if="
            tabKeyLoaded.indexOf(tabKeys.ResourceMonitorMemory) >= 0 ||
            resourceMonitorTabIndex === tabKeys.ResourceMonitorMemory
          "
          @goto="goto"
        >
        </Memory>
      </el-tab-pane>
      <el-tab-pane :label="$t('resourceMonitor.ioTab')" :name="tabKeys.ResourceMonitorIO">
        <IO
          :tabId="props.tabId"
          v-if="
            tabKeyLoaded.indexOf(tabKeys.ResourceMonitorIO) >= 0 ||
            resourceMonitorTabIndex === tabKeys.ResourceMonitorIO
          "
          @goto="goto"
        >
        </IO>
      </el-tab-pane>
      <el-tab-pane :label="$t('resourceMonitor.networkTab')" :name="tabKeys.ResourceMonitorNetwork">
        <Network
          :tabId="props.tabId"
          v-if="
            tabKeyLoaded.indexOf(tabKeys.ResourceMonitorNetwork) >= 0 ||
            resourceMonitorTabIndex === tabKeys.ResourceMonitorNetwork
          "
          @goto="goto"
        >
        </Network>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import CPU from '@/pages/dashboardV2/resourceMonitor/CPU/Index.vue'
import Memory from '@/pages/dashboardV2/resourceMonitor/memory/Index.vue'
import IO from '@/pages/dashboardV2/resourceMonitor/IO/Index.vue'
import Network from '@/pages/dashboardV2/resourceMonitor/network/Index.vue'
import { useMonitorStore } from '@/store/monitor'
import { tabKeys } from '@/pages/dashboardV2/common'
import { defineExpose, ref } from 'vue'
import { storeToRefs } from 'pinia'
const props = withDefaults(defineProps<{ tabId: string }>(), {})
const { updateCounter, tabNow } = storeToRefs(useMonitorStore(props.tabId))

const tabKeyLoaded = ref<Array<string>>([])
const resourceMonitorTabIndex = ref<string>('')
// tab render only once, prevent the echarts turns into a little one
watch(resourceMonitorTabIndex, (v) => {
  if (tabKeyLoaded.value.indexOf(v) < 0) tabKeyLoaded.value.push(v)
})

const setNowTab = () => {
  useMonitorStore(props.tabId).updateTabNow(resourceMonitorTabIndex.value)
}
const outsideGoto = (key: string) => {
  resourceMonitorTabIndex.value = key
}
defineExpose({ outsideGoto })
onMounted(() => {
  resourceMonitorTabIndex.value = tabKeys.ResourceMonitorCPU
})
watch(resourceMonitorTabIndex, (v) => {
  setNowTab()
})
const emit = defineEmits(['goto', 'changeCluster'])
const goto = (key: string, param: object) => {
  emit('goto', key, param)
}
const toChangeCluster = (publicIp: string, port: string) => {
  emit('changeCluster', publicIp, port)
}

// same for every page in index
watch(
  updateCounter,
  () => {
    if (tabNow.value === tabKeys.ResourceMonitor) {
      setNowTab()
    }
  },
  { immediate: true }
)
// same for every page in index
</script>

<style scoped lang="scss"></style>
