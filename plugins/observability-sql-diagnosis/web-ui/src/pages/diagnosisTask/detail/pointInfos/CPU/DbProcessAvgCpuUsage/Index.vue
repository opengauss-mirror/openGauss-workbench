<template>
    <el-tabs v-model="tab" class="tast-detail-tabs">
        <el-tab-pane :label="$t('historyDiagnosis.result')" :name="1">
            <div>
                <div class="suggest-content">
                    <svg-icon name="suggest" class="icon" />
                    <div>{{ pointData.suggest }}</div>
                </div>

                <my-card :title="pointData.cpuChart.title" height="300" :bodyPadding="false">
                    <LazyLine
                        :formatter="toFixed"
                        :data="pointData.cpuChart.data"
                        :xData="pointData.cpuChart.time"
                        :max="100"
                        :min="0"
                        :interval="25"
                        :unit="pointData.cpuChart.unit"
                    />
                </my-card>
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
import LazyLine from '@/components/echarts/LazyLine.vue'
import { toFixed } from '@/shared'

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
    cpuChart: {
        title: '',
        data: [],
        unit: '',
        time: [],
    },
}
const pointData = ref<{
    suggest: string
    explanation: string
    cpuChart: {
        title: string
        data: Array<any>
        unit: string
        time: Array<any>
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

    pointData.value.suggest = baseData.pointSuggestion
    pointData.value.explanation = baseData.pointDetail
    {
        let chartData = baseData.pointData[0]
        let tempData: string[] = []

        chartData.datas[0].data.forEach((d: number) => {
            tempData.push(toFixed(d))
        })
        pointData.value.cpuChart = {
            title: chartData.chartName,
            data: [
                {
                    data: tempData,
                    areaStyle: {},
                    stack: 'Total',
                    name: chartData.datas[0].name,
                },
            ],
            unit: chartData.unit,
            time: chartData.time,
        }
    }
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
