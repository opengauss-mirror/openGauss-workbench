<template>
    <div style="margin-bottom: 50px"></div>
    <IndexBar :tabId="props.tabId"></IndexBar>
    <div class="small-charts">
        <div style="display: flex; flex-direction: row; justify-content: space-between">
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
            <div class="spliter"></div>
            <div class="container" :class="[memoryBackGroud]">
                <div class="title">{{ $t('instanceIndex.memory') }}</div>
                <div class="value" v-if="metricsData.memory && metricsData.memory.length > 0">{{ memoryValue }}%</div>
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
            <div class="spliter"></div>
            <div class="container" :class="[networkBackGroud]">
                <div class="title">{{ $t('instanceIndex.networkInOut') }}</div>
                <div
                    class="value"
                    style="margin-top: -10px"
                    v-if="metricsData.network && metricsData.network.length > 0"
                >
                    <div class="network-box">
                        <div class="item">
                            <div class="title-row">
                                <div class="line-sample in"></div>
                                <div>In</div>
                            </div>
                            <div class="mb">
                                <div class="mb-value">{{ networkInValue }}</div>
                                <div class="mb-unit">MB/S</div>
                            </div>
                        </div>
                        <div class="item out">
                            <div class="title-row">
                                <div class="line-sample out"></div>
                                <div>Out</div>
                            </div>
                            <div class="mb">
                                <div class="mb-value">{{ networkOutValue }}</div>
                                <div class="mb-unit">MB/S</div>
                            </div>
                        </div>
                    </div>
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
            <div class="spliter"></div>
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
            <div class="spliter"></div>
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
        </div>
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
            <div class="info-row">
                <div>{{ $t('session.maxSessionCount') }}{{ metricsData.max_conn }}</div>
                <div>{{ $t('session.activeSessionCount') }}{{ metricsData.active }}</div>
                <div>{{ $t('session.blockedSessionCount') }}{{ metricsData.waiting }}</div>
                <div>{{ $t('session.longestSessionRuntime') }}{{ metricsData.max_runtime }}秒</div>
            </div>
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

    <div style="margin-bottom: 12px"></div>
    <div style="position: relative">
        <div
            style="
                position: absolute;
                right: 0px;
                top: 2px;
                z-index: 9;
                display: flex;
                flex-direction: row;
                align-items: center;
                font-size: 12px;
            "
        >
            <div style="margin-right: 12px">{{ $t('app.refreshOn') }} {{ innerRefreshDoneTime }}</div>
            <div>{{ $t('app.autoRefreshFor') }}</div>
            <el-select v-model="innerRefreshTime" style="width: 60px; margin: 0 4px" @change="updateTimerInner">
                <el-option :value="1" label="1s" />
                <el-option :value="15" label="15s" />
                <el-option :value="30" label="30s" />
                <el-option :value="60" label="60s" />
            </el-select>
            <el-button
                class="refresh-button"
                type="primary"
                :icon="Refresh"
                style="margin-left: 8px"
                @click="loadTopSQL(props.tabId)"
            />
        </div>
        <el-tabs v-model="tab" class="tab2">
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
                    <el-table-column prop="query_id" label="SQLID" width="150" />
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
            <el-tab-pane :label="$t('session.blockSessions.tabTitle')" :name="1">
                <div class="row" style="margin-bottom: 8px">
                    <div class="filter">
                        <el-button
                            @click="
                                () => {
                                    expandRowKeys = []
                                }
                            "
                            >{{ $t('session.blockSessions.collapseAll') }}</el-button
                        >
                    </div>
                </div>

                <el-table
                    :table-layout="'auto'"
                    :data="blockSessionTable"
                    style="width: 100%"
                    border
                    :header-cell-class-name="
                        () => {
                            return 'grid-header'
                        }
                    "
                    row-key="id"
                    :expand-row-keys="expandRowKeys"
                >
                    <el-table-column :label="$t('session.blockSessions.sessionID')" width="160">
                        <template #default="scope">
                            <el-link type="primary" @click="gotoSessionDetail(scope.row.id)">
                                {{ scope.row.id === '0' ? '' : scope.row.id }}
                            </el-link>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('session.blockSessions.blockedSessionID')" width="130">
                        <template #default="scope">
                            <el-link type="primary" @click="gotoSessionDetail(scope.row.parentid)">
                                {{ scope.row.parentid === '0' ? '' : scope.row.parentid }}
                            </el-link>
                        </template>
                    </el-table-column>
                    <el-table-column
                        prop="backend_start"
                        :label="$t('session.blockSessions.sessionStartTime')"
                        :formatter="(r) => dateFormat(r.backend_start, 'MM-DD HH:mm:ss')"
                        width="110"
                    />
                    <el-table-column prop="wait_status" :label="$t('session.blockSessions.waitState')" width="100" />
                    <el-table-column prop="wait_event" :label="$t('session.blockSessions.waitEvent')" width="100" />
                    <el-table-column prop="lockmode" :label="$t('session.blockSessions.waitLockMode')" width="120" />
                    <el-table-column prop="datname" :label="$t('session.blockSessions.dbName')" width="110" />
                    <el-table-column prop="usename" :label="$t('session.blockSessions.userName')" width="90" />
                    <el-table-column prop="client_addr" :label="$t('session.blockSessions.clientIP')" width="120" />
                    <el-table-column prop="application_name" :label="$t('session.blockSessions.appName')" />
                </el-table>
            </el-tab-pane>
            <el-tab-pane :label="$t('session.trans.longTransaction')" :name="2">
                <el-table
                    :table-layout="'auto'"
                    :data="transTable"
                    style="width: 100%"
                    border
                    :header-cell-class-name="
                        () => {
                            return 'grid-header'
                        }
                    "
                >
                    <el-table-column prop="pid" label="PID" width="130" />
                    <el-table-column :label="$t('session.trans.sessionID')" width="130">
                        <template #default="scope">
                            <el-link
                                type="primary"
                                class="top-sql-table-id"
                                @click="gotoSessionDetail(scope.row.sessionid)"
                            >
                                {{ scope.row.sessionid }}
                            </el-link>
                        </template>
                    </el-table-column>
                    <el-table-column prop="usename" :label="$t('session.trans.userName')" width="100" />
                    <el-table-column prop="datname" :label="$t('session.trans.dbName')" width="100" />
                    <el-table-column prop="application_name" :label="$t('session.trans.appName')" width="130" />
                    <el-table-column prop="client_addr" :label="$t('session.trans.clientAddr')" width="120" />
                    <el-table-column prop="query" label="SQL" width="200" />
                    <el-table-column prop="xact_start" :label="$t('session.trans.txStart')" width="134" />
                    <el-table-column prop="xact_duration" :label="$t('session.trans.txDuration')" width="120" />
                    <el-table-column prop="state" :label="$t('session.trans.sessionState')" width="100" />
                </el-table>
            </el-tab-pane>
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
import { getIndexMetrics, getTOPSQLNow, TopSQLNow, TransTable, BlockTable } from '@/api/observability'
import { hasSQLDiagnosisModule } from '@/api/sqlDiagnosis'
import { useRequest } from 'vue-request'
import { tabKeys } from '@/pages/dashboardV2/common'
import moment from 'moment'
import router from '@/router'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { dateFormat } from '@/utils/date'

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
    max_conn: number
    active: number
    waiting: number
    max_runtime: number
    cpu: LineData[]
    memory: LineData[]
    io: LineData[]
    network: LineData[]
    swap: LineData[]
    session: LineData[]
    sessionQty: LineData[]
    waitEvents: LineData[]
    time: string[]
}
const metricsData = ref<MetricsData>({
    max_conn: 0,
    active: 0,
    waiting: 0,
    max_runtime: 0,
    cpu: [],
    memory: [],
    io: [],
    network: [],
    swap: [],
    session: [],
    sessionQty: [],
    waitEvents: [],
    time: ['0'],
})
const expandRowKeys = ref<string[]>([])
const blockSessionTable = ref<BlockTable[]>([])
const topSQLData = ref<void | TopSQLNow[]>([])
const transTable = ref<TransTable[]>([])
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
const networkInValue = computed(() => {
    return getMetricsValue(metricsData.value.network, 1)
})
const networkOutValue = computed(() => {
    return getMetricsValue(metricsData.value.network, 0)
})
const swapValue = computed(() => {
    return getMetricsValue(metricsData.value.swap)
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
    return 'cold'
})
const swapBackGroud = computed(() => {
    if (metricsData.value.swap.length <= 0) return 'none'
    return 'cold'
})
const ioBackGroud = computed(() => {
    if (metricsData.value.io.length <= 0) return 'none'
    return 'cold'
})
const getBackground = (data: LineData[]) => {
    if (data.length <= 0) return 'none'
    else if (getMetricsValue(data) < 80) return 'green'
    else if (getMetricsValue(data) < 90) return 'yellow'
    else return 'red'
}
const getLineColor = (data: LineData[]) => {
    if (data.length <= 0) return 'none'
    else if (getMetricsValue(data) < 80) return '#ABFF83'
    else if (getMetricsValue(data) < 90) return '#FFDC83'
    else return '#FF8D8D'
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
        metricsData.value.sessionQty = []
        metricsData.value.waitEvents = []
        metricsData.value.time = ['0']

        const baseData = indexData.value
        if (!baseData) return

        // info
        metricsData.value.max_conn = baseData.max_conn
        metricsData.value.active = baseData.active
        metricsData.value.waiting = baseData.waiting
        metricsData.value.max_runtime = baseData.max_runtime

        // CPU
        if (baseData.CPU && baseData.CPU.length > 0) {
            let tempData: string[] = []
            baseData.CPU.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.cpu.push({
                data: tempData,
                name: 'CPU',
            })
            metricsData.value.cpu[0].lineStyle = {
                color: getLineColor(metricsData.value.cpu),
            }
        }
        // MEMORY
        if (baseData.MEMORY && baseData.MEMORY.length > 0) {
            let tempData: string[] = []
            baseData.MEMORY.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.memory.push({ data: tempData, name: 'MEMORY' })
            metricsData.value.memory[0].lineStyle = {
                color: getLineColor(metricsData.value.memory),
            }
        }
        // Network
        if (baseData.NETWORK_OUT_TOTAL && baseData.NETWORK_OUT_TOTAL.length > 0) {
            let tempData: string[] = []
            baseData.NETWORK_OUT_TOTAL.forEach((d: number) => {
                tempData.push(toFixed(d / 1024 / 1024, 1))
            })
            metricsData.value.network.push({
                data: tempData,
                areaStyle: {},
                stack: 'Total',
                name: 'Out',
                lineStyle: {
                    color: '#0E78DA',
                },
            })
        }
        if (baseData.NETWORK_IN_TOTAL && baseData.NETWORK_IN_TOTAL.length > 0) {
            let tempData: string[] = []
            baseData.NETWORK_IN_TOTAL.forEach((d: number) => {
                tempData.push(toFixed(d / 1024 / 1024, 1))
            })
            metricsData.value.network.push({
                data: tempData,
                areaStyle: {},
                stack: 'Total',
                name: 'In',
                lineStyle: {
                    color: '#83CBFF',
                },
            })
        }
        // IO
        if (baseData.IO && baseData.IO.length > 0) {
            let tempData: string[] = []
            baseData.IO.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.io.push({
                data: tempData,
                name: 'IO',
                lineStyle: {
                    color: '#83CBFF',
                },
            })
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
        const propertyNames = Object.keys(baseData.DB_ACTIVE_SESSION).sort()
        const sortedData = propertyNames
            .map((property) => ({
                [property]: baseData.DB_ACTIVE_SESSION[property],
            }))
            .sort((a, b) => b[Object.keys(b)[0]][0] - a[Object.keys(a)[0]][0])
        for (let item in sortedData) {
            let tempData: string[] = []
            Object.values(sortedData[item])[0].forEach((element) => {
                tempData.push(element.toString())
            })
            metricsData.value.session.push({
                data: tempData,
                areaStyle: {},
                stack: 'Total',
                name: Object.keys(sortedData[item])[0],
            })
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
        if (!topSQLNowData.value) return

        topSQLData.value = topSQLNowData.value.topSQLNow

        // block sessions
        if (topSQLNowData.value.blockTree) {
            fixTreeKey(topSQLNowData.value.blockTree)
            blockSessionTable.value = topSQLNowData.value.blockTree
        }

        // trans
        transTable.value = topSQLNowData.value.longTxc ? topSQLNowData.value.longTxc : []

        innerRefreshDoneTime.value = moment(new Date()).format('HH:mm:ss')
    },
    { deep: true }
)
const fixTreeKey = (nodes: any[]) => {
    // loop nodes
    for (let nodeIndex = 0; nodeIndex < nodes.length; nodeIndex++) {
        // node
        const node = nodes[nodeIndex]
        // node has children
        if (node.children && node.children.length > 0) {
            for (let nodeChildrenIndex = 0; nodeChildrenIndex < node.children.length; nodeChildrenIndex++) {
                const nodeChild = node.children[nodeChildrenIndex]
                // child has children
                if (nodeChild.children && nodeChild.children.length > 0) {
                    fixTreeKey(nodeChild.children)
                } else {
                    nodeChild.hasChildren = undefined
                    nodeChild.children = undefined
                }
            }
        } else {
            // node has not children
            node.children = undefined
        }
    }
}
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
.info-row {
    flex-grow: 1;
    font-size: 14px;
    font-weight: 400;
    display: flex;
    div {
        margin-right: 36px;
    }
}
.small-charts {
    color: #ffffff;
    .spliter {
        width: 12px;
        flex-shrink: 0;
    }
    .container {
        width: 100%;
        height: 114px;
        &.none {
            background: linear-gradient(124.23deg, #82848a 0.83%, #3d444a 69.42%);
        }
        &.cold {
            background: linear-gradient(124.23deg, #64a3f5 0.83%, #5e8be2 69.42%);
        }
        &.green {
            background: linear-gradient(124.23deg, #6abc6a 0.83%, #6ab357 69.41%);
        }
        &.yellow {
            background: linear-gradient(124.23deg, #f4bd24 0.83%, #f38825 78.2%);
        }
        &.red {
            background: linear-gradient(134deg, #fe6b4b 0%, #e31111 71.22%);
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
            z-index: 1;
            position: absolute;
            height: 32.57px;
            top: 32.64px;
            padding-left: 48px;
            box-sizing: border-box;
            width: 100%;

            font-family: 'DINPro';
            font-style: normal;
            font-weight: 700;
            font-size: 28.9424px;
            line-height: 14px;
            letter-spacing: -1.81613px;
            color: #ffffff;

            .network-box {
                display: flex !important;
                flex-direction: row;
                justify-content: space-between;
                widows: 100%;
                .item {
                    &.out {
                        padding-right: 10px;
                    }
                    .title-row {
                        display: flex;
                        flex-direction: row;
                        font-size: 14px;
                        align-items: center;
                        margin-bottom: 4px;
                        font-weight: 500;
                        .line-sample {
                            margin-right: 4px;
                            width: 8px;
                            height: 8px;
                            border: 1px solid #daefff;
                            &.in {
                                background-color: #83cbff;
                            }
                            &.out {
                                background-color: #0e78da;
                            }
                        }
                    }
                    .mb {
                        display: flex;
                        flex-direction: row;
                        align-items: baseline;
                        .mb-value {
                            font-size: 20px;
                        }
                        .mb-unit {
                            font-weight: 550;
                            font-size: 14px;
                        }
                    }
                }
            }
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
        .detail .text {
            display: flex;
        }
        .chart {
            padding-top: 10px;
            height: 110px;
        }
    }
}
</style>
