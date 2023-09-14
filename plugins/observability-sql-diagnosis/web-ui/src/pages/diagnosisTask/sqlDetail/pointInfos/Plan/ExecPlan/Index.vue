<template>
  <el-tabs v-model="tab" class="tast-detail-tabs">
    <point-info-wrapper :point-data="pointInfo">
      <ImplementationPlanThis
        v-if="pointInfo && pointInfo.pointData && pointInfo.pointData.plan"
        :planDataRes="pointInfo.pointData.plan"
      />
      <StatisticalInformation
        v-if="pointInfo && pointInfo.pointData && pointInfo.pointData.detail && large"
        :data="pointInfo.pointData.detail"
        :large="true"
      />
      <StatisticalInformation
        v-if="pointInfo && pointInfo.pointData && pointInfo.pointData.detail && !large"
        :data="pointInfo.pointData.detail"
        :large="false"
      />
    </point-info-wrapper>
  </el-tabs>
</template>

<script lang="ts" setup>
import { PointInfo, getPointData } from '@/api/historyDiagnosis'
import { useRequest } from 'vue-request'
import PointInfoWrapper from '@/pages/diagnosisTask/detail/PointInfoWrapper.vue'
import ImplementationPlanThis from '@/pages/sql_detail/implementation_plan/Index.vue'
import StatisticalInformation from '@/pages/sql_detail/statistical_information/Index.vue'

const props = withDefaults(
  defineProps<{
    nodesType: string
    taskId: string
    large: boolean
  }>(),
  {
    nodesType: '',
    taskId: '',
    large: false,
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
    return getPointData(props.taskId, props.nodesType, 'sql')
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

  pointData.value = baseData.pointData[0]
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
