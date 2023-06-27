<template>
    <el-tabs v-model="tab" class="tast-detail-tabs">
        <point-info-wrapper :point-data="pointInfo">
            <my-card :title="pointData.topSQL.title" height="500" :bodyPadding="false">
                <el-table
                    :data="pointData.topSQL.data"
                    style="width: 100%; height: 460px"
                    :border="true"
                    :header-cell-class-name="
                        () => {
                            return 'grid-header'
                        }
                    "
                >
                    <el-table-column prop="query_id" label="SQLID" width="130" />
                    <el-table-column prop="datname" :label="$t('instanceIndex.dbName')" width="90" />
                    <el-table-column prop="usename" :label="$t('instanceIndex.userName')" width="96" />
                    <el-table-column prop="application_name" :label="$t('instanceIndex.appName')" width="110" />
                    <el-table-column prop="query_start" :label="$t('instanceIndex.startTime')" width="110">
                        <template #default="scope">
                            {{ moment(scope.query_start).format('MM-DD HH:mm:ss') }}
                        </template>
                    </el-table-column>
                    <el-table-column prop="duration" :label="$t('instanceIndex.costTime')" width="60" />
                    <el-table-column prop="query" label="SQL" />
                    <el-table-column
                        prop="sessionid"
                        :label="$t('instanceIndex.sessionId')"
                        width="130"
                    ></el-table-column>
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
    topSQL: {
        title: '',
        data: [],
    },
}
const pointData = ref<{
    topSQL: {
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

    {
        let chartData = baseData.pointData[0][0]
        pointData.value.topSQL = {
            title: chartData.tableName,
            data: chartData.value[0],
        }
    }
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
</style>
