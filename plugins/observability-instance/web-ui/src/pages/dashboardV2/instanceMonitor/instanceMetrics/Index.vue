<template>
    <IndexBar :tabId="props.tabId"></IndexBar>
    <div style="margin-bottom: 38px"></div>
    <el-row :gutter="12">
        <el-col :span="12">
            <my-card :title="'TPS'" height="300" :bodyPadding="false">
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
                    :data="metricsData.tps"
                    :xData="metricsData.time"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="'QPS'" height="300" :bodyPadding="false">
                <LazyLine :tabId="props.tabId" :formatter="toFixed" :data="metricsData.qps" :xData="metricsData.time" />
            </my-card>
        </el-col>
    </el-row>

    <div class="gap-row"></div>

    <el-row :gutter="12">
        <el-col :span="12">
            <my-card :title="$t('instanceMonitor.instance.connectionQty')" height="300" :bodyPadding="false">
                <LazyLine
                    :tabId="props.tabId"
                    :formatter="toFixed"
                    :data="metricsData.connection"
                    :xData="metricsData.time"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('instanceMonitor.instance.slowSQL3s')" height="300" :bodyPadding="false">
                <LazyLine
                    :tabId="props.tabId"
                    :formatter="toFixed"
                    :data="metricsData.slowSQL"
                    :xData="metricsData.time"
                />
            </my-card>
        </el-col>
    </el-row>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import LazyLine from '@/components/echarts/LazyLine.vue'
import { useMonitorStore } from '@/store/monitor'
import { toFixed } from '@/shared'
import { storeToRefs } from 'pinia'
import { getInstanceMetrics } from '@/api/observability'
import { useIntervalTime } from '@/hooks/time'
import { tabKeys } from '@/pages/dashboardV2/common'
import { useRequest } from 'vue-request'
import { ElMessage } from 'element-plus'
import { hasSQLDiagnosisModule } from '@/api/sqlDiagnosis'

const { t } = useI18n()

const props = withDefaults(defineProps<{ tabId: string }>(), {})

interface LineData {
    name: string
    data: any[]
    [other: string]: any
}
interface MetricsData {
    tps: LineData[]
    qps: LineData[]
    connection: LineData[]
    slowSQL: LineData[]
    time: string[]
}
const metricsData = ref<MetricsData>({
    tps: [],
    qps: [],
    connection: [],
    slowSQL: [],
    time: [],
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
        if (tabNow.value === tabKeys.InstanceMonitorInstance) {
            if (updateCounter.value.source === sourceType.value.INSTANCE) {
                load()
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
        }
    },
    { immediate: false }
)

// load data
const load = (checkTab?: boolean, checkRange?: boolean) => {
    if (!instanceId.value) return
    requestData(props.tabId)
}
const { data: indexData, run: requestData } = useRequest(getInstanceMetrics, { manual: true })
watch(
    indexData,
    () => {
        // clear data
        metricsData.value.tps = []
        metricsData.value.qps = []
        metricsData.value.connection = []
        metricsData.value.slowSQL = []

        const baseData = indexData.value
        if (!baseData) return

        // TPS
        if (baseData.INSTANCE_TPS_ROLLBACK) {
            let tempData: string[] = []
            baseData.INSTANCE_TPS_ROLLBACK.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.tps.push({ data: tempData, name: t('instanceMonitor.instance.rollbackQty') })
        }
        if (baseData.INSTANCE_TPS_COMMIT) {
            let tempData: string[] = []
            baseData.INSTANCE_TPS_COMMIT.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.tps.push({ data: tempData, name: t('instanceMonitor.instance.commitQty') })
        }
        if (baseData.INSTANCE_TPS_CR) {
            let tempData: string[] = []
            baseData.INSTANCE_TPS_CR.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.tps.push({ data: tempData, name: t('instanceMonitor.instance.transTotalQty') })
        }

        // QPS
        if (baseData.INSTANCE_QPS) {
            let tempData: string[] = []
            baseData.INSTANCE_QPS.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.qps.push({ data: tempData, name: t('instanceMonitor.instance.queryQty') })
        }

        // connections
        if (baseData.INSTANCE_DB_CONNECTION_ACTIVE) {
            let tempData: string[] = []
            baseData.INSTANCE_DB_CONNECTION_ACTIVE.forEach((d: number) => {
                tempData.push(d.toString())
            })
            metricsData.value.connection.push({
                data: tempData,
                areaStyle: {},
                stack: 'Total',
                name: t('instanceMonitor.instance.idleConnectionQty'),
            })
        }
        if (baseData.INSTANCE_DB_CONNECTION_IDLE) {
            let tempData: string[] = []
            baseData.INSTANCE_DB_CONNECTION_IDLE.forEach((d: number) => {
                tempData.push(d.toString())
            })
            metricsData.value.connection.push({
                data: tempData,
                areaStyle: {},
                stack: 'Total',
                name: t('instanceMonitor.instance.activeConnectionQty'),
            })
        }
        if (baseData.INSTANCE_DB_CONNECTION_CURR) {
            let tempData: string[] = []
            baseData.INSTANCE_DB_CONNECTION_CURR.forEach((d: number) => {
                tempData.push(d.toString())
            })
            metricsData.value.connection.push({ data: tempData, name: t('instanceMonitor.instance.connectionQtyNow') })
        }
        if (baseData.INSTANCE_DB_CONNECTION_TOTAL) {
            let tempData: string[] = []
            baseData.INSTANCE_DB_CONNECTION_TOTAL.forEach((d: number) => {
                tempData.push(d.toString())
            })
            metricsData.value.connection.push({ data: tempData, name: t('instanceMonitor.instance.maxConnectionQty') })
        }

        // slow SQL
        if (baseData.INSTANCE_DB_SLOWSQL) {
            let tempData: string[] = []
            baseData.INSTANCE_DB_SLOWSQL.forEach((d: number) => {
                tempData.push(d.toString())
            })
            metricsData.value.slowSQL.push({ data: tempData, name: t('instanceMonitor.instance.slowSQLQty') })
        }

        // time
        metricsData.value.time = baseData.time
    },
    { deep: true }
)
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
