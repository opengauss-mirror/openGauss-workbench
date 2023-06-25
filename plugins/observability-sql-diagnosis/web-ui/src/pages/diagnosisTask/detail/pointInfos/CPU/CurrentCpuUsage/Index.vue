<template>
    <el-tabs v-model="tab" class="tast-detail-tabs">
        <el-tab-pane :label="$t('historyDiagnosis.result')" :name="1">
            <div>
                <div class="suggest-content">
                    <svg-icon name="suggest" class="icon" />
                    <div>
                        {{ pointData.suggest }}{{ $t('historyDiagnosis.currentCpuUsage.for') }}{{ pointData.avgUsage }}%
                    </div>
                </div>
            </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('historyDiagnosis.explanation')" :name="2">
            <div class="explanation">{{ pointData.explanation }}</div>
        </el-tab-pane>
    </el-tabs>
</template>

<script lang="ts" setup>
import { getPointData } from '@/api/historyDiagnosis'
import { useRequest } from 'vue-request'

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
const defaultData = {
    suggest: '',
    explanation: '',
    avgUsage: '',
    totalUsage: '',
}
const pointData = ref<{
    suggest: string
    explanation: string
    avgUsage: string
    totalUsage: string
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

    pointData.value.suggest = baseData.pointSuggestion
    pointData.value.explanation = baseData.pointDetail
    {
        let chartData = baseData.pointData[0]
        pointData.value.avgUsage = chartData.avgUsage.toFixed(2)
    }
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
