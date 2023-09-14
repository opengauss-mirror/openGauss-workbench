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
          <el-table-column :label="$t('historyDiagnosis.topCPUTimeSQL.uniqueSQLID')" width="130">
            <template #default="scope">
              <a class="top-sql-table-id">
                {{ scope.row.unique_sql_id }}
              </a>
            </template>
          </el-table-column>
          <el-table-column label="SQL" prop="query" width="500"></el-table-column>
          <el-table-column
            :label="$t('historyDiagnosis.topCPUTimeSQL.calls')"
            prop="n_calls"
            width="100"
          ></el-table-column>
          <el-table-column
            :label="$t('historyDiagnosis.topCPUTimeSQL.cpuTime')"
            :formatter="(r: any) =>(r.cpu_time/1000/1000).toFixed()"
            width="100"
          />
          <el-table-column
            :label="$t('historyDiagnosis.topCPUTimeSQL.dbTime')"
            :formatter="(r: any) =>(r.db_time/1000/1000).toFixed()"
            width="100"
          />
          <el-table-column
            :label="$t('historyDiagnosis.topCPUTimeSQL.lastUpdated')"
            :formatter="(r: any) => moment(r.last_updated).format('YYYY-MM-DD HH:mm:ss')"
            width="140"
          />
        </el-table>
      </my-card>
    </point-info-wrapper>
  </el-tabs>
</template>

<script lang="ts" setup>
import { PointInfo, getPointData } from '@/api/historyDiagnosis'
import { useRequest } from 'vue-request'
import moment from 'moment'
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
      title: chartData.tableName,
      data: chartData.value[0],
    }
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
