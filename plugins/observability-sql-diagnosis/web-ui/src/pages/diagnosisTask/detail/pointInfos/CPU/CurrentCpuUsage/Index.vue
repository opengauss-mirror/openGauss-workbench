<template>
    <el-tabs v-model="tab" class="tast-detail-tabs">
        <point-info-wrapper :point-data="pointInfo">
            <div>{{ $t('historyDiagnosis.currentCpuUsage.for') }}{{ pointData.avgUsage }}%</div>
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
    avgUsage: '',
    totalUsage: '',
}
const pointData = ref<{
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

    pointInfo.value = baseData
    if (pointInfo.value?.pointState !== 'NORMAL') return

    {
        let chartData = baseData.pointData[0]
        pointData.value.avgUsage = chartData.avgUsage.toFixed(2)
    }
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
