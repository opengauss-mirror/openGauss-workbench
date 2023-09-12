<template>
  <el-row :gutter="12">
    <el-col :span="12">
      <my-card :title="$t('resourceMonitor.cpu.cpuUse')" height="300" :bodyPadding="false">
        <template #headerExtend>
          <div class="card-links">
            <el-link
              v-if="isManualRangeSelected"
              type="primary"
              @click="goto(tabKeys.InstanceMonitorTOPSQL, { key: tabKeys.InstanceMonitorTOPSQLCPUTime })"
            >
              TOP CPU SQL
            </el-link>
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
          :data="metricsData.cpu"
          :xData="metricsData.time"
          :max="100"
          :min="0"
          :interval="25"
          :unit="'%'"
        />
      </my-card>
    </el-col>
    <el-col :span="12">
      <my-card :title="$t('resourceMonitor.cpu.cpuLoad')" height="300" :bodyPadding="false">
        <LazyLine :tabId="props.tabId" :formatter="toFixed" :data="metricsData.cpuPayload" :xData="metricsData.time" />
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
        @click="loadTOPCPUProcessNow(props.tabId)"
      />
    </div>
    <el-tabs v-model="tab" class="tab2">
      <el-tab-pane :label="$t('resourceMonitor.cpu.topProcess')" :name="0">
        <el-table
          :table-layout="'auto'"
          :data="topCPUProcessNowData == null ? [] : topCPUProcessNowData"
          style="width: 100%"
          :border="true"
          :header-cell-class-name="
            () => {
              return 'grid-header'
            }
          "
          :row-class-name="rowClassName"
        >
          <el-table-column prop="%CPU" label="%CPU" width="60" />
          <el-table-column prop="%MEM" label="%MEM" width="60" />
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
      <el-tab-pane :label="$t('resourceMonitor.cpu.topThread')" :name="1">
        <el-table
          :table-layout="'auto'"
          :data="topCPUDBThreadNowData == null ? [] : topCPUDBThreadNowData"
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
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import LazyLine from '@/components/echarts/LazyLine.vue'
import { useMonitorStore } from '@/store/monitor'
import { toFixed } from '@/shared'
import { storeToRefs } from 'pinia'
import { getCPUMetrics, getTOPCPUProcessNow, TopCPUProcessNow } from '@/api/observability'
import { useIntervalTime } from '@/hooks/time'
import { tabKeys } from '@/pages/dashboardV2/common'
import { useRequest } from 'vue-request'
import { Refresh } from '@element-plus/icons-vue'
import moment from 'moment'
import { hasSQLDiagnosisModule } from '@/api/sqlDiagnosis'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getWDRSnapshot } from '@/api/wdr'

const { t } = useI18n()

const props = withDefaults(defineProps<{ tabId: string }>(), {})
const tab = 0

interface LineData {
  name: string
  data: any[]
  [other: string]: any
}
interface MetricsData {
  cpu: LineData[]
  cpuPayload: LineData[]
  time: string[]
}
const metricsData = ref<MetricsData>({
  cpu: [],
  cpuPayload: [],
  time: [],
})
const topCPUProcessNowData = ref<void | TopCPUProcessNow[]>([])
const topCPUDBThreadNowData = ref<void | TopCPUProcessNow[]>([])
const innerRefreshTime = ref<number>(30)
const innerRefreshDoneTime = ref<string>('')
const { updateCounter, sourceType, autoRefreshTime, tabNow, instanceId, isManualRangeSelected, timeRange, node } =
  storeToRefs(useMonitorStore(props.tabId))

// same for every page in index
const timer = ref<number>()
onMounted(() => {
  load()
  loadTOPCPUProcessNow(props.tabId)
})
watch(
  updateCounter,
  () => {
    clearInterval(timer.value)
    if (tabNow.value === tabKeys.ResourceMonitorCPU) {
      if (updateCounter.value.source === sourceType.value.INSTANCE) {
        load()
        loadTOPCPUProcessNow(props.tabId)
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
const { data: indexData, run: requestData } = useRequest(getCPUMetrics, { manual: true })
watch(
  indexData,
  () => {
    // clear data
    metricsData.value.cpu = []
    metricsData.value.cpuPayload = []

    const baseData = indexData.value
    if (!baseData) return

    {
      let tempData: string[] = []
      baseData.CPU_DB.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.cpu.push({ data: tempData, name: t('resourceMonitor.cpu.dbThread') })
    }
    {
      let tempData: string[] = []
      baseData.CPU_TOTAL.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.cpu.push({ data: tempData, name: 'Total' })
    }
    {
      let tempData: string[] = []
      baseData.CPU_USER.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.cpu.push({ data: tempData, areaStyle: {}, stack: 'Total', name: 'User' })
    }
    {
      let tempData: string[] = []
      baseData.CPU_SYSTEM.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.cpu.push({ data: tempData, areaStyle: {}, stack: 'Total', name: 'System' })
    }
    {
      let tempData: string[] = []
      baseData.CPU_IOWAIT.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.cpu.push({ data: tempData, areaStyle: {}, stack: 'Total', name: 'IOWait' })
    }
    {
      let tempData: string[] = []
      baseData.CPU_NICE.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.cpu.push({ data: tempData, areaStyle: {}, stack: 'Total', name: 'Nice' })
    }
    {
      let tempData: string[] = []
      baseData.CPU_IRQ.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.cpu.push({ data: tempData, areaStyle: {}, stack: 'Total', name: 'IRQ' })
    }
    {
      let tempData: string[] = []
      baseData.CPU_SOFTIRQ.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.cpu.push({ data: tempData, areaStyle: {}, stack: 'Total', name: 'Soft IRQ' })
    }
    {
      let tempData: string[] = []
      baseData.CPU_STEAL.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.cpu.push({ data: tempData, areaStyle: {}, stack: 'Total', name: 'Steal' })
    }
    {
      let tempData: string[] = []
      baseData.CPU_IDLE.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.cpu.push({ data: tempData, areaStyle: {}, stack: 'Total', name: 'Idle' })
    }
    {
      let tempData: string[] = []
      baseData.CPU_TOTAL_5M_LOAD.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.cpuPayload.push({ data: tempData, name: t('resourceMonitor.cpu.total5mLoad') })
    }
    {
      let tempData: string[] = []
      baseData.CPU_TOTAL_CORE_NUM.forEach((d: number) => {
        tempData.push(toFixed(d))
      })
      metricsData.value.cpuPayload.push({ data: tempData, name: t('resourceMonitor.cpu.coreNum') })
    }

    metricsData.value.cpu = metricsData.value.cpu.sort((a, b) => {
      const sorts = ['CPU_TOTAL', 'CPU_USER', 'CPU_SYSTEM', 'CPU_IOWAIT']
      return sorts.indexOf(a.name) - sorts.indexOf(b.name)
    })
    metricsData.value.cpuPayload = metricsData.value.cpuPayload.sort((a, b) => {
      const sorts = ['CPU_TOTAL_5M_LOAD', 'CPU_TOTAL_CORE_NUM', 'CPU_TOTAL_AVERAGE_UTILIZATION']
      return sorts.indexOf(a.name) - sorts.indexOf(b.name)
    })

    // time
    metricsData.value.time = baseData.time
  },
  { deep: true }
)
const { data: topCPUProcessNowResult, run: loadTOPCPUProcessNow } = useRequest(getTOPCPUProcessNow, { manual: true })
watch(
  topCPUProcessNowResult,
  () => {
    topCPUProcessNowData.value = topCPUProcessNowResult.value ? topCPUProcessNowResult.value[0] : []
    topCPUDBThreadNowData.value = topCPUProcessNowResult.value ? topCPUProcessNowResult.value[1] : []
    topCPUProcessNowData.value.forEach((item) => {
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
      loadTOPCPUProcessNow(props.tabId)
    },
    computed(() => timeInner * 1000)
  )
}
const emit = defineEmits(['goto', 'changeCluster'])
const changeCluster = (row: TopCPUProcessNow) => {
  emit('changeCluster', row.publicIp, row.port)
}
const goto = (key: string, param: object) => {
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
</script>

<style scoped lang="scss"></style>
