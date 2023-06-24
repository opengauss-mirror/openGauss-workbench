<template>
    <div style="margin-bottom: 40px"></div>
    <IndexBar :tabId="props.tabId"></IndexBar>
    <div class="small-charts">
        <el-row :gutter="12">
            <el-col :span="4">
                <div class="container" :class="[cpuBackGroud]">
                    <div class="title">CPU</div>
                    <div class="value" v-if="metricsData.cpu && metricsData.cpu.length > 0">{{ cpuValue }}%</div>
                    <div class="detail" @click="goto(tabKeys.ResourceMonitorCPU)">
                        <div class="text">
                            <div>{{ $t('instanceIndex.detail') }}</div>
                            <svg-icon class="icon" name="detail" />
                        </div>
                    </div>
                    <div class="chart">
                        <IndexLine
                            :tabId="props.tabId"
                            :formatter="toFixed"
                            :data="metricsData.cpu"
                            :xData="metricsData.time"
                            :max="metricsData.cpu && metricsData.cpu.length > 0 ? 100 : undefined"
                            :min="0"
                            :interval="25"
                            :unit="'%'"
                        />
                    </div>
                </div>
            </el-col>
            <el-col :span="4">
                <div class="container" :class="[memoryBackGroud]">
                    <div class="title">{{ $t('instanceIndex.memory') }}</div>
                    <div class="value" v-if="metricsData.memory && metricsData.memory.length > 0">
                        {{ memoryValue }}%
                    </div>
                    <div class="detail" @click="goto(tabKeys.ResourceMonitorMemory)">
                        <div class="text">
                            <div>{{ $t('instanceIndex.detail') }}</div>
                            <svg-icon class="icon" name="detail" />
                        </div>
                    </div>
                    <div class="chart">
                        <IndexLine
                            :tabId="props.tabId"
                            :formatter="toFixed"
                            :data="metricsData.memory"
                            :xData="metricsData.time"
                            :max="metricsData.memory && metricsData.memory.length > 0 ? 100 : undefined"
                            :min="0"
                            :interval="25"
                            :unit="'%'"
                        />
                    </div>
                </div>
            </el-col>
            <el-col :span="4">
                <div class="container" :class="[networkBackGroud]">
                    <div class="title">{{ $t('instanceIndex.networkInOut') }}</div>
                    <div
                        class="value"
                        style="font-size: 28px"
                        v-if="metricsData.network && metricsData.network.length > 0"
                    >
                        {{ networkValue }} M/s
                    </div>
                    <div class="detail" @click="goto(tabKeys.ResourceMonitorNetwork)">
                        <div class="text">
                            <div>{{ $t('instanceIndex.detail') }}</div>
                            <svg-icon class="icon" name="detail" />
                        </div>
                    </div>
                    <div class="chart">
                        <IndexLine
                            :tabId="props.tabId"
                            :formatter="toFixed"
                            :data="metricsData.network"
                            :xData="metricsData.time"
                            :unit="'M/S'"
                        />
                    </div>
                </div>
            </el-col>
            <el-col :span="4">
                <div class="container" :class="[ioBackGroud]">
                    <div class="title">IO</div>
                    <div class="value" v-if="metricsData.io && metricsData.io.length > 0">{{ ioValue }}%</div>
                    <div class="detail" @click="goto(tabKeys.ResourceMonitorIO)">
                        <div class="text">
                            <div>{{ $t('instanceIndex.detail') }}</div>
                            <svg-icon class="icon" name="detail" />
                        </div>
                    </div>
                    <div class="chart">
                        <IndexLine
                            :tabId="props.tabId"
                            :formatter="toFixed"
                            :data="metricsData.io"
                            :xData="metricsData.time"
                            :max="metricsData.io && metricsData.io.length > 0 ? 100 : undefined"
                            :min="0"
                            :interval="25"
                            :unit="'%'"
                        />
                    </div>
                </div>
            </el-col>
            <el-col :span="4">
                <div class="container" :class="[swapBackGroud]">
                    <div class="title">SWAP</div>
                    <div class="value" v-if="metricsData.swap && metricsData.swap.length > 0">{{ swapValue }}%</div>
                    <div class="detail" @click="goto(tabKeys.ResourceMonitorMemory)">
                        <div class="text">
                            <div>{{ $t('instanceIndex.detail') }}</div>
                            <svg-icon class="icon" name="detail" />
                        </div>
                    </div>
                    <div class="chart">
                        <IndexLine
                            :tabId="props.tabId"
                            :formatter="toFixed"
                            :data="metricsData.swap"
                            :xData="metricsData.time"
                            :max="metricsData.swap && metricsData.swap.length > 0 ? 100 : undefined"
                            :min="0"
                            :interval="25"
                            :unit="'%'"
                        />
                    </div>
                </div>
            </el-col>
            <el-col :span="4">
                <div class="container" :class="[threadPoolBackGroud]">
                    <div class="title">{{ $t('instanceIndex.threadPoolUsed') }}</div>
                    <div class="value" v-if="metricsData.threadPool && metricsData.threadPool.length > 0">
                        {{ threadPooValue }}%
                    </div>
                    <div class="chart">
                        <IndexLine
                            :tabId="props.tabId"
                            :formatter="toFixed"
                            :data="metricsData.threadPool"
                            :xData="metricsData.time"
                            :max="metricsData.threadPool && metricsData.threadPool.length > 0 ? 100 : undefined"
                            :min="0"
                            :interval="25"
                            :unit="'%'"
                        />
                    </div>
                </div>
            </el-col>
        </el-row>
    </div>

    <div style="margin-bottom: 20px"></div>
    <my-card
        :title="$t('instanceIndex.activeSessionQty')"
        height="212"
        :legend="[
            { color: '#0D86E2', name: $t('metric.transactionRollbackNum') },
            { color: '#9CCC65', name: $t('metric.transactionCommitments') },
            { color: '#00C7F9', name: $t('metric.transactionAndRollbackTotal') },
        ]"
        :bodyPadding="false"
    >
        <template #headerExtend>
            <div class="card-links">
                <el-link v-if="isManualRangeSelected" type="primary" @click="goto(tabKeys.InstanceMonitorTOPSQL)">
                    TOP SQL
                </el-link>
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
            :data="metricsData.session"
            :xData="metricsData.time"
        />
    </my-card>

    <div style="position: relative">
        <div
            style="
                position: absolute;
                right: 0px;
                top: 7px;
                z-index: 9;
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
                @click="loadTopSQL(props.tabId)"
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
            <el-tab-pane :label="$t('instanceIndex.nowTOPSQL')" :name="0">
                <el-table
                    :table-layout="'auto'"
                    :data="topSQLData == null ? [] : topSQLData"
                    style="width: 100%"
                    :border="true"
                    :header-cell-class-name="
                        () => {
                            return 'grid-header'
                        }
                    "
                >
                    <el-table-column label="SQLID" width="130" v-if="false">
                        <template #default="scope">
                            <a class="table-link" @click="gotoTopsqlDetail(scope.row.query_id)">
                                {{ scope.row.query_id }}
                            </a>
                        </template>
                    </el-table-column>
                    <el-table-column prop="query_id" label="SQLID" width="130" />
                    <el-table-column prop="datname" :label="$t('instanceIndex.dbName')" width="90" />
                    <el-table-column prop="usename" :label="$t('instanceIndex.userName')" width="96" />
                    <el-table-column prop="application_name" :label="$t('instanceIndex.appName')" width="110" />
                    <el-table-column prop="query_start" :label="$t('instanceIndex.startTime')" width="110">
                        <template #default="scope">
                            {{ moment(scope.query_start).format('MM-DD HH:mm:ss') }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="duration" :label="$t('instanceIndex.costTime')" width="60" />
                    <el-table-column prop="query" label="SQL" />
                    <el-table-column :label="$t('instanceIndex.sessionId')" width="130">
                        <template #default="scope">
                            <el-link type="primary" @click="gotoSessionDetail(scope.row.sessionid)">
                                {{ scope.row.sessionid === '0' ? '' : scope.row.sessionid }}
                            </el-link>
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>
            <el-tab-pane label="&nbsp;" :name="1" disabled style="cursor: auto !important"> </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script setup lang="ts">
import IndexLine from '@/components/echarts/IndexLine.vue'
import LazyLine from '@/components/echarts/LazyLine.vue'
import { useMonitorStore } from '@/store/monitor'
import { toFixed } from '@/shared'
import { storeToRefs } from 'pinia'
import { useIntervalTime } from '@/hooks/time'
import { getIndexMetrics, getTOPSQLNow, TopSQLNow } from '@/api/observability'
import { hasSQLDiagnosisModule } from '@/api/sqlDiagnosis'
import { useRequest } from 'vue-request'
import { tabKeys } from '@/pages/dashboardV2/common'
import moment from 'moment'
import router from '@/router'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const props = withDefaults(defineProps<{ tabId: string }>(), {})
const { updateCounter, sourceType, autoRefreshTime, tabNow, instanceId, isManualRangeSelected, timeRange } =
    storeToRefs(useMonitorStore(props.tabId))
const { t } = useI18n()

const tab = 0

interface LineData {
    name: string
    data: any[]
    [other: string]: any
}
interface MetricsData {
    cpu: LineData[]
    memory: LineData[]
    io: LineData[]
    network: LineData[]
    swap: LineData[]
    session: LineData[]
    threadPool: LineData[]
    time: string[]
}
const metricsData = ref<MetricsData>({
    cpu: [],
    memory: [],
    io: [],
    network: [],
    swap: [],
    session: [],
    threadPool: [],
    time: ['0'],
})
const topSQLData = ref<void | TopSQLNow[]>([])
const innerRefreshTime = ref<number>(30)
const innerRefreshDoneTime = ref<string>('')
const cpuValue = computed(() => {
    return getMetricsValue(metricsData.value.cpu)
})
const memoryValue = computed(() => {
    return getMetricsValue(metricsData.value.memory)
})
const ioValue = computed(() => {
    return getMetricsValue(metricsData.value.io)
})
const networkValue = computed(() => {
    return getMetricsValue(metricsData.value.network, 0) + '/' + getMetricsValue(metricsData.value.network, 1)
})
const swapValue = computed(() => {
    return getMetricsValue(metricsData.value.swap)
})
const threadPooValue = computed(() => {
    return getMetricsValue(metricsData.value.threadPool)
})
const getMetricsValue = (data: LineData[], index: number = 0) => {
    let value = 0
    if (data && data.length > 0) {
        if (data[index].data && data[index].data.length > 0) {
            value = data[index].data[data[index].data.length - 1]
        }
    }
    return value
}
const cpuBackGroud = computed(() => {
    return getBackground(metricsData.value.cpu)
})
const memoryBackGroud = computed(() => {
    return getBackground(metricsData.value.memory)
})
const networkBackGroud = computed(() => {
    if (metricsData.value.network.length <= 0) return 'none'
    return 'normal'
})
const swapBackGroud = computed(() => {
    return getBackground(metricsData.value.swap)
})
const ioBackGroud = computed(() => {
    return getBackground(metricsData.value.io)
})
const threadPoolBackGroud = computed(() => {
    return getBackground(metricsData.value.threadPool)
})
const getBackground = (data: LineData[]) => {
    if (data.length <= 0) return 'none'
    else if (getMetricsValue(data) < 50) return 'cold'
    else if (getMetricsValue(data) < 75) return 'normal'
    else return 'hot'
}
// get data
const load = (checkTab?: boolean, checkRange?: boolean) => {
    if (!instanceId.value) return
    requestData(props.tabId)
}
const { data: indexData, run: requestData } = useRequest(getIndexMetrics, { manual: true })
watch(
    indexData,
    () => {
        // clear data
        metricsData.value.cpu = []
        metricsData.value.memory = []
        metricsData.value.network = []
        metricsData.value.io = []
        metricsData.value.swap = []
        metricsData.value.session = []
        metricsData.value.threadPool = []
        metricsData.value.time = ['0']

        const baseData = indexData.value
        if (!baseData) return

        // CPU
        if (baseData.CPU && baseData.CPU.length > 0) {
            let tempData: string[] = []
            baseData.CPU.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.cpu.push({ data: tempData, name: 'CPU' })
        }
        // MEMORY
        if (baseData.MEMORY && baseData.MEMORY.length > 0) {
            let tempData: string[] = []
            baseData.MEMORY.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.memory.push({ data: tempData, name: 'MEMORY' })
        }
        // Network
        if (baseData.NETWORK_IN_TOTAL && baseData.NETWORK_IN_TOTAL.length > 0) {
            let tempData: string[] = []
            baseData.NETWORK_IN_TOTAL.forEach((d: number) => {
                tempData.push(toFixed(d / 1024 / 1024, 1))
            })
            metricsData.value.network.push({ data: tempData, areaStyle: {}, stack: 'Total', name: 'In' })
        }
        if (baseData.NETWORK_OUT_TOTAL && baseData.NETWORK_OUT_TOTAL.length > 0) {
            let tempData: string[] = []
            baseData.NETWORK_OUT_TOTAL.forEach((d: number) => {
                tempData.push(toFixed(d / 1024 / 1024, 1))
            })
            metricsData.value.network.push({ data: tempData, areaStyle: {}, stack: 'Total', name: 'Out' })
        }
        // IO
        if (baseData.IO && baseData.IO.length > 0) {
            let tempData: string[] = []
            baseData.IO.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.io.push({ data: tempData, name: 'IO' })
        }

        // SWAP
        if (baseData.SWAP && baseData.SWAP.length > 0) {
            let tempData: string[] = []
            baseData.SWAP.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.swap.push({ data: tempData, name: 'SWAP' })
        }

        // Session
        for (let key in baseData.DB_ACTIVE_SESSION) {
            let tempData: string[] = []
            baseData.DB_ACTIVE_SESSION[key].forEach((element) => {
                tempData.push(toFixed(element))
            })
            metricsData.value.session.push({ data: tempData, areaStyle: {}, stack: 'Total', name: key })
        }

        // Thread Pool
        if (baseData.DB_THREAD_POOL && baseData.DB_THREAD_POOL.length > 0) {
            let tempData: string[] = []
            baseData.DB_THREAD_POOL.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.threadPool.push({ data: tempData, name: 'Thread Pool' })
        }

        // time
        metricsData.value.time = baseData.time
    },
    { deep: true }
)
const { data: topSQLNowData, run: loadTopSQL } = useRequest(getTOPSQLNow, { manual: true })
watch(
    topSQLNowData,
    () => {
        topSQLData.value = topSQLNowData.value
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
            loadTopSQL(props.tabId)
        },
        computed(() => timeInner * 1000)
    )
}
// same for every page in index
const timer = ref<number>()
watch(
    updateCounter,
    () => {
        clearInterval(timer.value)
        clearInterval(timerInner.value)
        if (tabNow.value === tabKeys.Home) {
            if (updateCounter.value.source === sourceType.value.INSTANCE) {
                load()
                loadTopSQL(props.tabId)
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
    { immediate: true }
)
// same for every page in index

const gotoTopsqlDetail = (id: string) => {
    const curMode = localStorage.getItem('INSTANCE_CURRENT_MODE')
    if (curMode === 'wujie') {
        // @ts-ignore plug-in components
        window.$wujie?.props.methods.jump({
            name: `Static-pluginObservability-instanceVemSql_detail`,
            query: {
                dbid: instanceId.value,
                id,
            },
        })
    } else {
        // local
        window.sessionStorage.setItem('sqlId', id)
        router.push(`/vem/sql_detail/${instanceId.value}/${id}`)
    }
}
const emit = defineEmits(['goto'])
const goto = (key: string, param?: object) => {
    emit('goto', key, param)
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
const gotoSessionDetail = (id: string) => {
    const curMode = localStorage.getItem('INSTANCE_CURRENT_MODE')
    if (curMode === 'wujie') {
        // @ts-ignore plug-in components
        window.$wujie?.props.methods.jump({
            name: `Static-pluginObservability-instanceVemSessionDetail`,
            query: {
                dbid: instanceId.value,
                id,
            },
        })
    } else {
        // local
        window.sessionStorage.setItem('sqlId', id)
        router.push(`/vem/sessionDetail/${instanceId.value}/${id}`)
    }
}
</script>

<style scoped lang="scss">
.small-charts {
    color: #ffffff;
    .container {
        height: 114px;
        &.none {
            background: linear-gradient(124.23deg, #82848a 0.83%, #3d444a 69.42%);
        }
        &.cold {
            background: linear-gradient(124.23deg, #64a3f5 0.83%, #5e8be2 69.42%);
        }
        &.normal {
            background: linear-gradient(124.23deg, #6abc6a 0.83%, #6ab357 69.41%);
        }
        &.hot {
            background: linear-gradient(124.23deg, #f4bd24 0.83%, #f38825 78.2%);
        }

        border-radius: 2.89424px;
        position: relative;
        .title {
            position: absolute;
            width: 100px;
            height: 12.57px;
            top: 5px;
            left: 5px;

            font-style: normal;
            font-weight: 500;
            font-size: 14px;
            line-height: 14px;
            letter-spacing: -0.369015px;
            color: #ffffff;
        }
        .value {
            z-index: 0;
            position: absolute;
            height: 12.57px;
            top: 32.64px;
            left: 39.78px;

            font-family: 'DINPro';
            font-style: normal;
            font-weight: 700;
            font-size: 28.9424px;
            line-height: 14px;
            letter-spacing: -1.81613px;
            color: #ffffff;
        }

        .detail {
            cursor: pointer;
            position: absolute;
            z-index: 1;
            top: 0px;
            left: 0px;
            right: 0px;
            bottom: 0px;

            .text {
                display: none;
                align-items: center;
                position: absolute;
                z-index: 1;
                top: 5px;
                right: 5px;
                font-size: 12px;
                .icon {
                    margin-left: 4px;
                    width: 10px;
                    height: 10px;
                }
            }
        }
        .detail:hover .text {
            display: flex;
        }
        .chart {
            padding-top: 10px;
            height: 110px;
        }
    }
}
</style>
