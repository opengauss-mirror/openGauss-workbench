<template>
    <div class="top-sql">
        <el-tabs v-model="typeTab" class="tab2">
            <el-tab-pane label="DB_TIME" name="db_time" />
            <el-tab-pane label="CPU_TIME" name="cpu_time" />
            <el-tab-pane label="EXEC_TIME" name="execution_time" />
        </el-tabs>
        <div class="top-sql-table" v-if="!errorInfo" v-loading="loading">
            <el-table :data="data.tableData" border>
                <el-table-column label="SQLID" width="150">
                    <template #default="scope">
                        <el-link
                            type="primary"
                            @click="gotoTopsqlDetail(scope.row.debug_query_id)"
                        >
                            {{ scope.row.debug_query_id }}
                        </el-link>
                    </template>
                </el-table-column>
                <el-table-column :label="$t('sql.dbName')" prop="db_name" width="90"></el-table-column>
                <el-table-column :label="$t('sql.schemaName')" prop="schema_name" width="140"></el-table-column>
                <el-table-column :label="$t('sql.userName')" prop="user_name" width="90"></el-table-column>
                <el-table-column :label="$t('sql.applicationName')" prop="application_name"></el-table-column>
                <el-table-column
                    :label="$t('sql.startTime')"
                    :formatter="(r: any) => moment(r.start_time).format('YYYY-MM-DD HH:mm:ss')"
                    width="140"></el-table-column>
                />
                <el-table-column
                    :label="$t('sql.finishTime')"
                    :formatter="(r: any) => moment(r.finish_time).format('YYYY-MM-DD HH:mm:ss')"
                    width="140"></el-table-column>
                />
                <el-table-column :label="$t('sql.dbTime')" prop="db_time" width="110"></el-table-column>
                <el-table-column :label="$t('sql.cpuTime')" prop="cpu_time" width="115"></el-table-column>
                <el-table-column
                    :label="$t('sql.excutionTime')"
                    prop="execution_time"
                    width="120"
                ></el-table-column>
            </el-table>
        </div>
        <my-message v-if="errorInfo" type="error" :tip="errorInfo" defaultTip="" />
    </div>
</template>

<script setup lang="ts">
import { useRequest } from 'vue-request'
import moment from 'moment'
import { useIntervalTime } from '@/hooks/time'
import { useMonitorStore } from '@/store/monitor'
import { storeToRefs } from 'pinia'
import ogRequest from '@/request'
import router from '@/router'
import { i18n } from '@/i18n'
import { useI18n } from 'vue-i18n'
import { tabKeys } from '@/pages/dashboardV2/common'
import { dateFormat } from '@/utils/date'

const { t } = useI18n()

const typeTab = ref('db_time')
const errorInfo = ref<string | Error>()

const props = withDefaults(defineProps<{ tabId: string }>(), {})
const { culRangeTimeAndStep, updateCounter, sourceType, autoRefreshTime, tabNow, instanceId } = storeToRefs(
    useMonitorStore(props.tabId)
)

const data = reactive<{
    tableData: Array<Record<string, string>>
}>({
    tableData: [],
})

const getParam = () => {
    return {
        dbid: instanceId,
        startTime: dateFormat(new Date(culRangeTimeAndStep.value[0] * 1000)),
        finishTime: dateFormat(new Date(culRangeTimeAndStep.value[1] * 1000)),
    }
}

const outsideGoto = (key: string, param: any) => {
    if (param && param.key === tabKeys.InstanceMonitorTOPSQLCPUTime) typeTab.value = 'cpu_time'
}
defineExpose({ outsideGoto })

// same for every page in index
const timer = ref<number>()
onMounted(() => {
    load()
})
watch(
    updateCounter,
    () => {
        clearInterval(timer.value)
        if (tabNow.value === tabKeys.InstanceMonitorTOPSQL) {
            if (updateCounter.value.source === sourceType.value.INSTANCE) load()
            if (updateCounter.value.source === sourceType.value.MANUALREFRESH) load()
            if (updateCounter.value.source === sourceType.value.TIMETYPE) load()
            if (updateCounter.value.source === sourceType.value.TIMERANGE) load()
            if (updateCounter.value.source === sourceType.value.TABCHANGE) load()
            const time = autoRefreshTime.value
            timer.value = useIntervalTime(
                () => {
                    load()
                },
                computed(() => time * 1000)
            )
        }
    },
    { immediate: false }
)

watch(typeTab, () => {
    load()
})
const load = () => {
    requestData(getParam())
}
const { run: requestData, loading } = useRequest(
    (query) => {
        const res = new Promise((resolve, reject) => {
            const result = ogRequest.getNative(
                `/observability/v1/topsql/list?id=${query.dbid}&startTime=${query.startTime}&finishTime=${query.finishTime}&orderField=${typeTab.value}`
            )
            result ? resolve(result) : reject(result)
        })
            .then((r: any) => {
                const code = r?.data.code
                const list = r?.data.data
                if (code === 602) {
                    errorInfo.value = t('dashboard.topsqlListTip')
                } else if (code === 200 && Array.isArray(list)) {
                    errorInfo.value = ''
                    data.tableData = list
                }
            })
            .catch((e) => {
                errorInfo.value = e
            })
        return res
    },
    { manual: true }
)

const gotoTopsqlDetail = (id: string) => {
    const curMode = localStorage.getItem('INSTANCE_CURRENT_MODE')
    if (curMode === 'wujie') {
        // @ts-ignore plug-in components
        window.$wujie?.props.methods.jump({
            name: `Static-pluginObservability-instanceVemSql_detail`,
            query: {
                dbid: getParam().dbid.value,
                id,
            },
        })
    } else {
        // local
        window.sessionStorage.setItem('sqlId', id)
        router.push(`/vem/sql_detail/${getParam().dbid.value}/${id}`)
    }
}
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
