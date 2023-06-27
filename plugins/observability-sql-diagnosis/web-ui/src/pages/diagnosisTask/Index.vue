<template>
    <el-tabs v-model="tab" class="tast-detail-tabs">
        <el-tab-pane :label="$t('historyDiagnosis.result')" :name="1">
            <div>
                <div class="suggest-content" v-if="pointData.state === 'NOT_ANALYZED'">
                    <svg-icon name="suggest" class="icon" />
                    <div>此诊断点未进行诊断</div>
                </div>

                <div class="suggest-content abnormal" v-else-if="pointData.state === 'ABNORMAL'">
                    <svg-icon name="suggest_abnormal" class="icon" />
                    <div>{{ pointData.suggest }}</div>
                </div>

                <div v-else>
                    <div class="suggest-content suggestions" v-if="pointData.isHint === 'SUGGESTIONS'">
                        <svg-icon name="suggest_suggestions" class="icon" />
                        <div>{{ pointData.suggest }}</div>
                    </div>
                    <div class="suggest-content" v-else>
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
                                prop="query_start"
                                :label="$t('historyDiagnosis.lock.logTime')"
                                width="160"
                            >
                                <template #default="scope">
                                    {{ moment(scope.row.logTime).format('YYYY-MM-DD HH:mm:ss.SSS') }}
                                </template>
                            </el-table-column>
                            <el-table-column
                                :label="$t('historyDiagnosis.lock.processId')"
                                prop="processId"
                                width="140"
                            >
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
    isHint: '',
    state: '',
    suggest: '',
    explanation: '',
    tableData: {
        title: '',
        data: [],
    },
}
const pointData = ref<{
    isHint: string
    state: string
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

    pointData.value.isHint = baseData.isHint
    pointData.value.state = baseData.pointState
    pointData.value.suggest = baseData.pointSuggestion
    pointData.value.explanation = baseData.pointDetail
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
