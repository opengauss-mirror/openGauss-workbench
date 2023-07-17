<template>
  <div class="report-container">
    <div class="report-header">
      {{ getContent(data.currentReportScene.title, data.curTableName, props.reportId, 'title') }}
    </div>
    <div class="report-main">
      <div class="report-main-header">
        <div v-if="!['ExecPlan'].includes(props.reportId as string)">{{ data.currentReportScene.name }}</div>
        <div class="report-main-suggestion" v-if="!['ObjectInfoCheck', 'ExecPlan'].includes(props.reportId as string)">
          <el-icon color="#0093FF" size="18px">
            <WarningFilled />
          </el-icon>
          <span class="report-main-suggestion-text">{{
            getContent(data.currentReportScene.advise, data.curTableName, props.reportId, 'advise')
          }}</span>
        </div>
        <div class="report-main-suggestion" v-if="['ObjectInfoCheck'].includes(props.reportId as string)">
          <div class="report-main-suggestion-analysisIdea" v-html="data.currentReportScene.analysisIdea"></div>
        </div>
        <div v-if="['ExecPlan'].includes(props.reportId as string)">
          <ImplementationPlanThis v-if="taskInfo.debugQueryId" :sqlId="taskInfo.debugQueryId" :dbid="taskInfo.nodeId" />
          <StatisticalInformation
            :large="large"
            v-if="taskInfo.debugQueryId"
            :sqlId="taskInfo.debugQueryId"
            :dbid="taskInfo.nodeId"
          />
        </div>
      </div>
      <div class="report-main-body" :style="`max-height: ${bodyHeight}px`">
        <div
          class="report-main-box"
          v-if="judgeIsShowMadal(['BaseInfo', 'ObjectStructure', 'PartitionInformation', 'IndexInformation'])"
        >
          <div class="report-main-box-header">{{ getMostCostTableTitle(data.curTableName, props.reportId) }}</div>
          <BaseInfo
            :metaData="data.tableMetaData"
            :nodeType="props.reportId"
            :key="props.reportId"
            v-if="judgeIsShowMadal(['BaseInfo'])"
          />
          <ObjectStructure :structureData="data.tableStructureData" v-if="judgeIsShowMadal(['ObjectStructure'])" />
          <PartitionInformation
            :partitionInfoList="data.partitionInfoList"
            v-if="judgeIsShowMadal(['PartitionInformation'])"
          />
          <IndexInformation :indexData="data.tableIndexData" v-if="judgeIsShowMadal(['IndexInformation'])" />
        </div>
        <div v-if="'PlanRecommendedToCreateIndex' === props.reportId && data.otherStructureAndIndexList.length > 0">
          <div class="report-main-box" v-for="item in data.otherStructureAndIndexList" :key="item.tableName">
            <div class="report-main-box-header">{{ `${item.tableName} ${$t('report.table')}` }}</div>
            <ObjectStructure :structureData="item.structureData" />
            <IndexInformation :indexData="item.indexData" />
          </div>
        </div>
        <div class="report-main-box" v-if="judgeIsShowMadal(['ImplementationPlan'])">
          <div class="report-main-box-header">{{ $t('report.originalExecutionPlan') }}</div>
          <ImplementationPlan :plan="data.jsonQueryPlan" :nodeId="props.id" />
        </div>
        <div class="report-main-box" v-if="judgeIsShowMadal(['RowsDiff', 'WorkMemDiff', 'ExplainAnalyze'])">
          <RowsDiff
            :rowdDiffList="data.rowdDiffList"
            :changeCurChooseRow="changeCurChooseRow"
            v-if="judgeIsShowMadal(['RowsDiff'])"
          />
          <WorkMemDiff :workmemAndPeakmemArr="data.workmemAndPeakmemArr" v-if="judgeIsShowMadal(['WorkMemDiff'])" />
          <ExplainAnalyze
            :planTextArr="data.planTextArr"
            :curChooseRow="curChooseRow"
            v-if="judgeIsShowMadal(['ExplainAnalyze'])"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import ImplementationPlanThis from '../../pages/sql_detail/implementation_plan/Index.vue'
import StatisticalInformation from '../../pages/sql_detail/statistical_information/Index.vue'
import { WarningFilled } from '@element-plus/icons-vue'
import { cloneDeep } from 'lodash-es'
import { useI18n } from 'vue-i18n'
import { useRequest } from 'vue-request'
import ogRequest from '../../request'
import BaseInfo from './BaseInfo.vue'
import ObjectStructure from './ObjectStructure.vue'
import IndexInformation from './IndexInformation.vue'
import ImplementationPlan from './ImplementationPlan.vue'
import RowsDiff from './RowsDiff.vue'
import ExplainAnalyze from './ExplainAnalyze.vue'
import PartitionInformation from './PartitionInformation.vue'
import WorkMemDiff from './WorkMemDiff.vue'
import {
  allReportSceneList,
  reportSceneType,
  tableStructureType,
  tableIndexType,
  nativePlanType,
  partitionInfoType,
  rowsDiffType,
} from './common'
import { useMonitorStore } from '../../store/monitor'
import { i18n } from '../../i18n'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    dbid?: string // instance id
    sqlId?: string // debug_query_id
    reportId: string
    id: string
    requestType?: string
    large?: boolean
  }>(),
  {
    dbid: '',
    sqlId: '',
    reportId: 'ObjectInfoCheck',
    id: '-1',
    requestType: 'NONE',
    large: false,
  }
)

const initFormData = {
  clusterId: '',
  nodeId: '',
  debugQueryId: '',
}
const taskInfo = reactive(cloneDeep(initFormData))
const data = reactive<{
  currentReportScene: reportSceneType
  allReportSceneIdList: Array<string>
  tableStructureData: Array<tableStructureType>
  tableIndexData: Array<tableIndexType>
  tableMetaData: Record<string, string>
  planTextArr: Array<string>
  workmemAndPeakmemArr: Array<nativePlanType>
  partitionInfoList: Array<partitionInfoType>
  rowdDiffList: Array<rowsDiffType>
  curTableName: string
  indexAdvicesData: Array<string>
  otherStructureAndIndexList: Array<Record<string, any>>
  jsonQueryPlan: Record<string, any>
}>({
  currentReportScene: { id: '', name: '', title: '', advise: '', show: [], analysisIdea: '' },
  allReportSceneIdList: [],
  tableStructureData: [],
  tableIndexData: [],
  tableMetaData: {},
  planTextArr: [],
  workmemAndPeakmemArr: [],
  partitionInfoList: [],
  rowdDiffList: [],
  curTableName: 'table',
  indexAdvicesData: [],
  otherStructureAndIndexList: [],
  jsonQueryPlan: {},
})

const bodyHeight = ref(200)
const instanceId = ref('')
const sqlId = ref('')
const curChooseRow = ref('')

const { data: diagnosisData, run: requestDiagnosisData } = useRequest(
  () => {
    // Query all data through ObjectInfoCheck
    return ogRequest.get('/sqlDiagnosis/api/v1/diagnosisTasks/' + props.id + '/suggestions/ObjectInfoCheck')
  },
  { manual: true }
)

const replaceTableName = (fullStr: string, tableName: string) => {
  if (typeof fullStr !== 'string') {
    return fullStr
  }
  return fullStr.replace('TABLE', tableName)
}

const dealIndexAdviceSuggestion = (adviseData: Array<string>) => {
  if (i18n.global.locale.value === 'en') {
    return adviseData.join(';')
  }
  let text = ''
  const count = adviseData.length - 1
  adviseData.forEach((item, index) => {
    try {
      const columnName = item.substring(item.indexOf(' column ') + 8, item.indexOf('of')).trim()
      const tableName = item.substring(item.indexOf(' table ') + 7).trim()
      let template = t('report.indexTemplate')
      if (columnName.includes(',')) {
        template = t('report.multiColumnIndexTemplate')
      }
      if (index === count) {
        template = template.replace('%t', tableName).replace('%c', columnName)
      } else {
        template = template.replace('%t', tableName).replace('%c', columnName) + '、'
      }
      text += template
    } catch (e) {
      return text
    }
  })
  return text
}

const getContent = (fullStr: string, tableName: string, nodeType: string, type: string) => {
  if (nodeType === 'PlanRecommendedToCreateIndex' && Array.isArray(data.indexAdvicesData)) {
    if (type === 'title') {
      return data.indexAdvicesData.length > 0
        ? dealIndexAdviceSuggestion(data.indexAdvicesData)
        : t('report.noIndexSuggestions')
    }
    return data.indexAdvicesData.length > 0
      ? `${dealIndexAdviceSuggestion(data.indexAdvicesData)}，${t('report.ImproveQueryEfficiency')}`
      : t('report.noIndexSuggestions')
  }
  return replaceTableName(fullStr, tableName)
}

const judgeIsShowMadal = (sceneList: Array<string>) => {
  let isShow = false
  data.currentReportScene.show.forEach((item) => {
    if (sceneList.includes(item)) {
      isShow = true
    }
  })
  return isShow
}

const init = () => {
  const allReportSceneIdListTemp: Array<string> = []
  let curReportId = props.reportId
  allReportSceneList.forEach((item) => {
    if (item.id === curReportId) {
      data.currentReportScene = {
        id: item.id,
        name: t(`report.suggestion.${item.id}.name`),
        title: t(`report.suggestion.${item.id}.title`),
        advise: t(`report.suggestion.${item.id}.advise`),
        analysisIdea: t(`report.suggestion.${item.id}.analysisIdea`),
        show: item.show,
      }
    }
    allReportSceneIdListTemp.push(item.id)
  })
  data.allReportSceneIdList = allReportSceneIdListTemp
  instanceId.value = useMonitorStore().instanceId || ''
  sqlId.value = window.sessionStorage.getItem('sqlId') || ''
  curChooseRow.value = ''
  requestDiagnosisData()

  console.log('props.id', props.id)
  getTastInfo()
}

onMounted(() => {
  nextTick(() => {
    const domRect = document.querySelector('.report-container')?.getBoundingClientRect() as DOMRect
    bodyHeight.value = Math.floor(domRect.height) - 90
  })
  init()
})

const { data: taskData, run: getTastInfo } = useRequest(
  () => {
    // Query all data through ObjectInfoCheck
    return ogRequest.get('/sqlDiagnosis/api/v1/diagnosisTasks/' + props.id)
  },
  { manual: true }
)

watch(taskData, (res) => {
  console.log('taskDetailData', res)
  Object.assign(taskInfo, {
    clusterId: res.clusterId,
    nodeId: res.nodeId,
    debugQueryId: res.data.debugQueryId,
  })
  console.log('taskInfo', taskInfo)
})

const getMostCostTableTitle = (tableName: string, nodeType: string) => {
  if (
    nodeType === 'PlanRecommendedToCreateIndex' &&
    Array.isArray(data.otherStructureAndIndexList) &&
    data.otherStructureAndIndexList.length > 0
  ) {
    return `${tableName} ${t('report.table')}（${t('report.maximumConsumption')}）`
  }
  return `${tableName} ${t('report.table')}`
}

const changeCurChooseRow = (rowId: string) => {
  curChooseRow.value = rowId
}

const dealPeakOrDiskText = (queryPlan: Array<string>) => {
  return queryPlan.map((query) => {
    if (query.includes('Disk') && query.includes(':') && query.includes('kB')) {
      const needReplaceStr = query.substring(query.indexOf(':') + 1)
      query = query.replace(needReplaceStr, `<span style="color: red">${needReplaceStr}</span>`)
    }
    return query
  })
}

const dealPlanText = (query: string, rowsDiffClone: Array<rowsDiffType>) => {
  let tempQuery = query
  rowsDiffClone.forEach((item, index) => {
    if (
      !item.use &&
      query.includes(item.stepName) &&
      query.includes(item.estimateRows) &&
      query.includes(item.actualRows)
    ) {
      item.use = true
      tempQuery = tempQuery
        .replace(item.stepName, `<span id='row-${index}' style="color: #177DDC">${item.stepName}</span>`)
        .replace(`rows=${item.estimateRows}`, `<span style="color: #177DDC">rows=${item.estimateRows}</span>`)
        .replace(`rows=${item.actualRows}`, `<span style="color: #177DDC">rows=${item.actualRows}</span>`)
    }
  })
  return tempQuery
}

const dealQueryPlan = (queryPlan: Array<string>, rowsDiff: Array<rowsDiffType>) => {
  // rowsDiffCloneType
  const rowsDiffClone = cloneDeep(rowsDiff)
  const newQueryPlan = queryPlan.map((query) => {
    return dealPlanText(query, rowsDiffClone)
  })
  return newQueryPlan
}

const getRightValue = (value: Record<string, any>, key: string, type: string) => {
  const emptyReturn = type === 'Array' ? [] : {}
  if (value != null) {
    return value[key] ? value[key] : emptyReturn
  }
  return emptyReturn
}

watch(diagnosisData, (res) => {
  const {
    data: { executionPlan = {} },
  } = res
  const {
    peakMem = '',
    queryPlan = [],
    rowsDiff = [],
    partitionData = [],
    workMem = '',
    maxCostTableName = '',
    tableIndexData = {},
    tableMetaData = {},
    tableStructureData = {},
    indexAdvicesData = [],
    otherStructureAndIndexList = [],
    jsonQueryPlan = {},
  } = executionPlan
  let tempPlanText = []
  data.workmemAndPeakmemArr = [{ unit: `${t('report.size')}（KB）`, peakMem, workMem }]
  data.rowdDiffList = rowsDiff
  if (Array.isArray(queryPlan) && Array.isArray(rowsDiff) && rowsDiff.length > 0) {
    tempPlanText = dealQueryPlan(queryPlan, rowsDiff)
  } else if (Array.isArray(queryPlan)) {
    tempPlanText = queryPlan
  }
  data.planTextArr = dealPeakOrDiskText(tempPlanText)
  data.curTableName = maxCostTableName
  if (maxCostTableName !== '') {
    data.tableStructureData = getRightValue(tableStructureData, maxCostTableName, 'Array')
    data.tableIndexData = getRightValue(tableIndexData, maxCostTableName, 'Array')
    data.tableMetaData = getRightValue(tableMetaData, maxCostTableName, 'Object')
  }
  data.partitionInfoList = partitionData
  data.indexAdvicesData = indexAdvicesData
  data.otherStructureAndIndexList = otherStructureAndIndexList
  data.jsonQueryPlan = jsonQueryPlan
})

watch(
  () => props.reportId,
  () => {
    init()
  }
)

watch(
  () => i18n.global.locale.value,
  (lang) => {
    init()
  }
)
</script>

<style scoped lang="scss">
.report-container {
  height: 100%;
  overflow-y: scroll;
}

.report-header {
  font-size: 14px;
  color: var(--color-text-1);
  font-weight: 700;
  padding: 8px 0 8px 16px;
  border-bottom: 1px solid $og-border-color;
}

.report-main {
  padding: 8px 16px;
  font-size: 14px;
  color: var(--el-text-color-og);
}

.report-main-header {
  font-weight: 700;
  margin-bottom: 8px;
}

.report-main-body {
  // border: 1px solid $og-border-color;
  // padding: 20px 18px;
  overflow-y: auto;
}

.report-main-box {
  border: 1px solid $og-border-color;
  padding: 10px 16px;
  margin-bottom: 10px;
}

.report-main-box-header {
  padding-bottom: 16px;
  font-weight: 700;
  border-bottom: 1px solid $og-border-color;
}

.report-main-suggestion {
  display: flex;
  align-items: center;
  margin: 16px 0 20px 0;
}

.report-main-suggestion-text {
  margin-left: 10px;
  font-weight: 400;
  font-size: 14px;
  color: var(--color-text-1);
  display: inline-block;
  padding: 5px 0;
}

.report-main-suggestion-analysisIdea {
  font-weight: 400;
  font-size: 14px;
  color: var(--el-text-color-og);
  display: inline-block;
  padding: 5px 0;
  line-height: 21px;
}
</style>
