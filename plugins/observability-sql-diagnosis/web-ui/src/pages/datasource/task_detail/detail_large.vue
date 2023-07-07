<template>
  <div class="s-i-base">
    <my-card
      :title="$t('datasource.detailTitle')"
      :bodyPadding="false"
      style="position: relative"
      v-if="!reportNodeList.includes(urlParam.nodesType)"
    >
      <template #headerExtend>
        <svg-icon name="expand" class="shrink-img" @click="goToTask" />
      </template>
      <el-tabs class="tast-detail-tabs">
        <el-tab-pane label="诊断结果">
          <!-- Suggestions -->
          <div
            class="suggsiton-wrap"
            v-if="
              requestType === 'Suggestion' ||
              (taskData.taskInfo && taskData.taskInfo.top.type === 'Suggestion') ||
              (taskData.taskInfo && taskData.taskInfo.center.type === 'Suggestion')
            "
          >
            <div class="report-header">{{ taskData.dataSuggestion.title }}</div>
            <div class="report-main-header">
              <div class="report-main-suggestion">
                <el-icon color="#0093FF" size="18px">
                  <WarningFilled />
                </el-icon>
                <div class="suggsiton-info">
                  <template v-for="item in taskData.dataSuggestion.suggestions" :key="item">
                    <span class="report-main-suggestion-text">{{ item }}</span>
                  </template>
                </div>
              </div>
            </div>
          </div>

          <!-- Parameter Diagnosis -->
          <template v-if="requestType === 'Param'">
            <div class="report-header">{{ taskData.dataParameterConfig.title }}</div>
            <p class="s-i-base-item title">{{ $t('datasource.paramName') }}</p>
            <p class="s-i-base-item content">{{ taskData.dataParameterConfig.paramName }}</p>
            <p class="s-i-base-item title">{{ $t('datasource.currentValue') }}</p>
            <p class="s-i-base-item content">
              {{ taskData.dataParameterConfig.currentValue
              }}{{ taskData.dataParameterConfig.unit === null ? '' : '(' + taskData.dataParameterConfig.unit + ')' }}
            </p>
            <p class="s-i-base-item title">{{ $t('datasource.paramDescription') }}</p>
            <p class="s-i-base-item content">{{ taskData.dataParameterConfig.paramDescription }}</p>
            <p class="s-i-base-item title">{{ $t('datasource.suggestValue') }}</p>
            <p class="s-i-base-item content">{{ taskData.dataParameterConfig.suggestValue }}</p>
            <p class="s-i-base-item title">{{ $t('datasource.suggestReason') }}</p>
            <p class="s-i-base-item content">{{ taskData.dataParameterConfig.suggestReason }}</p>
          </template>

          <!-- Flame diagram -->
          <div class="s-l-base" v-if="requestType === 'Frame'">
            <div class="svg-top" v-if="taskData.taskInfo.top.type === 'Flamefigure'">
              <iframe class="svg-img-wrap" :srcdoc="svgFile" frameBorder="0" border="0"></iframe>
              <p class="svg-title">{{ taskData.taskInfo.top.data.title }}</p>
            </div>
            <div class="svg-center" v-if="taskData.taskInfo.center.type === 'Paragraph'">
              <div class="info-item" v-for="item in taskData.taskInfo.center.data" :key="item.title">
                <p class="item-title">{{ item.title }}</p>
                <p class="item-content">{{ item.paragraph }}</p>
              </div>
            </div>
          </div>
          <!-- Tree Table -->
          <div
            class="table-wrap"
            v-if="
              requestType === 'Table' ||
              (taskData.taskInfo && taskData.taskInfo.top.type === 'Table') ||
              (taskData.taskInfo && taskData.taskInfo.center.type === 'Table')
            "
          >
            <el-table
              ref="singleTableRef"
              :data="taskData.dataTableInfo.data"
              :style="{ width: '100%', marginBottom: '20px' }"
              border
              max-height="500"
            >
              <el-table-column type="index" />
              <template v-for="item in taskData.dataTableInfo.columns" :key="item.id">
                <el-table-column :prop="item.key" :label="item.title" />
              </template>
            </el-table>
          </div>
          <!-- Thermodynamic diagram -->
          <div
            class="chart-wrap"
            v-if="
              requestType === 'HeatMap' ||
              (taskData.taskInfo && taskData.taskInfo.top.type === 'HeatMap') ||
              (taskData.taskInfo && taskData.taskInfo.center.type === 'HeatMap')
            "
          >
            <div class="chart-btn-wrap">
              <span>{{ $t('datasource.showNum') }}</span>
              <el-switch
                v-model="showNumFlag"
                @change="handleShowNum"
                style="--el-switch-on-color: #409eff; --el-switch-off-color: #dcdfe6"
              />
            </div>
            <template v-for="(item, index) in chartData" :key="index + '-'">
              <heat-map
                :data="item.data"
                :xData="item.x"
                :yData="item.y"
                :title="item.name"
                :maxData="heatMapMax"
                :showNum="showNumFlag"
                :unit="item.unit"
                v-bind="$attrs"
              >
              </heat-map>
            </template>
          </div>
          <!-- No result -->
          <div class="noresult-wrap" v-if="requestType === 'NONE'">
            <img src="@/assets/img/noresult.png" class="noresult-img" />
            <p class="noresult-text">{{ $t('datasource.noResult') }}</p>
          </div>
          <!-- Thermodynamic diagram -->
          <div
            class="chart-wrap"
            v-if="
              requestType === 'LineChart' ||
              (taskData.taskInfo && taskData.taskInfo.top.type === 'LineChart') ||
              (taskData.taskInfo && taskData.taskInfo.center.type === 'LineChart')
            "
          >
            <template v-for="(item, index) in chartData" :key="index + '-'">
              <line-chart :data="item.data" :xData="item.xAxis.data" :series="item.series" :title="item.title">
              </line-chart>
            </template>
          </div>

          <!-- Task info for root-->
          <div v-if="taskData.baseInfoData.length > 0 && requestType === 'UL'">
            <div>
              <div class="info-title">
                <svg-icon name="note" class="detail-icon" />
                <div class="title">{{ $t('datasource.taskInfo') }}</div>
              </div>
              <p class="s-i-base-item" v-for="item in taskData.baseInfoData" :key="item.value">
                {{ item.label }}：<span v-html="item.value || '-'"></span>
              </p>

              <div class="info-title" style="margin-top: 16px; margin-bottom: 8px">
                <svg-icon name="cmd" class="detail-icon" />
                <div class="title">SQL</div>
              </div>
              <el-input v-model="taskData.sql" :rows="4" type="textarea" :input-style="'fontSize:14px'" />

              <div class="info-title" style="margin-top: 16px; margin-bottom: 8px">
                <svg-icon name="cmd" class="detail-icon" />
                <div class="title">{{ $t('datasource.taskInfoRemarkKey') }}</div>
              </div>
              <el-input v-model="taskData.log" :rows="8" type="textarea" :input-style="'fontSize:14px'" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </my-card>
    <my-card
      :title="$t('datasource.reportDetail')"
      :bodyPadding="false"
      style="position: relative"
      v-if="reportNodeList.includes(urlParam.nodesType)"
    >
      <ReportDetail :large="true" :id="urlParam.dbId" :reportId="urlParam.nodesType" />
      <img src="@/assets/img/small.png" class="shrink-img" @click="goToTask" />
    </my-card>
  </div>
</template>

<script lang="ts" setup>
import { optionType, pieColorAll, reportNodeList } from '@/pages/datasource/common'
import ogRequest from '@/request'
import ogRequestSvg from '@/request/axios'
import { useRequest } from 'vue-request'
import { ElTable } from 'element-plus'
import ReportDetail from '@/components/reportDetail/Index.vue'
import { WarningFilled } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

interface Res {
  type: string
  data: any
  child: {
    top: {
      type: string
      data: {
        title: string
        data: string[]
        columns: [{ type: string; title: string; key: string }]
      }
    }
    center: {
      type: string
      data: any
    }
  }
}

const props = withDefaults(
  defineProps<{
    dbid?: string // instance id
    sqlId?: string // debug_query_id
    reportId: string
    id: string
    requestType?: string
  }>(),
  {
    dbid: '',
    sqlId: '',
    reportId: 'ObjectInfoCheck',
    id: '-1',
    requestType: 'NONE',
  }
)

const urlParam = reactive<{
  dbId: string
  nodesType: string
}>({
  dbId: '',
  nodesType: '',
})
const svgFile = ref('')
const requestType = ref('')
const chartData = ref<any>([])
const heatMapMax = ref(10)
const showNumFlag = ref(true)
const taskData = reactive<{
  sql: string
  log: string
  baseInfoData: Array<optionType>
  dataTreeInfo: any
  dataTableInfo: any
  dataPieInfo: any
  dataPieColor: Array<string>
  dataSuggestion: any
  dataParameterConfig: any
  taskInfo: {
    top: {
      type: string
      data: {
        title: string
        data: string[]
        columns: [{ type: string; title: string; key: string }]
      }
    }
    center: {
      type: string
      data: [
        {
          title: string
          paragraph: string
          data?: string[]
          columns?: [{ type: string; title: string; key: string }]
        }
      ]
    }
  }
}>({
  sql: '',
  log: '',
  baseInfoData: [],
  dataTreeInfo: [],
  dataTableInfo: {},
  dataPieInfo: [],
  dataPieColor: [],
  dataSuggestion: [],
  dataParameterConfig: {},
  taskInfo: {
    top: {
      type: '',
      data: {
        title: '',
        data: [],
        columns: [{ type: '', title: '', key: '' }],
      },
    },
    center: {
      type: '',
      data: [
        {
          title: '',
          paragraph: '',
          data: [],
          columns: [{ type: '', title: '', key: '' }],
        },
      ],
    },
  },
})

const queryData = computed(() => {
  const queryObj = {
    id: urlParam.dbId,
    type: urlParam.nodesType,
  }
  return queryObj
})
const emit = defineEmits(['hideModel'])
const goToTask = () => {
  console.log('hideModel')
  emit('hideModel')
}
const handleShowNum = () => {
  requestData()
}
onMounted(() => {
  urlParam.dbId = props.id
  urlParam.nodesType = props.reportId
  requestData()
})

const { data: res, run: requestData } = useRequest(
  () => {
    return ogRequest.get(
      '/sqlDiagnosis/api/v1/diagnosisTasks/' + queryData.value.id + '/suggestions/' + queryData.value.type
    )
  },
  { manual: true }
)

const { data: ret, run: requestSvg } = useRequest(
  (id: number) => {
    return ogRequestSvg.get(`/sqlDiagnosis/api/v1/diagnosisTasks/res/${id}.svg`)
  },
  { manual: true }
)

const handleRequestData = (val: Res, type: string, cases: number) => {
  let dataType: any
  switch (cases) {
    case 1:
      if (val.type === type) {
        dataType = val.child
      } else if (val.child && val.child.top.type === type) {
        dataType = val.child.top.data.data
      } else if (val.child && val.child.center.type === type) {
        dataType = val.child.center.data.data
      } else {
        dataType = []
      }
      break
    case 2:
      if (val.type === type) {
        dataType = val.data.data
      } else if (val.child && val.child.top.type === type) {
        dataType = val.child.top.data
      } else if (val.child && val.child.center.type === type) {
        dataType = val.child.center.data
      } else {
        dataType = []
      }
      break
    case 3:
      if (val.type === type) {
        dataType = val.data
      } else if (val.child && val.child.top.type === type) {
        dataType = val.child.top.data
      } else if (val.child && val.child.center.type === type) {
        dataType = val.child.center.data
      } else {
        dataType = []
      }
      break
  }

  return dataType
}

watch(res, (res: Res) => {
  chartData.value = [] // clear data
  if (res && Object.keys(res).length) {
    requestType.value = res.type
    taskData.taskInfo = res.child
    if (res.type === 'UL') {
      taskData.sql = ''
      taskData.log = ''
      taskData.baseInfoData = []
      for (let item of res.data) {
        let label = Object.keys(item)[0]
        let value = String(Object.values(item)[0])
        if (label === 'sql') taskData.sql = value.replace('<br/>', '').replace(/<br\/>/g, '\n')
        else if (label === t('datasource.taskInfoRemarkKey')) {
          taskData.log = value.replace('<br/>', '').replace(/<br\/>/g, '\n')
        } else {
          taskData.baseInfoData.push({ label, value })
        }
      }
    } else if (res.type === 'Param') {
      taskData.dataParameterConfig = res.data
    } else {
      let resultData: any
      let svgInfo: any
      svgInfo = handleRequestData(res, 'Flamefigure', 2)
      if (svgInfo && Object.keys(svgInfo).length > 0) {
        requestSvg(svgInfo.id)
      }
      taskData.dataTreeInfo = handleRequestData(res, 'DataTree', 1)
      taskData.dataPieInfo = handleRequestData(res, 'Pie', 1)

      let arraymax: any = []
      if (res.child.center.type === 'LineChart') {
        resultData = res.child.center.data
      } else {
        resultData = handleRequestData(res, 'HeatMap', 2)
        resultData.forEach((item: any) => {
          item.data.forEach((k: any) => {
            arraymax.push(k[2])
          })
        })
      }

      taskData.dataTableInfo = handleRequestData(res, 'Table', 3)
      taskData.dataSuggestion = handleRequestData(res, 'Suggestion', 3)
      nextTick(() => {
        if (res.child.center.type === 'LineChart') {
          chartData.value = [resultData]
        } else {
          heatMapMax.value = Math.max(...arraymax)
          chartData.value = resultData
        }
      })
      if (taskData.dataPieInfo.length > 0) {
        taskData.dataPieColor = pieColorAll.slice(0, taskData.dataPieInfo.length)
        taskData.dataPieInfo.forEach((item: any, index: number) => {
          item.color = taskData.dataPieColor[index]
        })
      }
    }
  }
})
// Get svg pictures
watch(ret, (res: any) => {
  if (res && Object.keys(res).length) {
    svgFile.value = res.replace('opacity:0.1;', 'opacity:0.7;').replace('>ic<', '><')
  }
})
</script>

<style lang="scss" scoped>
@use '@/assets/style/task.scss' as *;
:deep(.og-card-body) {
  overflow: scroll !important;
}
.chart-btn-wrap {
  margin: 0px 40px 0px;
  & > span {
    margin-right: 20px;
  }
}
.s-i-base-item {
  padding: 0 15px;
  margin-top: 12px !important;
}
.s-i-base {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  // padding: 20px;
  &:deep(.og-card-header--extra) {
    display: none;
  }
  &:deep(.og-card-header) {
    justify-content: center;
  }
  &-item {
    margin: 3px 0;
  }

  &:deep(.el-row) {
    line-height: 39px;
    border: 1px solid $og-border-color;
    border-top: none;
    border-right: none;
  }
  &:deep(.el-col) {
    padding-left: 5px;
    border-right: 1px solid $og-border-color;
  }
  .shrink-img {
    width: 20px;
    height: 20px;
  }
}
.s-l-base {
  box-sizing: border-box;
  // height: 240px;
  color: #fff;
  font-size: 14px;
  .svg-img-wrap {
    width: 100%;
    height: 800px;
    padding: 10px 0;
    border: none;
    overflow: scroll;
    &:deep(svg) {
      width: 100%;
      height: 100%;
      // max-height:500px;
    }
  }
  .svg-title {
    text-align: center;
    margin-top: 10px;
    font-size: 16px;
  }
  .svg-center {
    margin-top: 20px;
    .info-item {
      margin-bottom: 10px;
      .item-title {
        font-size: 16px;
      }
      .item-content {
        text-indent: 2em;
      }
    }
  }
}
.s-i-consuming {
  display: flex;
  align-items: center;
  padding: 0 0;
  overflow-y: auto;

  &-pie {
    height: 180px;
    width: 180px;
  }

  &-legend {
    width: calc(100% - 180px);
    max-width: 450px;
    height: 220px;
    display: flex;
    flex-wrap: wrap;
    padding-left: 100px;
  }

  &-item {
    font-size: 12px;
    display: flex;
    align-items: center;

    &:nth-child(1n) {
      width: 43%;
    }

    &:nth-child(2n) {
      width: 57%;
    }

    &-block {
      display: inline-block;
      width: 10px;
      height: 10px;
      margin-right: 3px;
    }
  }
}

.chart-wrap {
  width: 100%;
  height: 500px;
}
.table-wrap {
  width: 100%;
  padding: 10px;
  box-sizing: border-box;
}
.report-header {
  font-size: 20px;
  color: var(--color-text-1);
  font-weight: 700;
  padding: 8px 0 8px 28px;
  border-bottom: 1px solid $og-border-color;
}
.suggsiton-wrap {
  padding: 8px 16px;

  .report-main {
    padding: 8px 16px;
    font-size: 14px;
    color: $og-text-color;
  }
  .report-main-header {
    font-weight: 700;
    margin-bottom: 8px;
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
  .report-main-title {
    margin: 8px 0;
  }
}
.noresult-wrap {
  width: 100%;
  height: 500px;
  .noresult-img {
    width: 200px;
    display: block;
    margin: 100px auto 20px;
  }
  .noresult-text {
    text-align: center;
    color: #707070;
  }
}
</style>
