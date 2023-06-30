<template>
    <div class="top-sql">
        <el-tabs v-model="typeTab">
            <el-tab-pane label="DB_TIME" name="db_time" />
            <el-tab-pane label="CPU_TIME" name="cpu_time" />
            <el-tab-pane label="EXEC_TIME" name="execution_time" />
        </el-tabs>
        <div class="top-sql-table" v-if="!errorInfo" v-loading="loading">
            <el-table :data="data.tableData" border>
                <el-table-column label="SQLID">
                    <template #default="scope">
                        <a class="top-sql-table-id" @click="gotoTopsqlDetail(scope.row.debug_query_id)">
                            {{ scope.row.debug_query_id }}
                        </a>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('sql.dbName')" prop="db_name"></el-table-column>
                <el-table-column :label="$t('sql.schemaName')" prop="schema_name"></el-table-column>
                <el-table-column :label="$t('sql.userName')" prop="user_name"></el-table-column>
                <el-table-column :label="$t('sql.applicationName')" prop="application_name"></el-table-column>
                <el-table-column
                    :label="$t('sql.startTime')"
                    :formatter="(r: any) => moment(r.start_time).format('YYYY-MM-DD HH:mm:ss')"
                    width="120"
                />
                <el-table-column
                    :label="$t('sql.finishTime')"
                    :formatter="(r: any) => moment(r.finish_time).format('YYYY-MM-DD HH:mm:ss')"
                    width="120"
                />
                <el-table-column :label="$t('sql.dbTime')" prop="db_time" width="110"></el-table-column>
                <el-table-column :label="$t('sql.cpuTime')" prop="cpu_time" width="115"></el-table-column>
                <el-table-column
                    :label="$t('sql.excutionTime')"
                    prop="execution_time"
                    :width="i18n.global.locale.value === 'en' ? 150 : 105"
                ></el-table-column>
            </el-table>
        </div>
        <my-message v-if="errorInfo" type="error" :tip="errorInfo" defaultTip="" />
    </div>
</template>

<script setup lang="ts">
import { useRequest } from 'vue-request'
import moment from 'moment'
import { useIntervalTime } from '../../../hooks/time'
import { useMonitorStore } from '../../../store/monitor'
import { storeToRefs } from 'pinia'
import ogRequest from '../../../request'
import router from '../../../router'
import { i18n } from '../../../i18n'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const typeTab = ref('db_time')
const errorInfo = ref<string | Error>()

const props = withDefaults(
    defineProps<{
        instanceId?: string
    }>(),
    {
        instanceId: '',
    }
)

const data = reactive<{
    tableData: Array<Record<string, string>>
}>({
    tableData: [],
})

const curFilter = reactive({
    dbid: '',
    startTime: '2022-09-24 00:00:00',
    finishTime: '2022-10-30 00:00:00',
})

const { tab, time, refreshTime, autoRefresh, rangeTime } = storeToRefs(useMonitorStore())

const { run: requestData, loading } = useRequest(
    (query) => {
        if (tab.value !== 1) {
            return new Promise(() => {})
        }
        const res = new Promise((resolve, reject) => {
            const result = ogRequest.getNative(
                `/observability/v1/topsql/list?id=${query.dbid}&startTime=${query.startTime}&finishTime=${query.finishTime}&orderField=${typeTab.value}`
            )
            result ? resolve(result) : reject(result)
        })
            .then((r: any) => {
                const code = r?.data.code
                const list = r?.data.data
                if (code === '602') {
                    errorInfo.value = t('dashboard.topsqlListTip')
                } else if (code === '200' && Array.isArray(list)) {
                    data.tableData = list
                }
            })
            .catch((e) => {
                if (!props.instanceId) {
                    errorInfo.value = t('dashboard.pleaseChooseinstanceId')
                } else {
                    errorInfo.value = e
                }
            })
        return res
    },
    { manual: true }
)

useIntervalTime(
    () => {
        requestData(curFilter)
    },
    computed(() => refreshTime.value * 1000)
)

const gotoTopsqlDetail = (id: string) => {
    const curMode = localStorage.getItem('INSTANCE_CURRENT_MODE')
    if (curMode === 'wujie') {
        // @ts-ignore plug-in components
        window.$wujie?.props.methods.jump({
            name: `Static-pluginObservability-instanceVemSql_detail`,
            query: {
                dbid: curFilter.dbid,
                id,
            },
        })
    } else {
        // local
        window.sessionStorage.setItem('sqlId', id)
        router.push(`/vem/sql_detail/${curFilter.dbid}/${id}`)
    }
}

const setQueryTime = (range: number, curTime: [Date, Date] | null) => {
    if (range === -1 && curTime != null) {
        curFilter.startTime = moment(curTime[0]).format('YYYY-MM-DD HH:mm:ss')
        curFilter.finishTime = moment(curTime[1]).format('YYYY-MM-DD HH:mm:ss')
        return
    }
    if (range > 0) {
        const finishTimeStamp = new Date().getTime()
        const startTimeStamp = finishTimeStamp - range * 60 * 60 * 1000
        curFilter.startTime = moment(startTimeStamp).format('YYYY-MM-DD HH:mm:ss')
        curFilter.finishTime = moment(finishTimeStamp).format('YYYY-MM-DD HH:mm:ss')
    }
}

setQueryTime(rangeTime.value, time.value)

onMounted(() => {
    if (props.instanceId != null && props.instanceId !== '') {
        curFilter.dbid = props.instanceId
    }
    requestData(curFilter)
})

watch(
    () => props.instanceId,
    (res) => {
        if (typeof res === 'string') {
            curFilter.dbid = res
        }
    }
)

watch(typeTab, () => {
    requestData(curFilter)
})

watch(time, (curTime) => {
    setQueryTime(-1, curTime)
    requestData(curFilter)
})

watch(rangeTime, (r) => {
    if (r === -1) {
        time.value = null
        return
    }
    setQueryTime(r, null)
    requestData(curFilter)
})

watch(autoRefresh, () => {
    requestData(curFilter)
})
</script>

<style scoped lang="scss">
.top-sql {
    &:deep(.el-tabs__header) {
        width: 100%;
    }

    &-table-id {
        color: #0093ff;
        cursor: pointer;
    }
}
</style>
