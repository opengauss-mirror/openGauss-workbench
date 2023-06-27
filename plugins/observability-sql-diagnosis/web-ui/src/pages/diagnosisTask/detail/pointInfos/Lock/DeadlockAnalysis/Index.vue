<template>
    <el-tabs v-model="tab" class="tast-detail-tabs">
        <point-info-wrapper :point-data="pointInfo">
            <my-card :title="pointData.tableData.title" height="500" :bodyPadding="false">
                <el-table
                    :data="pointData.tableData.data"
                    style="width: 100%; height: 460px"
                    :border="true"
                    :header-cell-class-name="
                        () => {
                            return 'grid-header'
                        }
                    "
                >
                    <el-table-column prop="query_start" :label="$t('historyDiagnosis.lock.logTime')" width="160">
                        <template #default="scope">
                            {{ moment(scope.row.logTime).format('YYYY-MM-DD HH:mm:ss.SSS') }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('historyDiagnosis.lock.processId')" prop="processId" width="140">
                    </el-table-column>
                    <el-table-column
                        :label="$t('historyDiagnosis.lock.transactionId')"
                        prop="transactionId"
                        width="140"
                    >
                    </el-table-column>
                    <el-table-column :label="$t('historyDiagnosis.lock.log')" prop="logData" min-width="200">
                    </el-table-column>
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
    tableData: {
        title: '',
        data: [],
    },
}
const pointData = ref<{
    tableData: {
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
    if (pointInfo.value?.pointState !== 'NORMAL') return

    if (baseData.pointData && baseData.pointData.length > 0) {
        let chartData = baseData.pointData[0]
        pointData.value.tableData = {
            title: chartData.chartName,
            data: chartData.lockAnalysisDTOList,
        }
    }
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
