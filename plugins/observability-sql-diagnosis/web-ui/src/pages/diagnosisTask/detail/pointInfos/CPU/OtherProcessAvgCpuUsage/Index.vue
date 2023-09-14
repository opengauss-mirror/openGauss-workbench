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
  },
}
const pointData = ref<{
  cpuChart: {
    title: string
    data: Array<any>
    unit: string
    time: Array<any>
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
    let chartData = baseData.pointData
    let tempData: string[] = []
    let tempData1: string[] = []

    chartData.datas[0].data.forEach((d: number) => {
      tempData.push(toFixed(d))
    })
    chartData.datas[1].data.forEach((d: number) => {
      tempData1.push(toFixed(d))
    })
    pointData.value.cpuChart = {
      title: chartData.chartName,
      data: [
        {
          data: tempData,
          name: chartData.datas[0].name,
        },
        {
          data: tempData1,
          areaStyle: {},
          stack: 'Total',
          name: chartData.datas[1].name,
        },
      ],
      unit: chartData.unit,
      time: chartData.time,
    }
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
