<template>
    <my-card :title="$t('resourceMonitor.io.deviceIO')" :bodyPadding="false" skipBodyHeight>
        <el-table
            :data="metricsData.table"
            style="width: 100%"
            border
            :header-cell-class-name="
                () => {
                    return 'grid-header'
                }
            "
            @selection-change="handleSelectionChange"
        >
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column prop="device" label="Device" />
            <el-table-column prop="IO_TPS" label="TPS" />
            <el-table-column prop="IO_RD" label="rd(KB)/s" />
            <el-table-column prop="IO_WT" label="wt(KB)/s" />
            <el-table-column prop="IO_AVGRQ_SZ" label="avgrq-sz (KB)" />
            <el-table-column prop="IO_AVGQU_SZ" label="avgqu-sz" />
            <el-table-column prop="IO_AWAIT" label="await(ms)" />
            <el-table-column prop="IO_UTIL" label="%util" />
        </el-table>
    </my-card>

    <div class="gap-row"></div>

    <el-row :gutter="12">
        <el-col :span="12">
            <my-card :title="'IOPS'" height="300" :bodyPadding="false">
                <template #headerExtend>
                    <div class="card-links">
                        <el-link v-if="isManualRangeSelected" type="primary" @click="gotoSQLDiagnosis()">
                            {{ $t('app.diagnosis') }}
                        </el-link>
                    </div>
                </template>
                <LazyLine
                    :tips="$t('instanceIndex.activeSessionQtyTips')"
                    :rangeSelect="true"
                    :tabId="props.tabId"
                    :formatter="toFixed"
                    :data="deviceIOPS"
                    :xData="metricsData.time"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('resourceMonitor.io.rwSecond')" height="300" :bodyPadding="false">
                <LazyLine
                    :tabId="props.tabId"
                    :formatter="toFixed"
                    :data="deviceRW"
                    :xData="metricsData.time"
                    :unit="'B'"
                />
            </my-card>
        </el-col>
    </el-row>

    <div class="gap-row"></div>

    <el-row :gutter="12">
        <el-col :span="8">
            <my-card :title="$t('resourceMonitor.io.queueLength')" height="300" :bodyPadding="false">
                <LazyLine :tabId="props.tabId" :formatter="toFixed" :data="deviceIOQueue" :xData="metricsData.time" />
            </my-card>
        </el-col>
        <el-col :span="8">
            <my-card :title="$t('resourceMonitor.io.ioUsage')" height="300" :bodyPadding="false">
                <LazyLine
                    :tabId="props.tabId"
                    :formatter="toFixed"
                    :data="deviceIOUsage"
                    :xData="metricsData.time"
                    :max="100"
                    :min="0"
                    :interval="25"
                    :unit="'%'"
                />
            </my-card>
        </el-col>
        <el-col :span="8">
            <my-card :title="$t('resourceMonitor.io.ioTime')" height="300" :bodyPadding="false">
                <LazyLine
                    :tabId="props.tabId"
                    :formatter="toFixed"
                    :data="deviceIOTime"
                    :xData="metricsData.time"
                    :unit="'ms'"
                />
            </my-card>
        </el-col>
    </el-row>
</template>

<script setup lang="ts">
import LazyLine from '@/components/echarts/LazyLine.vue'
import { useMonitorStore } from '@/store/monitor'
import { toFixed } from '@/shared'
import { storeToRefs } from 'pinia'
import { getIOMetrics } from '@/api/observability'
import { useIntervalTime } from '@/hooks/time'
import { tabKeys } from '@/pages/dashboardV2/common'
import { useRequest } from 'vue-request'
import { hasSQLDiagnosisModule } from '@/api/sqlDiagnosis'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

const props = withDefaults(defineProps<{ tabId: string }>(), {})
const { t } = useI18n()

interface LineData {
    name: string
    data: any[]
    [other: string]: any
}
interface MetricsData {
    table: any[]
    iops: LineData[]
    rw: LineData[]
    queueLenth: LineData[]
    ioUse: LineData[]
    ioTime: LineData[]
    time: string[]
}
const metricsData = ref<MetricsData>({
    table: [],
    iops: [],
    rw: [],
    queueLenth: [],
    ioUse: [],
    ioTime: [],
    time: [],
})
const multipleSelection = ref<string[]>([])
const deviceIOPS = computed(() => {
    let baseData = metricsData.value.iops
    if (multipleSelection.value.length <= 0) {
        return baseData
    } else {
        let result = []
        let selectedKeys = []
        for (let index = 0; index < multipleSelection.value.length; index++) {
            const element: any = multipleSelection.value[index]
            selectedKeys.push(element.device)
        }
        for (let index = 0; index < baseData.length; index++) {
            const element: any = baseData[index]
            if (selectedKeys.indexOf(element.key) >= 0) {
                result.push(element)
            }
        }
        return result
    }
})
const deviceRW = computed(() => {
    let baseData = metricsData.value.rw
    if (multipleSelection.value.length <= 0) {
        return baseData
    } else {
        let result = []
        let selectedKeys = []
        for (let index = 0; index < multipleSelection.value.length; index++) {
            const element: any = multipleSelection.value[index]
            selectedKeys.push(element.device)
        }
        for (let index = 0; index < baseData.length; index++) {
            const element: any = baseData[index]
            if (selectedKeys.indexOf(element.key) >= 0) {
                result.push(element)
            }
        }
        return result
    }
})
const deviceIOUsage = computed(() => {
    let baseData = metricsData.value.ioUse
    if (multipleSelection.value.length <= 0) {
        return baseData
    } else {
        let result = []
        let selectedKeys = []
        for (let index = 0; index < multipleSelection.value.length; index++) {
            const element: any = multipleSelection.value[index]
            selectedKeys.push(element.device)
        }
        for (let index = 0; index < baseData.length; index++) {
            const element: any = baseData[index]
            if (selectedKeys.indexOf(element.key) >= 0) {
                result.push(element)
            }
        }
        return result
    }
})
const deviceIOQueue = computed(() => {
    let baseData = metricsData.value.queueLenth
    if (multipleSelection.value.length <= 0) {
        return baseData
    } else {
        let result = []
        let selectedKeys = []
        for (let index = 0; index < multipleSelection.value.length; index++) {
            const element: any = multipleSelection.value[index]
            selectedKeys.push(element.device)
        }
        for (let index = 0; index < baseData.length; index++) {
            const element: any = baseData[index]
            if (selectedKeys.indexOf(element.key) >= 0) {
                result.push(element)
            }
        }
        return result
    }
})
const deviceIOTime = computed(() => {
    let baseData = metricsData.value.ioTime
    if (multipleSelection.value.length <= 0) {
        return baseData
    } else {
        let result = []
        let selectedKeys = []
        for (let index = 0; index < multipleSelection.value.length; index++) {
            const element: any = multipleSelection.value[index]
            selectedKeys.push(element.device)
        }
        for (let index = 0; index < baseData.length; index++) {
            const element: any = baseData[index]
            if (selectedKeys.indexOf(element.key) >= 0) {
                result.push(element)
            }
        }
        return result
    }
})

const { updateCounter, sourceType, autoRefreshTime, tabNow, instanceId, isManualRangeSelected, timeRange } =
    storeToRefs(useMonitorStore(props.tabId))

// same for every page in index
const timer = ref<number>()
onMounted(() => {
    load()
})
watch(
    updateCounter,
    () => {
        clearInterval(timer.value)
        if (tabNow.value === tabKeys.ResourceMonitorIO) {
            if (updateCounter.value.source === sourceType.value.INSTANCE) load()
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
        }
    },
    { immediate: false }
)

// load data
const load = (checkTab?: boolean, checkRange?: boolean) => {
    if (!instanceId.value) return
    requestData(props.tabId)
}
const { data: indexData, run: requestData } = useRequest(getIOMetrics, { manual: true })
watch(
    indexData,
    () => {
        // clear data
        metricsData.value.iops = []
        metricsData.value.rw = []
        metricsData.value.queueLenth = []
        metricsData.value.ioUse = []
        metricsData.value.ioTime = []

        const baseData = indexData.value
        if (!baseData) return

        // table
        for (let index = 0; index < baseData.table.length; index++) {
            const element = baseData.table[index]
            element.IO_TPS = toFixed(element.IO_TPS)
            element.IO_RD = toFixed(element.IO_RD)
            element.IO_WT = toFixed(element.IO_WT)
            element.IO_AVGRQ_SZ = toFixed(element.IO_AVGRQ_SZ)
            element.IO_AVGQU_SZ = toFixed(element.IO_AVGQU_SZ)
            element.IO_AWAIT = toFixed(element.IO_AWAIT)
            element.IO_UTIL = toFixed(element.IO_UTIL)
        }
        metricsData.value.table = baseData.table

        // IOPS
        for (let key in baseData.IOPS_R) {
            let tempData: string[] = []
            baseData.IOPS_R[key].forEach((element) => {
                tempData.push(toFixed(element))
            })
            metricsData.value.iops.push({ data: tempData, name: key + '(读)', key })
        }
        for (let key in baseData.IOPS_W) {
            let tempData: string[] = []
            baseData.IOPS_W[key].forEach((element) => {
                tempData.push(toFixed(element))
            })
            metricsData.value.iops.push({ data: tempData, name: key + '(写)', key })
        }

        // rw byte
        for (let key in baseData.IO_DISK_READ_BYTES_PER_SECOND) {
            let tempData: string[] = []
            baseData.IO_DISK_READ_BYTES_PER_SECOND[key].forEach((element) => {
                tempData.push(toFixed(element))
            })
            metricsData.value.rw.push({ data: tempData, name: key + '(读)', key })
        }
        for (let key in baseData.IO_DISK_WRITE_BYTES_PER_SECOND) {
            let tempData: string[] = []
            baseData.IO_DISK_WRITE_BYTES_PER_SECOND[key].forEach((element) => {
                tempData.push(toFixed(element))
            })
            metricsData.value.rw.push({ data: tempData, name: key + '(写)', key })
        }

        // queue
        for (let key in baseData.IO_QUEUE_LENGTH) {
            let tempData: string[] = []
            baseData.IO_QUEUE_LENGTH[key].forEach((element) => {
                tempData.push(toFixed(element))
            })
            metricsData.value.queueLenth.push({ data: tempData, name: key, key })
        }

        // io use
        for (let key in baseData.IO_UTIL) {
            let tempData: string[] = []
            baseData.IO_UTIL[key].forEach((element) => {
                tempData.push(toFixed(element))
            })
            metricsData.value.ioUse.push({ data: tempData, name: key, key })
        }

        // io time
        for (let key in baseData.IO_AVG_REPONSE_TIME_READ) {
            let tempData: string[] = []
            baseData.IO_AVG_REPONSE_TIME_READ[key].forEach((element) => {
                tempData.push(toFixed(element))
            })
            metricsData.value.ioTime.push({ data: tempData, name: key + '(读)', key })
        }
        for (let key in baseData.IO_AVG_REPONSE_TIME_WRITE) {
            let tempData: string[] = []
            baseData.IO_AVG_REPONSE_TIME_WRITE[key].forEach((element) => {
                tempData.push(toFixed(element))
            })
            metricsData.value.ioTime.push({ data: tempData, name: key + '(写)', key })
        }
        for (let key in baseData.IO_AVG_REPONSE_TIME_RW) {
            let tempData: string[] = []
            baseData.IO_AVG_REPONSE_TIME_RW[key].forEach((element) => {
                tempData.push(toFixed(element))
            })
            metricsData.value.ioTime.push({ data: tempData, name: key + '(读+写)', key })
        }

        metricsData.value.time = baseData.time
    },
    { deep: true }
)
const handleSelectionChange = (val: any) => {
    multipleSelection.value = val
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

<style scoped lang="scss"></style>
