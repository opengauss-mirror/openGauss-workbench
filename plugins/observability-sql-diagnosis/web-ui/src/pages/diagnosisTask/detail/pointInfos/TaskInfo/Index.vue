<template>
    <el-tabs v-model="tab" class="tast-detail-tabs">
        <el-tab-pane :label="$t('historyDiagnosis.taskInfo.result')" :name="1">
            <div>
                <div class="info-title">
                    <svg-icon name="note" class="icon" />
                    <div class="title">{{ $t('datasource.taskInfo') }}</div>
                </div>
                <p class="s-i-base-item">
                    {{ $t('app.cluterTitle')
                    }}<span>
                        {{ taskData.pointData.nodeVOSub.clusterId }}/{{
                            taskData.pointData.nodeVOSub.clusterNodes[0].azName
                                ? taskData.pointData.nodeVOSub.clusterNodes[0].azName + '_'
                                : ''
                        }}{{ taskData.pointData.nodeVOSub.clusterNodes[0].publicIp }}:{{
                            taskData.pointData.nodeVOSub.clusterNodes[0].dbPort
                        }}({{ taskData.pointData.nodeVOSub.clusterNodes[0].clusterRole }})
                    </span>
                </p>
                <p class="s-i-base-item">
                    {{ $t('historyDiagnosis.taskInfo.diagnosisRange') }}
                    <span>
                        {{
                            formatDateStartAndEnd(
                                taskData.pointData.hisDataStartTime,
                                taskData.pointData.hisDataEndTime
                            )
                        }}
                    </span>
                </p>
                <p class="s-i-base-item">
                    {{ $t('historyDiagnosis.taskInfo.taskState') }}<span>{{ taskData.pointData.state }}</span>
                </p>
                <p class="s-i-base-item">
                    {{ $t('historyDiagnosis.taskInfo.cost') }}<span>{{ taskData.pointData.cost }}</span>
                </p>
                <p class="s-i-base-item">
                    {{ $t('historyDiagnosis.taskInfo.createTime')
                    }}<span>{{ moment(taskData.pointData.createTime).format('MM-DD HH:mm:ss') }}</span>
                </p>

                <div class="info-title" style="margin-top: 16px; margin-bottom: 8px">
                    <svg-icon name="note" class="detail-icon" />
                    <div class="title">{{ $t('historyDiagnosis.taskInfo.option') }}</div>
                </div>
                <div>
                    <el-checkbox-group>
                        <el-checkbox
                            v-for="item in taskData.pointData.configs"
                            :key="item.option"
                            :label="item.name"
                            :checked="item.value"
                        />
                    </el-checkbox-group>
                </div>
                <div class="info-title" style="margin-top: 16px; margin-bottom: 8px">
                    <svg-icon name="cmd" class="detail-icon" />
                    <div class="title">{{ $t('datasource.taskInfoRemarkKey') }}</div>
                </div>
                <el-input
                    v-model="taskData.pointData.remarks"
                    :rows="8"
                    type="textarea"
                    :input-style="'fontSize:14px'"
                />
            </div>
        </el-tab-pane>
        <el-tab-pane :label="$t('historyDiagnosis.taskInfo.thresholdsRemark')" :name="2">
            <el-table
                :table-layout="'auto'"
                :data="taskData.pointData.thresholds"
                :span-method="objectSpanMethod"
                style="width: 100%"
                :border="true"
                :header-cell-class-name="
                    () => {
                        return 'grid-header'
                    }
                "
            >
                <el-table-column prop="thresholdType" :label="$t('historyDiagnosis.kind')" width="180" />
                <el-table-column prop="thresholdName" :label="$t('historyDiagnosis.thresholdsName')" width="180" />
                <el-table-column prop="thresholdValue" :label="$t('historyDiagnosis.thresholdsConfig')" width="180">
                    <template #default="scope">
                        {{ scope.row.thresholdValue }}{{ scope.row.thresholdUnit ? scope.row.thresholdUnit : '' }}
                    </template>
                </el-table-column>
                <el-table-column prop="thresholdName" :label="$t('historyDiagnosis.thresholdsRemark')" />
            </el-table>
        </el-tab-pane>
    </el-tabs>
</template>

<script lang="ts" setup>
import { getPointData } from '@/api/historyDiagnosis'
import { useRequest } from 'vue-request'
import moment from 'moment'
import type { TableColumnCtx } from 'element-plus'
import { formatDateStartAndEnd } from '@/utils/date'

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
const typeId = ref('TaskInfo')
const taskData = ref<{
    pointData: {
        configs: Array<any>
        thresholds: Array<any>
        state: string
        remarks: string
        cost: string
        createTime: string
        hisDataEndTime: string
        hisDataStartTime: string
        nodeVOSub: {
            clusterId: String
            clusterNodes: Array<any>
        }
    }
}>({
    pointData: {
        createTime: '',
        state: '',
        remarks: '',
        cost: '',
        hisDataEndTime: '',
        hisDataStartTime: '',
        configs: [],
        thresholds: [],
        nodeVOSub: {
            clusterId: '',
            clusterNodes: [{}],
        },
    },
})

interface SpanMethodProps {
    row: any
    column: TableColumnCtx<any>
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
    if (taskData.value.pointData.thresholds.length === 0) return 0
    if (
        rowIndex === 0 ||
        taskData.value.pointData.thresholds[rowIndex].thresholdType !==
            taskData.value.pointData.thresholds[rowIndex - 1].thresholdType
    ) {
        count = 1
        for (let index = rowIndex + 1; index < taskData.value.pointData.thresholds.length; index++) {
            const element = taskData.value.pointData.thresholds[index]
            if (element.thresholdType === taskData.value.pointData.thresholds[rowIndex].thresholdType) count++
            else break
        }
    }
    return count
}
watch(
    () => props.nodesType,
    (val: string) => {
        typeId.value = val
        requestData()
    }
)

onMounted(() => {
    typeId.value = props.nodesType
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
    taskData.value = res
    taskData.value.pointData.remarks = taskData.value.pointData.remarks.replaceAll('<br/>', '\r\n')
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
