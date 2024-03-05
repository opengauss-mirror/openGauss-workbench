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
          <div class="page-header" v-if="props.diagnosisType === 'sql'">
            <div class="icon"></div>
            <div class="title">{{ $t('sql.sqlDiagnose') }}</div>
            <div class="seperator"></div>

            <el-breadcrumb separator="/" style="flex-grow: 1">
              <el-breadcrumb-item>
                <div @click="goback(2)">
                  <a>{{ $t('sql.sqlDiagnose') }}</a>
                </div>
              </el-breadcrumb-item>
              <el-breadcrumb-item>
                <div @click="goback()">
                  <a>{{ $t('diagnosis.newSQLTask') }}</a>
                </div>
              </el-breadcrumb-item>
              <el-breadcrumb-item>{{ $t('diagnosis.globalThresholdSetting') }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="page-header" v-if="props.diagnosisType === 'history'">
            <div class="icon"></div>
            <div class="title">{{ $t('histroyDiagnosis.menuName') }}</div>
            <div class="seperator"></div>

            <el-breadcrumb separator="/" style="flex-grow: 1">
              <el-breadcrumb-item>
                <div @click="goback()">
                  <a>{{ $t('diagnosis.histroyDiagnosis') }}</a>
                </div>
              </el-breadcrumb-item>
              <el-breadcrumb-item>{{ $t('diagnosis.globalThresholdSetting') }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="form page-padding" v-loading="loading">
            <div class="title-row">
              <div class="title" style="flex-grow: 1">{{ $t('diagnosis.globalThresholdLabel') }}</div>
            </div>
            <el-table
              :table-layout="'auto'"
              :data="ruleForm.tableData"
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
              <el-table-column prop="thresholdValue" :label="$t('historyDiagnosis.thresholdsConfig')" width="150">
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
              <el-button type="primary" @click="diagnosis">保存</el-button>
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
import { ElMessage } from 'element-plus'
import type { TableColumnCtx, FormInstance, FormRules } from 'element-plus'

import { getThresholds, Threshold, ThresholdGlobalSetting, setThresholds } from '@/api/historyDiagnosis'
import { dateAddHour, dateToUTCString } from '@/utils/date'

const { t } = useI18n()
const now = new Date()
const ruleFormRef = ref<FormInstance>()
const thresholdsUpdated = ref<boolean>(false)
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

const props = withDefaults(
  defineProps<{
    diagnosisType: string // sql,history
  }>(),
  {}
)

const rules = reactive<FormRules>({
  thresholdValue: [{ required: true, message: t('historyDiagnosis.pleaseSelectThreshold'), trigger: 'change' }],
})

const myEmit = defineEmits(['goback'])
const goback = (level?: number) => {
  myEmit('goback', level, { thresholdsUpdated })
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
  if (rowIndex === 0 || ruleForm.tableData[rowIndex].thresholdType !== ruleForm.tableData[rowIndex - 1].thresholdType) {
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

  let result = ref<ThresholdGlobalSetting>({
    diagnosisType: props.diagnosisType,
    thresholds: [],
  })
  ruleForm.tableData.forEach((element) => {
    result.value.thresholds.push({
      thresholdKey: element.threshold,
      thresholdValue: element.thresholdValue,
    })
  })
  addDiagnosisTask(result.value)
}
const { loading, run: addDiagnosisTask } = useRequest(
  (param) => {
    return setThresholds(param).then(() => {
      thresholdsUpdated.value = true
      ElMessage({
        showClose: true,
        message: t('app.saveSuccess'),
        type: 'success',
      })
    })
  },
  { manual: true }
)

const { data: thresholdsData, run: initThresholds } = useRequest(
  () => {
    return getThresholds(props.diagnosisType)
  },
  { manual: true }
)
watch(thresholdsData, (ret: any) => {
  ruleForm.tableData = []
  ruleForm.tableData = ret
})
onMounted(() => {
  initThresholds()
})
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
