<template>
  <el-tabs v-model="tab" class="tast-detail-tabs">
    <point-info-wrapper :point-data="pointInfo">
      <div class="title">{{ pointData.improvement }}</div>

      <div class="info-title" style="margin-top: 16px; margin-bottom: 8px">
        <svg-icon name="cmd" class="detail-icon" />
        <div class="title">{{ $t('sqlDiagnosisPoints.referSQL') }}</div>
      </div>
      <el-input
        style="margin-bottom: 10px"
        v-model="pointData.advisor"
        :rows="2"
        type="textarea"
        :input-style="'fontSize:14px'"
        readonly
      />

      <my-card
        :title="$t('sqlDiagnosisPoints.beforePlan')"
        height="auto"
        style="margin-bottom: 10px;height:300px"
        :bodyPadding="false"
      >
        <div style="padding: 10px">
          <MyPlan
            v-if="pointData && pointData.firstExplain && pointData.firstExplain"
            :planData="{
              data: [pointData.firstExplain.data],
              total: pointData.firstExplain.total,
            }"
          >
          </MyPlan>
        </div>
      </my-card>

      <my-card :title="$t('sqlDiagnosisPoints.afterPlan')" height="auto" :bodyPadding="false">
        <div style="padding: 10px;height:300px">
          <MyPlan
            v-if="pointData && pointData.afterExplain && pointData.afterExplain"
            :planData="{
              data: [pointData.afterExplain.data],
              total: pointData.afterExplain.total,
            }"
          >
          </MyPlan>
        </div>
      </my-card>
    </point-info-wrapper>
  </el-tabs>
</template>

<script lang="ts" setup>
import { PointInfo, getPointData } from '@/api/historyDiagnosis'
import { useRequest } from 'vue-request'
import PointInfoWrapper from '@/pages/diagnosisTask/detail/PointInfoWrapper.vue'
import MyPlan from '@/components/MyPlan.vue'

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
  console.log(pointData.value)
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
