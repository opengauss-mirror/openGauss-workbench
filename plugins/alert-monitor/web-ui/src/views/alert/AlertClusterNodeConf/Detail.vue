<template>
  <div v-if="showMain">
    <div class="page-header">
      <div class="icon"></div>
      <div class="title">{{ t('AlertClusterNodeConf.detailTitle') }}</div>
      <div class="seperator"></div>
      <div class="alert-title">{{ t('AlertClusterNodeConf.title') }} </div>
      <div class="alert-seperator">&nbsp;/&nbsp;</div>
      <div class="alert-title">{{ t('AlertClusterNodeConf.detailTitle') }} </div>
    </div>
    <el-form label-position="left" label-width="140" size="default" style="margin-top: 8px;">
      <el-form-item :label="$t('AlertClusterNodeConf.selectedInstance') + ':'" style="margin-bottom: 10px !important;">
        <el-tag v-for="(item, index) in clusterNodeList0" :key="item.clusterNodeId" size="large" closable
          @close="closeTag(index)">{{ item.nodeName }}</el-tag>
      </el-form-item>
    </el-form>
    <div class="alert-table">
      <el-tabs v-model="activeName" class="node-tabs">
        <el-tab-pane :label="$t('AlertClusterNodeConf.alertTemplateTab')" name="template" style="margin-top: 8px;">
          <el-row>
            <el-col :span="4">
              <div>
                <el-table size="small" :data="tableDatas" style="width: 100%;" ref="templateTable" class="templateTable"
                  header-cell-class-name="grid-header" border>
                  <el-table-column width="50">
                    <template #default="scope">
                      <el-radio :label="scope.$index + ''" v-model="templateIndex"
                        @change="getCurrentRow(scope.row.id)">&nbsp;</el-radio>
                    </template>
                  </el-table-column>
                  <el-table-column prop="templateName" :label="$t('alertTemplate.table[0]')" />
                </el-table>
              </div>
            </el-col>
            <el-col :span="20" style="padding-left: 5px;">
              <el-descriptions :title="t('alertTemplate.detailTitle')">
              </el-descriptions>
              <div class="template-table">
                <el-table size="small" :data="ruleTableDatas" style="width: 100%;" class="ruleTable"
                  header-cell-class-name="grid-header" border>
                  <el-table-column :label="$t('alertRule.table[0]')" prop="ruleName" width="140"></el-table-column>
                  <el-table-column :label="$t('alertRule.table[1]')" prop="ruleType" width="100">
                    <template #default="scope">
                      <span>{{ $t(`alertRule.${scope.row.ruleType}`) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="level" :label="$t('alertRule.table[2]')" width="100">
                    <template #default="scope">
                      <span>{{ $t(`alertRule.${scope.row.level}`) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('alertRule.table[8]')" min-width="200">
                    <template #default="scope">
                      <span v-html="showRuleExpDesc(scope.row)"></span>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('alertRule.table[9]')">
                    <template #default="scope">
                      <span v-html="showRuleExpComb(scope.row.ruleExpComb)"></span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="isRepeat" :label="$t('alertRule.table[3]')" width="100">
                    <template #default="scope">
                      <span>{{ scope.row.isRepeat === 1 ? $t('alertRule.isRepeat') :
                        $t('alertRule.isNotRepeat') }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="isSilence" :label="$t('alertRule.table[4]')" width="200">
                    <template #default="scope">
                      <span>{{ scope.row.isSilence === 1 ? scope.row.silenceStartTime +
                        $t('alertRule.to') +
                        scope.row.silenceEndTime : $t('alertRule.isNotSilence') }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="alertNotify" :label="$t('alertRule.table[5]')" width="120">
                    <template #default="scope">
                      <span v-text="showAlertNotify(scope.row.alertNotify)"></span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane :label="$t('AlertClusterNodeConf.alertRuleTab')" name="rule" style="margin-top: 8px;">
          <el-row>
            <el-table size="small" :data="ruleTableDataList" ref="ruleTable" style="width: 100%;" class="templateTable"
              header-cell-class-name="grid-header" border>
              <el-table-column type="selection" width="40" />
              <el-table-column :label="$t('alertRule.table[0]')" prop="ruleName" width="140"></el-table-column>
              <el-table-column :label="$t('alertRule.table[1]')" prop="ruleType" width="100">
                <template #default="scope">
                  <span>{{ $t(`alertRule.${scope.row.ruleType}`) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="level" :label="$t('alertRule.table[2]')" width="100">
                <template #default="scope">
                  <span>{{ $t(`alertRule.${scope.row.level}`) }}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('alertRule.table[8]')" min-width="200">
                <template #default="scope">
                  <span v-html="showRuleExpDesc(scope.row)"></span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('alertRule.table[9]')">
                <template #default="scope">
                  <span v-html="showRuleExpComb(scope.row.ruleExpComb)"></span>
                </template>
              </el-table-column>
              <el-table-column prop="isRepeat" :label="$t('alertRule.table[3]')" width="100">
                <template #default="scope">
                  <span>{{ scope.row.isRepeat === 1 ? $t('alertRule.isRepeat') :
                    $t('alertRule.isNotRepeat') }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="isSilence" :label="$t('alertRule.table[4]')" width="200">
                <template #default="scope">
                  <span>{{ scope.row.isSilence === 1 ? scope.row.silenceStartTime + $t('alertRule.to') +
                    scope.row.silenceEndTime : $t('alertRule.isNotSilence') }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="alertNotify" :label="$t('alertRule.table[5]')" width="120">
                <template #default="scope">
                  <span v-text="showAlertNotify(scope.row.alertNotify)"></span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('alertRule.table[7]')" fixed="right">
                <template #default="scope">
                  <el-button link type="primary" size="small" @click.prevent="editRule(scope.row)">
                    {{ t('app.edit') }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-row style="margin-top: 10px;">
      <el-button type="primary" @click="confirm" :loading="loading">{{ t('app.confirm') }}</el-button>
      <el-button @click="cancel" :loading="loading">{{ t('app.cancel') }}</el-button>
    </el-row>

    <el-dialog v-model="visible" :show-close="false" width="30%">
      <template #header="{ titleId, titleClass }">
        <div>
          <h4 :id="titleId" :class="titleClass">
            <InfoFilled style="width: 1em; height: 1em; margin-right: 8px" />{{ $t('app.tip') }}
          </h4>
        </div>
      </template>
      <div style="margin-bottom: 10px;"
        v-text="`${$t('AlertClusterNodeConf.generateTip1') + templateName + $t('AlertClusterNodeConf.generateTip2')}`">
      </div>
      <el-input v-model="templateName" size="large"></el-input>
      <template #footer>
        <span>
          <el-button @click="visible = false" :loading="loading">{{ t('app.cancel') }}</el-button>
          <el-button type="primary" :loading="loading" @click="saveTemplateAndConfig">{{ t('app.confirm') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
  <div v-else>
    <TemplateRuleDetail :ruleId="editRuleId" :templateRuleId="editTemplateRuleId" :titleList="titleList"
      @updateTemplateRuleSuccess="updateTemplateRuleSuccess" @cancelUpdateTemplateRule="cancelUpdateTemplateRule" />
  </div>
</template>

<script setup lang='ts'>
import { InfoFilled } from "@element-plus/icons-vue";
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import { ref, onMounted, nextTick } from 'vue'
import TemplateRuleDetail from "@/views/alert/AlertTemplate/TemplateRuleDetail.vue";
const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    clusterNodeList: any[],
  }>(),
  {
    clusterNodeList: () => [],
  }
);

const emit = defineEmits(["updateConfigSuccess", "cancelConfig"]);

const showMain = ref<boolean>(true)
const loading = ref<boolean>(false)
const ruleTable = ref()
const templateTable = ref()
const activeName = ref<string>('template')
const templateIndex = ref<string>()
const tableDatas = ref<any[]>([])
const ruleTableDatas = ref<any[]>()
const ruleTableDataList = ref<any[]>([])
const currentId = ref<number>()
const selectedData = ref<any>()
const visible = ref<boolean>(false)
const templateName = ref<string>('')

const selectedRuleRows = ref<any[]>([])
const editTemplateRuleId = ref<number>()
const editRuleId = ref<number>()
const titleList = ref<string[]>([])
const clusterNodeList0 = ref<any[]>([])

const closeTag = (index: number) => {
  if (clusterNodeList0.value.length === 1) {
    ElMessage({
      message: t('app.keepOneData'),
      type: 'warning'
    })
    return;
  }
  clusterNodeList0.value.splice(index, 1)
}

const { data: res, run: requestData } = useRequest(
  (clusterNodeId) => {
    return request.get(`/api/v1/alertClusterNodeConf/clusterNode/${clusterNodeId}`)
  },
  { manual: true }
)

const { data: templateRes, run: requestTemplateData } = useRequest(
  () => {
    return request.get("/api/v1/alertTemplate/list")
  },
  { manual: true }
);
const { data: ruleRes, run: requestRuleData } = useRequest(
  () => {
    return request.get(`/api/v1/alertTemplate/${currentId.value}/rule/list`)
  },
  { manual: true }
);
watch(res, (res: any) => {
  if (res && res.code === 200) {
    selectedData.value = res.data || {}
    if (tableDatas.value.length > 0) {
      for (let i = 0; i < tableDatas.value.length; i++) {
        let row = tableDatas.value[i]
        if (row.id === selectedData.value.templateId) {
          templateIndex.value = i + ''
          currentId.value = row.id
          requestRuleData()
          break;
        }
      }
    }
  } else {
    selectedData.value = {}
  }
});
watch(templateRes, (templateRes: any) => {
  if (templateRes && templateRes.code === 200) {
    tableDatas.value = templateRes.data || []
    if (selectedData.value && selectedData.value.id) {
      for (let i = 0; i < tableDatas.value.length; i++) {
        let row = tableDatas.value[i]
        if (row.id === selectedData.value.templateId) {
          templateIndex.value = i + ''
          currentId.value = row.id
          requestRuleData()
          break;
        }
      }
    }
  } else {
    tableDatas.value = []
    ruleTableDatas.value = []
  }
});
watch(ruleRes, (ruleRes: any) => {
  if (ruleRes && ruleRes.code === 200) {
    ruleTableDatas.value = ruleRes.data || []
  } else {
    ruleTableDatas.value = []
  }
}, { deep: true });

const getCurrentRow = (id: number) => {
  if (id) {
    currentId.value = id
    requestRuleData()
  }
}

const showRuleExpDesc = (rule: any) => {
  const alertRuleItemList = rule.alertRuleItemList;
  if (!alertRuleItemList || alertRuleItemList.length === 0) {
    return '';
  }

  if (rule.ruleType === 'index') {
    return alertRuleItemList.map((item: any) => {
      let paramStr = "";
      const ruleExpParam = item.ruleExpParam
      if (ruleExpParam) {
        let param = JSON.parse(ruleExpParam)
        paramStr = '(' + Object.keys(param).map((key: any) => param[key]).join(',') + ')'
      }
      return `[${item.ruleMark}]: ${t(`alertRule.${item.ruleExpName}`) + paramStr + ' ' + (item.action === 'normal' ? (t(`alertRule.${item.action}Action`) + ' ' + item.operate + item.limitValue + item.unit) : t(`alertRule.${item.action}Action`))}`
    }).join('<br />')
  } else {
    return alertRuleItemList.map((item: any) => {
      return `[${item.ruleMark}]:${t('alertRule.keyword')}(${item.keyword}${item.blockWord ? '，' + t('alertRule.blockWord') + ':' + item.blockWord : ''})${t('alertRule.happen')} ${item.operate}${item.limitValue}${t('alertRule.logUnit')}`
    }).join('<br />')
  }
}

const showRuleExpComb = (ruleExpComb: String) => {
  if (!ruleExpComb) {
    return ''
  }
  return ruleExpComb.split(' ').map((item: String) => {
    if (item === 'and' || item === 'or') {
      return t(`alertRule.${item}`)
    }
    return item
  }).join(' ')
}
const showAlertNotify = (val: string) => {
  if (!val) {
    return ''
  }
  let arr = val.split(',');
  let result = arr.map(item => t(`alertRule.${item}`)).join(',')
  return result
}

const { data: ruleListRes, run: requestRuleList } = useRequest(
  () => {
    return request.get(`/api/v1/alertRule/ruleList`)
  },
  { manual: true }
);
watch(ruleListRes, (ruleListRes: any) => {
  if (ruleListRes && ruleListRes.code === 200) {
    ruleTableDataList.value = ruleListRes.data
  } else {
    ruleTableDataList.value = []
  }
});

const confirm = () => {
  if (activeName.value === 'template') {
    if (!currentId.value) {
      return
    }
    if (clusterNodeList0.value.length === 0) {
      ElMessage({
        message: t('app.selecteDataTip'),
        type: 'error'
      })
      return
    }
    let clusterNodeIds = clusterNodeList0.value.map(item => item.clusterNodeId).join(',')
    let param = {
      clusterNodeIds,
      templateId: currentId.value
    }
    loading.value = true
    request.post(`/api/v1/alertClusterNodeConf`, param).then((res: any) => {
      loading.value = false
      if (res && res.code === 200) {
        ElMessage({
          message: t('app.saveSuccess'),
          type: 'success'
        })
        emit("updateConfigSuccess")
      } else {
        ElMessage({
          message: t('app.saveFail'),
          type: 'error'
        })
      }
    }).catch(() => {
      loading.value = false
      ElMessage({
        message: t('app.saveFail'),
        type: 'error'
      })
    })
  } else if (activeName.value === 'rule') {
    const rows = ruleTable.value.getSelectionRows() || []
    if (rows.length === 0) {
      ElMessage({
        message: t('app.selectDatasTip'),
        type: 'error'
      })
      return
    }
    visible.value = true
    templateName.value = t('alertTemplate.title') + rand()
  }
}
const cancel = () => {
  emit("cancelConfig")
}

const editRule = (ruleRow: any) => {
  selectedRuleRows.value = ruleTable.value.getSelectionRows() || []
  editTemplateRuleId.value = ruleRow.templateRuleId
  editRuleId.value = ruleRow.ruleId
  titleList.value = [t('AlertClusterNodeConf.title'), t('AlertClusterNodeConf.detailTitle'), t('alertRule.editTitle')]
  showMain.value = false
}
const updateTemplateRuleSuccess = (templateRule: any) => {
  for (let i = 0; i < ruleTableDataList.value.length; i++) {
    if (ruleTableDataList.value[i].ruleId === templateRule.ruleId) {
      ruleTableDataList.value[i].templateRuleId = templateRule.templateRuleId
      ruleTableDataList.value[i].level = templateRule.level
      ruleTableDataList.value[i].ruleExpDesc = templateRule.ruleExpDesc
      ruleTableDataList.value[i].ruleExpComb = templateRule.ruleExpComb
      ruleTableDataList.value[i].isRepeat = templateRule.isRepeat
      ruleTableDataList.value[i].isSilence = templateRule.isSilence
      ruleTableDataList.value[i].silenceStartTime = templateRule.silenceStartTime
      ruleTableDataList.value[i].silenceEndTime = templateRule.silenceEndTime
      ruleTableDataList.value[i].alertNotify = templateRule.alertNotify
      ruleTableDataList.value[i].notifyWayIds = templateRule.notifyWayIds
    }
  }
  showMain.value = true
  nextTick(() => {
    for (let row of selectedRuleRows.value) {
      ruleTable.value.toggleRowSelection(row, true)
    }
  })
}

const cancelUpdateTemplateRule = () => {
  showMain.value = true
  nextTick(() => {
    for (let row of selectedRuleRows.value) {
      ruleTable.value.toggleRowSelection(row, true)
    }
  })
}

const saveTemplateAndConfig = () => {
  const rows = ruleTable.value.getSelectionRows()
  let templateRuleReqList = rows.map((item: any) => {
    return { templateRuleId: item.templateRuleId, ruleId: item.ruleId }
  })
  let clusterNodeIds = clusterNodeList0.value.map(item => item.clusterNodeId).join(',')
  let param = {
    templateName: templateName.value,
    templateRuleReqList,
    clusterNodeIds,
  }
  loading.value = true
  request.post(`/api/v1/alertClusterNodeConf/alertTemplate`, param).then((res: any) => {
    loading.value = false
    if (res && res.code === 200) {
      ElMessage({
        message: t('app.saveSuccess'),
        type: 'success'
      })
      visible.value = false
      activeName.value = 'template'
      emit("updateConfigSuccess")
    } else {
      ElMessage({
        message: t('app.saveFail'),
        type: 'error'
      })
    }
  }).catch(() => {
    loading.value = false
    ElMessage({
      message: t('app.saveFail'),
      type: 'error'
    })
  })
}

const rand = () => {
  return Math.floor(Math.random() * (9999 - 1000)) + 1000
}

onMounted(() => {
  if (props.clusterNodeList && props.clusterNodeList.length === 1) {
    requestData(props.clusterNodeList[0].clusterNodeId)
  }
  clusterNodeList0.value = props.clusterNodeList
  requestTemplateData()
  requestRuleList()
})

</script>
<style scoped lang='scss'>
.templateTable {
  height: calc(100vh - 110px - 62px - 177px);
}

.ruleTable {
  height: calc(100vh - 110px - 62px - 177px - 34px);
}

.node-tabs>.el-tabs__content {
  padding: 32px;
  color: #6b778c;
  font-size: 32px;
  font-weight: 600;
}
</style>