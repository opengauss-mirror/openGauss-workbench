<template>
  <el-row :gutter="12">
    <el-col :span="8">
      <my-card :title="$t('resourceMonitor.network.in')" height="300" :bodyPadding="false">
        <template #headerExtend>
          <div class="card-links">
            <el-link v-if="isManualRangeSelected" type="primary" @click="gotoSQLDiagnosis()">
              {{ $t('app.diagnosis') }}
            </el-link>
            <el-link v-if="isManualRangeSelected" type="primary" @click="wdr(tabId)" v-loading="wdrLoading">
              {{$t('instanceIndex.wdrAnalysis')}}
            </el-link>
          </div>
        </template>
        <LazyLine
          :tips="$t('instanceIndex.activeSessionQtyTips')"
          :rangeSelect="true"
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.flowIn"
          :xData="metricsData.time"
          :unit="'MB'"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
        />
      </my-card>
    </el-col>
    <el-col :span="8">
      <my-card :title="$t('resourceMonitor.network.out')" height="300" :bodyPadding="false">
        <LazyLine
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.flowOut"
          :xData="metricsData.time"
          :unit="'MB'"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
        />
      </my-card>
    </el-col>
    <el-col :span="8">
      <my-card :title="$t('resourceMonitor.network.lost')" height="300" :bodyPadding="false">
        <LazyLine
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.lost"
          :xData="metricsData.time"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
        />
      </my-card>
    </el-col>
  </el-row>

  <div class="gap-row"></div>

  <el-row :gutter="12">
    <el-col :span="8">
      <my-card :title="$t('resourceMonitor.network.connection')" height="300" :bodyPadding="false">
        <LazyLine
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.networkSocket"
          :xData="metricsData.time"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
        />
      </my-card>
    </el-col>
    <el-col :span="8">
      <my-card :title="$t('resourceMonitor.network.tcpQty')" height="300" :bodyPadding="false">
        <LazyLine
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.tcpSocket"
          :xData="metricsData.time"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
        />
      </my-card>
    </el-col>
    <el-col :span="8">
      <my-card :title="$t('resourceMonitor.network.UDPQty')" height="300" :bodyPadding="false">
        <LazyLine
          :tabId="props.tabId"
          :formatter="toFixed"
          :data="metricsData.udpSocket"
          :xData="metricsData.time"
          :tool-tips-sort="'desc'"
          :tool-tips-exclude-zero="true"
        />
      </my-card>
    </el-col>
  </el-row>

  <div class="gap-row"></div>

  <my-card :title="$t('resourceMonitor.network.card')" :bodyPadding="false" skipBodyHeight>
    <el-table
      :data="metricsData.table"
      style="width: 100%"
      border
      :header-cell-class-name="
        () => {
          return 'grid-header'
        }
      "
    >
      <el-table-column prop="device" label="IFACE" />
      <el-table-column prop="NETWORK_RXPCK" label="rxpck/s" />
      <el-table-column prop="NETWORK_TXPCK" label="txpck/s" />
      <el-table-column prop="NETWORK_RX" label="rx(KB)/s" />
      <el-table-column prop="NETWORK_TX" label="tx(KB)/s" />
      <el-table-column prop="NETWORK_RXERR" label="rxerr/s" />
      <el-table-column prop="NETWORK_TXERR" label="txerr/s" />
      <el-table-column prop="NETWORK_RXDROP" label="rxdrop/s" />
      <el-table-column prop="NETWORK_TXDROP" label="txdrop/s" />
      <el-table-column prop="NETWORK_RXFIFO" label="rxfifo/s" />
      <el-table-column prop="NETWORK_TXFIFO" label="txfifo/s" />
    </el-table>
  </my-card>
</template>

<script setup lang="ts">
import LazyLine from '@/components/echarts/LazyLine.vue'
import { useMonitorStore } from '@/store/monitor'
import { toFixed } from '@/shared'
import { storeToRefs } from 'pinia'
import { getNetworkMetrics } from '@/api/observability'
import { useIntervalTime } from '@/hooks/time'
import { tabKeys } from '@/pages/dashboardV2/common'
import { useRequest } from 'vue-request'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { hasSQLDiagnosisModule } from '@/api/sqlDiagnosis'
import { getWDRSnapshot } from '@/api/wdr'
import moment from 'moment'

const props = withDefaults(defineProps<{ tabId: string }>(), {})
const { t } = useI18n()

const emit = defineEmits(['goto'])

interface LineData {
  name: string
  data: any[]
  [other: string]: any
}
interface MetricsData {
  flowIn: LineData[]
  flowOut: LineData[]
  lost: LineData[]
  networkSocket: LineData[]
  tcpSocket: LineData[]
  udpSocket: LineData[]
  table: any[]
  time: string[]
}
const defaultData = {
  flowIn: [],
  flowOut: [],
  lost: [],
  networkSocket: [],
  tcpSocket: [],
  udpSocket: [],
  table: [],
  time: [],
}
const metricsData = ref<MetricsData>(defaultData)

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
    if (tabNow.value === tabKeys.ResourceMonitorNetwork) {
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
const { data: indexData, run: requestData } = useRequest(getNetworkMetrics, { manual: true })
watch(
  indexData,
  () => {
    // clear data
    metricsData.value.flowIn = []
    metricsData.value.flowOut = []
    metricsData.value.lost = []
    metricsData.value.networkSocket = []
    metricsData.value.tcpSocket = []
    metricsData.value.udpSocket = []
    metricsData.value.table = []
    metricsData.value.time = []
    const baseData = indexData.value
    if (!baseData) return

    // network in
    for (let key in baseData.NETWORK_IN) {
      let tempData: string[] = []
      baseData.NETWORK_IN[key]?.forEach((element) => {
        tempData.push(byteToMB(element))
      })
      metricsData.value.flowIn.push({ data: tempData, name: key })
    }

    // network out
    for (let key in baseData.NETWORK_OUT) {
      let tempData: string[] = []
      baseData.NETWORK_OUT[key]?.forEach((element) => {
        tempData.push(byteToMB(element))
      })
      metricsData.value.flowOut.push({ data: tempData, name: key })
    }

    // lost
    for (let key in baseData.NETWORK_LOST_PACKAGE) {
      let tempData: string[] = []
      baseData.NETWORK_LOST_PACKAGE[key]?.forEach((element) => {
        tempData.push(toFixed(element))
      })
      metricsData.value.lost.push({ data: tempData, name: key })
    }

    // connection
    {
      let tempData: string[] = []
      baseData.NETWORK_TCP_ALLOC?.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.networkSocket.push({ data: tempData, name: 'tcpalloc' })
    }
    {
      let tempData: string[] = []
      baseData.NETWORK_CURRESTAB?.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.networkSocket.push({ data: tempData, name: 'currestab' })
    }
    {
      let tempData: string[] = []
      baseData.NETWORK_TCP_INSEGS?.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.networkSocket.push({ data: tempData, name: 'tcpinsegs' })
    }
    {
      let tempData: string[] = []
      baseData.NETWORK_TCP_OUTSEGS?.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.networkSocket.push({ data: tempData, name: 'tcpooutsegs' })
    }

    // tcp socket
    for (let key in baseData.NETWORK_TCP_SOCKET) {
      let tempData: string[] = []
      baseData.NETWORK_TCP_SOCKET[key]?.forEach((element) => {
        tempData.push(toFixed(element))
      })
      metricsData.value.tcpSocket.push({ data: tempData, name: key })
    }

    // udp socket
    {
      let tempData: string[] = []
      baseData.NETWORK_UDP_SOCKET?.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.udpSocket.push({ data: tempData, name: 'UDP sockets' })
    }

    for (let index = 0; index < baseData.table.length; index++) {
      const element = baseData.table[index]
      element.NETWORK_RXPCK = toFixed(element.NETWORK_RXPCK)
      element.NETWORK_TXPCK = toFixed(element.NETWORK_TXPCK)
      element.NETWORK_RX = toFixed(element.NETWORK_RX)
      element.NETWORK_TX = toFixed(element.NETWORK_TX)
      element.NETWORK_RXERR = toFixed(element.NETWORK_RXERR)
      element.NETWORK_TXERR = toFixed(element.NETWORK_TXERR)
      element.NETWORK_RXDROP = toFixed(element.NETWORK_RXDROP)
      element.NETWORK_TXDROP = toFixed(element.NETWORK_TXDROP)
      element.NETWORK_RXFIFO = toFixed(element.NETWORK_RXFIFO)
      element.NETWORK_TXFIFO = toFixed(element.NETWORK_TXFIFO)
    }
    metricsData.value.table = baseData.table

    metricsData.value.time = baseData.time
  },
  { deep: true }
)
const byteToMB = (val: number) => {
  return (val / 8 / 1024 / 1024).toFixed(2)
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

const { data: wdrData, run: wdr, loading: wdrLoading } = useRequest(getWDRSnapshot, { manual: true })
watch(
  wdrData,
  (res: any) => {
    // goto wdr
    if (res && res.wdrId && res.wdrId.length > 0) {
      const { timeRange } = useMonitorStore(props.tabId)
      let param = {
        operation: 'search',
        startTime: timeRange == null ? '' : moment(timeRange[0]).format("YYYY-MM-DD HH:mm:ss"),
        endTime: timeRange == null ? '' : moment(timeRange[1]).format("YYYY-MM-DD HH:mm:ss"),
      }
      emit('goto', tabKeys.WDR, param)
    } else if (res && res.start && res.end) {
      let param = {
        operation: 'edit',
        startId: res.start,
        endId: res.end
      }
      emit('goto', tabKeys.WDR, param)
    } else {
      ElMessage.error(t('wdrReports.wdrErrtip'))
    }
  },
  { deep: true }
)
</script>

<style scoped lang="scss"></style>
