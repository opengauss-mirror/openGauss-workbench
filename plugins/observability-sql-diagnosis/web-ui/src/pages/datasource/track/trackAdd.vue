<template>
  <div class="task-dialog">
    <el-dialog
      width="800px"
      :title="$t('datasource.addTaTitle')"
      v-model="visible"
      :close-on-click-modal="false"
      draggable
      @close="taskClose"
    >
      <div class="dialog-content">
        <el-form :model="formData" :rules="connectionFormRules" ref="connectionFormRef" label-width="120px">
          <el-form-item :label="$t('datasource.cluterTitle')" prop="cluster" v-if="props.type === 1">
            <el-cascader v-model="formData.cluster" :options="props.clusterList" @change="getClusterValue" />
          </el-form-item>
          <el-form-item :label="$t('datasource.database')" prop="dbName" v-if="props.type === 1">
            <el-select v-model="formData.dbName" :placeholder="$t('datasource.selectDatabaseType')">
              <el-option v-for="item in dbList" :key="item" :value="item" :label="item" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('datasource.taskName')" prop="name">
            <el-input
              class="form-input"
              v-model="formData.name"
              :placeholder="$t('datasource.selectTaskName')"
              type="text"
            />
          </el-form-item>
          <el-form-item label="SQL" prop="sql">
            <el-input
              v-if="props.sqlText"
              class="form-textarea"
              v-model="formData.sql"
              :disabled="true"
              type="textarea"
            />
            <el-input
              v-if="!props.sqlText"
              class="form-textarea"
              v-model="formData.sql"
              :placeholder="$t('datasource.selectSql')"
              type="textarea"
            />
          </el-form-item>
          <el-form-item :label="$t('datasource.option')">
            <el-checkbox-group v-model="ruleForm.optionSelected">
              <el-checkbox v-for="item in options" :key="item.option" :label="item.option">
                {{ item.name }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button style="padding: 5px 20px" :loading="addTasking" type="primary" @click="handleconfirmModel">{{
          $t('datasource.createTask')
        }}</el-button>
        <el-button style="padding: 5px 20px" @click="handleCancelModel">{{ $t('app.cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { cloneDeep } from 'lodash-es'
import ogRequest from '@/request'
import { useRequest } from 'vue-request'
import { FormRules, FormInstance, ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { parseSql } from '@/utils/commonUtils'
import { getOptions, DiagnosisParamConfig } from '@/api/historyDiagnosis'
const { t } = useI18n()

type Rez =
  | {
      data: string
    }
  | undefined

const visible = ref(false)
const dbList = ref<Array<any>>([])
const options = ref<Array<any>>([])
const ruleForm = reactive<{
  optionSelected: string[]
}>({
  optionSelected: [],
})
const props = withDefaults(
  defineProps<{
    show: boolean
    dbName?: string | string[]
    clusterId?: string[]
    sqlText?: string
    clusterList?: Array<any>
    dbList?: Array<any>
    sqlId?: any
    type?: number // 1 task List；  2  topsql 3 slow sql
  }>(),
  {}
)
onMounted(() => {
  if (props.clusterId && props.clusterId.length > 0) getClusterValue(props.clusterId)
  initOptions()
})
const emit = defineEmits(['changeModal', 'conveyFlag'])
const initFormData = {
  name: '',
  sql: props.sqlText ? parseSql(props.sqlText) : props.sqlText,
  onCpu: [],
  offCpu: [],
  analyze: [],
  paramAnalysis: [],
  cluster: props.clusterId,
  dbName: props.dbName,
}
const formData = reactive(cloneDeep(initFormData))
const queryData = computed(() => {
  const { name, sql, onCpu, offCpu, analyze, paramAnalysis, cluster, dbName } = formData
  let instanceId, clusterId: any
  if (props.type === 2) {
    instanceId = props.clusterId
    clusterId = cluster && cluster.length > 0 ? cluster[0] : ''
  } else if (props.type === 1) {
    instanceId = cluster && cluster.length > 0 ? cluster[1] : ''
    clusterId = cluster && cluster.length > 0 ? cluster[0] : ''
  } else if (props.type === 3) {
    instanceId = props.clusterId && props.clusterId.length > 0 ? props.clusterId[1] : ''
    clusterId = props.clusterId && props.clusterId.length > 0 ? props.clusterId[0] : ''
  }
  // set configs
  let configs: DiagnosisParamConfig[] = []
  ruleForm.optionSelected.forEach((element) => {
    configs.push({
      option: element,
      isCheck: true,
    })
  })

  const queryObj = {
    dbName: props.dbName ? props.dbName : dbName,
    clusterId,
    nodeId: instanceId,
    taskName: name,
    sql: props.sqlText ? parseSql(props.sqlText) : sql,
    sqlId: props.type === 2 ? props.sqlId : '',
    diagnosisType: 'sql',
    configs,
  }

  return queryObj
})

const { data: optionData, run: initOptions } = useRequest(
  () => {
    return getOptions('sql')
  },
  { manual: true }
)
watch(optionData, (ret: any) => {
  options.value = []
  options.value = ret
})

const taskClose = () => {
  visible.value = false
  emit('changeModal', visible.value)
}
const handleCancelModel = () => {
  visible.value = false
  emit('changeModal', visible.value)
}
const getClusterValue = (val: string[]) => {
  dbData(val[1])
}
const connectionFormRef = ref<FormInstance>()
async function handleconfirmModel() {
  try {
    let result = await connectionFormRef.value?.validate()
    if (result) {
      addTasks()
    }
  } catch (error) {
    console.log(error)
  }
}
const connectionFormRules = reactive<FormRules>({
  name: [{ required: true, message: t('datasource.trackFormRules[0]'), trigger: 'blur' }],
  dbName: [{ required: true, message: t('datasource.trackFormRules[1]'), trigger: 'blur' }],
  sql: [{ required: true, message: t('datasource.trackFormRules[2]'), trigger: 'blur' }],
  cluster: [{ required: true, message: t('datasource.trackFormRules[3]'), trigger: 'blur' }],
})
watch(
  () => props.show,
  (newValue) => {
    visible.value = newValue
  },
  { immediate: true }
)
watch(
  () => props.clusterId,
  (newValue) => {
    formData.cluster = newValue
  },
  { immediate: true }
)
watch(
  () => props.dbName,
  (newValue) => {
    formData.dbName = newValue
  },
  { immediate: true }
)

const { data: ret, run: dbData } = useRequest(
  (nodeId: string) => {
    return ogRequest.get('/sqlDiagnosis/api/v1/clusters/' + nodeId + '/instances', '')
  },
  { manual: true }
)
const {
  data: rez,
  run: addTasks,
  loading: addTasking,
} = useRequest(
  () => {
    const msg = t('datasource.diagnosisAddTaskSuccess')
    return ogRequest.post('/historyDiagnosis/api/v2/tasks', queryData.value).then(function (res) {
      ElMessage({
        showClose: true,
        message: msg,
        type: 'success',
      })
      return res
    })
  },
  { manual: true }
)

// 数据库类型
watch(ret, (ret: any[]) => {
  console.log('watch(ret')
  if (ret && Object.keys(ret).length) {
    dbList.value = ret
  } else {
    dbList.value = []
  }
})
watch(rez, (rez: Rez) => {
  if (rez && Object.keys(rez).length) {
    dbList.value = []
    Object.assign(formData, cloneDeep(initFormData))
    emit('conveyFlag', 200)
    visible.value = false
  }
})
</script>
<style lang="scss" scoped>
.task-dialog {
  &:deep(.el-dialog .el-dialog__header) {
    text-align: center;
  }

  &:deep(.el-dialog .el-dialog__title) {
    color: #fff;
  }

  &:deep(.el-form-item--small .el-form-item__label) {
    width: 110px;
  }

  &:deep(.el-dialog .el-dialog__footer) {
    border: none !important;
  }

  .form-textarea {
    width: 100%;

    &:deep(.el-textarea__inner) {
      height: 100px;
    }
  }

  .dialog-content {
    padding-bottom: 180px;
  }

  .option-wrap {
    margin-right: 20px;
  }
}
</style>
