<template>
  <el-tabs v-model="tab" class="tast-detail-tabs">
    <point-info-wrapper :point-data="pointInfo">
      <my-card :title="pointData.session.title" height="300" :bodyPadding="false">
        <LazyLine
          :formatter="toFixed"
          :data="pointData.session.data"
          :xData="pointData.session.time"
          :unit="pointData.session.unit"
        />
      </my-card>

      <my-card :title="pointData.pool.title" height="300" :bodyPadding="false" style="margin-top: 10px">
        <LazyLine
          :formatter="toFixed"
          :data="pointData.pool.data"
          :xData="pointData.pool.time"
          :max="100"
          :min="0"
          :interval="25"
          :unit="pointData.pool.unit"
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
  session: {
    title: '',
    data: [],
    unit: '',
    time: [],
  },
  pool: {
    title: '',
    data: [],
    unit: '',
    time: [],
  },
}
const pointData = ref<{
  session: {
    title: string
    data: Array<any>
    unit: string
    time: Array<any>
  }
  pool: {
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
    let chartData = baseData.pointData[0]
    let tempData: string[] = []

    chartData.datas[0].data.forEach((d: number) => {
      tempData.push(toFixed(d))
    })
    pointData.value.session = {
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
    }
  }
  {
    let chartData = baseData.pointData[1]
    let tempData: string[] = []

    chartData.datas[0].data.forEach((d: number) => {
      tempData.push(toFixed(d))
    })
    pointData.value.pool = {
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
    }
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
