<template>
  <IndexBar :tabId="props.tabId"></IndexBar>
  <div style="margin-bottom: 38px"></div>
  <my-card :title="$t('instanceMonitor.asp.sampleActiveSessionCount')" height="200" :bodyPadding="false">
    <LazyLine
      :rangeSelect="true"
      :tabId="props.tabId"
      :formatter="toFixed"
      :data="metricsData.sessionCount"
      :xData="metricsData.time"
    />
  </my-card>

  <div class="gap-row"></div>

  <my-card :title="$t('instanceMonitor.asp.aspAnalysis')" height="300" :bodyPadding="false">
    <div class="asp-row">
      <div class="title">{{ $t('instanceMonitor.asp.analysisMetrics') }}</div>
      <el-select class="category" v-model="optionValue" @change="selectChange">
        <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <div class="title">{{ $t('instanceMonitor.asp.filterConditions') }}</div>
      <div class="filter">
        <div class="item" v-for="item in filter" :key="item">
          {{ item.label }}={{ item.value }}
          <svg-icon class="close" name="close" style="margin-left: 4px" @click="removeFilter(item)" />
        </div>
      </div>
      <div style="flex-grow: 1"></div>
      <div class="line-tips">
        <div><svg-icon name="info" />{{ $t('instanceMonitor.asp.clickLegendToAddFilter') }}</div>
      </div>
    </div>
    <div style="height: 200px">
      <LazyLine
        :tabId="props.tabId"
        :formatter="toFixed"
        :data="analyzeData.data"
        :xData="analyzeData.time"
        @legendSelected="legendSelected"
        :bar="true"
      />
    </div>
  </my-card>

  <div class="gap-row"></div>

  <el-row :gutter="12" style="min-height: 420px;">
    <el-col :span="12">
      <div class="table-column">
        <div class="header">
          <el-select class="category" v-model="tableOption1" @change="selectChangeTable1">
            <el-option v-for="item in tableOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>
        <el-table class="table" :table-layout="'auto'" :data="table1" style="width: 100%" :border="true">
          <el-table-column :label="tableOptions.find((item) => item.value == tableOption1)?.label" width="150">
            <template #default="scope">
              <el-link type="primary" @click="gotoTopsqlDetail(scope.row.key)">
                {{ scope.row.key }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column :label="$t('instanceMonitor.asp.sampleCount')">
            <template #default="scope">
              <div
                style="
                  width: 100%;
                  display: flex;
                  flex-direction: row;
                  align-items: center;
                  justify-content: flex-start;
                "
              >
                <div class="persent-row">
                  <div
                    class="persent"
                    :style="{ width: scope.row.persent ? scope.row.persent * 100 * 0.8 + '%' : 'auto' }"
                  ></div>
                  <div class="persent-text">
                    {{ scope.row.count }}({{ Number(scope.row.persent * 100).toFixed(2) }}%)
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-col>
    <el-col :span="12">
      <div class="table-column">
        <div class="header">
          <el-select class="category" v-model="tableOption2" @change="selectChangeTable2">
            <el-option v-for="item in tableOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>
        <el-table class="table" :table-layout="'auto'" :data="table2" style="width: 100%" :border="true">
          <el-table-column :label="tableOptions.find((item) => item.value == tableOption2)?.label" width="150">
            <template #default="scope">
              <el-link v-if="tableOption2 == 'sessionid'" type="primary" @click="gotoSessionDetail(scope.row.key)">
                {{ scope.row.key }}
              </el-link>
              <el-link v-if="tableOption2 == 'queryId'" type="primary" @click="gotoTopsqlDetail(scope.row.key)">
                {{ scope.row.key }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column :label="$t('instanceMonitor.asp.sampleCount')">
            <template #default="scope">
              <div
                style="
                  width: 100%;
                  display: flex;
                  flex-direction: row;
                  align-items: center;
                  justify-content: flex-start;
                "
              >
                <div class="persent-row">
                  <div
                    class="persent"
                    :style="{ width: scope.row.persent ? scope.row.persent * 100 * 0.8 + '%' : 'auto' }"
                  ></div>
                  <div class="persent-text">
                    {{ scope.row.count }}({{ Number(scope.row.persent * 100).toFixed(2) }}%)
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import LazyLine from '@/components/echarts/LazyLine.vue'
import { useMonitorStore } from '@/store/monitor'
import { toFixed } from '@/shared'
import { storeToRefs } from 'pinia'
import { getAspCount, getAspAnalysis } from '@/api/asp'
import { useIntervalTime } from '@/hooks/time'
import { tabKeys } from '@/pages/dashboardV2/common'
import { useRequest } from 'vue-request'
import router from '@/router'

const { t } = useI18n()

const props = withDefaults(defineProps<{ tabId: string }>(), {})

const optionValue = ref('applicationName')
const options = [
  {
    value: 'waitStatus',
    label: 'Wait Status',
  },
  {
    value: 'event',
    label: 'Event',
  },
  {
    value: 'databaseid',
    label: 'Database ID',
  },
  {
    value: 'applicationName',
    label: 'Application Name',
  },
]
const tableOption1 = ref('queryId')
const tableOption2 = ref('sessionid')
const tableOptions = [
  {
    value: 'queryId',
    label: 'SQL ID',
  },
  {
    value: 'sessionid',
    label: 'Session ID',
  },
]

interface LineData {
  name: string
  data: any[]
  [other: string]: any
}
interface ActiveSessionData {
  sessionCount: LineData[]
  time: string[]
}
const metricsData = ref<ActiveSessionData>({
  sessionCount: [],
  time: [],
})
interface AnalyzeData {
  data: LineData[]
  time: string[]
}
const analyzeData = ref<AnalyzeData>({
  data: [],
  time: [],
})
const filter = ref<any[]>([])
const filterValue = ref<string[]>([])
const table1 = ref<any[]>([])
const table2 = ref<any[]>([])

const { updateCounter, sourceType, autoRefreshTime, tabNow, instanceId } = storeToRefs(useMonitorStore(props.tabId))

// same for every page in index
const timer = ref<number>()
onMounted(() => {
  load()
})
watch(
  updateCounter,
  () => {
    clearInterval(timer.value)
    if (tabNow.value === tabKeys.ASP) {
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
const load = (checkTab?: boolean, checkRange?: boolean) => {
  if (!instanceId.value) return
  requestData(props.tabId)
  requestData2(props.tabId)
}

// load data
const legendSelected = (key: string) => {
  if (!filter.value.some((item) => item.category === optionValue.value)) {
    filter.value.push({
      category: optionValue.value,
      label: options.find((item) => item.value === optionValue.value)?.label,
      value: key,
    })
  }
  filterValue.value = []
  filter.value.forEach((filterItem) => {
    filterValue.value.push(filterItem.label + '=' + filterItem.value)
  })
  analyzeData.value = getAnalyzeData()
}
const removeFilter = (item: any) => {
  let index = filter.value.indexOf(item)
  if (index >= 0) {
    filter.value.splice(index, 1)
    filterValue.value.splice(index, 1)
  }
  analyzeData.value = getAnalyzeData()
}
const { data: indexData, run: requestData } = useRequest(getAspCount, { manual: true })
watch(
  indexData,
  () => {
    // clear data
    metricsData.value.sessionCount = []
    const baseData = indexData.value
    if (!baseData) return

    // TPS
    if (baseData) {
      metricsData.value.sessionCount.push({
        data: baseData.sessionCount,
        name: t('instanceMonitor.asp.activeSessionCount'),
      })
      metricsData.value.time = baseData.sampleTime
    }

    // time
  },
  { deep: true }
)
const selectChange = () => {
  analyzeData.value = getAnalyzeData()
}
const selectChangeTable1 = () => {
  table1.value = getTopData(tableOption1.value)
}
const selectChangeTable2 = () => {
  table2.value = getTopData(tableOption2.value)
}
const { data: indexData2, run: requestData2 } = useRequest(getAspAnalysis, { manual: true })
watch(
  indexData2,
  () => {
    analyzeData.value = getAnalyzeData()
    // time
  },
  { deep: true }
)
const getAnalyzeData = () => {
  analyzeData.value = {
    data: [],
    time: [],
  }
  // clear data
  let dataTemp: any[] = []
  let timeTemp: Set<any> = new Set()
  if (indexData2.value) {
    table1.value = getTopData(tableOption1.value)
    table2.value = getTopData(tableOption2.value)

    let data = indexData2.value

    let categoryField = optionValue.value

    // output sampleTime Array
    timeTemp = new Set(data.map((obj) => obj.sampleTime))

    let filterData = data.filter((obj) => {
      let match = true
      filter.value.forEach((filterItem) => {
        if (obj[filterItem.category] !== filterItem.value) match = false
      })
      return match
    })

    // Count the number of categoryField
    const categorySet = new Set(filterData.map((obj) => obj[categoryField]))

    dataTemp.push({ data: [], name: 'Total', type: 'line', step: 'middle' })
    categorySet.forEach((categoryItem) => {
      dataTemp.push({ data: [], name: categoryItem, stack: 'Total', areaStyle: {} })
    })

    let lastDate = ''
    let indexData = -1
    for (let index = 0; index < data.length; index++) {
      const element = data[index]
      if (element.sampleTime !== lastDate) {
        dataTemp.forEach((obj) => {
          obj.data.push(0)
        })
        indexData++
        lastDate = element.sampleTime
      }

      let match = true
      filter.value.forEach((filterItem) => {
        if (element[filterItem.category] !== filterItem.value) match = false
      })
      if (match) {
        dataTemp.forEach((dateItem) => {
          if (dateItem.name === element[categoryField]) {
            dateItem.data[indexData]++
          }
        })
      }
      dataTemp[0].data[indexData]++
    }
  }

  // time
  return {
    data: dataTemp,
    time: [...timeTemp],
  }
}
const getTopData = (analyzeField: any) => {
  let data = indexData2.value
  if (!data) return []

  let filterData = data.filter((obj) => {
    let match = true
    filter.value.forEach((filterItem) => {
      if (obj[filterItem.category] !== filterItem.value) match = false
    })
    return match
  })

  const sum = filterData.reduce((accumulator: number) => accumulator + 1, 0)

  // Using the reduce method for grouping and counting.
  const countByQueryId = filterData.reduce((result, obj) => {
    const analyzeFiledValue = obj[analyzeField]
    result[analyzeFiledValue] = (result[analyzeFiledValue] || 0) + 1
    return result
  }, {})

  // Convert to an array and sort by occurrence count.
  const sortedArray = Object.entries(countByQueryId)
    .map(([key, count]) => ({ key, count, persent: ((count as number) / sum).toFixed(2) }))
    .sort((a, b) => (b.count as number) - (a.count as number))

  return sortedArray.slice(0, 10)
}

const getParam = () => {
  return {
    dbid: instanceId,
  }
}
const gotoTopsqlDetail = (id: string) => {
  const curMode = localStorage.getItem('INSTANCE_CURRENT_MODE')
  if (curMode === 'wujie') {
    // @ts-ignore plug-in components
    window.$wujie?.props.methods.jump({
      name: `Static-pluginObservability-instanceVemSql_detail`,
      query: {
        dbid: getParam().dbid.value,
        id,
      },
    })
  } else {
    // local
    window.sessionStorage.setItem('sqlId', id)
    router.push(`/vem/sql_detail/${getParam().dbid.value}/${id}`)
  }
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
.asp-row {
  font-size: 12px;
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 16px 16px 0px 16px;
  .title {
    flex-shrink: 0;
    margin-right: 6px;
    &:not(:first-child) {
      margin-left: 16px;
    }
  }

  .category {
    width: 150px;
    flex-shrink: 0;
  }
  .filter {
    min-width: 200px;
    padding: 0px 4px;
    height: 30px;
    align-items: center;
    border-radius: 2px;
    border: 1px solid var(--unnamed, #d9d9d9);
    display: flex;
    flex-direction: row;
    gap: 4px;
    overflow-x: auto;
    .item {
      height: 20px;
      padding: 0px 4px 0px 8px;
      border-radius: 2px;
      border: 1px solid var(--unnamed, #d9d9d9);
      background: var(--fill, #f7f7f7);
      flex-shrink: 0;
    }
  }
  .line-tips {
    position: inherit;
    margin-left: 16px;
    flex-shrink: 0;
  }
}
::-webkit-scrollbar {
  width: 1px;
  height: 5px;
  background-color: skyblue;
}
::-webkit-scrollbar-thumb {
  background-color: orange;
}
.table-column {
  .header {
    display: flex;
    padding: 4px;
    align-items: center;
    border-top: 1px solid var(--dividers, #f0f0f0);
    border-right: 1px solid var(--dividers, #f0f0f0);
    background: var(--fill, #f7f7f7);
  }
}

.persent-row {
  flex-grow: 1;
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
  .persent {
    min-width: 2px;
    height: 8px;
    border-radius: 1px;
    background: #246cff;
  }
  .persent-text {
    margin-left: 2px;
  }
}
</style>
