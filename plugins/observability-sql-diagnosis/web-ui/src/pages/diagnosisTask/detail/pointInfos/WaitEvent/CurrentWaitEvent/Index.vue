<template>
    <el-tabs v-model="tab" class="tast-detail-tabs">
        <el-tab-pane :label="$t('historyDiagnosis.result')" :name="1">
            <div>
                <div class="suggest-content">
                    <svg-icon name="suggest" class="icon" />
                    <div>{{ pointData.suggest }}</div>
                </div>

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
                        <el-table-column
                            :label="$t('historyDiagnosis.waitEvent.eventName')"
                            prop="eventName"
                            width="140"
                        ></el-table-column>
                        <el-table-column
                            :label="$t('historyDiagnosis.waitEvent.sampleNum')"
                            prop="eventCount"
                            width="140"
                        ></el-table-column>
                        <el-table-column
                            :label="$t('historyDiagnosis.waitEvent.description')"
                            prop="eventDetail"
                        ></el-table-column>
                        <el-table-column
                            :label="$t('historyDiagnosis.waitEvent.troubleshooting')"
                            prop="suggestion"
                        ></el-table-column>
                    </el-table>
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
import { i18n } from '@/i18n'
import { useI18n } from 'vue-i18n'
import moment from 'moment'
const { t } = useI18n()

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
    tableData: {
        title: '',
        data: [],
    },
}
const pointData = ref<{
    suggest: string
    explanation: string
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

    pointData.value.suggest = baseData.pointSuggestion
    pointData.value.explanation = baseData.pointDetail
    {
        let chartData = baseData.pointData[0]
        pointData.value.tableData = {
            title: chartData.chartName,
            data: chartData.data,
        }
    }
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
