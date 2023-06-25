<template>
    <div>
        <IndexBar :tabId="props.tabId"></IndexBar>

        <el-tabs v-model="instanceMonitorTabIndex" type="card" class="card-tabs">
            <el-tab-pane label="实例指标" :name="tabKeys.InstanceMonitorInstance">
                <InstanceMetrics :tabId="props.tabId"></InstanceMetrics>
            </el-tab-pane>
            <el-tab-pane label="会话监控" :name="tabKeys.InstanceMonitorSession">
                <SessionMonitor :tabId="props.tabId"></SessionMonitor>
            </el-tab-pane>
            <el-tab-pane label="TOP SQL" :name="tabKeys.InstanceMonitorTOPSQL">
                <TOPSQL ref="refTopSQL" :tabId="props.tabId"></TOPSQL>
            </el-tab-pane>
        </el-tabs>
    </div>

    <div style="margin-bottom: 20px"></div>
</template>

<script setup lang="ts">
import { i18n } from '@/i18n'
import { useI18n } from 'vue-i18n'
import InstanceMetrics from '@/pages/dashboardV2/instanceMonitor/instanceMetrics/Index.vue'
import SessionMonitor from '@/pages/dashboardV2/instanceMonitor/sessionMonitor/Index.vue'
import TOPSQL from '@/pages/dashboardV2/instanceMonitor/topSQL/Index.vue'
import { useMonitorStore } from '@/store/monitor'
import { tabKeys } from '@/pages/dashboardV2/common'
import { storeToRefs } from 'pinia'
const props = withDefaults(defineProps<{ tabId: string }>(), {})
const { updateCounter, tabNow } = storeToRefs(useMonitorStore(props.tabId))
const refTopSQL = ref<InstanceType<typeof TOPSQL>>()

const { t } = useI18n()

const instanceMonitorTabIndex = ref<string>('')

const setNowTab = () => {
    useMonitorStore(props.tabId).updateTabNow(instanceMonitorTabIndex.value)
}
const outsideGoto = (key: string, param: object) => {
    instanceMonitorTabIndex.value = key
    refTopSQL.value!.outsideGoto(key, param)
}
defineExpose({ outsideGoto })
onMounted(() => {
    instanceMonitorTabIndex.value = tabKeys.InstanceMonitorInstance
})
watch(instanceMonitorTabIndex, (v) => {
    setNowTab()
})

// same for every page in index
watch(
    updateCounter,
    () => {
        if (tabNow.value === tabKeys.InstanceMonitor) {
            setNowTab()
        }
    },
    { immediate: true }
)
// same for every page in index
</script>

<style scoped lang="scss"></style>
