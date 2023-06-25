<template>
    <div class="info-row">
        <div>{{ $t('session.maxSessionCount') }}{{ metricsData.max_conn }}</div>
        <div>{{ $t('session.activeSessionCount') }}{{ metricsData.active }}</div>
        <div>{{ $t('session.blockedSessionCount') }}{{ metricsData.waiting }}</div>
        <div>{{ $t('session.longestSessionRuntime') }}{{ metricsData.max_runtime }}秒</div>
    </div>
    <el-row :gutter="12">
        <el-col :span="12">
            <my-card :title="$t('session.sessionCount')" height="300" :bodyPadding="false">
                <LazyLine
                    :tabId="props.tabId"
                    :formatter="toFixed"
                    :data="metricsData.sessionQty"
                    :xData="metricsData.time"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('session.waitEvents')" height="300" :bodyPadding="false">
                <LazyLine
                    :tabId="props.tabId"
                    :formatter="toFixed"
                    :data="metricsData.waitEvents"
                    :xData="metricsData.time"
                />
            </my-card>
        </el-col>
    </el-row>

    <div class="gap-row"></div>

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
                @click="loadSessionTable(props.tabId)"
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
            <el-tab-pane :label="$t('session.blockSessions.tabTitle')" :name="0">
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
                    <el-table-column :label="$t('session.blockSessions.sessionID')" width="120">
                        <template #default="scope">
                            <el-link type="primary" @click="gotoSessionDetail(scope.row.id)">
                                {{ scope.row.id === '0' ? '' : scope.row.id }}
                            </el-link>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('session.blockSessions.blockedSessionID')" width="100">
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
            <el-tab-pane :label="$t('session.trans.longTransaction')" :name="1">
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
                    <el-table-column :label="$t('session.trans.sessionID')" width="80">
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
import { useI18n } from 'vue-i18n'
import LazyLine from '@/components/echarts/LazyLine.vue'
import { useMonitorStore } from '@/store/monitor'
import { toFixed } from '@/shared'
import { storeToRefs } from 'pinia'
import { getSessionMetrics, getSessionTables, TransTable, BlockTable } from '@/api/observability'
import { useIntervalTime } from '@/hooks/time'
import { tabKeys } from '@/pages/dashboardV2/common'
import { useRequest } from 'vue-request'
import { Refresh } from '@element-plus/icons-vue'
import moment from 'moment'
import { dateFormat } from '@/utils/date'
import router from '@/router'

const { t } = useI18n()

const props = withDefaults(defineProps<{ tabId: string }>(), {})
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
    sessionQty: LineData[]
    waitEvents: LineData[]
    time: string[]
}
const metricsData = ref<MetricsData>({
    max_conn: 0,
    active: 0,
    waiting: 0,
    max_runtime: 0,
    sessionQty: [],
    waitEvents: [],
    time: [],
})
const expandRowKeys = ref<string[]>([])
const blockSessionTable = ref<BlockTable[]>([])
const transTable = ref<TransTable[]>([])
const innerRefreshTime = ref<number>(30)
const innerRefreshDoneTime = ref<string>('')
const { updateCounter, sourceType, autoRefreshTime, tabNow, instanceId } = storeToRefs(useMonitorStore(props.tabId))

// same for every page in index
const timer = ref<number>()
onMounted(() => {
    load()
    loadSessionTable(props.tabId)
})
watch(
    updateCounter,
    () => {
        clearInterval(timer.value)
        if (tabNow.value === tabKeys.InstanceMonitorSession) {
            if (updateCounter.value.source === sourceType.value.INSTANCE) {
                load()
                loadSessionTable(props.tabId)
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
const { data: indexData, run: requestData } = useRequest(getSessionMetrics, { manual: true })
watch(
    indexData,
    () => {
        // clear data
        metricsData.value.sessionQty = []
        metricsData.value.waitEvents = []

        const baseData = indexData.value
        if (!baseData) return

        // info
        metricsData.value.max_conn = baseData.max_conn
        metricsData.value.active = baseData.active
        metricsData.value.waiting = baseData.waiting
        metricsData.value.max_runtime = baseData.max_runtime

        // session qty
        {
            let tempData: string[] = []
            baseData.SESSION_ACTIVE_CONNECTION.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.sessionQty.push({
                data: tempData,
                areaStyle: {},
                stack: 'Total',
                name: t('session.sessionActive.activeSessionCount'),
            })
        }
        {
            let tempData: string[] = []
            baseData.SESSION_IDLE_CONNECTION.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.sessionQty.push({
                data: tempData,
                areaStyle: {},
                stack: 'Total',
                name: t('session.sessionActive.idleConnectionCount'),
            })
        }
        {
            let tempData: string[] = []
            baseData.SESSION_MAX_CONNECTION.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.sessionQty.push({ data: tempData, name: t('session.sessionActive.maxConnectionCount') })
        }
        {
            let tempData: string[] = []
            baseData.SESSION_WAITING_CONNECTION.forEach((d: number) => {
                tempData.push(toFixed(d))
            })
            metricsData.value.sessionQty.push({
                data: tempData,
                areaStyle: {},
                stack: 'Total',
                name: t('session.sessionActive.waitingConnectionCount'),
            })
        }

        // wait events
        for (let key in baseData.gauss_wait_events_value) {
            let tempData: string[] = []
            let item = baseData.gauss_wait_events_value[key]
            item.data.forEach((element: any) => {
                tempData.push(toFixed(element))
            })
            metricsData.value.waitEvents.push({ data: tempData, areaStyle: {}, stack: 'Total', name: item.name })
        }

        // time
        metricsData.value.time = baseData.time
    },
    { deep: true }
)
const { data: sessionTableData, run: loadSessionTable } = useRequest(getSessionTables, { manual: true })
watch(
    sessionTableData,
    () => {
        if (!sessionTableData.value) return

        // block sessions
        for (let index = 0; index < sessionTableData.value.blockTree.length; index++) {
            const element = sessionTableData.value.blockTree[index]
            if (element.children && element.children.length > 0) {
                for (let index2 = 0; index2 < element.children.length; index2++) {
                    const element2 = element.children[index2]
                    element2.hasChildren = undefined
                    element2.children = undefined
                }
            } else {
                element.children = undefined
            }
        }
        blockSessionTable.value = sessionTableData.value.blockTree ? sessionTableData.value.blockTree : []

        // trans
        transTable.value = sessionTableData.value.longTxc ? sessionTableData.value.longTxc : []

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
            loadSessionTable(props.tabId)
        },
        computed(() => timeInner * 1000)
    )
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
    display: flex;
    margin-bottom: 12px;
    div {
        margin-right: 36px;
    }
}
</style>
