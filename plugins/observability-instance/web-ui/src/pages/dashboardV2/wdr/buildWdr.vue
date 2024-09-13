<template>
  <div class="dialog">
    <el-dialog width="400px" :title="$t('dashboard.wdrReports.buildWDR')" v-model="visible" :close-on-click-modal="false"
      draggable @close="taskClose">
      <div class="dialog-content" v-loading="generating">
        <el-form :model="formData" :rules="connectionFormRules" ref="connectionFormRef">
          <el-form-item :label="$t('datasource.cluterTitle')" prop="hostId">
            <ClusterCascader ref="clusterComponent" width="200" instanceValueKey="hostId" @loaded="loaded"
              @getCluster="handleClusterValue" notClearable />
          </el-form-item>
          <el-form-item :label="$t('dashboard.wdrReports.reportRange')" prop="reportRange">
            <el-select v-model="formData.scope" style="width: 200px; margin: 0 4px">
              <el-option value="CLUSTER" :label="$t('dashboard.wdrReports.reportRangeSelect[0]')" />
              <el-option value="NODE" :label="$t('dashboard.wdrReports.reportRangeSelect[1]')" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('dashboard.wdrReports.reportType')" prop="reportType">
            <el-select v-model="formData.type" style="width: 200px; margin: 0 4px">
              <el-option value="DETAIL" :label="$t('dashboard.wdrReports.reportTypeSelect[0]')" />
              <el-option value="SUMMARY" :label="$t('dashboard.wdrReports.reportTypeSelect[1]')" />
              <el-option value="ALL" :label="$t('dashboard.wdrReports.reportTypeSelect[2]')" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('dashboard.wdrReports.buildWDRDialog.startSnapshot')" prop="startId">
            <el-select v-model="formData.startId" style="width: 200px; margin: 0 4px">
              <el-option v-for="item in tableData" :key="item.snapshotId" :label="`${item.snapshotId}(${$t('dashboard.wdrReports.buildWDRDialog.startTime')}:${moment(item.startTs).format('YYYY-MM-DD HH:mm:ss')})`"
                :value="item.snapshotId" :disabled="parseInt(item.snapshotId) >= parseInt(formData.endId)" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('dashboard.wdrReports.buildWDRDialog.endSnapshot')" prop="endId">
            <el-select v-model="formData.endId" style="width: 200px; margin: 0 4px">
              <el-option v-for="item in tableData" :key="item.snapshotId" :label="`${item.snapshotId}(${$t('dashboard.wdrReports.buildWDRDialog.endTime')}:${moment(item.endTs).format('YYYY-MM-DD HH:mm:ss')})`"
                :value="item.snapshotId" :disabled="parseInt(item.snapshotId) <= parseInt(formData.startId)" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button style="padding: 5px 20px" :loading="generating" type="primary" @click="handleconfirmModel">{{
          $t('dashboard.wdrReports.buildWDRDialog.build')
        }}</el-button>
        <el-button style="padding: 5px 20px" @click="handleCancelModel">{{ $t('app.cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { cloneDeep } from 'lodash-es'
import { useRequest } from 'vue-request'
import { FormRules, FormInstance, ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import restRequest from '@/request/restful'
import { useMonitorStore } from '@/store/monitor'
import moment from 'moment'
const { t } = useI18n()

const visible = ref(true)
const props = withDefaults(defineProps<{
  tabId: string,
  startId: number,
  endId: number
}>(), {})

const clusterComponent = ref<any>(null)
const loaded = () => {
  let instanceId = useMonitorStore(props.tabId).instanceId
  if (instanceId && clusterComponent.value != null) {
    clusterComponent.value.setNodeId(instanceId)
    nextTick(() => {
      requestData()
    })
  }
}

// form data
const initFormData = {
  clusterId: '',
  endId: props.endId ? props.endId : '',
  hostId: '',
  scope: 'CLUSTER',
  startId: props.startId ? props.startId : '',
  type: 'DETAIL',
}
const formData = reactive(cloneDeep(initFormData))

// cluster component
const handleClusterValue = (val: any) => {
  formData.clusterId = val.length ? val[0] : ''
  formData.hostId = val.length > 1 ? val[1] : ''
  if (formData.hostId) requestData()
}

// snapshotList
const tableData = ref<Array<any>>([])
const { data: res, run: requestData } = useRequest(
  () => {
    return restRequest
      .get('/wdr/listSnapshot', {
        clusterId: formData.clusterId,
        hostId: formData.hostId,
        orderby: 'snapshot_id desc',
        pageSize: 99999,
        pageNum: 1,
      })
      .then(function (res) {
        return res
      })
      .catch(function (res) {
        tableData.value = []
      })
  },
  { manual: true }
)
watch(res, (res) => {
  if (res && res.records && res.records.length) {
    tableData.value = res.records
    if (tableData.value.length > 0) {
      formData.startId = props.startId ? props.startId : tableData.value[tableData.value.length - 1].snapshotId
      formData.endId = props.endId ? props.endId : tableData.value[0].snapshotId
    }
  } else {
    tableData.value = []
  }
})

// build
const connectionFormRef = ref<FormInstance>()
async function handleconfirmModel() {
  try {
    let result = await connectionFormRef.value?.validate()
    if (result) {
      buildWDR()
    }
  } catch (error) { }
}
const validateStartId = (rule: any, value: any, callback: any) => {
  if (!value || !formData.endId) {
    callback()
  } else {
    if (parseInt(value) >= parseInt(formData.endId)) {
      callback(new Error(t('datasource.trackFormRules[5]')))
      return
    }
    callback()
  }
}
const validateEndId = (rule: any, value: any, callback: any) => {
  if (!value || !formData.startId) {
    callback()
  } else {
    if (parseInt(value) <= parseInt(formData.startId)) {
      callback(new Error(t('datasource.trackFormRules[6]')))
    }
    callback()
  }
}
const connectionFormRules = reactive<FormRules>({
  hostId: [{ required: true, message: t('datasource.trackFormRules[0]'), trigger: 'blur' }],
  startId: [
    { required: true, message: t('datasource.trackFormRules[4]'), trigger: 'blur' },
    { validator: validateStartId, trigger: 'blur' },
  ],
  endId: [
    { required: true, message: t('datasource.trackFormRules[4]'), trigger: 'blur' },
    { validator: validateEndId, trigger: 'blur' },
  ],
})
const {
  data: rez,
  run: buildWDR,
  loading: generating,
} = useRequest(
  () => {
    return restRequest.post('/wdr/generate', formData,  {timeout: 300000}).then(function (res) {
      return res
    })
  },
  {
    manual: true,
    onSuccess: (res) => {
      if (res && res.code === 200) {
        const msg = t('dashboard.wdrReports.buildWDRDialog.buildSuccess')
        ElMessage({
          showClose: true,
          message: msg,
          type: 'success',
        })
      } else {
        const msg = t('dashboard.wdrReports.buildWDRDialog.buildFail')
        ElMessage({
          showClose: true,
          message: msg,
          type: 'error',
        })
      }
    },
  }
)
watch(rez, (rez) => {
  emit('conveyFlag')
  visible.value = false
  emit('changeModal', visible.value)
})

const emit = defineEmits(['changeModal', 'conveyFlag'])
const taskClose = () => {
  visible.value = false
  emit('changeModal', visible.value)
}
const handleCancelModel = () => {
  visible.value = false
  emit('changeModal', visible.value)
}
</script>
<style lang="scss" scoped>
@import '@/assets/style/style1.scss';
</style>
