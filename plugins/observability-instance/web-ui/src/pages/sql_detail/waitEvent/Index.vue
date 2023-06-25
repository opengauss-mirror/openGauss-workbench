<template>
    <div class="index-suggestions" v-loading="loading">
        <el-table
            :data="data"
            style="width: 100%; height: 560px"
            border
            :header-cell-class-name="
                () => {
                    return 'grid-header'
                }
            "
        >
            <el-table-column prop="starttime" :label="$t('sql.waitEventTable.startTime')">
                <template #default="scope">
                    {{ convertToCurrentTime(scope.row.starttime) }}
                </template>
            </el-table-column>
            <el-table-column prop="lockType" :label="$t('sql.waitEventTable.waitEventName')" />
            <el-table-column prop="waittime" :label="$t('sql.waitEventTable.waitLockTime')" />
        </el-table>
    </div>
</template>

<script setup lang="ts">
import { useRequest } from 'vue-request'
import moment from 'moment'
import { getSQLEvent } from '@/api/sqlDetail'

const props = withDefaults(
    defineProps<{
        dbid: string
        sqlId: string
        sqlText: string
    }>(),
    {
        dbid: '',
        sqlId: '',
        sqlText: '',
    }
)

const data = ref<Array<any>>([])

const convertToCurrentTime = (timeStr: string) => {
    let momentObj = moment(timeStr, 'YYYY-MM-DD HH:mm:ss.SSS') // 解析字符串为 moment 对象
    let currentTimeString = moment().format('YYYY-MM-DD HH:mm:ss.SSS') // 获取当前时间的字符串表示
    let diff = momentObj.diff(currentTimeString) // 计算时间差
    let currentMomentObj = moment().add(diff, 'milliseconds') // 将时间差加到当前时间上，得到与给定时间相同的 Moment 对象
    let ms = timeStr.substring(23, timeStr.length - 3)
    return currentMomentObj.format('YYYY-MM-DD HH:mm:ss.SSS') + ms // 将 Moment 对象转换为字符串并返回（注意使用了 'SSSSSS' 和 'ZZ' 格式化字符串）
}

// load data
const { data: eventResult, loading, run: loadData } = useRequest(getSQLEvent, { manual: true })
watch(
    eventResult,
    () => {
        if (eventResult.value) data.value = eventResult.value
    },
    { deep: true }
)

onMounted(() => {
    loadData(props.dbid, props.sqlId)
})
</script>

<style scoped lang="scss">
.index-suggestions {
    font-size: 14px;
    border-top: 1px solid var(--el-color--col-border-color);
    min-height: 100px;

    .i-s-el-col-index {
        border-right: 1px solid var(--el-color--col-border-color);
        text-align: center;
    }

    .i-s-el-col-content {
        text-align: left;
        padding-left: 10px;
    }

    &:deep(.el-row) {
        line-height: 39px;
        border: 1px solid var(--el-color--col-border-color);
        border-top: none;
    }
}
</style>
