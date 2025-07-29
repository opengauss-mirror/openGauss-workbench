<template>
  <div id="notifyWayDetail">
    <el-form :model="formData" size="default" :rules="formRules" ref="formRef" label-position="left" label-width="130px">
      <el-form-item :label="$t('notifyWay.name')" prop="name">
        <el-input v-model="formData.name" :placeholder="$t('notifyWay.namePlaceholder')" :disabled="disabled"></el-input>
      </el-form-item>
      <el-form-item :label="$t('notifyWay.notifyType')" prop="notifyType">
        <el-select v-model="formData.notifyType" :disabled="disabled" @change="notifyTypeChange">
          <el-option v-for="item in templateTypeList" :key="item.value" :value="item.value" :label="item.name" />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('notifyTemplate.email')" prop="email" v-if="formData.notifyType === 'email'"
        :required="true">
        <el-input v-model="formData.email" :placeholder="$t('notifyWay.emailPlaceholder')"
          :disabled="disabled"></el-input>
      </el-form-item>
      <el-form-item :label="$t('notifyWay.sendWay')" :required="true" prop="sendWay"
        v-if="formData.notifyType === 'WeCom' || formData.notifyType === 'DingTalk'">
        <el-radio-group v-model="formData.sendWay" :disabled="disabled">
          <el-radio :label="0">{{ $t(`notifyWay.normalSendWay`) }}</el-radio>
          <el-radio :label="1">{{ $t(`notifyWay.robotSendWay`) }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="$t('notifyWay.webhook')" :required="true" prop="webhook"
        v-if="(formData.notifyType === 'WeCom' || formData.notifyType === 'DingTalk') && formData.sendWay === 1 || formData.notifyType === 'webhook'">
        <el-input v-model="formData.webhook" :placeholder="$t('notifyWay.webhookPlaceholder')"
          :disabled="disabled"></el-input>
      </el-form-item>
      <el-form-item :label="$t('notifyWay.sign')" v-if="formData.notifyType === 'DingTalk' && formData.sendWay === 1">
        <el-input v-model="formData.sign" :placeholder="$t('notifyWay.signPlaceholder')" :disabled="disabled"></el-input>
      </el-form-item>
      <el-form-item :label="$t('notifyWay.personId')"
        v-if="(formData.notifyType === 'WeCom' || formData.notifyType === 'DingTalk') && formData.sendWay === 0">
        <el-input v-model="formData.personId" :placeholder="$t('notifyWay.personIdPlaceholder')"
          :disabled="disabled"></el-input>
      </el-form-item>
      <el-form-item :label="$t('notifyWay.deptId')"
        v-if="(formData.notifyType === 'WeCom' || formData.notifyType === 'DingTalk') && formData.sendWay === 0">
        <el-input v-model="formData.deptId" :placeholder="$t('notifyWay.deptIdPlaceholder')"
          :disabled="disabled"></el-input>
      </el-form-item>
      <div v-if="formData.notifyType === 'webhook'">
        <el-form-item label="header">
          <span v-for="(item, index) in headerList" :key="index">
            <el-input :disabled="disabled" style="width: 100px;" v-model="item.key"></el-input>
            =
            <el-input :disabled="disabled" style="width: 200px;" v-model="item.val"></el-input>
            <el-button type="primary" v-if="headerList.length > 1" style="margin: 0 4px;" circle :icon="Delete"
              @click="removeHeader(item)"></el-button>
          </span>
          <span style="margin: 5px;"><el-button type="primary" @click="addHeader" circle :icon="Plus"></el-button></span>
        </el-form-item>
        <el-form-item :label="$t('notifyWay.params')">
          <span v-for="(item, index) in paramList" :key="index">
            <el-input :disabled="disabled" style="width: 100px;" v-model="item.key"></el-input>
            =
            <el-input :disabled="disabled" style="width: 200px;" v-model="item.val"></el-input>
            <el-button type="primary" v-if="paramList.length > 1" style="margin: 0 4px;" circle :icon="Delete"
              @click="removeParam(item)"></el-button>
          </span>
          <span style="margin: 5px;"><el-button type="primary" @click="addParam" circle :icon="Plus"></el-button></span>
        </el-form-item>
        <el-row>
          <el-col :span="14">
            <el-form-item label="body" prop="body">
              <el-input v-model="formData.body" type="textarea" :rows="10" :disabled="disabled"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div class="alert-param" style="margin-left: 5px;">
              <div class="title"><svg-icon name="tip" />{{ t('notifyWay.bodyTitleTip') }}</div>
              <div class="content">
                <span v-html="t('notifyWay.bodyContentTip')"></span>
                <el-row style="margin-top: 10px;">
                  <el-col :span="12">{{ `notifyTitle:${t('notifyWay.bodyNotifyTitle')}` }}</el-col>
                  <el-col :span="12">{{ `notifyContent:${t('notifyWay.bodyNotifyContent')}` }}</el-col>
                </el-row>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-form-item :label="$t('notifyWay.resultCode')">
          <el-input :disabled="disabled" style="width: 100px;" v-model="resultCode.key"></el-input>
          =
          <el-input :disabled="disabled" style="width: 100px;" v-model="resultCode.val"></el-input>
        </el-form-item>
      </div>
      <div v-if="formData.notifyType === 'SNMP'">
        <el-form-item label="IP" prop="snmpIp" >
          <el-input v-model="formData.snmpIp" :placeholder="$t('notifyWay.snmpIpPlaceholder')"
            :disabled="disabled"></el-input>
        </el-form-item>
        <el-form-item :label="$t('notifyWay.snmpPort')" prop="snmpPort" >
          <el-input v-model="formData.snmpPort" :placeholder="$t('notifyWay.snmpIpPlaceholder')"
            :disabled="disabled"></el-input>
        </el-form-item>
        <el-form-item label="Community" prop="snmpCommunity" >
          <el-input v-model="formData.snmpCommunity" :placeholder="$t('notifyWay.snmpIpPlaceholder')"
            :disabled="disabled"></el-input>
        </el-form-item>
        <el-form-item label="Oid" prop="snmpOid">
          <el-input v-model="formData.snmpOid" :placeholder="$t('notifyWay.snmpIpPlaceholder')"
            :disabled="disabled"></el-input>
        </el-form-item>
        <el-form-item label="Version" prop="snmpVersion">
          <el-select v-model="formData.snmpVersion" :disabled="disabled">
            <el-option v-for="item in snmpVersionList" :key="item.value" :value="item.value" :label="item.label" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('notifyWay.snmpUsername')" prop="snmpUsername" v-if="formData.snmpVersion === 3">
          <el-input v-model="formData.snmpUsername" :placeholder="$t('notifyWay.snmpIpPlaceholder')"
            :disabled="disabled"></el-input>
        </el-form-item>
        <el-form-item :label="$t('notifyWay.snmpAuthPasswd')" prop="snmpAuthPasswd" v-if="formData.snmpVersion === 3">
          <el-input v-model="formData.snmpAuthPasswd" :placeholder="$t('notifyWay.snmpIpPlaceholder')"
            :disabled="disabled"></el-input>
        </el-form-item>
        <el-form-item :label="$t('notifyWay.snmpPrivPasswd')" prop="snmpPrivPasswd" v-if="formData.snmpVersion === 3">
          <el-input v-model="formData.snmpPrivPasswd" :placeholder="$t('notifyWay.snmpIpPlaceholder')"
            :disabled="disabled"></el-input>
        </el-form-item>
      </div>
      <el-form-item
        :label="formData.notifyType === 'email' ? $t('notifyWay.emailTemplate') : formData.notifyType === 'WeCom' ? $t('notifyWay.WeComTemplate') : formData.notifyType === 'DingTalk' ? $t('notifyWay.DingTalkTemplate') : $t('notifyWay.thirdPartyTemplate')"
        prop="notifyTemplateId" v-if="formData.notifyType">
        <el-select v-model="formData.notifyTemplateId" :disabled="disabled">
          <el-option v-for="item in templateList" :key="item.id" :value="item.id" :label="item.notifyTemplateName" />
        </el-select>
      </el-form-item>
    </el-form>
    <el-row style="margin-top: 10px;">
      <el-button type="primary" @click="testNotifyWay"
        v-if="state !== 'detail' && (formData.notifyType === 'webhook' || formData.notifyType === 'SNMP')">{{
          t('app.test') }}</el-button>
      <el-button type="primary" @click="confirm" v-if="state !== 'detail'">{{ t('app.confirm') }}</el-button>
      <el-button @click="cancel">{{ t('app.cancel') }}</el-button>
    </el-row>
  </div>
</template>

<script setup lang='ts'>
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import { cloneDeep } from 'lodash-es';
import { encryptPassword } from '@/utils/jsencrypt'
import type { FormInstance, FormRules } from 'element-plus'
import { Delete, Plus } from "@element-plus/icons-vue";

const validateEmailNotNull = (rule: any, value: any, callback: any) => {
  if (formData.value.notifyType === 'email' && !value) {
    callback(new Error(t('notifyWay.emailPlaceholder')))
  }
  callback()
}
const validateSendWay = (rule: any, value: any, callback: any) => {
  if ((formData.value.notifyType === 'WeCom' || formData.value.notifyType === 'DingTalk') && value !== 0 && value !== 1) {
    callback(new Error(t('notifyWay.sendWayPlaceholder')))
  }
  callback()
}
const validateWebhook = (rule: any, value: any, callback: any) => {
  if ((formData.value.notifyType === 'WeCom' || formData.value.notifyType === 'DingTalk') && formData.value.sendWay === 1 && !value) {
    callback(new Error(t('notifyWay.webhookPlaceholder')))
  }
  if (formData.value.notifyType === 'webhook' && !value) {
    callback(new Error(t('notifyWay.webhookPlaceholder')))
  }
  callback()
}
const { t } = useI18n();
const props = withDefaults(
  defineProps<{
    id: number,
    state: string,
  }>(),
  {
    state: 'detail',
    id: undefined
  }
)

const emit = defineEmits(["updateNotifyWay", "cancelNotifyWay"])

const disabled = ref<boolean>(true)
const initFormData = {
  id: '',
  name: '',
  notifyType: '',
  email: '',
  personId: '',
  deptId: '',
  notifyTemplateId: null
}
const formData = ref<any>(cloneDeep(initFormData));
const templateTypeList = ref<any[]>([{
  name: t('notifyTemplate.email'), value: 'email'
}, {
  name: t('notifyTemplate.WeCom'), value: 'WeCom'
}, {
  name: t('notifyTemplate.DingTalk'), value: 'DingTalk'
}, {
  name: 'webhook', value: 'webhook'
}, {
  name: 'SNMP', value: 'SNMP'
}])
const templateList = ref<any[]>([])
const headerList = ref<any[]>([{
  key: "",
  val: ""
}])
const paramList = ref<any[]>([{
  key: "",
  val: ""
}])
const resultCode = ref<any>({
  key: "",
  val: ""
})
const snmpVersionList = ref<any>([{
  label: 'v1', value: 0
}, {
  label: 'v2c', value: 1
}, {
  label: 'v3', value: 3
}])

const formRef = ref<FormInstance>()
const formRules = reactive<FormRules>({
  name: [
    { required: true, message: t('notifyWay.namePlaceholder'), trigger: 'blur' },
  ],
  notifyType: [
    { required: true, message: t('notifyWay.notifyTypePlaceholder'), trigger: 'blur' },
  ],
  email: [
    { validator: validateEmailNotNull, message: t('notifyWay.emailPlaceholder'), trigger: 'blur' },
  ],
  notifyTemplateId: [
    { required: true, message: t('notifyWay.templatePlaceholder'), trigger: 'blur' },
  ],
  sendWay: [
    { validator: validateSendWay, trigger: 'blur' }
  ],
  webhook: [
    { validator: validateWebhook, trigger: 'blur' }
  ],
  snmpIp: [
    { required: true, message: t('notifyWay.snmpIpPlaceholder'), trigger: 'blur' },
  ],
  snmpPort: [
    { required: true, message: t('notifyWay.snmpPortPlaceholder'), trigger: 'blur' },
  ],
  snmpCommunity: [
    { required: true, message: t('notifyWay.snmpCommunityPlaceholder'), trigger: 'blur' },
  ],
  snmpOid: [
    { required: true, message: t('notifyWay.snmpOidPlaceholder'), trigger: 'blur' },
  ],
  snmpVersion: [
    { required: true, message: t('notifyWay.snmpVersionPlaceholder'), trigger: 'blur' },
  ],
  snmpUsername: [
    { required: true, message: t('notifyWay.snmpUsernamePlaceholder'), trigger: 'blur' },
  ],
  snmpAuthPasswd: [
    { required: true, message: t('notifyWay.snmpAuthPasswdPlaceholder'), trigger: 'blur' },
  ],
  snmpPrivPasswd: [
    { required: true, message: t('notifyWay.snmpPrivPasswdPlaceholder'), trigger: 'blur' },
  ],
})

const { data: res, run: requestData } = useRequest(
  (id) => {
    return request.get(`/api/v1/notifyWay/${id}`)
  },
  { manual: true }
)
watch(res, (res: any) => {
  if (res && res.code === 200) {
    formData.value = res.data
    if (formData.value.header) {
      let header = JSON.parse(formData.value.header)
      headerList.value = []
      Object.keys(header).forEach(key => {
        headerList.value.push({ key, val: header[key] })
      })
    }
    if (formData.value.params) {
      let params = JSON.parse(formData.value.params)
      paramList.value = []
      Object.keys(params).forEach(key => {
        paramList.value.push({ key, val: params[key] })
      })
    }
    if (formData.value.resultCode) {
      let resultCodeJson = JSON.parse(formData.value.resultCode)
      resultCode.value = {}
      Object.keys(resultCodeJson).forEach(key => {
        resultCode.value.key = key
        resultCode.value.val = resultCodeJson[key]
      })
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

const { data: templateRes, run: requestTemplateData } = useRequest(
  (notifyTemplateType) => {
    return request.get(`/api/v1/notifyTemplate/list`)
  },
  { manual: true }
)
watch(templateRes, (templateRes: any) => {
  if (templateRes && templateRes.code === 200) {
    templateList.value = templateRes.data || []
  } else {
    const msg = t("app.queryFail");
    ElMessage({
      showClose: true,
      message: msg,
      type: "error",
    });
  }
});

const notifyTypeChange = () => {
  formData.value.notifyTemplateId = null
  if (!formData.value.notifyType) {
    templateList.value = []
    return
  }
  formData.value.email = ''
  formData.value.personId = ''
  formData.value.deptId = ''
}

const addHeader = () => {
  headerList.value.push({
    key: "",
    val: ""
  })
}
const removeHeader = (header: any) => {
  headerList.value.splice(header, 1)
}

const addParam = () => {
  paramList.value.push({
    key: "",
    val: ""
  })
}
const removeParam = (param: any) => {
  paramList.value.splice(param, 1)
}

const testNotifyWay = () => {
  if (!formRef) return
  formRef.value?.validate(async (valid, fields) => {
    if (valid) {
      if ((formData.value.notifyType === 'WeCom' || formData.value.notifyType === 'DingTalk') && formData.sendWay === 0 && !formData.value.personId && !formData.value.deptId) {
        const msg = t("notifyWay.existOneOfTargetTip");
        ElMessage({
          showClose: true,
          message: msg,
          type: "error",
        });
        return
      }
      if (headerList.value.length > 0) {
        let header = {}
        headerList.value.forEach((item: any) => {
          if (item.key && item.val) {
            header[item.key] = item.val
          }
        })
        formData.value.header = JSON.stringify(header)
      } else {
        formData.value.header = ''
      }
      if (paramList.value.length > 0) {
        let params = {}
        paramList.value.forEach((item: any) => {
          if (item.key && item.val) {
            params[item.key] = item.val
          }
        })
        formData.value.params = JSON.stringify(params)
      } else {
        formData.value.params = ''
      }
      formData.value.resultCode = ''
      if (resultCode.value && resultCode.value.key && resultCode.value.val) {
        let code = {}
        code[resultCode.value.key] = resultCode.value.val
        formData.value.resultCode = JSON.stringify(code)
      }
      let param = cloneDeep(formData.value)
      if (param.snmpAuthPasswd) {
         param.snmpAuthPasswd = await encryptPassword(param.snmpAuthPasswd)
      }
      if (param.snmpPrivPasswd) {
         param.snmpPrivPasswd = await encryptPassword(param.snmpPrivPasswd)
      }
      request.post(`/api/v1/notifyWay/test`, param).then((res: any) => {
        if (res && res.code === 200) {
          ElMessage({
            message: t('app.testSuccess'),
            type: 'success'
          })
        } else {
          ElMessage({
            message: t('app.testFail'),
            type: 'error'
          })
        }
      }).catch(() => {
        ElMessage({
          message: t('app.testFail'),
          type: 'error'
        })
      })
    }
  })
}
const confirm = () => {
  if (!formRef) return
  formRef.value?.validate(async (valid, fields) => {
    if (valid) {
      if ((formData.value.notifyType === 'WeCom' || formData.value.notifyType === 'DingTalk') && formData.sendWay === 0 && !formData.value.personId && !formData.value.deptId) {
        const msg = t("notifyWay.existOneOfTargetTip");
        ElMessage({
          showClose: true,
          message: msg,
          type: "error",
        });
        return
      }
      if (headerList.value.length > 0) {
        let header = {}
        headerList.value.forEach((item: any) => {
          if (item.key && item.val) {
            header[item.key] = item.val
          }
        })
        formData.value.header = JSON.stringify(header)
      } else {
        formData.value.header = ''
      }
      if (paramList.value.length > 0) {
        let params = {}
        paramList.value.forEach((item: any) => {
          if (item.key && item.val) {
            params[item.key] = item.val
          }
        })
        formData.value.params = JSON.stringify(params)
      } else {
        formData.value.params = ''
      }
      formData.value.resultCode = ''
      if (resultCode.value && resultCode.value.key && resultCode.value.val) {
        let code = {}
        code[resultCode.value.key] = resultCode.value.val
        formData.value.resultCode = JSON.stringify(code)
      }
      let param = cloneDeep(formData.value)
        if (param.snmpAuthPasswd) {
           param.snmpAuthPasswd = await encryptPassword(param.snmpAuthPasswd)
        }
        if (param.snmpPrivPasswd) {
           param.snmpPrivPasswd = await encryptPassword(param.snmpPrivPasswd)
        }
      request.post(`/api/v1/notifyWay`, param).then((res: any) => {
        if (res && res.code === 200) {
          ElMessage({
            message: t('app.saveSuccess'),
            type: 'success'
          })
          formData.value = {}
          formData.value = cloneDeep(initFormData)
          emit("updateNotifyWay")
        } else {
          ElMessage({
            message: t('app.saveFail'),
            type: 'error'
          })
        }
      }).catch(() => {
        ElMessage({
          message: t('app.saveFail'),
          type: 'error'
        })
      })
    }
  })
}
const cancel = () => {
  formData.value = {}
  formData.value = cloneDeep(initFormData)
  emit("cancelNotifyWay")
}
onMounted(() => {
  requestTemplateData()
  if (props.id) {
    requestData(props.id)
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
    font-weight: 400;
    font-size: 12px;
    line-height: 18px;
  }
}
</style>