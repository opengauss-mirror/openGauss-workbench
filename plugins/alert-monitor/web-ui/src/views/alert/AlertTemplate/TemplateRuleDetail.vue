<template>
  <div>
    <div class="page-header" v-if="titleList.length > 0">
      <div class="icon"></div>
      <div class="title">{{ titleList[titleList.length - 1] }}</div>
      <div class="seperator"></div>
      <el-breadcrumb separator="/" style="flex-grow: 1">
        <el-breadcrumb-item v-for="(item, index) in titleList" :key="item">
          <div @click="cancel(titleList.length - 1 - index)" v-if="index < titleList.length - 1">
            <a>{{ item }}</a>
          </div>
          <div v-else>{{ item }} </div>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-form :model="formData" ref="formRef" :rules="formRules" label-position="left" label-width="120px" size="default">
      <div class="form-header">
        <el-descriptions :title="$t('alertRule.alertTitle')"></el-descriptions>
      </div>
      <el-form-item :label="$t('alertRule.ruleName')" prop="ruleName">
        <el-input v-model="formData.ruleName" disabled :placeholder="$t('alertRule.ruleNamePlaceholder')"></el-input>
      </el-form-item>
      <el-form-item :label="$t('alertRule.ruleType')" prop="ruleType">
        <el-radio-group v-model="formData.ruleType" disabled>
          <el-radio v-for="item in ruleTypeList" :key="item" :label="item">{{ $t(`alertRule.${item}`)
          }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="$t('alertRule.level')" prop="level">
        <el-radio-group v-model="formData.level">
          <el-radio v-for="item in levelList" :key="item" :label="item">{{ $t(`alertRule.${item}`) }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="$t('alertRule.ruleItem')" prop="ruleItem">
        <el-row v-for="(item, index ) in formData.alertRuleItemList" :key="item.id" class="rule"
          style="margin-bottom: 5px;">
          <span v-if="formData.ruleType === 'log'">
            <div style="width: 100%;">
              <span style="margin: 5px;">{{ $t('alertRule.ruleItemNum') }}:</span><el-input v-model="item.ruleMark"
                disabled style="width: 40px;margin: 5px;height: 32px;"></el-input>
            </div>
            <div style="width: 100%;">
              <span style="margin: 5px 0 10px 5px;">{{ $t('alertRule.blockWord') }}:</span><el-input
                v-model="item.keyword" disabled style="width: 400px;margin: 5px;height: 32px;"></el-input>
              <span style="margin: 5px 0 10px 5px;">{{ $t('alertRule.happen') }}:</span>
              <el-select v-model="item.operate" disabled style="width: 70px;margin: 5px;">
                <el-option v-for="item0 in compareSymbolList" :key="item0" :value="item0" :label="item0" />
              </el-select>
              <el-input v-model="item.limitValue" class="request" style="width: 100px;margin: 5px;height: 32px;"></el-input>
              <span style="width: 50px;margin: 5px;">{{ $t('alertRule.logUnit') }}</span>
            </div>
            <div style="width: 100%;">
              <span style="margin: 5px 0 10px 5px;">{{ $t('alertRule.blockWord') }}:</span><el-input
                v-model="item.blockWord" disabled style="width: 600px;margin: 5px;height: 32px;"></el-input>
            </div>
          </span>
          <span v-else>
            <span style="margin: 5px;">{{ $t('alertRule.ruleItemNum') }}:</span><el-input v-model="item.ruleMark" disabled
              style="width: 40px;margin: 5px;height: 32px;"></el-input>
            <span style="margin: 5px 0 10px 5px;">{{ $t('alertRule.ruleItemExp') }}:</span>
            <el-select v-model="item.ruleExpName" disabled style="width: 130px;margin: 5px 0 5px 5px;">
              <el-option v-for="item0 in ruleItemSrcList" :key="item0.name" :value="item0.name"
              :label="i18n.global.locale.value === 'zhCn' && item0.nameZh ? item0.nameZh : item0.nameEn ? item0.nameEn : $t(`alertRule.${item0.name}`)" />
            </el-select>
            <el-input v-for="(val, key) in item.params" v-model="item.params[key]"
              style="width: 120px;margin: 5px 2px;height: 32px;" disabled :key="key"></el-input>
            <!-- <el-select v-model="item.action" disabled style="width: 150px;margin: 5px;">
              <el-option v-for="item0 in item.ruleItemExpSrcList" :key="item0.id" :value="item0.action"
                :label="$t(`alertRule.${item0.action}Action`)" />
            </el-select> -->
            <el-select v-if="item.showLimitValue !== 0" v-model="item.operate" disabled
              style="width: 70px;margin: 5px;">
              <el-option v-for="item0 in compareSymbolList" :key="item0" :value="item0" :label="item0" />
            </el-select>
            <el-input v-if="item.showLimitValue !== 0" class="request" v-model="item.limitValue"
              style="width: 100px;margin: 5px;height: 32px;"></el-input>
            <span v-if="item.unit" style="width: 50px;margin: 5px;">{{ item.unit }}</span>
          </span>
        </el-row>
      </el-form-item>
      <el-form-item :label="$t('alertRule.ruleExpComb')" prop="ruleExpComb">
        <span v-for="(item, index) in ruleExpComb" :key="item" style="margin: 0 5px">
          <el-select v-model="ruleExpComb[index]" v-if="index % 2 === 1" style="width: 60px;" disabled>
            <el-option v-for="item0 in logicSymbolList" :key="item0" :value="item0" :label="$t(`alertRule.${item0}`)" />
          </el-select>
          <el-select v-model="ruleExpComb[index]" v-else style="width: 60px;" disabled>
            <el-option v-for="item0 in ruleMarkList" :key="item0" :value="item0" :label="item0" />
          </el-select>
        </span>
      </el-form-item>
      <el-row>
        <el-col :span="14">
          <el-form-item :label="$t('alertRule.ruleContent')" prop="ruleContent" style="margin-bottom: 0px !important;">
            <el-input type="textarea" v-model="formData.ruleContent" @blur="preview"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="2" style="padding-left: 5px;">
          <el-button @click="reset" style="display: flex;">{{ t('app.reset') }}</el-button>
        </el-col>
        <el-col :span="8" style="margin-bottom: 0px;">
          <div class="alert-param">
            <div class="title"><svg-icon name="tip" />{{ t('alertRule.alertContentTitle') }}</div>
            <div class="content">
              <span v-html="t('alertRule.alertContentTip')"></span>
              <el-row style="margin-top: 10px;">
                <el-col :span="12" v-for="item in paramNameList" :key="item">{{ `${item}:
                                  ${alertContentParam[item]['name']}` }}</el-col>
              </el-row>
            </div>
          </div>
        </el-col>
        <el-col :span="14">
          <el-form-item>
            <span style="white-space: pre-wrap">{{ previewContent }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item :label="$t('alertRule.notifyDuration')" prop="notifyDuration">
        <el-input v-model="formData.notifyDuration" style="width: 100px;margin-right: 5px;" disabled></el-input>
        <el-select v-model="formData.notifyDurationUnit" disabled style="width: 80px;">
          <el-option v-for="item in durationUnitList" :key="item.name" :value="item.value"
            :label="$t(`alertRule.${item.name}`)" />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('alertRule.checkFrequency')" prop="checkFrequency" :required="true"
        v-if="formData.ruleType === 'log'">
        <el-input v-model="formData.checkFrequency" style="width: 200px;margin-right: 5px;" disabled>
          <template #append>
            <el-select v-model="formData.checkFrequencyUnit" disabled style="width: 80px;">
              <el-option v-for="item in durationUnitList"  :key="item.name" :value="item.value"
                :label="$t(`alertRule.${item.name}`)" />
            </el-select>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('alertRule.alertDesc')" prop="alertDesc">
        <el-input v-model="formData.alertDesc" style="width: 60%;"></el-input>
      </el-form-item>
      <div class="form-header">
        <el-descriptions :title="$t('alertRule.notifyTitle')"></el-descriptions>
      </div>
      <el-form-item :label="$t('alertRule.alertNotify')" prop="notifyDuration">
        <el-checkbox-group v-model="alertNotifyList">
          <el-checkbox label="firing">{{ $t('alertRule.firing') }}</el-checkbox>
          <el-checkbox label="recover" v-if="formData.ruleType === 'index'">{{ $t('alertRule.recover') }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item :label="$t('alertRule.repeat')" prop="isRepeat" v-if="formData.ruleType === 'index'" :required="true">
        <el-radio-group v-model="formData.isRepeat">
          <el-radio :label="1">{{ $t('alertRule.isRepeat') }}</el-radio>
          <el-radio :label="0">{{ $t('alertRule.isNotRepeat') }}</el-radio>
        </el-radio-group>
        <span v-if="formData.isRepeat === 1">
          <span style="margin-left: 30px;margin-right: 5px;">{{ $t('alertRule.nextRepeat')
          }}:</span>
          <el-input v-model="formData.nextRepeat" style="width: 100px;margin-right: 5px;"></el-input>
          <el-select v-model="formData.nextRepeatUnit" style="width: 80px;">
            <el-option v-for="item in durationUnitList" :key="item.name" :value="item.value"
              :label="$t(`alertRule.${item.name}`)" />
          </el-select>
          <span style="margin-left: 30px;margin-right: 5px;">{{
            $t('alertRule.maxRepeatCount') }}:</span>
          <el-input v-model="formData.maxRepeatCount" style="width: 100px;margin-right: 5px;"></el-input>
        </span>
      </el-form-item>
      <el-form-item :label="$t('alertRule.silence')" prop="isSilence">
        <el-radio-group v-model="formData.isSilence">
          <el-radio :label="1" key="1">{{ $t('alertRule.isSilence') }}</el-radio>
          <el-radio :label="0" key="0">{{ $t('alertRule.isNotSilence') }}</el-radio>
        </el-radio-group>
        <span class="request" style="margin-left: 30px;" v-if="formData.isSilence === 1 || formData.isSilence === '1'">{{
          $t('alertRule.silenceTime')
        }}</span>
        <span style="width: 250px;margin-left: 5px;">
          <el-date-picker v-if="formData.isSilence === 1 || formData.isSilence === '1'" v-model="silenceTimes"
            type="datetimerange" range-separator="~" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss"
            :start-placeholder="t(`alertRecord.startTimePlaceholder`)"
            :end-placeholder="t(`alertRecord.endTimePlaceholder`)" />
        </span>
      </el-form-item>
      <el-form-item :label="$t('alertRule.notifyWay')" prop="notifyWay">
        <el-select v-model="notifyWayIdArr" multiple>
          <el-option v-for="item in notifyWayList" :key="item.id" :value="item.id + ''" :label="item.name" />
        </el-select>
      </el-form-item>
    </el-form>
    <el-row>
      <el-button type="primary" @click="confirm" :loading="loading">{{ t('app.confirm') }}</el-button>
      <el-button @click="cancel" :loading="loading">{{ t('app.cancel') }}</el-button>
    </el-row>
  </div>
</template>

<script setup lang='ts'>
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessage } from "element-plus";
import { i18n } from '@/i18n'
import { useI18n } from "vue-i18n";
import SvgIcon from "@/components/SvgIcon.vue";
import { parseContent, getAlertContentParam } from "@/utils/commonUtil"
import type { FormInstance, FormRules } from 'element-plus'
const { t } = useI18n();

const alertContentParam = ref<any>()
const paramNameList = ref<any>()
const previewContent = ref<string>()

const props = withDefaults(
  defineProps<{
    ruleId: number,
    templateRuleId: number,
    titleList: string[]
  }>(),
  {
    titleList: () => [],
    ruleId: undefined,
    templateRuleId: undefined
  }
);
const emit = defineEmits(["updateTemplateRuleSuccess", "cancelUpdateTemplateRule"]);

const loading = ref<boolean>(false)
const ruleItemSrcList = ref<any[]>()
const durationUnitList = ref<any[]>([{
  name: 'second',
  value: 's'
}, {
  name: 'minute',
  value: 'm'
}, {
  name: 'hour',
  value: 'h'
}, {
  name: 'day',
  value: 'd'
}])
const notifyWayIdArr = ref<string[]>([])
const notifyWayList = ref<string[]>([])
const logicSymbolList = ref<string[]>(['and', 'or'])
const compareSymbolList = ref<string[]>(['>', '>=', '==', '<=', '<', '!='])
const formData = ref<any>({
  ruleName: '',
  ruleType: '',
  level: '',
  ruleExpComb: 'A',
  isRepeat: 0,
  isSilence: 0,
  notifyDurationUnit: 'm',
  checkFrequencyUnit: 'm',
  nextRepeatUnit: 'm',
  alertRuleItemList: [{
    ruleMark: "A",
    ruleExpName: "",
    action: "",
    operate: "",
    limitValue: "",
    unit: "",
    ruleExp: "",
    ruleExpParam: "",
    keyword: "",
    blockWord: "",
    ruleItemExpSrcList: [],
    params: {},
    paramsExplanation: {}
  }]
})
const ruleTypeList = reactive(['index', 'log'])
const levelList = reactive(['serious', 'warn', 'info'])
const ruleExpComb = ref<string[]>(['A'])
const ruleMarkList = ref<string[]>(['A'])
const alertNotifyList = ref<string[]>([])
const silenceTimes = ref<any[]>([])

const ruleContent = ref<string>('')
const reset = () => {
  formData.value.ruleContent = ruleContent.value
  preview()
}

const { data: ruleDetail, run: requestData } = useRequest(
  (templateRuleId, ruleId) => {
    if (templateRuleId) {
      return request.get(`/api/v1/alertTemplate/ruleList/${templateRuleId}`)
    } else {
      return request.get(`/api/v1/alertRule/${ruleId}`)
    }
  },
  { manual: true }
)
watch(ruleDetail, (ruleDetail: any) => {
  if (ruleDetail && ruleDetail.code === 200) {
    formData.value = ruleDetail.data
    ruleContent.value = formData.value?.ruleContent
    if (formData.value.alertRuleItemList && formData.value.alertRuleItemList.length > 0) {
      ruleMarkList.value = formData.value.alertRuleItemList.map((item: any) => item.ruleMark)
      formData.value.alertRuleItemList.forEach((item: any) => {
        if (item.ruleExpParam) {
          item.params = JSON.parse(item.ruleExpParam)
        } else {
          item.params = {}
        }
      });
      if (formData.value.ruleType === 'index' && ruleItemSrcList.value && ruleItemSrcList.value.length > 0) {
        formData.value.alertRuleItemList.forEach((item: any, index: number) => {
          let itemSrcList = ruleItemSrcList.value?.filter(item0 => item0.name === item.ruleExpName) || []
          requestRuleItemExpSrcList(itemSrcList[0].id, index)
        })
      }
    }
    if (formData.value?.ruleExpComb) {
      ruleExpComb.value = formData.value?.ruleExpComb.split(' ')
    }
    if (formData.value?.alertNotify) {
      alertNotifyList.value = formData.value.alertNotify.split(',')
    }
    if (formData.value.notifyWayIds) {
      notifyWayIdArr.value = formData.value.notifyWayIds.split(',')
    }
    if (formData.value.silenceStartTime && formData.value.silenceEndTime) {
      silenceTimes.value = [formData.value.silenceStartTime, formData.value.silenceEndTime]
    }
    if (alertContentParam.value && formData.value.ruleContent) {
      let param = {}
      let keys = Object.keys(alertContentParam.value);
      for (let key of keys) {
        param[key] = alertContentParam.value[key]['preVal']
      }
      previewContent.value = parseContent(formData.value.ruleContent, param)
    }
  } else {
    const msg = t("app.queryFail");
    ElMessage({
      showClose: true,
      message: msg,
      type: "error",
    });
  }
});

const requestNotifyWayData = () => {
  request.get(`/api/v1/notifyWay/list`).then((res: any) => {
    if (res && res.code === 200) {
      notifyWayList.value = res.data
    }
  })
}
const requestRuleItemSrcList = () => {
  request.get(`/api/v1/alertRule/ruleItemSrc/list`).then((res: any) => {
    if (res && res.code === 200) {
      ruleItemSrcList.value = res.data
      if (formData.value.ruleType === 'index' && ruleItemSrcList.value && ruleItemSrcList.value.length > 0 && formData.value.alertRuleItemList && formData.value.alertRuleItemList.length > 0) {
        formData.value.alertRuleItemList.forEach((item: any, index: number) => {
          let itemSrcList = ruleItemSrcList.value?.filter(item0 => item0.name === item.ruleExpName) as any[]
          requestRuleItemExpSrcList(itemSrcList[0].id, index)
        })
      }
    }
  })
}
const requestRuleItemExpSrcList = (ruleItemSrcId: number, index: number) => {
  request.get(`/api/v1/alertRule/ruleItemSrc/${ruleItemSrcId}/ruleItemExpSrcList`).then((res: any) => {
    if (res && res.code === 200) {
      formData.value.alertRuleItemList[index].ruleItemExpSrcList = res.data
      let action = formData.value.alertRuleItemList[index].action
      if (action) {
        let itemExpSrcList = res.data.filter((item: any) => item.action === action) as any[]
        formData.value.alertRuleItemList[index].showLimitValue = itemExpSrcList[0].showLimitValue
      }
    }
  })
}

const checkRuleItem = (rule: any, value: any, callback: any) => {
  let alertRuleItemList = formData.value.alertRuleItemList
  for (let ruleItem of alertRuleItemList) {
    if (formData.value.ruleType === 'log') {
      if (!ruleItem.keyword || !ruleItem.operate || !ruleItem.limitValue) {
        callback(new Error(t('alertRule.ruleItemTip')))
      }
    } else {
      if (!ruleItem.ruleExpName || !ruleItem.action || !ruleItem.operate || !ruleItem.limitValue) {
        callback(new Error(t('alertRule.ruleItemTip')))
      }
      if (ruleItem.params && Object.keys(ruleItem.params).length > 0) {
        for (let key of Object.keys(ruleItem.params)) {
          if (ruleItem.paramsExplanation[key].required && !ruleItem.params[key]) {
            callback(new Error(t('alertRule.ruleItemTip')))
          }
        }
      }
    }
    if (isNaN(Number(ruleItem.limitValue))) {
      callback(new Error(t('alertRule.limitValueMustBeNumber')))
    }
  }
  callback()
}
const checkAlertNotify = (rule: any, value: any, callback: any) => {
  if (!alertNotifyList.value || alertNotifyList.value.length === 0) {
    callback(new Error(t('alertRule.alertNotifyPlaceholder')))
  }
  callback()
}
const checkIsRepeat = (rule: any, value: any, callback: any) => {
  if (formData.value.ruleType !== 'index') {
    callback()
  }
  if (formData.value.isRepeat !== 0 && formData.value.isRepeat !== 1) {
    callback(new Error(t('alertRule.isRepeatTip')))
  }
  if (formData.value.nextRepeat && !/^\d+$/.test(formData.value.nextRepeat)) {
    callback(new Error(t('alertRule.nextRepeatIsNum')))
  }
  if (formData.value.maxRepeatCount && !/^\d+$/.test(formData.value.maxRepeatCount)) {
    callback(new Error(t('alertRule.maxRepeatCountIsNum')))
  }
  callback()
}
const checkIsSilence = (rule: any, value: any, callback: any) => {
  if (formData.value.isSilence !== 0 && formData.value.isSilence !== 1) {
    callback(new Error(t('alertRule.isSilenceTip')))
  }
  if (formData.value.isSilence === 1 && (!silenceTimes.value || silenceTimes.value.length !== 2)) {
    callback(new Error(t('alertRule.silenceTimeTip')))
  }
  callback()
}
const checkNotifyWay = (rule: any, value: any, callback: any) => {
  if (!notifyWayIdArr.value || notifyWayIdArr.value.length === 0) {
    callback(new Error(t('alertRule.notifyWayTip')))
  }
  callback()
}
const formRef = ref<FormInstance>()
const formRules = reactive<FormRules>({
  level: [
    { required: true, message: t('alertRule.selectLevelType'), trigger: 'blur' },
  ],
  ruleItem: [
    { required: true, validator: checkRuleItem, trigger: 'blur' },
  ],
  ruleContent: [
    { required: true, message: t('alertRule.ruleContentPlaceholder'), trigger: 'blur' },
  ],
  alertNotify: [
    { required: true, validator: checkAlertNotify, trigger: 'blur' },
  ],
  isRepeat: [
    { validator: checkIsRepeat, trigger: 'blur' },
  ],
  isSilence: [
    { required: true, validator: checkIsSilence, trigger: 'blur' },
  ],
  notifyWay: [
    { required: true, validator: checkNotifyWay, trigger: 'blur' },
  ]
})

const confirm = () => {
  for (let ruleItem of formData.value.alertRuleItemList) {
    let params = ruleItem.params
    if (params && Object.keys(params).length > 0) {
      ruleItem.ruleExpParam = JSON.stringify(params)
      parseContent(ruleItem.ruleExp, params)
    } else {
      ruleItem.ruleExpParam = ''
    }
  }
  formData.value.ruleExpComb = ruleExpComb.value.join(' ')
  formData.value.alertNotify = alertNotifyList.value.join(',')
  formData.value.notifyWayIds = notifyWayIdArr.value.join(',')
  if (silenceTimes.value.length > 0) {
    formData.value.silenceStartTime = silenceTimes.value[0]
    formData.value.silenceEndTime = silenceTimes.value[1]
  }
  if (formData.value.alertRuleItemList && formData.value.alertRuleItemList.length > 0) {
    for (let row of formData.value.alertRuleItemList) {
      if (row.ruleId) {
        row.ruleId = undefined
      }
      if (row.itemParamList && row.itemParamList.length > 0) {
        for (let row0 of row.itemParamList) {
          row0.ruleItemParamId = undefined
          row0.itemId = undefined
        }
      }
    }
  }
  loading.value = true
  request.post(`/api/v1/alertTemplate/templateRule`, formData.value).then((res: any) => {
    loading.value = false
    if (res && res.code === 200) {
      let data = res.data
      emit("updateTemplateRuleSuccess", data)
    }
  }).catch(() => {
    loading.value = false
  })
}
const cancel = (num = 1) => {
  emit("cancelUpdateTemplateRule", num - 1)
}

const preview = () => {
  if (alertContentParam.value && formData.value.ruleContent) {
    let param = {}
    let keys = Object.keys(alertContentParam.value);
    for (let key of keys) {
      param[key] = alertContentParam.value[key]['preVal']
    }
    previewContent.value = parseContent(formData.value.ruleContent, param)
  }
}

onMounted(() => {
  requestNotifyWayData()
  requestRuleItemSrcList()

  alertContentParam.value = getAlertContentParam()
  paramNameList.value = Object.keys(alertContentParam.value)
  if (alertContentParam.value && formData.value.ruleContent) {
    let param = {}
    let keys = Object.keys(alertContentParam.value);
    for (let key of keys) {
      param[key] = alertContentParam.value[key]['preVal']
    }
    previewContent.value = parseContent(formData.value.ruleContent, param)
  }

  if (props.templateRuleId || props.ruleId) {
    requestData(props.templateRuleId, props.ruleId)
  }
})

</script>
<style scoped lang='scss'>
.alert-param {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  position: absolute;
  align-items: flex-start;
  padding: 9px 16px;
  gap: 6px;
  border: 1px solid #94BFFF;
  border-radius: 2px;

  .title {
    font-style: normal;
    font-weight: 800;
    font-size: 14px;
    line-height: 24px;
  }

  .content {
    font-style: normal;
    font-weight: 800;
    font-size: 12px;
    line-height: 18px;
  }
}
.request::before {
  content: "*";
  color: var(--el-color-danger);
  // margin-right: 4px;
}
</style>