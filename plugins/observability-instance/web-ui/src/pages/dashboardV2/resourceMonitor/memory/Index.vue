<template>
  <div class="chart-link">
    <span>{{$t('echart.linkage')}}:&nbsp;&nbsp;</span> <span><el-switch v-model="isLinkage" @change="changeChartLinkage"/></span>
  </div>
  <el-row :gutter="12">
    <el-col :span="12">
      <my-card :title="$t('resourceMonitor.memory.memoryUse')" height="377" :bodyPadding="false" :showBtns="true" @download="title => download(title,memoryUse)" :info="memoryUseInfo">
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
        <div style="height: 257px">
          <LazyLine
            ref="memoryUse"
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
            :isLinkage="isLinkage"
          />
        </div>
        <div class="table-in-card">
          <el-table
            :data="metricsData.memoryInfo"
            style="width: 100%; height: 60px"
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
            <el-table-column prop="MEMORY_DB_USED_CURR" :label="$t('resourceMonitor.memory.dbMemory')" />
          </el-table>
        </div>
      </my-card>
    </el-col>
    <el-col :span="12">
      <my-card :title="$t('resourceMonitor.memory.interactiveAreaUsage')" height="377" :bodyPadding="false" :showBtns="true" @download="title => download(title,interactiveAreaUsage)" :info="memorySwapInfo">
        <div style="height: 257px">
          <LazyLine
            ref="interactiveAreaUsage"
            :tabId="props.tabId"
            :formatter="toFixed"
            :data="metricsData.swap"
            :xData="metricsData.time"
            :max="100"
            :min="0"
            :interval="25"
            :unit="'%'"
            :isLinkage="isLinkage"
          />
        </div>
        <div class="table-in-card">
          <el-table
            :data="metricsData.swapInfo"
            style="width: 100%; height: 60px"
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

  <div style="margin-bottom: 12px"></div>
  <div style="position: relative">
    <div
      style="
        position: absolute;
        right: 0px;
        top: 2px;
        z-index: 1;
        display: flex;
        flex-direction: row;
        align-items: center;
        font-size: 12px;
      "
    >
      <div style="margin-right: 12px">{{ $t('app.refreshOn') }} {{ innerRefreshDoneTime }}</div>
      <div>{{ $t('app.autoRefreshFor') }}</div>
      <el-select v-model="innerRefreshTime" style="width: 100px; margin: 0 4px" @change="updateTimerInner">
        <el-option :value="99999999" label="NO-AUTO" />
        <el-option :value="15" label="15s" />
        <el-option :value="30" label="30s" />
        <el-option :value="60" label="60s" />
      </el-select>
      <el-button
        class="refresh-button"
        type="primary"
        :icon="Refresh"
        style="margin-left: 8px"
        @click="loadTOPMemoryProcessNow(props.tabId)"
      />
    </div>
    <el-tabs v-model="tab" class="tab2">
      <el-tab-pane :label="$t('resourceMonitor.memory.topProcess')" :name="0">
        <template #label>
          <span>{{ $t('resourceMonitor.memory.topProcess') }} <show-info :info="topProcessInfo"/></span>
        </template>
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
          :row-class-name="rowClassName"
        >
          <el-table-column prop="%MEM" label="%MEM" width="70" />
          <el-table-column prop="%CPU" label="%CPU" width="60" />
          <el-table-column label="COMMAND" width="260" show-overflow-tooltip>
            <template #default="scope">
              <el-link
                v-if="scope.row.port && node.dbPort != scope.row.port"
                type="primary"
                class="top-sql-table-id"
                @click="changeCluster(scope.row)"
              >
                {{ scope.row.COMMAND }}
              </el-link>
              <span v-else>{{ scope.row.COMMAND }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="FullCommand" label="FULL COMMAND" show-overflow-tooltip />
          <el-table-column prop="NI" label="NI" width="40" />
          <el-table-column prop="PID" label="PID" width="90" />
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
        <template #label>
          <span>{{ $t('resourceMonitor.memory.topThread') }} <show-info :info="topThreadInfo"/> </span>
        </template>
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
          <el-table-column prop="%MEM" label="%MEM" width="70" />
          <el-table-column prop="%CPU" label="%CPU" width="60" />
          <el-table-column prop="COMMAND" label="COMMAND" show-overflow-tooltip />
          <el-table-column prop="NI" label="NI" width="40" />
          <el-table-column prop="PID" label="PID" width="90" />
          <el-table-column prop="PR" label="PR" width="40" />
          <el-table-column prop="RES" label="RES" width="80" />
          <el-table-column prop="S" label="S" width="40" />
          <el-table-column prop="SHR" label="SHR" width="80" />
          <el-table-column prop="TIME+" label="TIME+" width="100" />
          <el-table-column prop="USER" label="USER" width="120" />
          <el-table-column prop="VIRT" label="VIRT" width="120" />
          <el-table-column :label="$t('session.trans.sessionID')" width="130">
            <template #default="scope">
              <el-link type="primary" class="top-sql-table-id" @click="gotoSessionDetail(scope.row.sessionid)">
                {{ scope.row.sessionid }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="SQLID" width="150">
            <template #default="scope">
              <el-link type="primary" @click="gotoTopsqlDetail(scope.row.query_id)">
                {{ scope.row.query_id }}
              </el-link>
            </template>
          </el-table-column>
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
          <el-table-column prop="memorytype" :label="$t('resourceMonitor.memory.memoryName')" width="180" />
          <el-table-column prop="desc" :label="$t('resourceMonitor.memory.description')" />
          <el-table-column prop="memorymbytes" :label="$t('resourceMonitor.memory.sizeOfMemoryUsed')" width="140" />
        </el-table>
      </my-card>
    </el-col>
    <el-col :span="12">
      <my-card :title="$t('resourceMonitor.memory.parameterConfiguration')" height="382" :bodyPadding="false">
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
          <el-table-column prop="name" :label="$t('resourceMonitor.memory.parameterName')" width="180" />
          <el-table-column prop="desc" :label="$t('resourceMonitor.memory.description')" />
          <el-table-column prop="value" :label="$t('resourceMonitor.memory.settings')" width="140" />
        </el-table>
      </my-card>
    </el-col>
  </el-row>
  <ShowInfo
      :info="topInfo"
      v-if="topInfoShow"
      @close="closeInfo"
      :tranform="tranform"
    />
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
import router from '@/router'
import { getWDRSnapshot } from '@/api/wdr'
import ShowInfo from "@/components/ShowInfo.vue";
import { useParamsStore } from "@/store/params";

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

const emit = defineEmits(['changeCluster', 'goto'])
const topMemoryProcessNowData = ref<void | TopMemoryProcessNow[]>([])
const topMemoryDBThreadNowData = ref<void | TopMemoryProcessNow[]>([])
const innerRefreshTime = ref<number>(30)
const innerRefreshDoneTime = ref<string>('')
const { updateCounter, sourceType, autoRefreshTime, tabNow, instanceId, isManualRangeSelected, timeRange, node } =
  storeToRefs(useMonitorStore(props.tabId))

// same for every page in index
const timer = ref<number>()
onMounted(() => {
  isLinkage.value = isChartLinkage.value
  load()
  loadTOPMemoryProcessNow(props.tabId)
})
watch(
  updateCounter,
  () => {
    isLinkage.value = isChartLinkage.value
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
const rowClassName = ({ row }: { row: any }) => {
  if (row.port) {
    return 'highlight-row'
  }
  return ''
}
const { data: indexData, run: requestData } = useRequest(getMemoryMetrics, { manual: true })
watch(
  indexData,
  () => {
    // clear data
    metricsData.value.memoryUsed = []
    metricsData.value.swap = []
    memoryUseInfo.value.option = []
    memorySwapInfo.value.option = []

    const baseData = indexData.value
    if (!baseData) return

    // info
    metricsData.value.memoryInfo = [
      {
        MEM_CACHE: baseData.MEM_CACHE ? byteToMB(baseData.MEM_CACHE) + 'MB' : '',
        MEM_FREE: baseData.MEM_FREE ? byteToMB(baseData.MEM_FREE) + 'MB' : '',
        MEM_TOTAL: baseData.MEM_TOTAL ? byteToMB(baseData.MEM_TOTAL) + 'MB' : '',
        MEM_USED: baseData.MEM_USED ? byteToMB(baseData.MEM_USED) + 'MB' : '',
        MEMORY_DB_USED_CURR: baseData.MEMORY_DB_USED_CURR ? byteToMB(baseData.MEMORY_DB_USED_CURR) + 'MB' : '',
      },
    ]
    metricsData.value.swapInfo = [
      {
        SWAP_FREE: baseData.SWAP_FREE ? byteToMB(baseData.SWAP_FREE) + 'MB' : '',
        SWAP_TOTAL: baseData.SWAP_TOTAL ? byteToMB(baseData.SWAP_TOTAL) + 'MB' : '',
        SWAP_USED: baseData.SWAP_USED ? byteToMB(baseData.SWAP_USED) + 'MB' : '',
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
      memoryUseInfo.value.option.push({ name: t('resourceMonitor.memory.memoryUse'), value: t('resourceMonitor.memory.memoryUseContent')})
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
      memoryUseInfo.value.option.push({ name: t('resourceMonitor.memory.memoryDBUse'), value: t('resourceMonitor.memory.memoryDBUseContent') })
    }

    // swap
    if (baseData.MEMORY_SWAP) {
      let tempData: string[] = []
      baseData.MEMORY_SWAP.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.swap.push({ data: tempData, areaStyle: {}, stack: 'Total', name: 'swap' })
      memoryUseInfo.value.option.push({ name: 'swap', value: t('resourceMonitor.memory.swapContent') })
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
    topMemoryProcessNowData.value.forEach((item) => {
      if (item.port) {
        item.COMMAND += '(' + node.value.publicIp + ':' + item.port
        if (node.value.dbPort.toString() === item.port.toString()) {
          item.COMMAND += t('instanceMonitor.thisInstance')
        }
        item.COMMAND += ')'
        item.publicIp = node.value.publicIp
      }
    })
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
const changeCluster = (row: TopMemoryProcessNow) => {
  emit('changeCluster', row.publicIp, row.port)
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

const memoryUse = ref();
const interactiveAreaUsage = ref();
const download = (title: string, ref: any) => {
  ref.download(title)
}

const memoryUseInfo = ref<any>({
  title: t("app.fieldOverview"),
  option: []
})

const memorySwapInfo = ref<any>({
  title: t("app.fieldOverview"),
  option: []
})

const topThreadInfo = ref<any>({
  title: t("app.fieldOverview"),
  option: [
    { name: "%CPU", value: t("resourceMonitor.topThread.cpuContent") },
    { name: "%MEM", value: t("resourceMonitor.topThread.memContent") },
    { name: "COMMAND", value: t("resourceMonitor.topThread.commandContent") },
    { name: "NI", value: t("resourceMonitor.topThread.niContent") },
    { name: "PID", value: t("resourceMonitor.topThread.pidContent") },
    { name: "PR", value: t("resourceMonitor.topThread.prContent") },
    { name: "RES", value: t("resourceMonitor.topThread.resContent") },
    { name: "S", value: t("resourceMonitor.topThread.sContent") },
    { name: "SHR", value: t("resourceMonitor.topThread.shrContent") },
    { name: "TIME+", value: t("resourceMonitor.topThread.timeContent") },
    { name: "USER", value: t("resourceMonitor.topThread.userContent") },
    { name: "VIRT", value: t("resourceMonitor.topThread.virtContent") },
    { name: t('session.trans.sessionID'), value: t("resourceMonitor.topThread.sessionIDContent") },
    { name: 'SQLID', value: t("resourceMonitor.topThread.sqlIDContent") },
  ],
});
const topProcessInfo = ref<any>({
  title: t("app.fieldOverview"),
  option: [
    { name: "%CPU", value: t("resourceMonitor.topProcess.cpuContent") },
    { name: "%MEM", value: t("resourceMonitor.topProcess.memContent") },
    { name: "COMMAND", value: t("resourceMonitor.topProcess.commandContent") },
    { name: "FullCommand", value: t("resourceMonitor.topProcess.fullCommandContent") },
    { name: "NI", value: t("resourceMonitor.topProcess.niContent") },
    { name: "PID", value: t("resourceMonitor.topProcess.pidContent") },
    { name: "PR", value: t("resourceMonitor.topProcess.prContent") },
    { name: "RES", value: t("resourceMonitor.topProcess.resContent") },
    { name: "S", value: t("resourceMonitor.topProcess.sContent") },
    { name: "SHR", value: t("resourceMonitor.topProcess.shrContent") },
    { name: "TIME+", value: t("resourceMonitor.topProcess.timeContent") },
    { name: "USER", value: t("resourceMonitor.topProcess.userContent") },
    { name: "VIRT", value: t("resourceMonitor.topProcess.virtContent") }
  ]
})

const { isChartLinkage } = storeToRefs(useParamsStore());
const paramsStore = useParamsStore();
const isLinkage = ref<boolean>(false)
const changeChartLinkage = () => {
  paramsStore.setChartLinkage(isLinkage.value)
}
</script>
<style scoped lang="scss">
.chart-link {
  display: flex;
  justify-content: flex-end;
}
</style>
