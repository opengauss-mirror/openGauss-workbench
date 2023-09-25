<template>
  <div>
    <div class="page-header">
      <div class="icon"></div>
      <div class="title" v-if="state === 'add'">{{ t('alertRule.addTitle') }}</div>
      <div class="title" v-if="state === 'edit'">{{ t('alertRule.editTitle') }}</div>
      <div class="title" v-if="state === 'detail'">{{ t('alertRule.detailTitle') }}</div>
      <div class="seperator"></div>
      <el-breadcrumb separator="/" style="flex-grow: 1">
        <el-breadcrumb-item>
          <div @click="cancel">
            <a>{{ t('alertRule.title') }}</a>
          </div>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <div v-if="state === 'add'">{{ t('alertRule.addTitle') }} </div>
          <div v-if="state === 'edit'">{{ t('alertRule.editTitle') }} </div>
          <div v-if="state === 'detail'">{{ t('alertRule.detailTitle') }} </div>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-form :model="formData" ref="formRef" :rules="formRules" label-position="left" label-width="120px" size="default">
      <div class="form-header">
        <el-descriptions :title="$t('alertRule.alertTitle')"></el-descriptions>
      </div>
      <el-form-item :label="$t('alertRule.ruleName')" prop="ruleName">
        <el-input v-model="formData.ruleName" :placeholder="$t('alertRule.ruleNamePlaceholder')" :disabled="disabled"
          maxlength="50" show-word-limit></el-input>
      </el-form-item>
      <el-form-item :label="$t('alertRule.ruleType')" prop="ruleType">
        <el-radio-group v-model="formData.ruleType" :disabled="disabled || state === 'edit'">
          <el-radio v-for="item in ruleTypeList" :key="item" :label="item">{{ $t(`alertRule.${item}`)
          }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="$t('alertRule.level')" prop="level">
        <el-radio-group v-model="formData.level" :disabled="disabled">
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
              <span style="margin: 5px 0 10px 5px;" :class="disabled ? '' : 'request'">{{ $t('alertRule.keyword')
              }}:</span><el-input v-model="item.keyword" :disabled="disabled"
                style="width: 400px;margin: 5px;height: 32px;"></el-input>
              <span style="margin: 5px 0 10px 5px;" :class="disabled ? '' : 'request'">{{ $t('alertRule.happen')
              }}:</span>
              <el-select v-model="item.operate" :disabled="disabled" style="width: 70px;margin: 5px;">
                <el-option v-for="item0 in compareSymbolList" :key="item0" :value="item0" :label="item0" />
              </el-select>
              <el-input :class="disabled ? '' : 'request'" v-model="item.limitValue"
                style="width: 100px;margin: 5px;height: 32px;" :disabled="disabled"></el-input>
              <span style="width: 50px;margin: 5px;">{{ $t('alertRule.logUnit') }}</span>
            </div>
            <div style="width: 100%;">
              <span style="margin: 5px 0 10px 5px;">{{ $t('alertRule.blockWord') }}:</span><el-input
                v-model="item.blockWord" :disabled="disabled" style="width: 600px;margin: 5px;height: 32px;"></el-input>
            </div>
          </span>
          <span v-else>
            <span style="margin: 5px;">{{ $t('alertRule.ruleItemNum') }}:</span><el-input v-model="item.ruleMark" disabled
              style="width: 40px;margin: 5px;height: 32px;"></el-input>
            <span style="margin: 5px 0 10px 5px;" :class="disabled ? '' : 'request'">{{ $t('alertRule.ruleItemExp')
            }}:</span>
            <el-select v-model="item.ruleExpName" :disabled="disabled" style="width: 130px;margin: 5px 0 5px 5px;"
              @change="(val) => changeRuleExpName(val, index)">
              <el-option v-for="item0 in ruleItemSrcList" :key="item0.name" :value="item0.name"
                :label="$t(`alertRule.${item0.name}`)" />
            </el-select>
            <el-input :class="!disabled && item.paramsExplanation[key].required ? 'request' : ''"
              v-for="(val, key) in item.params" v-model="item.params[key]"
              :placeholder="item.paramsExplanation[key] && item.paramsExplanation[key].tip ? t(`alertRule.${item.paramsExplanation[key].tip}`) : ''"
              style="width: 120px;margin: 5px 5px 5px 2px;height: 32px;" :disabled="disabled" :key="key"></el-input>
            <span :class="disabled ? '' : 'request'"></span><el-select v-model="item.action" :disabled="disabled"
              style="width: 150px;margin: 5px;" @change="(val) => changeAction(val, index)">
              <el-option v-for="item0 in item.ruleItemExpSrcList" :key="item0.id" :value="item0.action"
                :label="$t(`alertRule.${item0.action}Action`)" />
            </el-select>
            <span :class="disabled ? '' : 'request'"></span><el-select v-if="item.showLimitValue !== 0"
              v-model="item.operate" :disabled="disabled" style="width: 70px;margin: 5px;">
              <el-option v-for="item0 in compareSymbolList" :key="item0" :value="item0" :label="item0" />
            </el-select>
            <span :class="disabled ? '' : 'request'"></span><el-input v-if="item.showLimitValue !== 0"
              v-model="item.limitValue" style="width: 100px;margin: 5px;height: 32px;" :disabled="disabled"></el-input>
            <span v-if="item.unit" style="width: 50px;margin: 5px;">{{ item.unit }}</span>
          </span>
          <span v-if="(state === 'add' || state === 'edit') && formData.alertRuleItemList.length > 1"
            style="margin: 5px 10px 5px 15px;"><el-button type="primary" circle :icon="Delete"
              @click="delItem(index)"></el-button></span>
        </el-row>
        <div v-if="state === 'add' || state === 'edit'" style="width: 100%;">
          <span style="margin: 5px;"><el-button type="primary" @click="addItem" circle :icon="Plus"></el-button></span>
        </div>
      </el-form-item>
      <el-form-item :label="$t('alertRule.ruleExpComb')" prop="ruleExpComb">
        <span v-for="(item, index) in ruleExpComb" :key="item" style="margin: 0 5px">
          <el-select v-model="ruleExpComb[index]" v-if="index % 2 === 1" style="width: 60px;" :disabled="disabled">
            <el-option v-for="item0 in logicSymbolList" :key="item0" :value="item0" :label="$t(`alertRule.${item0}`)" />
          </el-select>
          <el-select v-model="ruleExpComb[index]" v-else style="width: 60px;" :disabled="disabled">
            <el-option v-for="item0 in ruleMarkList" :key="item0" :value="item0" :label="item0" />
          </el-select>
        </span>
      </el-form-item>
      <el-row>
        <el-col :span="14">
          <el-form-item :label="$t('alertRule.ruleContent')" prop="ruleContent">
            <el-input v-model="formData.ruleContent" type="textarea" :placeholder="$t('alertRule.ruleContentPlaceholder')"
              :disabled="disabled" @blur="preview"></el-input>
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
            <span>{{ previewContent }}</span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item :label="$t('alertRule.notifyDuration')" prop="notifyDuration">
        <el-input v-model="formData.notifyDuration" style="width: 200px;margin-right: 5px;" :disabled="disabled">
          <template #append>
            <el-select v-model="formData.notifyDurationUnit" :disabled="disabled" style="width: 80px;">
              <el-option v-for="item in durationUnitList" :disabled="disabled" :key="item.name" :value="item.value"
                :label="$t(`alertRule.${item.name}`)" />
            </el-select>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('alertRule.checkFrequency')" prop="checkFrequency"
        v-if="formData.ruleType === 'log'">
        <el-input v-model="formData.checkFrequency" style="width: 200px;margin-right: 5px;" :disabled="disabled">
          <template #append>
            <el-select v-model="formData.checkFrequencyUnit" :disabled="disabled" style="width: 80px;">
              <el-option v-for="item in durationUnitList" :disabled="disabled" :key="item.name" :value="item.value"
                :label="$t(`alertRule.${item.name}`)" />
            </el-select>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item :label="$t('alertRule.alertDesc')" prop="alertDesc">
        <el-input v-model="formData.alertDesc" style="width: 60%;" :disabled="disabled"></el-input>
      </el-form-item>
      <div class="form-header">
        <el-descriptions :title="$t('alertRule.notifyTitle')"></el-descriptions>
      </div>
      <el-form-item :label="$t('alertRule.alertNotify')" prop="alertNotify">
        <el-checkbox-group v-model="alertNotifyList" :disabled="disabled">
          <el-checkbox label="firing">{{ $t('alertRule.firing') }}</el-checkbox>
          <el-checkbox label="recover" v-if="formData.ruleType === 'index'">{{ $t('alertRule.recover') }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item :label="$t('alertRule.repeat')" prop="isRepeat" v-if="formData.ruleType === 'index'" :required="true">
        <el-radio-group v-model="formData.isRepeat" :disabled="disabled">
          <el-radio :label="1">{{ $t('alertRule.isRepeat') }}</el-radio>
          <el-radio :label="0">{{ $t('alertRule.isNotRepeat') }}</el-radio>
        </el-radio-group>
        <span v-if="formData.isRepeat === 1">
          <span style="margin-left: 30px;margin-right: 5px;">{{ $t('alertRule.nextRepeat')
          }}:</span>
          <el-input v-model="formData.nextRepeat" style="width: 200px;margin-right: 5px;" :disabled="disabled">
            <template #append>
              <el-select v-model="formData.nextRepeatUnit" :disabled="disabled" style="width: 80px;">
                <el-option v-for="item in durationUnitList" :disabled="disabled" :key="item.name" :value="item.value"
                  :label="$t(`alertRule.${item.name}`)" />
              </el-select>
            </template>
          </el-input>
          <span style="margin-left: 30px;margin-right: 5px;">{{
            $t('alertRule.maxRepeatCount') }}:</span>
          <el-input v-model="formData.maxRepeatCount" style="width: 100px;margin-right: 5px;"
            :disabled="disabled"></el-input>
        </span>
      </el-form-item>
      <el-form-item :label="$t('alertRule.silence')" prop="isSilence">
        <el-radio-group v-model="formData.isSilence" :disabled="disabled">
          <el-radio :label="1">{{ $t('alertRule.isSilence') }}</el-radio>
          <el-radio :label="0">{{ $t('alertRule.isNotSilence') }}</el-radio>
        </el-radio-group>
        <span :class="disabled ? '' : 'request'" style="margin-left: 30px;" v-if="formData.isSilence === 1">{{
          $t('alertRule.silenceTime')
        }}</span>
        <span style="width: 250px;margin-left: 5px;">
          <el-date-picker :disabled="disabled" v-if="formData.isSilence === 1" v-model="silenceTimes" type="datetimerange"
            range-separator="~" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss"
            :start-placeholder="t(`alertRecord.startTimePlaceholder`)"
            :end-placeholder="t(`alertRecord.endTimePlaceholder`)" />
        </span>
      </el-form-item>
      <el-form-item :label="$t('alertRule.notifyWay')" prop="notifyWay">
        <el-select v-model="notifyWayIdArr" multiple :disabled="disabled">
          <el-option v-for="item in notifyWayList" :key="item.id" :value="item.id + ''" :label="item.name" />
        </el-select>
      </el-form-item>
    </el-form>
    <el-row style="margin-top: 10px;">
      <el-button v-if="state === 'add' || state === 'edit'" type="primary" @click="confirm" :loading="loading">{{
        t('app.confirm') }}</el-button>
      <el-button @click="cancel" :loading="loading">{{ t('app.cancel') }}</el-button>
    </el-row>
  </div>
</template>

<script setup lang='ts'>
import "element-plus/es/components/message-box/style/index";
import { Delete, Plus } from "@element-plus/icons-vue";
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import { parseContent } from "@/utils/commonUtil"
import type { FormInstance, FormRules } from 'element-plus'
const { t } = useI18n();

const alertContentParam = ref<any>()
const paramNameList = ref<any>()
const previewContent = ref<string>()
const ruleContent = ref<string>('')

const props = withDefaults(
  defineProps<{
    ruleId: number,
    state: string,
  }>(),
  {
    state: 'detail',
    ruleId: undefined
  }
);

const emit = defineEmits(["updateRule", "cancelRule"]);

const loading = ref<boolean>(false)
const disabled = ref<boolean>(true)
const ruleItemSrcList = ref<any[]>()
const logicSymbolList = ref<string[]>(['and', 'or'])
const compareSymbolList = ref<string[]>(['>', '>=', '==', '<=', '<', '!='])
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
const notifyWayList = ref<any[]>([])
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

const { data: ruleDetail, run: requestData } = useRequest(
  (id) => {
    return request.get(`/api/v1/alertRule/${id}`)
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
          item.paramsExplanation = itemSrcList[0].paramsExplanation ? JSON.parse(itemSrcList[0].paramsExplanation) : {}
          requestRuleItemExpSrcList(itemSrcList[0].id, index)
        })
      }
    }
    if (formData.value?.ruleExpComb) {
      ruleExpComb.value = formData.value?.ruleExpComb.split(' ')
    }
    if (formData.value?.alertNotify) {
      alertNotifyList.value = formData.value?.alertNotify.split(',')
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

const addItem = () => {
  const str = "A";
  let ruleMark = String.fromCharCode(str.charCodeAt(0) + formData.value.alertRuleItemList.length)
  formData.value.alertRuleItemList.push(
    {
      ruleMark,
      ruleExpName: "",
      action: "",
      operate: "",
      limitValue: "",
      unit: "",
      ruleExpParam: "",
      ruleItemExpSrcList: [],
      params: {},
      paramsExplanation: {}
    }
  )
  ruleMarkList.value = formData.value.alertRuleItemList.map((item: any) => item.ruleMark)
  ruleExpComb.value.push("")
  ruleExpComb.value.push(ruleMark)
}
const delItem = (index: number) => {
  formData.value.alertRuleItemList.splice(index, 1)
  for (let i = 0; i < formData.value.alertRuleItemList.length; i++) {
    formData.value.alertRuleItemList[i].ruleMark = String.fromCharCode('A'.charCodeAt(0) + i)
  }
  ruleMarkList.value = formData.value.alertRuleItemList.map((item: any) => item.ruleMark)
  ruleExpComb.value = []
  for (let i = 0; i < ruleMarkList.value.length; i++) {
    ruleExpComb.value.push(ruleMarkList.value[i]);
    if (i < ruleMarkList.value.length - 1) {
      ruleExpComb.value.push("");
    }
  }
}
const changeRuleExpName = (val: any, index: number) => {
  formData.value.alertRuleItemList[index].ruleItemExpSrcList = []
  if (val) {
    let itemSrcList = ruleItemSrcList.value?.filter(item => item.name === val) as any[]
    formData.value.alertRuleItemList[index].unit = itemSrcList[0].unit
    if (itemSrcList[0].params) {
      formData.value.alertRuleItemList[index].params = Object.assign(formData.value.alertRuleItemList[index].params, JSON.parse(itemSrcList[0].params))
      formData.value.alertRuleItemList[index].paramsExplanation = itemSrcList[0].paramsExplanation ? JSON.parse(itemSrcList[0].paramsExplanation) : {}
    } else {
      formData.value.alertRuleItemList[index].params = {}
    }
    let itemSrcIdList = itemSrcList.map(item => item.id) as any[]
    requestRuleItemExpSrcList(itemSrcIdList[0], index)
  }
}
const changeAction = (val: any, index: number) => {
  let ruleItem = formData.value.alertRuleItemList[index]
  let itemExpSrcList = ruleItem.ruleItemExpSrcList.filter((item: any) => item.action === val) as any[]
  ruleItem.operate = itemExpSrcList[0].operate
  ruleItem.limitValue = itemExpSrcList[0].limitValue
  ruleItem.ruleExp = itemExpSrcList[0].exp
  ruleItem.showLimitValue = itemExpSrcList[0].showLimitValue
}

const requestAlertContentParam = () => {
  request.get(`/api/v1/environment/alertContentParam`, { type: 'alert' }).then((res: any) => {
    if (res && res.code === 200) {
      alertContentParam.value = res.data
      paramNameList.value = Object.keys(alertContentParam.value)
      if (alertContentParam.value && formData.value.ruleContent) {
        let param = {}
        let keys = Object.keys(alertContentParam.value);
        for (let key of keys) {
          param[key] = alertContentParam.value[key]['preVal']
        }
        previewContent.value = parseContent(formData.value.ruleContent, param)
      }
    }
  })
}
const preview = () => {
  if (alertContentParam.value && formData.value.ruleContent) {
    let param = {}
    let keys = Object.keys(alertContentParam.value);
    for (let key of keys) {
      param[key] = alertContentParam.value[key]['preVal']
    }
    previewContent.value = parseContent(formData.value.ruleContent, param)
  } else {
    previewContent.value = ''
  }
}
const reset = () => {
  formData.value.ruleContent = ruleContent.value
  preview()
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
const checkRuleExpComb = (rule: any, value: any, callback: any) => {
  if (ruleExpComb.value.length === 0) {
    callback(new Error(t('alertRule.ruleExpCombTip')))
  }
  for (let item of ruleExpComb.value) {
    if (!item) {
      callback(new Error(t('alertRule.ruleExpCombTip')))
    }
  }
  callback()
}
const checkNotifyDuration = (rule: any, value: any, callback: any) => {
  if (!formData.value.notifyDuration || !formData.value.notifyDurationUnit) {
    callback(new Error(t('alertRule.notifyDurationTip')))
  }
  if (!/^\d+$/.test(formData.value.notifyDuration)) {
    callback(new Error(t('alertRule.notifyDurationIsNumber')))
  }
  callback()
}
const checkCheckFrequency = (rule: any, value: any, callback: any) => {
  if (formData.value.ruleType !== 'log') {
    callback()
  }
  if (!formData.value.checkFrequency || !formData.value.checkFrequencyUnit) {
    callback(new Error(t('alertRule.checkFrequencyTip')))
  }
  if (!/^\d+$/.test(formData.value.checkFrequency)) {
    callback(new Error(t('alertRule.checkFrequencyIsNumber')))
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
  ruleName: [
    { required: true, message: t('alertRule.ruleNamePlaceholder'), trigger: 'blur' },
  ],
  ruleType: [
    { required: true, message: t('alertRule.selectRuleType'), trigger: 'blur' },
  ],
  level: [
    { required: true, message: t('alertRule.selectLevelType'), trigger: 'blur' },
  ],
  ruleItem: [
    { required: true, validator: checkRuleItem, trigger: 'blur' },
  ],
  ruleExpComb: [
    { required: true, validator: checkRuleExpComb, trigger: 'blur' },
  ],
  ruleContent: [
    { required: true, message: t('alertRule.ruleContentPlaceholder'), trigger: 'blur' },
  ],
  notifyDuration: [
    { required: true, validator: checkNotifyDuration, trigger: 'blur' },
  ],
  checkFrequency: [
    { required: true, validator: checkCheckFrequency, trigger: 'blur' },
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
  if (!formRef) return
  formRef.value?.validate((valid, fields) => {
    if (valid) {
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
      loading.value = true
      request.post(`/api/v1/alertRule`, formData.value).then((res: any) => {
        loading.value = false
        if (res && res.code === 200) {
          ElMessage({
            showClose: true,
            message: t("app.saveSuccess"),
            type: "success",
          });
          emit("updateRule")
        } else {
          ElMessage({
            showClose: true,
            message: t("app.saveFail"),
            type: "error",
          });
        }
      }).catch(() => {
        ElMessage({
          showClose: true,
          message: t("app.saveFail"),
          type: "error",
        });
        loading.value = false
      })
    }
  })
}
const cancel = () => {
  emit("cancelRule")
}

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
          item.paramsExplanation = itemSrcList[0].paramsExplanation ? JSON.parse(itemSrcList[0].paramsExplanation) : {}
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
onMounted(() => {
  requestNotifyWayData()
  requestRuleItemSrcList()
  requestAlertContentParam();
  if (props.ruleId) {
    requestData(props.ruleId)
  }
  if (props.state !== 'detail') {
    disabled.value = false
  } else {
    disabled.value = true
  }
})
</script>
<style scoped lang='scss'>
.alert-param {
  z-index: 9999;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  position: absolute;
  align-items: flex-start;
  padding: 15px 16px;
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