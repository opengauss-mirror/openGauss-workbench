<template>
    <div class="s-i-base">
        <!-- Task info for root-->
        <TaskInfo v-if="props.nodesType == 'TaskInfo' && props.taskId" :nodesType="'TaskInfo'" :taskId="props.taskId" />

        <!-- CPU -->
        <DbProcessCurrentCpuUsage
            v-if="props.nodesType == 'DbProcessCurrentCpuUsage' && props.taskId"
            :nodesType="'DbProcessCurrentCpuUsage'"
            :taskId="props.taskId"
        />
        <OtherProcessCurrentCpuUsage
            v-if="props.nodesType == 'OtherProcessCurrentCpuUsage' && props.taskId"
            :nodesType="'OtherProcessCurrentCpuUsage'"
            :taskId="props.taskId"
        />
        <TopDbProcess
            v-if="props.nodesType == 'TopDbProcess' && props.taskId"
            :nodesType="'TopDbProcess'"
            :taskId="props.taskId"
        />
        <CurrentCpuUsage
            v-if="props.nodesType == 'CurrentCpuUsage' && props.taskId"
            :nodesType="'CurrentCpuUsage'"
            :taskId="props.taskId"
        />
        <OtherProcessAvgCpuUsage
            v-if="props.nodesType == 'OtherProcessAvgCpuUsage' && props.taskId"
            :nodesType="'OtherProcessAvgCpuUsage'"
            :taskId="props.taskId"
        />
        <DBHostPress
            v-if="props.nodesType == 'DatabaseHostPressure' && props.taskId"
            :nodesType="'DatabaseHostPressure'"
            :taskId="props.taskId"
        />
        <AspAnalysis
            v-if="props.nodesType == 'AspAnalysis' && props.taskId"
            :nodesType="'AspAnalysis'"
            :taskId="props.taskId"
        />
        <BusinessConnCount
            v-if="props.nodesType == 'BusinessConnCount' && props.taskId"
            :nodesType="'BusinessConnCount'"
            :taskId="props.taskId"
        />
        <DBHostPress
            v-if="props.nodesType == 'DatabaseHostLowPressure' && props.taskId"
            :nodesType="'DatabaseHostLowPressure'"
            :taskId="props.taskId"
        />

        <WdrAnalysis
            v-if="props.nodesType == 'WdrAnalysis' && props.taskId"
            :nodesType="'WdrAnalysis'"
            :taskId="props.taskId"
        />

        <AvgCpuUsageRate
            v-if="props.nodesType == 'AvgCpuUsageRate' && props.taskId"
            :nodesType="'AvgCpuUsageRate'"
            :taskId="props.taskId"
        />

        <DbProcessAvgCpuUsage
            v-if="props.nodesType == 'DbProcessAvgCpuUsage' && props.taskId"
            :nodesType="'DbProcessAvgCpuUsage'"
            :taskId="props.taskId"
        />

        <TopCpuTimeSql
            v-if="props.nodesType == 'TopCpuTimeSql' && props.taskId"
            :nodesType="'TopCpuTimeSql'"
            :taskId="props.taskId"
        />

        <CpuTimeTopSql
            v-if="props.nodesType == 'CpuTimeTopSql' && props.taskId"
            :nodesType="'CpuTimeTopSql'"
            :taskId="props.taskId"
        />

        <!-- SQL -->
        <CurrentSlowSql
            v-if="props.nodesType == 'CurrentSlowSql' && props.taskId"
            :nodesType="'CurrentSlowSql'"
            :taskId="props.taskId"
        />
        <PoorSql v-if="props.nodesType == 'PoorSql' && props.taskId" :nodesType="'PoorSql'" :taskId="props.taskId" />
        <HistorySlowSql
            v-if="props.nodesType == 'HistorySlowSql' && props.taskId"
            :nodesType="'HistorySlowSql'"
            :taskId="props.taskId"
        />

        <!-- Wait Event -->
        <WaitEventAnalysis
            v-if="props.nodesType == 'WaitEventAnalysis' && props.taskId"
            :nodesType="'WaitEventAnalysis'"
            :taskId="props.taskId"
        />
        <HistoryWaitEvent
            v-if="props.nodesType == 'HistoryWaitEvent' && props.taskId"
            :nodesType="'HistoryWaitEvent'"
            :taskId="props.taskId"
        />
        <CurrentWaitEvent
            v-if="props.nodesType == 'CurrentWaitEvent' && props.taskId"
            :nodesType="'CurrentWaitEvent'"
            :taskId="props.taskId"
        />

        <!-- Lock -->
        <LockAnalysis
            v-if="props.nodesType == 'LockAnalysis' && props.taskId"
            :nodesType="'LockAnalysis'"
            :taskId="props.taskId"
        />
        <DeadlockAnalysis
            v-if="props.nodesType == 'DeadlockAnalysis' && props.taskId"
            :nodesType="'DeadlockAnalysis'"
            :taskId="props.taskId"
        />
        <LockTimeout
            v-if="props.nodesType == 'LockTimeout' && props.taskId"
            :nodesType="'LockTimeout'"
            :taskId="props.taskId"
        />
    </div>
</template>

<script lang="ts" setup>
import TaskInfo from '@/pages/diagnosisTask/detail/pointInfos/TaskInfo/Index.vue'
import DBHostPress from '@/pages/diagnosisTask/detail/pointInfos/DBHostPress/Index.vue'
// CPU
import DbProcessAvgCpuUsage from '@/pages/diagnosisTask/detail/pointInfos/CPU/DbProcessAvgCpuUsage/Index.vue'
import TopCpuTimeSql from '@/pages/diagnosisTask/detail/pointInfos/CPU/TopCpuTimeSql/Index.vue'
import AvgCpuUsageRate from '@/pages/diagnosisTask/detail/pointInfos/CPU/AvgCpuUsageRate/Index.vue'
import CpuTimeTopSql from '@/pages/diagnosisTask/detail/pointInfos/CPU/CpuTimeTopSql/Index.vue'
import WdrAnalysis from '@/pages/diagnosisTask/detail/pointInfos/CPU/WdrAnalysis/Index.vue'
import AspAnalysis from '@/pages/diagnosisTask/detail/pointInfos/CPU/AspAnalysis/Index.vue'
import BusinessConnCount from '@/pages/diagnosisTask/detail/pointInfos/CPU/BusinessConnCount/Index.vue'
import OtherProcessAvgCpuUsage from '@/pages/diagnosisTask/detail/pointInfos/CPU/OtherProcessAvgCpuUsage/Index.vue'
import CurrentCpuUsage from '@/pages/diagnosisTask/detail/pointInfos/CPU/CurrentCpuUsage/Index.vue'
import TopDbProcess from '@/pages/diagnosisTask/detail/pointInfos/CPU/TopDbProcess/Index.vue'
import OtherProcessCurrentCpuUsage from '@/pages/diagnosisTask/detail/pointInfos/CPU/OtherProcessCurrentCpuUsage/Index.vue'
import DbProcessCurrentCpuUsage from '@/pages/diagnosisTask/detail/pointInfos/CPU/DbProcessCurrentCpuUsage/Index.vue'

// SQL
import PoorSql from '@/pages/diagnosisTask/detail/pointInfos/SQL/PoorSql/Index.vue'
import HistorySlowSql from '@/pages/diagnosisTask/detail/pointInfos/SQL/HistorySlowSql/Index.vue'
import CurrentSlowSql from '@/pages/diagnosisTask/detail/pointInfos/SQL/CurrentSlowSql/Index.vue'

// Wait Event
import WaitEventAnalysis from '@/pages/diagnosisTask/detail/pointInfos/WaitEvent/WaitEventAnalysis/Index.vue'
import HistoryWaitEvent from '@/pages/diagnosisTask/detail/pointInfos/WaitEvent/HistoryWaitEvent/Index.vue'
import CurrentWaitEvent from '@/pages/diagnosisTask/detail/pointInfos/WaitEvent/CurrentWaitEvent/Index.vue'

// Lock
import LockAnalysis from '@/pages/diagnosisTask/detail/pointInfos/Lock/LockAnalysis/Index.vue'
import DeadlockAnalysis from '@/pages/diagnosisTask/detail/pointInfos/Lock/DeadlockAnalysis/Index.vue'
import LockTimeout from '@/pages/diagnosisTask/detail/pointInfos/Lock/LockTimeout/Index.vue'

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

const typeId = ref('TaskInfo')

onMounted(() => {
    typeId.value = props.nodesType
})
</script>

<style lang="scss" scoped></style>
