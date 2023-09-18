<template>
  <el-tabs v-model="tab" class="tast-detail-tabs">
    <point-info-wrapper :point-data="pointInfo">
      <my-card :title="pointData.topSQL.title" height="500" :bodyPadding="false">
        <el-table
          :data="pointData.topSQL.data"
          style="width: 100%; height: 460px"
          :border="true"
          :header-cell-class-name="
            () => {
              return 'grid-header'
            }
          "
        >
          <el-table-column prop="cpu" label="%CPU" width="60" />
          <el-table-column prop="mem" label="%MEM" width="60" />
          <el-table-column prop="command" label="COMMAND" />
          <el-table-column prop="ni" label="NI" width="40" />
          <el-table-column prop="pid" label="PID" width="60" />
          <el-table-column prop="pr" label="PR" width="40" />
          <el-table-column prop="res" label="RES" width="80" />
          <el-table-column prop="s" label="S" width="40" />
          <el-table-column prop="shr" label="SHR" width="80" />
          <el-table-column prop="time" label="TIME+" width="100" />
          <el-table-column prop="user" label="USER" width="120" />
          <el-table-column prop="virt" label="VIRT" width="120" />
        </el-table>
      </my-card>
    </point-info-wrapper>
  </el-tabs>
</template>

<script lang="ts" setup>
import { PointInfo, getPointData } from '@/api/historyDiagnosis'
import { useRequest } from 'vue-request'
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
  topSQL: {
    title: '',
    data: [],
  },
}
const pointData = ref<{
  topSQL: {
    title: string
    data: Array<any>
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
    pointData.value.topSQL = {
      title: chartData.chartName,
      data: chartData.dataList,
    }
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
