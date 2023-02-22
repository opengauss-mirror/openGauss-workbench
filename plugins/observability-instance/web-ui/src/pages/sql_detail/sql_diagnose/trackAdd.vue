<template>
    <div class="task-dialog">
        <el-dialog width="800px" :title="$t('datasource.addTaTitle')" v-model="visible" :close-on-click-modal="false" draggable @close="taskClose">
            <div class="dialog-content">
                <el-form :model="formData" :rules="connectionFormRules" ref="connectionFormRef">
                    <el-form-item :label="$t('datasource.taskName')" prop="name">
                        <el-input class="form-input" v-model="formData.name" :placeholder="$t('datasource.selectTaskName')" type="text" />
                    </el-form-item>
                    <el-form-item label="SQL" prop="sql">
                        <el-input v-if="props.sqlText" class="form-textarea" v-model="formData.sql" disabled="true" type="textarea" />
                        <el-input v-if="!props.sqlText" class="form-textarea" v-model="formData.sql" :placeholder="$t('datasource.selectSql')" type="textarea" />
                    </el-form-item>
                    <el-form-item :label="$t('datasource.option')">
                        <div class="option-wrap">
                            <el-checkbox-group v-model="formData.onCpu">
                                <el-checkbox :label="$t('datasource.ebpfOnLable')" name="onCpu" />
                            </el-checkbox-group>
                        </div>
                        <div class="option-wrap">
                            <el-checkbox-group v-model="formData.offCpu">
                                <el-checkbox :label="$t('datasource.ebpfOffLable')" name="offCpu" />
                            </el-checkbox-group>
                        </div>
                        <div class="option-wrap">
                            <el-checkbox-group v-model="formData.analyze">
                                <el-checkbox label="explain analyze" name="analyze" />
                            </el-checkbox-group>
                        </div>
                    </el-form-item>
                </el-form>
            </div>

            <template #footer>
                <el-button style="padding: 5px 20px" type="primary" @click="handleconfirmModel">{{ $t('datasource.createTask') }}</el-button>
                <el-button style="padding: 5px 20px" @click="handleCancelModel">{{ $t('app.cancel') }}</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { cloneDeep } from 'lodash-es'
import diagnosisRequest from '../../../request/diagnosis'
import { useRequest } from 'vue-request'
import { FormRules, FormInstance, ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

type Rez =
    | {
          data: string
      }
    | undefined

const visible = ref(false)
const dbList = ref<Array<any>>([])
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
const emit = defineEmits(['changeModal', 'conveyFlag'])
const initFormData = {
    name: '',
    sql: props.sqlText,
    onCpu: [],
    offCpu: [],
    analyze: [],
    cluster: props.clusterId,
    dbName: props.dbName,
}
const formData = reactive(cloneDeep(initFormData))
const queryData = computed(() => {
    const { name, sql, onCpu, offCpu, analyze, cluster, dbName } = formData
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
    const queryObj = {
        dbName: props.dbName ? props.dbName : dbName,
        clusterId,
        nodeId: instanceId,
        name,
        sql: props.sqlText ? props.sqlText : sql,
        onCpu: onCpu.length > 0,
        offCpu: offCpu.length > 0,
        explainAnalysis: analyze.length > 0,
        sqlId: props.type === 2 ? props.sqlId : '',
    }
    return queryObj
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
            visible.value = false
            emit('changeModal', visible.value)
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

const { data: rez, run: addTasks } = useRequest(
    () => {
        const msg = t('datasource.diagnosisAddTaskSuccess')
        return diagnosisRequest
            .post('/sqlDiagnosis/api/v1/diagnosisTasks', queryData.value, {
                headers: {
                    'content-type': 'multipart/form-data',
                },
            })
            .then(function (res) {
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
watch(rez, (rez: Rez) => {
    if (rez && Object.keys(rez).length) {
        dbList.value = []
        Object.assign(formData, cloneDeep(initFormData))
        emit('conveyFlag', 200)
    }
})
</script>
<style lang="scss" scoped>
.el-button {
    color: var(--color-text-2) !important;
    background-color: var(--color-secondary) !important;
    border: none !important;
}
.el-button.el-button--primary {
    color: #fff !important;
    background-color: var(--primary-6) !important;
}
.el-button.el-button--primary.search-button {
    color: var(--primary-6) !important;
    background-color: rgb(255, 255, 255, 0) !important;
    border: 1px solid var(--primary-6) !important;
}
.task-dialog {
    &:deep(.el-dialog .el-dialog__header) {
        text-align: center;
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
