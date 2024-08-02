<template>
  <div>
    <div class="page-header">
      <div class="icon"></div>
      <div class="title" v-if="state === 'add'">{{$t(`alertShielding.addTitle`)}}</div>
      <div class="title" v-if="state === 'edit'">{{$t(`alertShielding.editTitle`)}}</div>
      <div class="title" v-if="state === 'detail'">{{$t(`alertShielding.detailTitle`)}}</div>
      <div class="seperator"></div>
      <el-breadcrumb separator="/" style="flex-grow: 1">
        <el-breadcrumb-item>
          <div @click="cancel">
            <a>{{$t(`alertShielding.title`)}}</a>
          </div>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <div v-if="state === 'add'">{{$t(`alertShielding.addTitle`)}}</div>
          <div v-if="state === 'edit'">{{$t(`alertShielding.editTitle`)}}</div>
          <div v-if="state === 'detail'">{{$t(`alertShielding.detailTitle`)}}</div>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-form
      style="margin-top: 10px"
      :model="formData"
      ref="formRef"
      :rules="formRules"
      label-position="left"
      label-width="120px"
      size="default"
    >
      <el-form-item :label="$t(`alertShielding.ruleName`)" prop="ruleName">
        <el-input
          v-model="formData.ruleName"
          :placeholder="$t(`alertShielding.inputRuleNameTip`)"
          :disabled="disabled"
          maxlength="50"
          show-word-limit
        ></el-input>
      </el-form-item>
      <el-form-item :label="$t(`alertShielding.ruleDetail`)">
        <el-input
          v-model="formData.ruleDetail"
          :placeholder="$t(`alertShielding.inputRuleDetailTip`)"
          maxlength="300"
          show-word-limit
          :rows="2"
          type="textarea"
        ></el-input>
      </el-form-item>
      <el-form-item :label="$t(`alertShielding.shieldInstance`)" prop="clusterNodeIdList">
        <el-cascader
          style="margin-right: 10px"
          v-model="formData.clusterNodeIdList"
          max-collapse-tags="1"
          :props="cascaderProps"
          :collapse-tags="true"
          :collapse-tags-tooltip="true"
          :options="clusterList"
          @change="changeClusterNode"
          clearable
        />
      </el-form-item>
      <el-form-item :label="$t(`alertShielding.effectTimeRange`)" prop="type">
        <el-radio-group v-model="formData.type" :disabled="disabled" class="vertical-radio">
          <div>
            <el-radio key="a" label="a" selected>{{$t(`alertShielding.permanent`)}}</el-radio>
            <el-radio key="b" label="b">
              <span style="margin-right: 10px">{{$t(`alertShielding.timeRangeSchedule`)}}</span>
              <el-date-picker
                v-model="formData.dateValue"
                type="datetimerange"
                range-separator="~"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :start-placeholder="t(`app.startDate`)"
                :end-placeholder="t(`app.endDate`)"
                style="width: 350px"
                v-if="formData.type == 'b'"
              />
            </el-radio>

            <el-radio key="c" label="c" style="margin-bottom: 0">
              <span style="margin-right: 10px">{{$t(`alertShielding.schedule`)}}</span>
              <el-date-picker
                v-model="formData.dateValue"
                type="daterange"
                range-separator="~"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                :start-placeholder="t(`app.startDate`)"
                :end-placeholder="t(`app.endDate`)"
                style="width: 200px"
                v-if="formData.type == 'c'"
              />
              <span style="margin-right: 6px; color: #303133; margin-left: 6px" v-if="formData.type == 'c'">{{$t(`alertShielding.everyday`)}}</span>
              <el-time-picker
                v-model="formData.timeValue"
                is-range
                range-separator="~"
                format="HH:mm:ss"
                value-format="HH:mm:ss"
                :start-placeholder="t(`app.startTime`)"
                :end-placeholder="t(`app.endTime`)"
                style="width: 200px"
                v-if="formData.type == 'c'"
              />
            </el-radio>
          </div>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="$t(`alertShielding.isEnable`)" prop="isEnable">
        <el-switch v-model="formData.isEnable" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <el-row style="margin-top: 10px">
      <el-button v-if="state === 'add' || state === 'edit'" type="primary" @click="confirm" :loading="loading">{{
        t('app.confirm')
      }}</el-button>
      <el-button @click="cancel" :loading="loading">{{ t('app.cancel') }}</el-button>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import 'element-plus/es/components/message-box/style/index'
import { nextTick } from 'vue'
import { Delete, Plus } from '@element-plus/icons-vue'
import { useRequest } from 'vue-request'
import request from '@/request'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { parseContent } from '@/utils/commonUtil'
import type { FormInstance, FormRules } from 'element-plus'
import { cloneDeep } from 'lodash'
import { i18n } from '@/i18n'
const { t } = useI18n()


const formRef = ref<FormInstance>()

const cascaderProps = { multiple: true }

const clusterList = ref<any[]>([])

const checkType = (rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error(t('alertShielding.effectTimeRangeTip')))
  }
  if (value === 'b' && (!formData.value.dateValue || formData.value.dateValue.length !== 2)) {
    console.log("bbbbbbbbb")
    callback(new Error(t('alertShielding.effectTimeRangeTip')))
  }
  if (value === 'c' && (!formData.value.dateValue || formData.value.dateValue.length !== 2) && (!formData.value.timeValue || formData.value.timeValue.length !== 2)) {
    callback(new Error(t('alertShielding.effectTimeRangeTip')))
  }
  callback()
}

const checkClusterNodeIdList = (rule: any, value: any, callback: any) => {
  if (!value || value.length === 0) {
    callback(new Error(t('alertShielding.shieldInstanceTip')))
  }
  callback()
}

const formRules = reactive<FormRules>({
  ruleName: [
    { required: true, message: t('alertShielding.inputRuleNameTip'), trigger: 'blur' },
  ],
  type: [
    { required: true, validator: checkType, trigger: 'change' },
  ],
  clusterNodeIdList: [
    { required: true, validator: checkClusterNodeIdList, trigger: 'change' },
  ],
  isEnable: [
    { required: true, message: t('alertShielding.isEnableTip'), trigger: 'blur' },
  ],
})

const { data: opsClusterData } = useRequest(() => request.get("/api/v1/environment/cluster"), { manual: false });
const treeTransform = (arr: any) => {
  let obj: any = [];
  if (arr instanceof Array) {
    arr.forEach((item) => {
      obj.push({
        label: item.clusterId ? item.clusterId : (item.azName ? item.azName + "_" : "") + item.publicIp + ":" + item.dbPort + (item.clusterRole ? "(" + item.clusterRole + ")" : ""),
        value: item.clusterId ? item.clusterId : item.nodeId,
        children: treeTransform(item.clusterNodes),
      });
    });
  }
  return obj;
};
watch(opsClusterData, (res: any) => {
  if (res && res.code === 200) {
    clusterList.value = treeTransform(res.data);
  }
});

const getClusterValue = (val: string[]) => {
  if (val == null) emit('getCluster', [])
  else emit('getCluster', val)
  console.log('val', val)
}

const props = withDefaults(
  defineProps<{
    id: number
    state: string
    multiple?: boolean
  }>(),
  {
    state: 'add',
    id: undefined,
    multiple: false,
  }
)

const emit = defineEmits(['updateShielding', 'cancelShielding'])

const loading = ref<boolean>(false)
const disabled = ref<boolean>(true)
const formData = ref<any>({
  ruleName: '',
  ruleDetail: '',
  clusterNodeIds: '',
  clusterNodeIdList: [],
  type: 'a',
  startDate: '',
  endDate: '',
  startTime: '',
  endTime: '',
  isEnable: 1,
  dateValue: [],
  timeValue: []
})

const { data: shieldingDetail, run: requestData } = useRequest(
  (id) => {
    return request.get(`/api/v1/alertShielding/${id}`)
  },
  { manual: true }
)
watch(shieldingDetail, (shieldingDetail: any) => {
  if (shieldingDetail && shieldingDetail.code === 200) {
    formData.value = shieldingDetail.data
    let dateTmp = []
    dateTmp.push(shieldingDetail.data.startDate)
    dateTmp.push(shieldingDetail.data.endDate)
    formData.value.dateValue = dateTmp
    let timeTmp = []
    timeTmp.push(shieldingDetail.data.startTime)
    timeTmp.push(shieldingDetail.data.endTime)
    formData.value.timeValue = timeTmp
    formData.value.clusterNodeIdList = formData.value.clusterNodeIds.split(',')
  } else {
    const msg = t('app.queryFail')
    ElMessage({
      showClose: true,
      message: msg,
      type: 'error',
    })
  }
})

const confirm = async () => {
  if (!formRef) return
  formRef.value?.validate(async (valid, fields) => {
    if (valid) {
      save()
    }
  })
}
const cancel = () => {
  emit('cancelShielding')
}


const save = () => {
  loading.value = true
  const { dateValue ,timeValue } = formData.value
  const resultData = {
      id: props.id,
      ruleName: formData.value.ruleName,
      ruleDetail: formData.value.ruleDetail,
      type: formData.value.type,
      clusterNodeIds: formData.value.clusterNodeIdList.length ? formData.value.clusterNodeIdList.join(',') : '',
      startDate: dateValue.length ? dateValue[0] : null,
      endDate: dateValue.length ? dateValue[1] : null,
      startTime: timeValue.length ? timeValue[0] : null,
      endTime: timeValue.length ? timeValue[1] : null,
      isEnable: formData.value.isEnable
  }
  request
    .post(`/api/v1/alertShielding`, resultData)
    .then((res: any) => {
      loading.value = false
      if (res && res.code === 200) {
        ElMessage({
          showClose: true,
          message: t('app.saveSuccess'),
          type: 'success',
        })
        emit('updateShielding')
      } else {
        ElMessage({
          showClose: true,
          message: t('app.saveFail'),
          type: 'error',
        })
      }
    })
    .catch(() => {
      ElMessage({
        showClose: true,
        message: t('app.saveFail'),
        type: 'error',
      })
      loading.value = false
    })
}

onMounted(() => {
  if (props.state !== 'detail') {
    disabled.value = false
  } else {
    disabled.value = true
  }
  if (props.id) {
    requestData(props.id)
  }
})
</script>
<style scoped lang="scss">
.alert-param {
  z-index: 9999;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  position: absolute;
  align-items: flex-start;
  padding: 15px 16px;
  gap: 6px;
  border: 1px solid #94bfff;
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
  content: '*';
  color: var(--el-color-danger);
  // margin-right: 4px;
}
.vertical-radio .el-radio {
  display: block;
  margin-bottom: 10px; /* 调整单选框之间的间距 */
}
</style>
