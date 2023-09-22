<template>
  <el-tabs v-model="tab" class="tast-detail-tabs">
    <el-table :data="pointData" style="width: 100%" border show-overflow-tooltip>
        <el-table-column prop="wSessionId" :label="$t('sql.blockSession.sessionId')" width="130"></el-table-column>
        <el-table-column :label="$t('sql.blockSession.blockSessionID')" prop="lSessionId" width="130"></el-table-column>
        <el-table-column prop="startTime" :label="$t('sql.blockSession.startTime')" :formatter="(r:any) => dateFormat(r.startTime, 'YYYY-MM-DD HH:mm:ss')" width="140" />
        <el-table-column prop="endTime" :label="$t('sql.blockSession.endTime')" :formatter="(r:any) => dateFormat(r.endTime, 'YYYY-MM-DD HH:mm:ss')" width="140" />
        <el-table-column prop="state" :label="$t('sql.blockSession.waitStatus')" width="100" />
        <el-table-column prop="lockingQuery" label="sql" min-width="200"/>
        <el-table-column prop="mode" :label="$t('sql.blockSession.waitLockMode')" width="120" />
        <el-table-column prop="tableName" :label="$t('sql.blockSession.tableName')" width="150" />
        <el-table-column prop="lUser" :label="$t('sql.blockSession.userName')" width="90" />
        <el-table-column prop="clientAddress" :label="$t('sql.blockSession.clientIP')" width="120" />
        <el-table-column prop="applicationName" :label="$t('sql.blockSession.applicationName')" width="150"/>
      </el-table>
  </el-tabs>
</template>

<script setup lang='ts'>
import { getPointData } from '@/api/historyDiagnosis'
import { useRequest } from 'vue-request'
import { dateFormat } from '@/utils/date'

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
const pointData = ref<any[]>([])

const { data: res, run: requestData } = useRequest(
  () => {
    return getPointData(props.taskId, props.nodesType, 'sql')
  },
  { manual: true }
)
watch(res, (res: any) => {
  // clear data
  pointData.value = []
  if (res.pointState !== 'SUCCEED') {
    return
  }
  pointData.value = res.pointData || []
})

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
</script>
<style scoped lang='scss'>
@use '@/assets/style/task.scss' as *;
</style>