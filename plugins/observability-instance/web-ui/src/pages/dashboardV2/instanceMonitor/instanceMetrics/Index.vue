<template>
  <IndexBar :tabId="props.tabId"></IndexBar>
  <el-tabs v-model="instanceMonitorTabIndex" type="card" class="card-tabs">
      <el-tab-pane :label="t('instanceIndex.instanceInfo')" :name="tabKeys.InstanceMonitorInstanceInfo">
        <InstanceInfo
          :tabId="props.tabId"
          v-if="
            tabKeyLoaded.indexOf(tabKeys.InstanceMonitorInstanceInfo) >= 0 ||
            instanceMonitorTabIndex === tabKeys.InstanceMonitorInstanceInfo
          "
        >
        </InstanceInfo>
      </el-tab-pane>
      <el-tab-pane :label="t('instanceIndex.instanceOverload')" :name="tabKeys.InstanceMonitorInstanceOverload">
        <InstanceOverload
          :tabId="props.tabId"
          v-if="
            tabKeyLoaded.indexOf(tabKeys.InstanceMonitorInstanceOverload) >= 0 ||
            instanceMonitorTabIndex === tabKeys.InstanceMonitorInstanceOverload
          "
        >
        </InstanceOverload>
      </el-tab-pane>
      <el-tab-pane :label="t('instanceIndex.instanceTablespace')" :name="tabKeys.InstanceMonitorInstanceTablespace">
        <instanceTablespace
          :tabId="props.tabId"
          v-if="
            tabKeyLoaded.indexOf(tabKeys.InstanceMonitorInstanceTablespace) >= 0 ||
            instanceMonitorTabIndex === tabKeys.InstanceMonitorInstanceTablespace
          "
        >
        </instanceTablespace>
      </el-tab-pane>
    </el-tabs>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import InstanceOverload from '@/pages/dashboardV2/instanceMonitor/instanceMetrics/instanceOverload/Index.vue'
import InstanceInfo from '@/pages/dashboardV2/instanceMonitor/instanceMetrics/instanceInfo/Index.vue'
import instanceTablespace from '@/pages/dashboardV2/instanceMonitor/instanceMetrics/instanceTablespace/Index.vue'
import { useMonitorStore } from '@/store/monitor'
import { tabKeys } from '@/pages/dashboardV2/common'
import { ref } from 'vue'
import { storeToRefs } from 'pinia'

const { t } = useI18n();

const props = withDefaults(defineProps<{ tabId: string }>(), {});
const { updateCounter, tabNow } = storeToRefs(useMonitorStore(props.tabId))

const tabKeyLoaded = ref<Array<string>>([])
const instanceMonitorTabIndex = ref<string>('')
// tab render only once, prevent the echarts turns into a little one
watch(instanceMonitorTabIndex, (v) => {
  if (tabKeyLoaded.value.indexOf(v) < 0) tabKeyLoaded.value.push(v)
})

const setNowTab = () => {
  useMonitorStore(props.tabId).updateTabNow(instanceMonitorTabIndex.value)
}

onMounted(() => {
  instanceMonitorTabIndex.value = tabKeys.InstanceMonitorInstanceInfo
})
watch(instanceMonitorTabIndex, (v) => {
  setNowTab()
})

watch(
  updateCounter,
  () => {
    if (tabNow.value === tabKeys.InstanceMonitorInstance) {
      setNowTab()
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss"></style>
