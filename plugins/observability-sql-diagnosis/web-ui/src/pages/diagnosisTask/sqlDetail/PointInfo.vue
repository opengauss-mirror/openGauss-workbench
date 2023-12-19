<template>
  <div class="s-i-base">
    <!-- Task info for root-->
    <TaskInfo
      v-if="props.nodesType == 'SqlTaskInfo' && props.taskId"
      :nodesType="'SqlTaskInfo'"
      :taskId="props.taskId"
    />

    <!-- Plan -->
    <IndexAdvisor
      v-else-if="props.nodesType == 'IndexAdvisor' && props.taskId"
      :nodesType="'IndexAdvisor'"
      :taskId="props.taskId"
    />
    <SmpParallelQuery
      v-else-if="props.nodesType == 'SmpParallelQuery' && props.taskId"
      :nodesType="'SmpParallelQuery'"
      :taskId="props.taskId"
    />
    <ExecPlan
      v-else-if="props.nodesType == 'ExecPlan' && props.taskId"
      :nodesType="'ExecPlan'"
      :taskId="props.taskId"
      :large="large"
    />
    <BlockSession
      v-else-if="props.nodesType == 'BlockSession' && props.taskId"
      :nodesType="'BlockSession'"
      :taskId="props.taskId"
    />

    <AutoShowData v-else :nodesType="props.nodesType" :taskId="props.taskId" :diagnosisType="'sql'" />
  </div>
</template>

<script lang="ts" setup>
import TaskInfo from '@/pages/diagnosisTask/sqlDetail/pointInfos/TaskInfo/Index.vue'

import AutoShowData from '@/pages/diagnosisTask/detail/pointInfos/AutoShowData/Index.vue'
import IndexAdvisor from '@/pages/diagnosisTask/sqlDetail/pointInfos/Plan/IndexAdvisor/Index.vue'
import SmpParallelQuery from '@/pages/diagnosisTask/sqlDetail/pointInfos/Plan/SmpParallelQuery/Index.vue'
import ExecPlan from '@/pages/diagnosisTask/sqlDetail/pointInfos/Plan/ExecPlan/Index.vue'
import BlockSession from '@/pages/diagnosisTask/sqlDetail/pointInfos/Plan/BlockSession/Index.vue'

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

const typeId = ref('SqlTaskInfo')

onMounted(() => {
  typeId.value = props.nodesType
})
</script>

<style lang="scss" scoped></style>
