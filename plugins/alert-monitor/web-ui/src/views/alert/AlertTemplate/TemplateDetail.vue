<template>
  <div v-if="showMain">
    <div class="page-header">
      <div class="icon"></div>
      <div class="title">{{ t('alertTemplate.title') }}</div>
      <div class="seperator"></div>
      <el-breadcrumb separator="/" style="flex-grow: 1">
        <el-breadcrumb-item>
          <div @click="cancel">
            <a>{{ t('alertTemplate.title') }}</a>
          </div>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <div>{{ title }} </div>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-form :model="formData" :rules="formRules" ref="formRef" size="default" label-position="left" style="margin-top: 8px;"
      label-width="100px">
      <el-form-item :label="$t('alertTemplate.templateName')" prop="templateName">
        <el-input v-model="formData.templateName" style="height: 32px !important;"
          :placeholder="$t('alertTemplate.templateNamePlaceholder')"></el-input>
      </el-form-item>
      <el-form-item :label="$t('alertTemplate.table[1]')" prop="type">
        <el-radio-group v-model="formData.type" :disabled="disabled || state === 'edit'" @change="changeType">
          <el-radio label="instance">{{ $t('app.instance') }}</el-radio>
          <el-radio label="plugin">{{ $t('app.plugin') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="$t('alertTemplate.selectRule')" :required="true">
        <el-table size="small" :data="tableDatas" ref="ruleTable" style="width: 100%" header-cell-class-name="grid-header"
          border>
          <el-table-column type="selection" width="55" />
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
              <el-link :underline="false" type="primary" size="small"
                @click.prevent="edit(scope.row.templateRuleId, scope.row.ruleId)">
                {{ t('app.edit') }}
              </el-link>
            </template>
          </el-table-column>
          <!-- <el-table-column prop="ruleContent" :label="$t('alertRule.table[6]')" /> -->
        </el-table>
      </el-form-item>
    </el-form>
    <el-row>
      <el-button type="primary" @click="confirm" :loading="loading">{{ t('app.confirm') }}</el-button>
      <el-button @click="cancel" :loading="loading">{{ t('app.cancel') }}</el-button>
    </el-row>
  </div>
  <div v-else>
    <TemplateRuleDetail :ruleId="editRuleId" :templateRuleId="editTemplateRuleId" :titleList="titleList"
      @updateTemplateRuleSuccess="updateTemplateRuleSuccess" @cancelUpdateTemplateRule="cancelUpdateTemplateRule" />
  </div>
</template>

<script setup lang='ts'>
import "element-plus/es/components/message-box/style/index";
import { ref, nextTick } from 'vue'
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import { i18n } from '@/i18n'
import type { FormInstance, FormRules } from 'element-plus'
import TemplateRuleDetail from "@/views/alert/AlertTemplate/TemplateRuleDetail.vue";
const { t } = useI18n();
const props = withDefaults(
  defineProps<{
    templateId: number,
    state: string,
    type: string,
  }>(),
  {
    state: 'add',
    templateId: undefined,
    type: 'instance',
  }
);
const emit = defineEmits(["updateTemplate", "cancelTemplate"]);

const loading = ref<boolean>(false)
const title = ref<string>()
const showMain = ref<boolean>(true)
const ruleTable = ref();
const templateRuleIdsSrc = ref<string[]>([])
const formData = ref<any>({
  templateName: '',
  type: 'instance',
  templateRuleList: []
})
const tableDatas = ref<any[]>([])

const selectedRuleRows = ref<any[]>([])
const editTemplateRuleId = ref<number>()
const editRuleId = ref<number>()
const titleList = ref<string[]>([])

const formRef = ref<FormInstance>()
const formRules = reactive<FormRules>({
  templateName: [
    { required: true, message: t('alertTemplate.templateNamePlaceholder'), trigger: 'blur' },
  ],
  type: [
      { required: true, message: t('alertTemplate.selectTypePlaceholder'), trigger: 'change' },
  ],
})

const showRuleExpDesc = (rule: any) => {
  const alertRuleItemList = rule.alertRuleItemList;
  if (!alertRuleItemList || alertRuleItemList.length === 0) {
    return '/';
  }

  if (rule.ruleType === 'index') {
    return alertRuleItemList.map((item: any) => {
      let ruleItemSrc = ruleItemSrcList.value.filter(item0 => item0.name === item.ruleExpName)[0]
      let paramStr = "";
      const ruleExpParam = item.ruleExpParam
      if (ruleExpParam) {
        let param = JSON.parse(ruleExpParam)
        paramStr = '(' + Object.keys(param).map((key: any) => param[key]).join(',') + ')'
      }
      let name = i18n.global.locale.value === 'zhCn' && ruleItemSrc && ruleItemSrc.nameZh ? ruleItemSrc.nameZh : (ruleItemSrc && ruleItemSrc.nameEn) ? ruleItemSrc.nameEn : ruleItemSrc ? t(`alertRule.${ruleItemSrc.name}`) : ''
      if (!item.operate) {
        return `[${item.ruleMark}]: ${name}`
      }
      return `[${item.ruleMark}]: ${name + paramStr + ' ' + (item.action === 'normal' ? (item.operate + item.limitValue + item.unit) : t(`alertRule.${item.action}Action`))}`
    }).join('<br />')
  } else {
    return alertRuleItemList.map((item: any) => {
      return `[${item.ruleMark}]:${t('alertRule.keyword')}(${item.keyword}${item.blockWord ? '，' + t('alertRule.blockWord') + ':' + item.blockWord : ''})${t('alertRule.happen')} ${item.operate}${item.limitValue}${t('alertRule.logUnit')}`
    }).join('<br />')
  }
}

const showRuleExpComb = (ruleExpComb: String) => {
  if (!ruleExpComb) {
    return '/'
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
const { data: ruleRes, run: requestRuleData } = useRequest(
  (ruleTypes) => {
    return request.get(`/api/v1/alertRule/ruleList`, { ruleTypes })
  },
  { manual: true }
);
watch(ruleRes, (ruleRes: any) => {
  if (ruleRes && ruleRes.code === 200) {
    tableDatas.value = ruleRes.data
    let selectDatas = formData.value.templateRuleList || [];
    if (selectDatas.length > 0) {
      tableDatas.value = tableDatas.value.map(item => {
        let datas = selectDatas.filter((item0: any) => item0.ruleId === item.ruleId) || []
        if (datas.length > 0) {
          if (datas[0].isIncluded === 1) {
            nextTick(() => {
              ruleTable.value.toggleRowSelection(datas[0], true)
            })
          }
          return datas[0]
        }
        return item;
      })
    }
  } else {
    tableDatas.value = []
  }
});

const { data: templateRes, run: requestData } = useRequest(
  (id) => {
    return request.get(`/api/v1/alertTemplate/${id}`)
  },
  { manual: true }
);
watch(templateRes, (templateRes: any) => {
  if (templateRes && templateRes.code === 200) {
    formData.value = templateRes.data
    changeType(formData.value.type)
    let selectDatas = formData.value.templateRuleList || [];
    templateRuleIdsSrc.value = selectDatas.filter(item => item.isIncluded).map(item => item.templateRuleId) || []
    if (tableDatas.value.length > 0) {
      tableDatas.value = tableDatas.value.map(item => {
        let datas = selectDatas.filter((item0: any) => item0.ruleId === item.ruleId) || []
        if (datas.length > 0) {
          if (datas[0].isIncluded === 1) {
            nextTick(() => {
              ruleTable.value.toggleRowSelection(datas[0], true)
            })
          }
          return datas[0]
        }
        return item;
      })
    }
  } else {
    tableDatas.value = []
  }
});

const confirm = () => {
  if (!formRef.value) return
  formRef.value?.validate((valid, fields) => {
    if (!valid) {
      return;
    }
    const rows = ruleTable.value.getSelectionRows() || []
    if (rows.length === 0) {
      ElMessage({
        message: t('app.selectDatasTip'),
        type: 'warning'
      })
      return;
    }
    let templateRuleIds = []
    let templateRuleReqList = rows.map((item: any) => {
      if (item.templateRuleId) {
        templateRuleIds.push(item.templateRuleId)
      }
      return { templateRuleId: item.templateRuleId, ruleId: item.ruleId }
    })
    let excludedTemplateRuleIds = templateRuleIdsSrc.value.filter(item => !templateRuleIds.includes(item)) || []
    let param = {
      id: formData.value.id,
      templateName: formData.value.templateName,
      type: formData.value.type,
      templateRuleReqList,
      excludedTemplateRuleIds
    }
    loading.value = true
    request.post(`/api/v1/alertTemplate`, param).then((res: any) => {
      loading.value = false
      if (res && res.code === 200) {
        ElMessage({
          message: t('app.saveSuccess'),
          type: 'success'
        })
        formData.value = {
          templateName: '',
          templateRuleList: []
        }
        templateRuleIdsSrc.value = []
        emit("updateTemplate")
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
  })
}
const cancel = () => {
  formData.value = {
    templateName: '',
    templateRuleList: []
  }
  emit("cancelTemplate")
}

const edit = (templateRuleId: any, ruleId: any) => {
  selectedRuleRows.value = ruleTable.value.getSelectionRows() || []
  editTemplateRuleId.value = templateRuleId
  editRuleId.value = ruleId
  let curTitle = title.value || ''
  titleList.value = [t('alertTemplate.title'), curTitle, t('alertRule.editTitle')]
  showMain.value = false
}

const updateTemplateRuleSuccess = (templateRule: any) => {
  for (let i = 0; i < tableDatas.value.length; i++) {
    if (tableDatas.value[i].ruleId === templateRule.ruleId) {
      tableDatas.value[i].templateRuleId = templateRule.templateRuleId
      tableDatas.value[i].level = templateRule.level
      tableDatas.value[i].ruleExpDesc = templateRule.ruleExpDesc
      tableDatas.value[i].ruleExpComb = templateRule.ruleExpComb
      tableDatas.value[i].isRepeat = templateRule.isRepeat
      tableDatas.value[i].isSilence = templateRule.isSilence
      tableDatas.value[i].silenceStartTime = templateRule.silenceStartTime
      tableDatas.value[i].silenceEndTime = templateRule.silenceEndTime
      tableDatas.value[i].alertNotify = templateRule.alertNotify
      tableDatas.value[i].notifyWayIds = templateRule.notifyWayIds
      tableDatas.value[i].alertRuleItemList = templateRule.alertRuleItemList
      break
    }
  }
  showMain.value = true
  nextTick(() => {
    for (let row of selectedRuleRows.value) {
      ruleTable.value.toggleRowSelection(row, true)
    }
  })
}

const cancelUpdateTemplateRule = (num = 0) => {
  showMain.value = true
  nextTick(() => {
    for (let row of selectedRuleRows.value) {
      if (row.isIncluded === 1) {
        ruleTable.value.toggleRowSelection(row, true)
      }
    }
  })
  if (num > 0) {
    cancel()
  }
}

const ruleItemSrcList = ref<any[]>([])
const requestRuleItemSrcList = () => {
  request.get(`/api/v1/alertRule/ruleItemSrc/list`).then((res: any) => {
    if (res && res.code === 200) {
      ruleItemSrcList.value = res.data
    }
  })
}

const changeType = (type) => {
  let ruleTypes = ''
  if (type === 'instance') {
    ruleTypes = 'index,log'
  } else {
    ruleTypes = 'plugin'
  }
  requestRuleData(ruleTypes)
}

onMounted(() => {
  requestRuleItemSrcList()
  title.value = props.state === 'add' ? t('alertTemplate.addTitle') : props.state === 'edit' ? t('alertTemplate.editTitle') : t('alertTemplate.detailTitle')
  formData.value.type = props.type || 'instance'
  changeType(props.type)
  if (props.templateId) {
    requestData(props.templateId)
  } 
})

</script>
<style scoped lang='scss'></style>