<script setup lang="ts">
import { useRequest } from 'vue-request'
import { useRouter } from 'vue-router'
import { useDebounceFn } from '@vueuse/core'
import ogRequest from '@/request'
import StatisticalInformation from '@/pages/sql_detail/statistical_information/Index.vue'
import IndexSuggestionData from '@/pages/sql_detail/index_suggestions/Index.vue'
import ObjectInformation from '@/pages/sql_detail/object_information/Index.vue'
import SystemSource from '@/pages/sql_detail/system_source/Index.vue'
import ImplementationPlanThis from '@/pages/sql_detail/implementation_plan/Index.vue'
import SqlDiagnose from '@/pages/sql_detail/sql_diagnose/Index.vue'
import WaitEvent from '@/pages/sql_detail/waitEvent/Index.vue'
import { getDatabaseMetrics } from '@/api/prometheus'

const router = useRouter()

const tab = ref('statisticalInformation')

const urlParam = reactive<{
    dbid: string | string[]
    sqlId: string | string[]
}>({
    dbid: '',
    sqlId: '',
})

const data = reactive<{
    sqlText: string
    statisticalInfo: Record<string, string>
    fixedRangeTime: Array<string>
}>({
    sqlText: '',
    statisticalInfo: {},
    fixedRangeTime: [],
})

const {
    data: statisticalInfoRes,
    run: requestStatisticalInfo,
    loading: statisticalInfoLoading,
} = useRequest(
    (sqlId: string | string[], dbid: string | string[]) =>
        ogRequest.get(`/observability/v1/topsql/detail?id=${dbid}&sqlId=${sqlId}`),
    { manual: true }
)

const editorHeight = ref(200)
const editorMaxHeight = ref(400)
const dbName = ref('')

const resizeBodyHeight = useDebounceFn((height: number) => {
    editorHeight.value = height
}, 100)

onMounted(() => {
    const { sqlId, dbid } = router.currentRoute.value.params
    if (typeof sqlId === 'string' && typeof dbid === 'string') {
        urlParam.dbid = dbid
        urlParam.sqlId = sqlId
    } else {
        // @ts-ignore
        const wujie = window.$wujie
        urlParam.dbid = wujie?.props.data.dbid
        urlParam.sqlId = wujie?.props.data.id
    }
    console.log('sql detail urlParam:', urlParam)
    requestStatisticalInfo(urlParam.sqlId, urlParam.dbid)
    nextTick(() => {
        const domRect = document.querySelector('.page')?.getBoundingClientRect() as DOMRect
        editorHeight.value = Math.floor(domRect.height / 3)
        editorMaxHeight.value = editorHeight.value * 2
    })
})

watch(statisticalInfoRes, (res) => {
    if (res != null) {
        data.statisticalInfo = res
        data.sqlText = res.query
        data.fixedRangeTime = [res.start_time, res.finish_time]
        dbName.value = res.db_name
        getDatabaseMetrics()
    }
})
</script>

<template>
    <div class="sql-detail" v-loading="statisticalInfoLoading">
        <my-card
            :title="$t('sql.sqlText')"
            :bodyPadding="false"
            @resize-body-height="resizeBodyHeight"
            collapse
            resize
            :maxBodyHeight="`${editorMaxHeight}px`"
        >
            <monaco-editor
                :modelValue="data.sqlText"
                style="margin: 4px 0; border: 1px solid #ddd"
                :height="editorHeight"
            />
        </my-card>
        <div class="sql-detail-tab">
            <el-tabs v-model="tab">
                <el-tab-pane :label="$t('sql.statisticalInformation')" name="statisticalInformation">
                    <StatisticalInformation :data="data.statisticalInfo" :loading="statisticalInfoLoading" />
                </el-tab-pane>
                <el-tab-pane :label="$t('sql.implementationPlan')" name="implementationPlan">
                    <ImplementationPlanThis
                        v-if="tab === 'implementationPlan'"
                        :sqlId="urlParam.sqlId"
                        :dbid="urlParam.dbid"
                    />
                </el-tab-pane>
                <el-tab-pane :label="$t('sql.systemSource')" name="systemSource">
                    <SystemSource v-if="tab === 'systemSource'" :fixedRangeTime="data.fixedRangeTime" />
                </el-tab-pane>
                <el-tab-pane :label="$t('sql.objectInformation')" name="objectInformation">
                    <ObjectInformation
                        v-if="tab === 'objectInformation'"
                        :sqlId="urlParam.sqlId"
                        :dbid="urlParam.dbid"
                    />
                </el-tab-pane>
                <el-tab-pane :label="$t('sql.indexSuggestions')" name="indexSuggestions">
                    <IndexSuggestionData
                        v-if="tab === 'indexSuggestions'"
                        :sqlId="urlParam.sqlId"
                        :dbid="urlParam.dbid"
                        :sqlText="data.sqlText"
                    />
                </el-tab-pane>
                <el-tab-pane :label="$t('sql.waitEvent')" name="waitEvent">
                    <WaitEvent
                        v-if="tab === 'waitEvent'"
                        :sqlId="urlParam.sqlId"
                        :dbid="urlParam.dbid"
                        :sqlText="data.sqlText"
                    />
                </el-tab-pane>
                <el-tab-pane :label="$t('sql.sqlDiagnose')" name="diagnose">
                    <SqlDiagnose
                        v-if="tab === 'diagnose'"
                        :sqlId="urlParam.sqlId"
                        :dbid="urlParam.dbid"
                        :dbName="dbName"
                        :sqlText="data.sqlText"
                    />
                </el-tab-pane>
            </el-tabs>
        </div>
    </div>
</template>

<style scoped lang="scss">
.sql-detail {
    /* background-color: transparent; */
    padding: 0;

    &-text {
        margin-top: 10px;
    }

    &-tab {
        font-size: 14px;
        background-color: var(--el-bg-color);
        margin-top: 16px;

        &:deep(.el-tabs__header) {
            background-color: var(--el-bg-color-sub);
            padding: 0 10px;
            margin-bottom: 8px;
        }
    }
}
</style>
