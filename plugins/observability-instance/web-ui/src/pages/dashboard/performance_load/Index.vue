<!-- eslint-disable camelcase -->
<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import ListMetric from './ListMetric.vue';
import LazyLine from '../LazyLine.vue';
import { useMonitorStore } from '../../../store/monitor';
import { toFixed } from '../../../shared';
import { storeToRefs } from 'pinia';
import { getDatabaseMetrics } from '../../../api/prometheus';
import { useIntervalTime } from '../../../hooks/time';
 
const { t } = useI18n();

const props = withDefaults(defineProps<{
    nodeVersion?: string,
}>(), {
    nodeVersion: "",
})

const brushRangeSign = ref<boolean>(false);
const tempSaveTime = ref<Array<Date>>([]);
const tempSaveRangeTime = ref<number>(-2);

const monitorStore = useMonitorStore()
const { refreshTime, rangeTime, time, tab: topTab, autoRefresh, instanceId, brushRange, timeRange } = storeToRefs(useMonitorStore())

const load = (checkTab?: boolean, checkRange?: boolean) => {
    if (checkTab && topTab.value !== 0) {
        return
    }
    if (checkRange && (brushRange.value.length > 0)) {
        return
    }
    if (rangeTime.value > 0 || (time.value && time.value.length === 2)) {
        getDatabaseMetrics()
    }
}
if (topTab.value === 0) {
    useIntervalTime(() => {
        if (instanceId.value) {
            load(true);
        }
    }, computed(() => refreshTime.value * 1000))
}

watch(autoRefresh, () => load(true, true))

watch(rangeTime, v => {
    if (v > 0 && topTab.value === 0) {
        if (brushRangeSign.value) {
            brushRangeSign.value = false;
        } else {
            brushRange.value = []
        }
        getDatabaseMetrics()
    }
})
watch(time, v => {
    if (v && topTab.value === 0) {
        if (brushRangeSign.value) {
            brushRangeSign.value = false;
        } else {
            brushRange.value = []
        }
        load()
    }
})

const ioRateData = ref<{ name: string, cur: string, min: string, avg: string, max: string }[]>([])
const loadRateData = (data: any[]) => {
    if (data.length === 0) {
        ioRateData.value = []
        return
    }
    ioRateData.value = data.map(d => ({
        name: `${t(`metric.${d.name}`)}${t('dashboard.rate')}`,
        cur: `${d.data[Math.max(0, d.data.length - 1)]}KB/s`,
        min: `${toFixed(Math.min(...d.data))}KB/s`,
        avg: `${d.data.length ? toFixed(d.data.reduce((a: number, s: string) => a + Number.parseFloat(s), 0) / d.data.length) : '0.00'}KB/s`,
        max: `${toFixed(Math.max(...d.data))}KB/s`,
    }))
}
const ioData = ref<{ name: string, cur: string, min: string, avg: string, max: string }[]>([])
const loadData = (data: any[]) => {
    if (data.length === 0) {
        ioData.value = []
        return
    }
    ioData.value = data.map(d => ({
        name: `${t(`metric.${d.name}`)}${t('dashboard.capacity')}`,
        cur: `${d.data[Math.max(0, d.data.length - 1)]}MB/s`,
        min: `${toFixed(Math.min(...d.data))}MB/s`,
        avg: `${d.data.length ? toFixed(d.data.reduce((a: number, s: string) => a + Number.parseFloat(s), 0) / d.data.length) : '0.00'}MB/s`,
        max: `${toFixed(Math.max(...d.data))}MB/s`,
    }))
}

const toTopSQL = () => {
    monitorStore.filters[1].time = [new Date(timeRange.value[0]), new Date(timeRange.value[1])]
    brushRange.value = []
    monitorStore.tab = 1
    monitorStore.filters[1].rangeTime = -1
}

const cardRef = ref()
const colors = [
    '#F5222D', '#FA8C16', '#FADB14', '#52C41A', '#13C2C2', '#1890FF', '#2F54EB', '#722ED1', '#EB2F96', '#A97526',
    '#FFB7B7', '#FFC9AB', '#FFFFAB', '#C0FF99', '#A8FFE5', '#B4E5FF', '#ADBAFF', '#D3ADF7', '#FFD1EC',
    '#FF7875', '#FFC069', '#FFF566', '#95DE64', '#5CDBD3', '#69C0FF', '#597EF7', '#9254DE', '#FF85C0',
    '#A8071A', '#AD4E00', '#AD8B00', '#237804', '#006D75', '#0050B3', '#1D39C4', '#531DAB', '#9E1068',
]
const waitLegends = ref<{color: string, name: string}[]>([])
const loadWaitData = (data: any[]) => {
    waitLegends.value = data.map((d, i) => {
        return {
            color: colors[i] as string,
            name: d.name as string,
        }
    })
    nextTick(cardRef.value.trigger)
}

watch(instanceId, id => {
    if (id) {
        load()
    }
}, { immediate: true })

const clearZoom = () => {
    brushRange.value = [];
    // reset
    if (tempSaveRangeTime.value > 0) {
        monitorStore.filters[0].rangeTime = tempSaveRangeTime.value;
        monitorStore.filters[0].time = null;
    } else {
        monitorStore.filters[0].time = [tempSaveTime.value[0], tempSaveTime.value[1]];
        monitorStore.filters[0].rangeTime = -1;
    }
    brushRangeSign.value = false;
    // clear
    tempSaveTime.value = [];
    tempSaveRangeTime.value = -2;
}

watch(timeRange, () => {
    if (tempSaveTime.value.length === 0 && tempSaveRangeTime.value === -2) {
        if (rangeTime.value === -1) {
            tempSaveTime.value = [time.value![0], time.value![1]];
        } else {
            tempSaveRangeTime.value = rangeTime.value;
        }
    }
    monitorStore.filters[0].time = [new Date(timeRange.value[0]), new Date(timeRange.value[1])];
    monitorStore.filters[0].rangeTime = -1;
    brushRangeSign.value = true;
});

</script>

<template>
<ListMetric />
<div class="load-flex">
    <el-row :gutter="16" :key="nodeVersion">
        <el-col :span="props.nodeVersion === 'LITE' ? 24 : 12">
            <my-card :title="$t('dashboard.timeConsumption')" height="300" :legend="[
                {color: '#9CCC65', name: 'CPU_TIME'},
                {color: '#FA8C16', name: 'NET_SEND_TIME'},
                {color: '#00C7F9', name: 'DATA_IO_TIME'},
            ]" :bodyPadding="false">
                <div class="instance_time">
                    <div class="instance_time-tip"><svg-icon name="info"/>{{ $t('dashboard.runningInAnalysisTip') }}</div>
                    <div class="instance_time-button" v-show="brushRange && brushRange.length === 2">
                        <el-button type="primary" text bg @click="toTopSQL">{{ $t('dashboard.runningInAnalysis') }}</el-button>
                        <el-button type="primary" text bg @click="clearZoom">{{ $t('dashboard.uncheckRegion') }}</el-button>
                    </div>
                </div>
                <div style="height: 225px;">
                    <LazyLine
                        :color="['#9CCC65', '#FA8C16', '#00C7F9']"
                        :names="['CPU_TIME', 'NET_SEND_TIME', 'DATA_IO_TIME']"
                        brush="time"
                        :lineWidth="0"
                        areaStyle
                        stack
                        :translate="false"
                    />
                </div>
            </my-card>
        </el-col>
        <el-col :span="12" v-if="props.nodeVersion !== 'LITE'">
            <my-card :title="$t('dashboard.waitEvent')" height="300" :legend="waitLegends" :bodyPadding="false" style="margin-bottom: 16px;" ref="cardRef">
                <div class="instance_time">
                    <div><svg-icon name="info"/>{{ $t('dashboard.runningInAnalysisTip') }}</div>
                    <div v-show="brushRange && brushRange.length === 2">
                        <el-button type="primary" text bg @click="toTopSQL">{{ $t('dashboard.runningInAnalysis') }}</el-button>
                        <el-button type="primary" text bg @click="clearZoom">{{ $t('dashboard.uncheckRegion') }}</el-button>
                    </div>
                </div>
                <div style="height: 225px;">
                    <LazyLine
                        :color="colors"
                        @load-data="loadWaitData"
                        legendName="event"
                        brush="wait"
                        bar
                        stack
                        enterable
                        :translate="false"
                    />
                </div>
            </my-card>
        </el-col>
    </el-row>
</div>

<my-card :title="$t('dashboard.databaseLoad')" collapse style="margin-bottom: 16px;">
    <el-row :gutter="16">
        <el-col :span="12">
            <my-card :title="$t('dashboard.tps')" height="255" :legend="[
                {color: '#0D86E2', name: $t('metric.transactionRollbackNum')},
                {color: '#9CCC65', name: $t('metric.transactionCommitments')},
                {color: '#00C7F9', name: $t('metric.transactionAndRollbackTotal')},
            ]" :bodyPadding="false">
                <LazyLine
                    :color="['#0D86E2', '#9CCC65', '#00C7F9']"
                    :names="['transactionRollbackNum', 'transactionCommitments', 'transactionAndRollbackTotal']"
                    :formatter="toFixed"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.qps')" height="255" :legend="[
                {color: '#00C7F9', name: $t('metric.queryTransactions')},
            ]" :bodyPadding="false">
                <LazyLine
                    :color="['#00C7F9']"
                    :names="['queryTransactions']"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.connectionNum')" height="255" :legend="[
                {color: '#E64A19', name: $t('metric.currentIdleConnections')},
                {color: '#0D86E2', name: $t('metric.currentActiveConnections')},
                {color: '#9CCC65', name: $t('metric.currentConnections')},
                {color: '#00C7F9', name: $t('metric.totalConnections')},
            ]" :bodyPadding="false">
                <LazyLine
                    :color="['#E64A19', '#0D86E2', '#9CCC65', '#00C7F9']"
                    :names="['currentIdleConnections', 'currentActiveConnections', 'currentConnections', 'totalConnections']"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.slowSqlMoreThan3Seconds')" height="255" :legend="[
                {color: '#00C7F9', name: $t('metric.slowSqlNum')},
            ]" :bodyPadding="false">
                <LazyLine
                    :color="['#00C7F9']"
                    :names="['slowSqlNum']"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.longTransactionNumGreaterThan30Seconds')" height="255" :legend="[
                {color: '#00C7F9', name: $t('metric.longTransactions')},
            ]" :bodyPadding="false">
                <LazyLine
                    :color="['#00C7F9']"
                    :names="['longTransactions']"
                />
            </my-card>
        </el-col>
        <el-col :span="12" v-if="props.nodeVersion !== 'LITE'">
            <my-card :title="$t('dashboard.sqlResponseTime')" height="255" :legend="[
                {color: '#9CCC65', name: $t('metric.sqlResponseTime80')},
                {color: '#00C7F9', name: $t('metric.sqlResponseTime95')},
            ]" :bodyPadding="false">
                <LazyLine
                    :color="['#9CCC65', '#00C7F9']"
                    :names="['sqlResponseTime80', 'sqlResponseTime95']"
                    unit="ms"
                    :formatter="(d: string) => toFixed((Number.parseFloat(d) / 1000))"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.transactionLockInfo')" height="255" :legend="[
                {color: '#E64A19', name: 'accessExclusiveLock'},
                {color: '#0D86E2', name: 'accessShareLock'},
                {color: '#9CCC65', name: 'ExclusiveLock'},
                {color: '#00C7F9', name: 'ShareUpdateExclusiveLock'},
                {color: '#0F866A', name: 'ShareRowExclusiveLock'},
                {color: '#37D4D1', name: 'RowShareLock'},
                {color: '#425ADD', name: 'RowExclusiveLock'},
                {color: '#A97526', name: 'ShareLock'},
            ]" :bodyPadding="false" :overflowHidden="false">
                <LazyLine
                    :color="['#E64A19', '#0D86E2', '#9CCC65', '#00C7F9', '#0F866A', '#37D4D1', '#425ADD', '#A97526']"
                    :names="['accessExclusiveLock', 'accessShareLock', 'ExclusiveLock', 'ShareUpdateExclusiveLock', 'ShareRowExclusiveLock', 'RowShareLock', 'RowExclusiveLock', 'ShareLock']"
                    :translate="false"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.cacheHitRate')" height="255" :legend="[
                {color: '#9CCC65', name: $t('metric.queryCacheHitRate')},
                {color: '#00C7F9', name: $t('metric.databaseCacheHitRate')},
            ]" :bodyPadding="false">
                <LazyLine
                    :color="['#9CCC65', '#00C7F9']"
                    :names="['queryCacheHitRate', 'databaseCacheHitRate']"
                    unit="%"
                    :formatter="toFixed"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.ioBlockNumberTrend')" height="255" :legend="[
                {color: '#9CCC65', name: $t('metric.readPhysicalFileBlockNum')},
                {color: '#00C7F9', name: $t('metric.writePhysicalFileBlockNum')},
            ]" :bodyPadding="false">
                <LazyLine
                    :color="['#9CCC65', '#00C7F9']"
                    :names="['readPhysicalFileBlockNum', 'writePhysicalFileBlockNum']"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.ScrubDirtyPageInfo')" height="255" :legend="[
                {color: '#9CCC65', name: $t('metric.lastBatchDirtyPageNum')},
                {color: '#00C7F9', name: $t('metric.currentRemainingDirtyPages')},
            ]" :bodyPadding="false">
                <LazyLine
                    :color="['#9CCC65', '#00C7F9']"
                    :names="['lastBatchDirtyPageNum', 'currentRemainingDirtyPages']"
                />
            </my-card>
        </el-col>
    </el-row>
</my-card>
<my-card :title="$t('dashboard.serverResources')" collapse>
    <el-row :gutter="16">
        <el-col :span="12">
            <my-card :title="$t('dashboard.loadAndCpu')" height="255" :legend="[
                {color: '#E64A19', name: $t('metric.totalCoreNum')},
                {color: '#9CCC65', name: $t('metric.total5mLoad')},
                {color: '#00C7F9', name: $t('metric.totalAverageUtilization')},
            ]" :bodyPadding="false">
                <div class="linename">
                    <div>{{ $t('metric.totalCoreNum') }}</div>
                    <div>
                        <LazyLine
                            :color="['#E64A19', '#9CCC65', '#00C7F9']"
                            :names="['totalCoreNum', 'total5mLoad', 'totalAverageUtilization']"
                            :nameIndexs="[2]"
                            nameFix="1"
                            scatterUnit="%"
                            hasScatterData
                        />
                    </div>
                    <div>{{ $t('metric.totalAverageUtilization') }}</div>
                </div>
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.cpuUsage')" height="255" :legend="[
                {color: '#E64A19', name: $t('metric.diskIOUsage')},
                {color: '#0D86E2', name: $t('metric.systemUsage')},
                {color: '#9CCC65', name: $t('metric.userUsage')},
                {color: '#00C7F9', name: $t('metric.totalUsage')},
            ]" :bodyPadding="false">
                <LazyLine
                    :color="['#E64A19', '#0D86E2', '#9CCC65', '#00C7F9']"
                    :names="['diskIOUsage', 'systemUsage', 'userUsage', 'totalUsage']"
                    unit="%"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.memoryAndAverageMemory')" height="255" :legend="[
                {color: '#9CCC65', name: $t('metric.totalMemory')},
                {color: '#E64A19', name: $t('metric.usedMemory')},
                {color: '#00C7F9', name: $t('metric.totalAverageUtilization')},
            ]" :bodyPadding="false">
                <div class="linename">
                    <div>{{ $t('metric.totalMemory') }}(GB)</div>
                    <div>
                        <LazyLine
                            :color="['#9CCC65', '#E64A19', '#00C7F9']"
                            :names="['totalMemory', 'usedMemory', 'totalAverageUtilization']"
                            :nameIndexs="[2]"
                            nameFix="2"
                            :formatter="(d: string) => toFixed((Number.parseFloat(d) / 1073741824))"
                            scatterUnit="%"
                            hasScatterData
                        />
                    </div>
                    <div>{{ $t('metric.totalAverageUtilization') }}</div>
                </div>
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.diskAndAverageDisk')" height="255" :legend="[
                {color: '#9CCC65', name: $t('metric.totalDisks')},
                {color: '#E64A19', name: $t('metric.totalNumber')},
                {color: '#00C7F9', name: $t('metric.totalAverageUtilization')},
            ]" :bodyPadding="false">
                <div class="linename">
                    <div>{{ $t('metric.totalDisks') }}(GB)</div>
                    <div>
                        <LazyLine
                            :color="['#9CCC65', '#E64A19', '#00C7F9']"
                            :names="['totalDisks', 'totalNumber', 'totalAverageUtilization']"
                            :nameIndexs="[2]"
                            nameFix="3"
                            :formatter="(d: string) => toFixed((Number.parseFloat(d) / 1073741824))"
                            scatterUnit="%"
                            hasScatterData
                        />
                    </div>
                    <div>{{  $t('metric.totalAverageUtilization') }}</div>
                </div>
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.diskReadAndWriteRate')" height="255" style="margin-bottom: 0;" :legend="[
                {color: '#0D86E2', name: $t('metric.read')},
                {color: '#00C7F9', name: $t('metric.write')},
            ]" :bodyPadding="false" :withTable="true">
                <LazyLine
                    :color="['#0D86E2', '#00C7F9']"
                    :names="['read', 'write']"
                    :nameIndexs="[0, 1]"
                    nameFix="1"
                    :formatter="(d: string) => toFixed((Number.parseFloat(d) / 1024))"
                    unit="KB/s"
                    @load-data="loadRateData"
                />
            </my-card>
            <el-table :data="ioRateData" border>
                <el-table-column label="" prop="name" />
                <el-table-column :label="$t('dashboard.currentRate')" prop="cur" />
                <el-table-column :label="$t('dashboard.minimumRate')" prop="min" />
                <el-table-column :label="$t('dashboard.averageRate')" prop="avg" />
                <el-table-column :label="$t('dashboard.maxRate')" prop="max" />
            </el-table>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.diskReadAndWritCapacity')" height="255" style="margin-bottom: 0;" :legend="[
                {color: '#0D86E2', name: $t('metric.read')},
                {color: '#00C7F9', name: $t('metric.write')},
            ]" :bodyPadding="false" :withTable="true">
                <LazyLine
                    :color="['#0D86E2', '#00C7F9']"
                    :names="['read', 'write']"
                    :nameIndexs="[0, 1]"
                    nameFix="2"
                    :formatter="(d: string) => toFixed((Number.parseFloat(d) / 1048576))"
                    unit="MB/s"
                    @load-data="loadData"
                />
            </my-card>
            <el-table :data="ioData" border>
                <el-table-column label="" prop="name" />
                <el-table-column :label="$t('dashboard.currentCapacity')" prop="cur" />
                <el-table-column :label="$t('dashboard.minimumCapacity')" prop="min" />
                <el-table-column :label="$t('dashboard.averageCapacity')" prop="avg" />
                <el-table-column :label="$t('dashboard.maxCapacity')" prop="max" />
            </el-table>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.networkBandwidthUsage')" height="255" :legend="[
                {color: '#0D86E2', name: $t('metric.upload')},
                {color: '#00C7F9', name: $t('metric.download')},
            ]" :bodyPadding="false">
                <LazyLine
                    :color="['#0D86E2', '#00C7F9']"
                    :names="['upload', 'download']"
                    unit="MB/s"
                    :formatter="(d: string) => toFixed((Number.parseFloat(d) / 1048576))"
                />
            </my-card>
        </el-col>
        <el-col :span="12">
            <my-card :title="$t('dashboard.networkSocketConnection')" height="255" :legend="[
                {color: '#0D86E2', name: 'TCPAlloc'},
                {color: '#9CCC65', name: 'CurrEstab'},
                {color: '#E64A19', name: 'TcpOutSegs'},
                {color: '#0F866A', name: 'TcpInSegs'},
                {color: '#37D4D1', name: 'UdpInuse'},
                {color: '#425ADD', name: 'TcpTw'},
                {color: '#A97526', name: 'TcpRetransSegs'},
                {color: '#00C7F9', name: 'SocketsUsed'},
            ]" :bodyPadding="false" :overflowHidden="false">
                <div class="linename">
                    <div>{{ $t('dashboard.totalLoad') }}</div>
                    <div>
                        <LazyLine
                            :color="['#0D86E2', '#9CCC65', '#E64A19', '#0F866A', '#37D4D1', '#425ADD', '#A97526', '#00C7F9']"
                            :names="['TCP_alloc', 'CurrEStab', 'Tcp_OutSegs', 'Tcp_InSegs', 'UDP_inuse', 'TCP_tw', 'Tcp_RetransSegs', 'Sockets_used']"
                            :formatter="toFixed"
                            hasScatterData
                            :translate="false"
                        />
                    </div>
                    <div>{{ $t('dashboard.allProtocolSocketsUsed') }}</div>
                </div>
            </my-card>
        </el-col>
    </el-row>
</my-card>

</template>

<style scoped lang="scss">
.og-server {
    :deep(tr) {
        background-color: $og-background-color;
    }
    :deep(.el-table__inner-wrapper::before) {
        height: 0px;
    }
    :deep(.el-table__row:last-of-type td) {
        border-bottom: none;
    }
    :deep(.el-table__inner-wrapper tr:first-child td:first-child) {
        border-left: none;
    }
}
.instance_time {
    display: flex;
    justify-content: space-between;
    padding: 8px 16px 0;
    font-size: 12px;
    align-items: center;
    svg {
        margin-right: 8px;
    }

    &-tip {
        width: 68%;
    }
    &-button {
        width: 30%;
    }
    .el-button {
        text-decoration: underline;
    }
    &_tabs {
        :deep(.el-tabs__header) {
            padding: 0 16px;
            width: 100%;
            background-color: $og-border-color !important;
        }
        :deep(.el-tabs__content) {
            width: 100%;
            height: 100%;
        }
    }
}
.linename {
    display: flex;
    width: 100%;
    height: 100%;
    align-items: center;
    position: relative;
    justify-content: center;
    > div:nth-of-type(2) {
        width: calc(100% - 60px);
        height: 100%;
        margin: 0 10px;
    }
    > div:nth-of-type(1), > div:nth-of-type(3) {
        color: var(--el-color-line-text-color);
        font-size: 12px;
        text-align: center;
        position: absolute;
        width: 200px;
        height: 15px;
    }
    > div:nth-of-type(1) {
        transform: rotate(-90deg);
        left: -80px;
    }
    > div:nth-of-type(3) {
        transform: rotate(90deg);
        right: -80px;
    }
}
:deep(.og-card) {
    margin-bottom: 16px;
}
:deep(.og-card-body .el-row) {
    margin-bottom: -16px;
}
:deep(.el-table) {
    background-color: var(--el-bg-color-og);
    margin-bottom: 16px;
}
:deep(.el-table__header-wrapper th) {
        background-color: var(--el-bg-color-og);
        color: var(--el-text-color-og);
    }
:deep(.el-table tr) {
    background-color: var(--el-bg-color-og);
}
:deep(.el-table th.el-table__cell) {
    background-color: var(--el-bg-color-og);
}

:deep(.el-table__row) {
    background-color: var(--el-bg-color-og);
}
:deep(.el-table__row th) {
    color: var(--el-text-color-og);
}
</style>
