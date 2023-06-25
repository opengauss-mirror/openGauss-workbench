<template>
    <el-row :gutter="12">
        <el-col :span="12">
            <my-card :title="$t('resourceMonitor.memory.memoryUse')" height="377" :bodyPadding="false">
                <template #headerExtend>
                    <div class="card-links">
                        <el-link v-if="isManualRangeSelected" type="primary" @click="gotoSQLDiagnosis()">
                            {{ $t('app.diagnosis') }}
                        </el-link>
                    </div>
                </template>
                <div style="height: 257px">
                    <LazyLine
                        :tips="$t('instanceIndex.activeSessionQtyTips')"
                        :tabId="props.tabId"
                        :formatter="toFixed"
                        :data="metricsData.memoryUsed"
                        :xData="metricsData.time"
                        :max="100"
                        :min="0"
                        :interval="25"
                        :unit="'%'"
                        :rangeSelect="true"
                    />
                </div>
                <div class="table-in-card">
                    <el-table
                        :data="metricsData.memoryInfo"
                        style="width: 100%"
                        :border="true"
                        :header-cell-class-name="
                            () => {
                                return 'grid-header-in-card'
                            }
                        "
                    >
                        <el-table-column prop="MEM_TOTAL" :label="$t('resourceMonitor.memory.physicalmemory')" />
                        <el-table-column prop="MEM_USED" :label="$t('resourceMonitor.memory.usedMemory')" />
                        <el-table-column prop="MEM_FREE" :label="$t('resourceMonitor.memory.freeMemory')" />
                        <el-table-column prop="MEM_CACHE" :label="$t('resourceMonitor.memory.cachedMemory')" />
                    </el-table>
                </div>
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('resourceMonitor.memory.interactiveAreaUsage')" height="377" :bodyPadding="false">
                <div style="height: 257px">
                    <LazyLine
                        :tabId="props.tabId"
                        :formatter="toFixed"
                        :data="metricsData.swap"
                        :xData="metricsData.time"
                        :max="100"
                        :min="0"
                        :interval="25"
                        :unit="'%'"
                    />
                </div>
                <div class="table-in-card">
                    <el-table
                        :data="metricsData.swapInfo"
                        style="width: 100%"
                        :border="true"
                        :header-cell-class-name="
                            () => {
                                return 'grid-header-in-card'
                            }
                        "
                    >
                        <el-table-column prop="SWAP_TOTAL" :label="$t('resourceMonitor.memory.totalExchangeArea')" />
                        <el-table-column prop="SWAP_USED" :label="$t('resourceMonitor.memory.ysedSwapArea')" />
                        <el-table-column prop="SWAP_FREE" :label="$t('resourceMonitor.memory.freeMemory')" />
                    </el-table>
                </div>
            </my-card>
        </el-col>
    </el-row>

    <div style="position: relative">
        <div
            style="
                position: absolute;
                right: 0px;
                top: 7px;
                z-index: 1;
                display: flex;
                flex-direction: row;
                align-items: center;
                font-size: 12px;
            "
        >
            <div style="margin-right: 12px">{{ $t('app.refreshOn') }} {{ innerRefreshDoneTime }}</div>
            <el-button
                class="refresh-button"
                type="primary"
                :icon="Refresh"
                style="margin-right: 8px"
                @click="loadTOPMemoryProcessNow(props.tabId)"
            />
            <div>{{ $t('app.autoRefreshFor') }}</div>
            <el-select v-model="innerRefreshTime" style="width: 60px; margin: 0 4px" @change="updateTimerInner">
                <el-option :value="1" label="1s" />
                <el-option :value="15" label="15s" />
                <el-option :value="30" label="30s" />
                <el-option :value="60" label="60s" />
            </el-select>
        </div>
        <el-tabs v-model="tab">
            <el-tab-pane :label="$t('resourceMonitor.memory.topProcess')" :name="0">
                <el-table
                    :table-layout="'auto'"
                    :data="topMemoryProcessNowData == null ? [] : topMemoryProcessNowData"
                    style="width: 100%"
                    :border="true"
                    :header-cell-class-name="
                        () => {
                            return 'grid-header'
                        }
                    "
                >
                    <el-table-column prop="%CPU" label="%CPU" width="60" />
                    <el-table-column prop="%MEM" label="%MEM" width="60" />
                    <el-table-column prop="COMMAND" label="COMMAND" />
                    <el-table-column prop="NI" label="NI" width="40" />
                    <el-table-column prop="PID" label="PID" width="60" />
                    <el-table-column prop="PR" label="PR" width="40" />
                    <el-table-column prop="RES" label="RES" width="80" />
                    <el-table-column prop="S" label="S" width="40" />
                    <el-table-column prop="SHR" label="SHR" width="80" />
                    <el-table-column prop="TIME+" label="TIME+" width="100" />
                    <el-table-column prop="USER" label="USER" width="120" />
                    <el-table-column prop="VIRT" label="VIRT" width="120" />
                </el-table>
            </el-tab-pane>
            <el-tab-pane :label="$t('resourceMonitor.memory.topThread')" :name="1">
                <el-table
                    :table-layout="'auto'"
                    :data="topMemoryDBThreadNowData == null ? [] : topMemoryDBThreadNowData"
                    style="width: 100%"
                    :border="true"
                    :header-cell-class-name="
                        () => {
                            return 'grid-header'
                        }
                    "
                >
                    <el-table-column prop="%CPU" label="%CPU" width="60" />
                    <el-table-column prop="%MEM" label="%MEM" width="60" />
                    <el-table-column prop="COMMAND" label="COMMAND" />
                    <el-table-column prop="NI" label="NI" width="40" />
                    <el-table-column prop="PID" label="PID" width="60" />
                    <el-table-column prop="PR" label="PR" width="40" />
                    <el-table-column prop="RES" label="RES" width="80" />
                    <el-table-column prop="S" label="S" width="40" />
                    <el-table-column prop="SHR" label="SHR" width="80" />
                    <el-table-column prop="TIME+" label="TIME+" width="100" />
                    <el-table-column prop="USER" label="USER" width="120" />
                    <el-table-column prop="VIRT" label="VIRT" width="120" />
                </el-table>
            </el-tab-pane>
        </el-tabs>
    </div>

    <div class="gap-row"></div>

    <el-row :gutter="12">
        <el-col :span="12">
            <my-card :title="$t('resourceMonitor.memory.instanceMemoryUsage')" height="380" :bodyPadding="false">
                <el-table
                    :data="metricsData.dbMemoryCondition"
                    style="width: 100%; height: 340px"
                    border
                    :header-cell-class-name="
                        () => {
                            return 'grid-header'
                        }
                    "
                >
                    <el-table-column prop="memorytype" :label="$t('resourceMonitor.memory.memoryName')" />
                    <el-table-column prop="desc" :label="$t('resourceMonitor.memory.description')" />
                    <el-table-column prop="memorymbytes" :label="$t('resourceMonitor.memory.sizeOfMemoryUsed')" />
                </el-table>
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('resourceMonitor.memory.parameterConfiguration')" height="380" :bodyPadding="false">
                <el-table
                    :data="metricsData.dbMemoryConfig"
                    style="width: 100%; height: 340px"
                    border
                    :header-cell-class-name="
                        () => {
                            return 'grid-header'
                        }
                    "
                >
                    <el-table-column prop="name" :label="$t('resourceMonitor.memory.parameterName')" />
                    <el-table-column prop="desc" :label="$t('resourceMonitor.memory.description')" />
                    <el-table-column prop="value" :label="$t('resourceMonitor.memory.settings')" />
                </el-table>
            </my-card>
        </el-col>
    </el-row>
</template>

<!-- eslint-disable camelcase -->
<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import LazyLine from '@/components/echarts/LazyLine.vue'
import { useMonitorStore } from '@/store/monitor'
import { toFixed } from '@/shared'
import { storeToRefs } from 'pinia'
import { useIntervalTime } from '@/hooks/time'
import { tabKeys } from '@/pages/dashboardV2/common'
import { useRequest } from 'vue-request'
import { getMemoryMetrics, getTOPMemoryProcessNow, TopMemoryProcessNow } from '@/api/observability'
import { Refresh } from '@element-plus/icons-vue'
import { hasSQLDiagnosisModule } from '@/api/sqlDiagnosis'
import moment from 'moment'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const props = withDefaults(defineProps<{ tabId: string }>(), {})
const tab = 0

interface LineData {
    name: string
    data: any[]
    [other: string]: any
}
interface MetricsData {
    dbMemoryCondition: any[]
    dbMemoryConfig: any[]
    memoryInfo: any[]
    swapInfo: any[]
    memoryUsed: LineData[]
    swap: LineData[]
    time: string[]
}
const metricsData = ref<MetricsData>({
    dbMemoryCondition: [],
    dbMemoryConfig: [],
    memoryInfo: [],
    swapInfo: [],
    memoryUsed: [],
    swap: [],
    time: [],
})

const topMemoryProcessNowData = ref<void | TopMemoryProcessNow[]>([])
const topMemoryDBThreadNowData = ref<void | TopMemoryProcessNow[]>([])
const innerRefreshTime = ref<number>(30)
const innerRefreshDoneTime = ref<string>('')
const { updateCounter, sourceType, autoRefreshTime, tabNow, instanceId, isManualRangeSelected, timeRange } =
    storeToRefs(useMonitorStore(props.tabId))

// same for every page in index
const timer = ref<number>()
onMounted(() => {
    load()
    loadTOPMemoryProcessNow(props.tabId)
})
watch(
    updateCounter,
    () => {
        clearInterval(timer.value)
        if (tabNow.value === tabKeys.ResourceMonitorMemory) {
            if (updateCounter.value.source === sourceType.value.INSTANCE) {
                load()
                loadTOPMemoryProcessNow(props.tabId)
            }
            if (updateCounter.value.source === sourceType.value.MANUALREFRESH) load()
            if (updateCounter.value.source === sourceType.value.TIMETYPE) load()
            if (updateCounter.value.source === sourceType.value.TIMERANGE) load()
            if (updateCounter.value.source === sourceType.value.TABCHANGE) load()
            const time = autoRefreshTime.value
            timer.value = useIntervalTime(
                () => {
                    load()
                },
                computed(() => time * 1000)
            )
            updateTimerInner()
        }
    },
    { immediate: false }
)

// load data
const load = (checkTab?: boolean, checkRange?: boolean) => {
    if (!instanceId.value) return
    requestData(props.tabId)
}
const { data: indexData, run: requestData } = useRequest(getMemoryMetrics, { manual: true })
watch(
    indexData,
    () => {
        // clear data
        metricsData.value.memoryUsed = []
        metricsData.value.swap = []

        const baseData = indexData.value
        if (!baseData) return

        // info
        metricsData.value.memoryInfo = [
            {
                MEM_CACHE: byteToMB(baseData.MEM_CACHE) + 'MB',
                MEM_FREE: byteToMB(baseData.MEM_FREE) + 'MB',
                MEM_TOTAL: byteToMB(baseData.MEM_TOTAL) + 'MB',
                MEM_USED: byteToMB(baseData.MEM_USED) + 'MB',
            },
        ]
        metricsData.value.swapInfo = [
            {
                SWAP_FREE: byteToMB(baseData.SWAP_FREE) + 'MB',
                SWAP_TOTAL: byteToMB(baseData.SWAP_TOTAL) + 'MB',
                SWAP_USED: byteToMB(baseData.SWAP_USED) + 'MB',
            },
        ]

        // memory
        if (baseData.MEMORY_USED) {
            let tempData: string[] = []
            baseData.MEMORY_USED.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.memoryUsed.push({
                data: tempData,
                areaStyle: {},
                name: t('resourceMonitor.memory.memoryUse'),
            })
        }
        if (baseData.MEMORY_DB_USED) {
            let tempData: string[] = []
            baseData.MEMORY_DB_USED.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.memoryUsed.push({
                data: tempData,
                areaStyle: {},
                name: t('resourceMonitor.memory.memoryDBUse'),
            })
        }

        // swap
        {
            let tempData: string[] = []
            baseData.MEMORY_SWAP.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.swap.push({ data: tempData, areaStyle: {}, stack: 'Total', name: 'swap' })
        }

        // dbMemoryConfig
        metricsData.value.dbMemoryConfig = baseData.memoryConfig

        // dbMemoryConfig
        metricsData.value.dbMemoryCondition = baseData.memoryNodeDetail

        // time
        metricsData.value.time = baseData.time
    },
    { deep: true }
)
const byteToMB = (val: number) => {
    return (val / 1024 / 1024).toFixed(0)
}
const { data: topMemoryProcessNowResult, run: loadTOPMemoryProcessNow } = useRequest(getTOPMemoryProcessNow, {
    manual: true,
})
watch(
    topMemoryProcessNowResult,
    () => {
        topMemoryProcessNowData.value = topMemoryProcessNowResult.value ? topMemoryProcessNowResult.value[0] : []
        topMemoryDBThreadNowData.value = topMemoryProcessNowResult.value ? topMemoryProcessNowResult.value[1] : []
        innerRefreshDoneTime.value = moment(new Date()).format('HH:mm:ss')
    },
    { deep: true }
)
const timerInner = ref<number>()
const updateTimerInner = () => {
    clearInterval(timerInner.value)
    const timeInner = innerRefreshTime.value
    timerInner.value = useIntervalTime(
        () => {
            loadTOPMemoryProcessNow(props.tabId)
        },
        computed(() => timeInner * 1000)
    )
}

const gotoSQLDiagnosis = () => {
    hasSQLDiagnosisModule()
        .then(() => {
            const curMode = localStorage.getItem('INSTANCE_CURRENT_MODE')
            if (curMode === 'wujie') {
                // @ts-ignore plug-in components
                window.$wujie?.props.methods.jump({
                    name: `Static-pluginObservability-sql-diagnosisVemHistoryDiagnosis`,
                    query: {
                        instanceId: instanceId.value,
                        startTime: timeRange.value[0],
                        endTime: timeRange.value[1],
                    },
                })
            } else ElMessage.error(t('app.needSQLDiagnosis'))
        })
        .catch(() => {
            ElMessage.error(t('app.needSQLDiagnosis'))
        })
}
</script>
