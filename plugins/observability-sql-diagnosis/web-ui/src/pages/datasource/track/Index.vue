<script setup lang="ts">
import { Fold, Expand } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useMonitorStore } from '../../../store/monitor'
import TrackSlowLog from './TrackSlowLog.vue'
import TrackTasks from './TrackTasks.vue'
import { i18n } from '../../../i18n'
import Install from '../../datasource/install/Index.vue';

const clusterNodeId = ref()

const { tab, filters, rangeTime, instanceId } = storeToRefs(useMonitorStore())
const tabLoaded = reactive([tab.value === 0, tab.value === 1])
watch(tab, (v) => {
    if (!tabLoaded[v]) {
        tabLoaded[v] = true
    }
})

watch(clusterNodeId, (res) => {
    let curInstanceId = instanceId.value
    if (typeof res === 'string') {
        curInstanceId = res
    } else if (Array.isArray(res) && res.length > 0) {
        curInstanceId = res[res.length - 1]
    }
    instanceId.value = curInstanceId
    useMonitorStore().instanceId = curInstanceId
    useMonitorStore().tab = 0
})

watch(rangeTime, (r) => {
    if (r !== -1) {
        filters.value[tab.value].time = null
    }
})

const isCollapse = ref(true)
const toggleCollapse = () => {
    isCollapse.value = !isCollapse.value
}
</script>

<template>
    <el-container>
        <el-aside :width="isCollapse ? '0px' : '300px'">
            <div style="height: 23px"></div>
            <Install />
        </el-aside>
        <el-main style="position: relative">
            <div>
                <div style="position: absolute; left: 22px; top: 32px; z-index: 9999" @click="toggleCollapse">
                    <el-icon v-if="!isCollapse" size="20px"><Fold /></el-icon>
                    <el-icon v-if="isCollapse" size="20px"><Expand /></el-icon>
                </div>
            </div>
            <div class="tab-wrapper" :key="clusterNodeId" style="height: 100%">
                <el-tabs v-model="tab" type="border-card" style="height: 100%">
                    <el-tab-pane :label="$t('datasource.slowSQL')" :name="0">
                        <track-slow-log v-if="tabLoaded[0] || tab === 0" />
                    </el-tab-pane>
                    <el-tab-pane :label="$t('datasource.sqlTrackTask')" :name="1">
                        <track-tasks v-if="tabLoaded[1] || tab === 1" :instanceId="instanceId" />
                    </el-tab-pane>
                </el-tabs>
            </div>
        </el-main>
    </el-container>
</template>

<style scoped lang="scss">
.el-tabs__header {
    margin-left: 50px !important;
}
.cluster-container {
    height: 40px;
    background-color: #424242;
    padding: 0 16px;
    margin-bottom: 16px;
    display: flex;
    align-items: center;

    :deep(.el-cascader) {
        width: 400px;
    }

    :deep(.el-input__wrapper) {
        background-color: transparent;
        box-shadow: none;
        border: none;
        border-radius: 0;
        font-size: 16px;
        font-weight: 700;
    }
}

.cluster-info {
    display: flex;
    align-items: center;
    font-size: 14px;

    &-light {
        display: inline-block;
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background-color: green;
        margin-right: 8px;
    }
}

.tab-wrapper {
    position: relative;
    &-filter {
        z-index: 10;
        position: absolute;
        padding-right: 16px;
        display: flex;
        align-items: center;
        right: 0;
        top: 0px;
        height: 40px;
        background-color: $og-sub-background-color;
        > div:not(:last-of-type),
        > span,
        > button {
            margin-right: 4px;
        }
    }

    :deep(.el-range-input) {
        background-color: $og-background-color;
    }

    :deep(.el-date-editor--datetimerange) {
        background-color: $og-background-color;
    }
}
.divider {
    height: 30px;
    width: 1px;
    margin: 0 8px !important;
    background-color: #807f80;
}
</style>
