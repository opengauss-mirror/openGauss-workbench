<template>
  <div class="system-source">
    <my-message
      type="info"
      :tip="`${$t('dashboard.rangeTimeTip')}（${getRangeTime()}）`"
      style="margin-bottom: 8px"
      :key="i18n.global.locale.value"
    />
    <div class="s-i-row">
      <div class="s-i-col">
        <my-card :title="$t('dashboard.cpuUseSituation')" height="255" :bodyPadding="false">
          <div id="system_source_0" style="height: 100%">
            <LazyLine
              :tabId="uuid()"
              :formatter="toFixed"
              :data="metricsData.cpu"
              :xData="metricsData.time"
              :max="100"
              :min="0"
              :interval="25"
              :unit="'%'"
            />
          </div>
        </my-card>
      </div>
      <div class="s-i-col">
        <my-card :title="$t('dashboard.memoryUsage')" height="255" :bodyPadding="false">
          <div id="system_source_1" style="height: 100%">
            <LazyLine
              :tabId="uuid()"
              :formatter="toFixed"
              :data="metricsData.memoryUsed"
              :xData="metricsData.time"
              :max="100"
              :min="0"
              :interval="25"
              :unit="'%'"
            />
          </div>
        </my-card>
      </div>
    </div>
    <div class="s-i-row">
      <div class="s-i-col">
        <my-card :title="$t('dashboard.networkTransmissionRate')" height="255" :bodyPadding="false">
          <div id="system_source_2" style="height: 100%">
            <LazyLine
              :tabId="uuid()"
              :formatter="toFixed"
              :data="metricsData.network"
              :xData="metricsData.time"
              :unit="'M/S'"
            />
          </div>
        </my-card>
      </div>
      <div class="s-i-col">
        <my-card
          :title="$t('resourceMonitor.io.ioUsage')"
          height="255"
          :legend="[
            { color: '#00C7F9', name: $t('metric.read') },
            { color: '#37D4D1', name: $t('metric.write') },
          ]"
          :bodyPadding="false"
        >
          <div id="system_source_3" style="height: 100%">
            <LazyLine
              :tabId="uuid()"
              :formatter="toFixed"
              :data="metricsData.ioUse"
              :xData="metricsData.time"
              :max="100"
              :min="0"
              :interval="25"
              :unit="'%'"
              :tool-tips-sort="'desc'"
              :tool-tips-exclude-zero="true"
            />
          </div>
        </my-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import moment from 'moment'
import LazyLine from '@/components/echarts/LazyLine.vue'
import { toFixed, uuid } from '@/shared'
import { i18n } from '@/i18n'
import { getSQLMetrics } from '@/api/sqlDetail'
import { useRequest } from 'vue-request'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

dayjs.extend(utc)
dayjs.extend(timezone)

interface LineData {
  name: string
  data: any[]
  [other: string]: any
}
interface MetricsData {
  cpu: LineData[]
  memoryUsed: LineData[]
  network: LineData[]
  ioUse: LineData[]
  time: string[]
}
const metricsData = ref<MetricsData>({
  cpu: [],
  memoryUsed: [],
  network: [],
  ioUse: [],
  time: [],
})
const props = withDefaults(
  defineProps<{
    dbid: string
    fixedRangeTime?: string[]
  }>(),
  {
    dbid: '',
    fixedRangeTime: () => [],
  }
)

const dealTime = (value: string) => {
  return moment(value).format('HH:mm:ss')
}

const getRangeTime = () => {
  return `${dealTime(props.fixedRangeTime[0])} ~ ${dealTime(props.fixedRangeTime[1])}`
}

onMounted(() => {
  requestData(
    props.dbid,
    Math.floor(moment(props.fixedRangeTime[0]).valueOf() / 1000).toString(),
    Math.floor(moment(props.fixedRangeTime[1]).valueOf() / 1000).toString(),
    '60'
  )
})

const { data: indexData, run: requestData } = useRequest(getSQLMetrics, { manual: true })
watch(
  indexData,
  () => {
    // clear data
    metricsData.value.cpu = []
    metricsData.value.memoryUsed = []
    metricsData.value.network = []
    metricsData.value.ioUse = []

    const baseData = indexData.value
    if (!baseData) return

    // CPU
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

    // io use
    for (let key in baseData.IO_UTIL) {
      let tempData: string[] = []
      baseData.IO_UTIL[key].forEach((element: any) => {
        tempData.push(toFixed(element))
      })
      metricsData.value.ioUse.push({ data: tempData, name: key, key })
    }

    // time
    metricsData.value.time = baseData.time
  },
  { deep: true }
)
</script>

<style scoped lang="scss">
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
  > div:nth-of-type(1),
  > div:nth-of-type(3) {
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
.system-source {
  .s-i-row {
    width: 100%;
    display: flex;
    justify-content: space-between;
    margin-bottom: 18px;
  }

  .s-i-col {
    height: inherit;
    width: 49%;
    position: relative;
  }
}
</style>
