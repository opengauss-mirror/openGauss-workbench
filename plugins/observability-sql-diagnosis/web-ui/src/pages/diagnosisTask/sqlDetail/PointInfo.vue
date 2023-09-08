<template>
  <div class="s-i-base">
    <!-- Task info for root-->
    <TaskInfo
      v-if="props.nodesType == 'SqlTaskInfo' && props.taskId"
      :nodesType="'SqlTaskInfo'"
      :taskId="props.taskId"
    />
    
    <OldSQLDiagnosis v-else :nodesType="props.nodesType" :taskId="props.taskId" />
  </div>
</template>

<script lang="ts" setup>
import TaskInfo from '@/pages/diagnosisTask/sqlDetail/pointInfos/TaskInfo/Index.vue'

import OldSQLDiagnosis from '@/pages/diagnosisTask/sqlDetail/pointInfos/OldSQLDiagnosis/Index.vue'

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

const typeId = ref('SqlTaskInfo')

onMounted(() => {
  typeId.value = props.nodesType
})
</script>

<style lang="scss" scoped></style>
