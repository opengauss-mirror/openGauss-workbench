<template>
  <div v-if="showMain">
    <div class="page-header">
      <div class="icon"></div>
      <div class="title">{{ t('alertTemplate.title') }}</div>
      <div class="seperator"></div>
      <el-breadcrumb separator="/" style="flex-grow: 1">
        <el-breadcrumb-item>
          {{ t('alertTemplate.title') }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-row>
      <el-col :span="6">
        <div class="alert-table">
          <div class="page-header" style="padding: 7px;">
            <div class="icon"></div>
            <div class="title" style="font-size: 14px;font-weight: 500;">{{ t('alertTemplate.title') }}
            </div>
          </div>
          <div class="search-form">
            <div class="filter">
              <el-button type="primary" @click="addTemplate">{{ $t('app.add') }}</el-button>
            </div>
            <div class="seperator"></div>
            <div class="filter">
              <el-input v-model="formData.templateName" style="width: 150px"
                :placeholder="$t('alertTemplate.templateNamePlaceholder')">
                <template #suffix>
                  <el-button :icon="Search" @click="search" />
                </template>
              </el-input>
            </div>
          </div>
          <el-table size="small" :data="tableDatas" style="width: 100%;" header-cell-class-name="grid-header" border>
            <el-table-column width="31">
              <template #default="scope">
                <el-radio :label="scope.row.id" v-model="currentId" style="width: 31px;"
                  @change="getCurrentRow(scope.row.id)">&nbsp;</el-radio>
              </template>
            </el-table-column>
            <el-table-column prop="templateName" :label="$t('alertTemplate.table[0]')" />
            <el-table-column :label="$t('alertTemplate.table[1]')">
              <template #default="scope">
                <el-button link type="primary" size="small" @click="editTemplate(scope.row.id)">{{
                  $t('app.edit') }}</el-button>
                <el-button link type="primary" size="small" @click="delTemplate(scope.row.id)">{{
                  $t('app.delete') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:currentPage="page.currentPage" v-model:pageSize="page.pageSize" :total="page.total"
              :page-sizes="[10, 20, 30, 40]" layout="total,sizes,prev,next" background small
              @size-change="handleSizeChange" @current-change="handleCurrentChange" />
          </div>
        </div>
      </el-col>
      <el-col :span="18" style="padding-left: 8px;">
        <div class="alert-table">
          <div class="page-header" style="padding: 7px;">
            <div class="icon"></div>
            <div class="title" style="font-size: 14px;font-weight: 500;">{{ t('alertRule.title') }}
            </div>
          </div>
          <div class="search-form">
            <div class="filter">
              <el-input v-model="ruleFormData.ruleName" style="width: 150px"
                :placeholder="$t('alertRule.ruleNamePlaceholder')">
                <template #suffix>
                  <el-button :icon="Search" @click="ruleSearch" />
                </template>
              </el-input>
            </div>
          </div>
          <el-table size="small" :data="ruleTableDatas" style="width: 100%;" header-cell-class-name="grid-header" border>
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
            <el-table-column :label="$t('alertRule.table[7]')" width="100" fixed="right">
              <template #default="scope">
                <el-switch v-model="scope.row.enable" inline-prompt :active-value="1" :inactive-value="0"
                  :active-text="$t('app.enable')" :inactive-text="$t('app.disable')"
                  @change="val => changeEnable(scope.row.templateRuleId, val)" :loading="enableLoading"/>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination v-model:currentPage="rulePage.currentPage" v-model:pageSize="rulePage.pageSize"
              :total="rulePage.total" :page-sizes="[10, 20, 30, 40]" layout="total,sizes,prev,pager,next" background small
              @size-change="handleRuleSizeChange" @current-change="handleRuleCurrentChange" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
  <div v-else>
    <TemplateDetail :templateId="curId" :state="state" @updateTemplate="updateTemplate"
      @cancelTemplate="cancelTemplate" />
  </div>
</template>

<script setup lang='ts'>
import { Search } from "@element-plus/icons-vue";
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessageBox, ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import TemplateDetail from "@/views/alert/AlertTemplate/TemplateDetail.vue";
const { t } = useI18n();

const showMain = ref<boolean>(true)
const curId = ref<number>()
const state = ref<string>()
const offsetHeight = ref<number>()
const page = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
})
const formData = ref<any>({
  templateName: '',
})
const ruleFormData = ref<any>({
  ruleName: '',
})
const rulePage = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
})

const currentId = ref<number>()
const tableDatas = ref<any[]>([])
const ruleTableDatas = ref<any[]>([])
const enableLoading = ref<boolean>(false)
const getCurrentRow = (id: number) => {
  if (id) {
    currentId.value = id
    requestRuleData()
  }
}
const { data: templateRes, run: requestTemplateData } = useRequest(
  () => {
    let params = {}
    params = Object.assign(params, formData.value, { pageNum: page.currentPage, pageSize: page.pageSize })
    return request.get("/api/v1/alertTemplate", params)
  },
  { manual: true }
);
const { data: ruleRes, run: requestRuleData } = useRequest(
  () => {
    let params = {}
    params = Object.assign(params, ruleFormData.value, { pageNum: rulePage.currentPage, pageSize: rulePage.pageSize })
    return request.get(`/api/v1/alertTemplate/${currentId.value}/rule`, params)
  },
  { manual: true }
);
watch(templateRes, (templateRes: any) => {
  if (templateRes && templateRes.code === 200) {
    tableDatas.value = templateRes.rows || []
    page.total = templateRes.total
    if (tableDatas.value.length > 0) {
      currentId.value = tableDatas.value[0].id
      requestRuleData()
    }
    if (tableDatas.value.length === 0) {
      ruleTableDatas.value = []
      rulePage.total = 0
    }
  } else {
    tableDatas.value = []
    page.total = 0
    ruleTableDatas.value = []
    rulePage.total = 0
    const msg = t("app.queryFail");
    ElMessage({
      showClose: true,
      message: msg,
      type: "error",
    });
  }
});
watch(ruleRes, (ruleRes: any) => {
  if (ruleRes && ruleRes.code === 200) {
    ruleTableDatas.value = ruleRes.rows || []
    rulePage.total = ruleRes.total
  } else {
    ruleTableDatas.value = []
    rulePage.total = 0
    const msg = t("app.queryFail");
    ElMessage({
      showClose: true,
      message: msg,
      type: "error",
    });
  }
});
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
const search = () => {
  page.currentPage = 1
  page.pageSize = 10
  requestTemplateData()
}
const handleSizeChange = (val: any) => {
  page.pageSize = val
  requestTemplateData()
}
const handleCurrentChange = (val: any) => {
  page.currentPage = val
  requestTemplateData()
}

const ruleSearch = () => {
  rulePage.currentPage = 1
  rulePage.pageSize = 10
  requestRuleData()
}
const handleRuleSizeChange = (val: any) => {
  rulePage.pageSize = val
  requestRuleData()
}
const handleRuleCurrentChange = (val: any) => {
  rulePage.currentPage = val
  requestRuleData()
}
const showAlertNotify = (val: string) => {
  if (!val) {
    return ''
  }
  let arr = val.split(',');
  let result = arr.map(item => t(`alertRule.${item}`)).join(',')
  return result
}

const addTemplate = () => {
  state.value = 'add'
  showMain.value = false
}
const editTemplate = (id: number) => {
  state.value = 'edit'
  curId.value = id
  showMain.value = false
}
const updateTemplate = () => {
  formData.value = {
    ruleName: ''
  }
  search()
  curId.value = undefined
  showMain.value = true
}
const cancelTemplate = () => {
  curId.value = undefined
  showMain.value = true
}
const delTemplate = (id: number) => {
  ElMessageBox.confirm(
    t('app.deleteTip'),
    t('app.tip'),
    {
      confirmButtonText: t('app.confirm'),
      cancelButtonText: t('app.cancel'),
      type: 'warning',
    }
  ).then(() => {
    request.delete(`/api/v1/alertTemplate`, { id }).then((res: any) => {
      if (res && res.code === 200) {
        ElMessage({
          type: 'success',
          message: t('app.delSuccess'),
        })
        search()
      } else {
        ElMessage({
          type: 'error',
          message: t('app.delFail') + (res && res.msg ? "," + res.msg : ""),
        })
      }
    }).catch((res: any) => {
      ElMessage({
        type: 'error',
        message: t('app.delFail') + (res && res.msg ? "," + res.msg : ""),
      })
    })
  }).catch(() => {

  })
}

const changeEnable = (ruleId: any, enable: any) => {
  enableLoading.value = true
  if (enable === 0) {
    request.post(`/api/v1/alertTemplate/templateRule/${ruleId}/disable`).then((res: any) => {
      if (res.code !== 200) {
        ElMessage({
          message: t('app.disableFail'),
          type: 'error'
        })
      }
      enableLoading.value = false
      requestRuleData()
    }).catch(() => {
      ElMessage({
        message: t('app.disableFail'),
        type: 'error'
      })
      enableLoading.value = false
      requestRuleData()
    })
  } else {
    request.post(`/api/v1/alertTemplate/templateRule/${ruleId}/enable`).then((res: any) => {
      if (res.code !== 200) {
        ElMessage({
          message: t('app.enableFail'),
          type: 'error'
        })
      }
      enableLoading.value = false
      requestRuleData()
    }).catch(() => {
      ElMessage({
        message: t('app.enableFail'),
        type: 'error'
      })
      enableLoading.value = false
      requestRuleData()
    })
  }
}
onMounted(() => {
  offsetHeight.value = document.body.offsetHeight - 110
  requestTemplateData()
})
</script>
<style scoped lang='scss'>
.el-table {
  height: calc(100vh - 110px - 62px - 88px - 34px);
}

.pagination {
  margin-top: 5px;
}
</style>