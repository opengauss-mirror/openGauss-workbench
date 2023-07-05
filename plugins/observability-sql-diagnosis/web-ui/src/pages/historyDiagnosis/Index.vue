<template>
    <div class="tab-wrapper">
        <el-container>
            <el-main style="position: relative; padding-top: 0px">
                <el-form
                    ref="ruleFormRef"
                    :model="ruleForm"
                    :rules="rules"
                    label-width="0px"
                    class="demo-ruleForm"
                    inline-message
                >
                    <div class="page-header">
                        <div class="icon"></div>
                        <div class="title">{{ $t('histroyDiagnosis.menuName') }}</div>
                        <div class="seperator"></div>
                        <div class="cluster-title">{{ $t('app.cluterTitle') }}</div>

                        <el-form-item prop="nodeId">
                            <ClusterCascader
                                @loaded="clusterLoaded"
                                @getCluster="handleClusterValue"
                                ref="clusterComponent"
                            />
                        </el-form-item>
                    </div>

                    <div
                        class="form page-padding"
                        v-loading="loading"
                        :element-loading-text="$t('historyDiagnosis.diagnosing')"
                    >
                        <div class="title">{{ $t('historyDiagnosis.selectTime') }}</div>
                        <el-radio-group v-model="ruleForm.radio1" class="ml-4">
                            <div style="display: flex; flex-direction: column">
                                <el-form-item prop="timeType1">
                                    <el-input v-model="ruleForm.timeType1" style="display: none"></el-input>
                                    <el-radio label="1" size="large">
                                        <div class="time-row">
                                            <div class="label">{{ $t('app.startTime') }}</div>

                                            <el-date-picker
                                                v-model="ruleForm.hisDataStartTime"
                                                type="datetime"
                                                :valueFormatToUTC="true"
                                            />

                                            <svg-icon class="time" name="right" />

                                            <div class="label">{{ $t('app.endTime') }}</div>
                                            <MyDatePicker
                                                v-model="ruleForm.hisDataEndTime"
                                                type="datetime"
                                                :valueFormatToUTC="true"
                                            />
                                        </div>
                                    </el-radio>
                                </el-form-item>

                                <el-form-item prop="timeType2">
                                    <el-input v-model="ruleForm.timeType2" style="display: none"></el-input>
                                    <el-radio label="2" size="large">
                                        <div class="time-row">
                                            <div class="label">{{ $t('app.startTime') }}</div>
                                            <MyDatePicker
                                                v-model="ruleForm.hisDataStartTime"
                                                type="datetime"
                                                :valueFormatToUTC="true"
                                            />
                                            <svg-icon class="time" name="right" />
                                            <div class="label">{{ $t('historyDiagnosis.nowTime') }}</div>
                                        </div>
                                    </el-radio>
                                </el-form-item>
                            </div>
                        </el-radio-group>

                        <div class="title">{{ $t('historyDiagnosis.option') }}</div>
                        <div>
                            <el-checkbox-group v-model="ruleForm.optionSelected">
                                <el-checkbox v-for="item in options" :key="item.option" :label="item.option">
                                    {{ item.name }}
                                </el-checkbox>
                            </el-checkbox-group>
                        </div>

                        <div class="title">{{ $t('historyDiagnosis.thresholdsThisTime') }}</div>
                        <el-table
                            :table-layout="'auto'"
                            :data="ruleForm.tableData"
                            :span-method="objectSpanMethod"
                            style="width: 100%"
                            :border="true"
                            :header-cell-class-name="girdHeaderClass"
                        >
                            <el-table-column prop="thresholdType" :label="$t('historyDiagnosis.kind')" width="180" />
                            <el-table-column
                                prop="thresholdName"
                                :label="$t('historyDiagnosis.thresholdsName')"
                                width="180"
                            />
                            <el-table-column
                                prop="thresholdValue"
                                :label="$t('historyDiagnosis.thresholdsConfig')"
                                width="150"
                            >
                                <template #default="scope">
                                    <el-form-item
                                        class="table-item"
                                        :prop="'tableData.' + scope.$index + '.thresholdValue'"
                                        :rules="rules.thresholdValue"
                                        :inline-message="false"
                                    >
                                        <el-input v-model="scope.row.thresholdValue" placeholder="Please input">
                                            <template #append>{{ scope.row.thresholdUnit }}</template>
                                        </el-input>
                                    </el-form-item>
                                </template>
                            </el-table-column>
                            <el-table-column prop="thresholdDetail" :label="$t('historyDiagnosis.thresholdsRemark')" />
                        </el-table>

                        <div class="filter" style="margin-top: 32px">
                            <el-button type="primary" @click="diagnosis">{{
                                $t('historyDiagnosis.diagnosis')
                            }}</el-button>
                        </div>
                    </div>
                </el-form>
            </el-main>
        </el-container>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRequest } from 'vue-request'
import type { TableColumnCtx, FormInstance, FormRules } from 'element-plus'

import {
    getOptions,
    getThresholds,
    Threshold,
    DiagnosisParam,
    DiagnosisParamConfig,
    DiagnosisParamThreshold,
    addTask,
} from '@/api/historyDiagnosis'
import { dateAddHour, dateToUTCString } from '@/utils/date'

const { t } = useI18n()
const router = useRouter()
const options = ref<Array<any>>([])
const now = new Date()
const ruleFormRef = ref<FormInstance>()
const clusterComponent = ref()
const ruleForm = reactive<{
    nodeId: string
    timeType1: string
    timeType2: string
    radio1: string
    hisDataStartTime: string
    hisDataEndTime: string
    tableData: Threshold[]
    optionSelected: string[]
}>({
    nodeId: '',
    timeType1: '1',
    timeType2: '1',
    radio1: '1',
    hisDataStartTime: dateToUTCString(dateAddHour(now, -1)),
    hisDataEndTime: dateToUTCString(now),
    tableData: [],
    optionSelected: [],
})

watch(
    () => [ruleForm.radio1, ruleForm.hisDataStartTime, ruleForm.hisDataEndTime],
    () => {
        if (ruleForm.radio1 === '1' && (ruleForm.hisDataStartTime == null || ruleForm.hisDataEndTime == null)) {
            ruleForm.timeType1 = ''
        } else ruleForm.timeType1 = '1'
        if (ruleForm.radio1 === '1') {
            diagnosisParam.value.hisDataStartTime = ruleForm.hisDataStartTime
            diagnosisParam.value.hisDataEndTime = ruleForm.hisDataEndTime
        }
    }
)
watch(
    () => [ruleForm.radio1, ruleForm.hisDataStartTime],
    () => {
        if (ruleForm.radio1 === '2' && ruleForm.hisDataStartTime == null) {
            ruleForm.timeType2 = ''
        } else ruleForm.timeType2 = '1'
        if (ruleForm.radio1 === '2') {
            diagnosisParam.value.hisDataStartTime = ruleForm.hisDataStartTime
            diagnosisParam.value.hisDataEndTime = null
        }
    }
)

const rules = reactive<FormRules>({
    nodeId: [{ required: true, message: t('historyDiagnosis.pleaseSelectInstance'), trigger: 'change' }],
    timeType1: [{ required: true, message: t('historyDiagnosis.pleaseSelectStartAndEndTime'), trigger: 'change' }],
    timeType2: [{ required: true, message: t('historyDiagnosis.pleaseSelectStartTime'), trigger: 'change' }],
    thresholdValue: [{ required: true, message: t('historyDiagnosis.pleaseSelectThreshold'), trigger: 'change' }],
})

const diagnosisParam = ref<DiagnosisParam>({
    nodeId: '',
    hisDataStartTime: dateToUTCString(dateAddHour(now, -1)),
    hisDataEndTime: dateToUTCString(now),
    configs: [],
    thresholds: [],
})

const girdHeaderClass = () => {
    return 'grid-header'
}

interface User {
    id: string
    name: string
    amount1: string
    amount2: string
    amount3: number
}
interface SpanMethodProps {
    row: User
    column: TableColumnCtx<User>
    rowIndex: number
    columnIndex: number
}
const objectSpanMethod = ({ row, column, rowIndex, columnIndex }: SpanMethodProps) => {
    if (columnIndex === 0) {
        let span = culRowspan(rowIndex)
        return {
            rowspan: span,
            colspan: span > 0 ? 1 : 0,
        }
    }
}
const culRowspan = (rowIndex: number) => {
    let count = 0
    if (ruleForm.tableData.length === 0) return 0
    if (
        rowIndex === 0 ||
        ruleForm.tableData[rowIndex].thresholdType !== ruleForm.tableData[rowIndex - 1].thresholdType
    ) {
        count = 1
        for (let index = rowIndex + 1; index < ruleForm.tableData.length; index++) {
            const element = ruleForm.tableData[index]
            if (element.thresholdType === ruleForm.tableData[rowIndex].thresholdType) count++
            else break
        }
    }
    return count
}
const diagnosis = async () => {
    if (!ruleFormRef.value) return
    if (!(await ruleFormRef.value.validate())) return

    // set configs
    let configs: DiagnosisParamConfig[] = []
    ruleForm.optionSelected.forEach((element) => {
        configs.push({
            option: element,
            isCheck: true,
        })
    })
    diagnosisParam.value.configs = configs

    // set thresholds
    let thresholds: DiagnosisParamThreshold[] = []
    ruleForm.tableData.forEach((element) => {
        thresholds.push({ threshold: element.threshold, thresholdValue: element.thresholdValue })
    })
    diagnosisParam.value.thresholds = thresholds

    addDiagnosisTask(diagnosisParam.value)
}
const {
    loading,
    data: addTaskResult,
    run: addDiagnosisTask,
} = useRequest(
    (param) => {
        return addTask(param)
    },
    { manual: true }
)
watch(addTaskResult, (ret: any) => {
    gotoTaskDetail(ret)
})
const gotoTaskDetail = (id: string) => {
    if (process.env.mode === 'production') {
        window.$wujie?.props.methods.jump({
            name: `Static-pluginObservability-sql-diagnosisVemDiagnosisTaskDetail`,
            query: {
                id,
            },
        })
    } else router.push(`/vem/diagnosisTaskDetail/${id}`)
}

const { data: optionData, run: initOptions } = useRequest(
    () => {
        return getOptions()
    },
    { manual: true }
)
const { data: thresholdsData, run: initThresholds } = useRequest(
    () => {
        return getThresholds()
    },
    { manual: true }
)
watch(optionData, (ret: any) => {
    options.value = []
    options.value = ret
})
watch(thresholdsData, (ret: any) => {
    ruleForm.tableData = []
    ruleForm.tableData = ret
})

const handleClusterValue = (val: any) => {
    let nodeId = ''
    if (val.length > 1) nodeId = val[1]
    ruleForm.nodeId = nodeId
    diagnosisParam.value.nodeId = nodeId
}
onMounted(() => {
    // @ts-ignore
    const wujie = window.$wujie
    const startTime = wujie?.props.data.startTime
    const endTime = wujie?.props.data.endTime
    if (startTime && typeof startTime === 'string') ruleForm.hisDataStartTime = startTime
    if (endTime && typeof endTime === 'string') ruleForm.hisDataEndTime = endTime
    initOptions()
    initThresholds()
})
const clusterLoaded = (val: any) => {
    // @ts-ignore
    const wujie = window.$wujie
    const instanceId = wujie?.props.data.instanceId
    if (instanceId && typeof instanceId === 'string') {
        clusterComponent.value.setNodeId(instanceId)
    }
}
</script>

<style scoped lang="scss">
.time-row {
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
    .label {
        margin-right: 8px;
    }
    :deep(.el-date-editor.el-input) {
        width: auto !important;
    }
    :deep(.el-input__wrapper) {
        width: 160px !important;
    }

    .time {
        margin: 0px 10px;
    }
}
.el-form-item {
    margin-bottom: 0px;
}
.table-item.el-form-item.is-error {
    margin-bottom: 18px;
}
</style>
