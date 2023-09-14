<template>
  <el-tabs v-model="tab" class="tast-detail-tabs">
    <point-info-wrapper :point-data="pointInfo">
      <my-card :title="pointData.cpuChart.title" height="300" :bodyPadding="false">
        <LazyLine
          :formatter="toFixed"
          :data="pointData.cpuChart.data"
          :xData="pointData.cpuChart.time"
          :max="100"
          :min="0"
          :interval="25"
          :unit="pointData.cpuChart.unit"
          :markArea="pointData.cpuChart.markArea"
        />
      </my-card>
    </point-info-wrapper>
  </el-tabs>
</template>

<script lang="ts" setup>
import { PointInfo, getPointData } from '@/api/historyDiagnosis'
import { useRequest } from 'vue-request'
import LazyLine from '@/components/echarts/LazyLine.vue'
import { toFixed } from '@/shared'
import PointInfoWrapper from '@/pages/diagnosisTask/detail/PointInfoWrapper.vue'

const props = withDefaults(
  defineProps<{
    nodesType: string
    taskId: string
  }>(),
  {
    nodesType: '',
    taskId: '',
  }
)

const tab = ref(1)
const pointInfo = ref<PointInfo | null>(null)
const defaultData = {
  cpuChart: {
    title: '',
    data: [],
    unit: '',
    time: [],
    markArea: [],
  },
}
const pointData = ref<{
  cpuChart: {
    title: string
    data: Array<any>
    unit: string
    time: Array<any>
    markArea: Array<any>
  }
}>(defaultData)

onMounted(() => {
  requestData()
  const wujie = window.$wujie
  if (wujie) {
    wujie?.bus.$on('opengauss-locale-change', (val: string) => {
      nextTick(() => {
        requestData()
      })
    })
  }
})

const { data: res, run: requestData } = useRequest(
  () => {
    return getPointData(props.taskId, props.nodesType)
  },
  { manual: true }
)
watch(res, (res: any) => {
  // clear data
  pointData.value = defaultData

  const baseData = res
  if (!baseData) return

  pointInfo.value = baseData
  if (pointInfo.value?.pointState !== 'SUCCEED') return
  {
    let chartData = baseData.pointData[0][0]
    let tempData: string[] = []

    chartData.datas[0].data.forEach((d: number) => {
      tempData.push(toFixed(d))
    })
    let matchData = findMatchTime(baseData.pointData[1], chartData.time)
    let markArea = []
    if (matchData.length > 0) {
      for (let index = 0; index < matchData.length; index++) {
        const element = matchData[index]
        markArea.push([
          {
            xAxis: element.startTime,
          },
          {
            xAxis: element.endTime,
          },
        ])
      }
    }
    pointData.value.cpuChart = {
      title: chartData.chartName,
      data: [
        {
          data: tempData,
          areaStyle: {},
          stack: 'Total',
          name: chartData.datas[0].name,
        },
      ],
      unit: chartData.unit,
      time: chartData.time,
      markArea,
    }
  }
})
const findMatchTime = (realMarkArea: any, times: any) => {
  console.log('DEBUG: OLD realMarkArea', realMarkArea)
  let timesIndex = 0
  for (let index = 0; index < realMarkArea.length; index++) {
    const element = realMarkArea[index]
    while (times[timesIndex + 1] <= element.startTime && timesIndex < times.length) {
      timesIndex++
    }
    element.startTime = times[timesIndex]
    while (times[timesIndex] <= element.endTime && timesIndex < times.length) {
      console.log('DEBUG: times[timesIndex + 1]', times[timesIndex + 1])
      console.log('DEBUG: element.startTime', element.startTime)
      timesIndex++
    }
    element.endTime = times[timesIndex]
  }
  console.log('DEBUG: realMarkArea', realMarkArea)
  return realMarkArea
}
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
