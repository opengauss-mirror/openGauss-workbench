<template>
    <div class="dialog">
        <el-dialog
            :width="dialogWith"
            height="200px"
            :title="t('install.installAgent')"
            v-model="visible"
            :close-on-click-modal="false"
            draggable
            @close="closeDialog"
        >
            <div class="dialog-content" v-show="installData.length != 0" style="padding-bottom: 0px">
                <div>
                    <el-steps direction="vertical" :active="doingIndex">
                        <el-step v-for="item in installData" :key="item.name" :title="item.name">
                            <template #description>
                                <div v-for="msg in item.msg">
                                    <b>{{ msg }}</b>
                                </div>
                                <el-input v-if="item.error" v-model="item.error" :rows="5" type="textarea" readonly />
                            </template>
                        </el-step>
                    </el-steps>
                </div>
            </div>
            <div class="dialog-content" v-loading="started" v-show="installData.length === 0">
                <el-form :model="formData" :rules="connectionFormRules" ref="connectionFormRef" label-width="140px">
                    <el-form-item :label="t('install.collectInstance')" prop="nodeId">
                        <ClusterCascader
                            width="200"
                            instanceValueKey="nodeId"
                            @getCluster="handleClusterValue"
                            autoSelectFirst
                            notClearable
                            @loaded="getClusterList"
                        />
                    </el-form-item>
                    <el-form-item :label="'监控指标采集端口'" prop="exporterPort">
                        <el-input v-model="formData.exporterPort" style="width: 200px; margin: 0 4px" />
                    </el-form-item>
                    <el-form-item :label="'代理Web服务端口'" prop="httpPort">
                        <el-input v-model="formData.httpPort" style="width: 200px; margin: 0 4px" />
                    </el-form-item>
                    <el-form-item :label="t('install.installPath')" prop="path">
                        <el-input v-model="formData.path" style="width: 200px; margin: 0 4px" readonly />
                    </el-form-item>
                </el-form>
            </div>

            <template #footer>
                <el-button
                    v-if="installData.length === 0"
                    :loading="started"
                    style="padding: 5px 20px"
                    type="primary"
                    @click="install"
                    >{{ $t('install.install') }}</el-button
                >
                <el-button v-if="installData.length != 0" style="padding: 5px 20px" @click="back">{{
                    $t('app.back')
                }}</el-button>
                <el-button style="padding: 5px 20px" @click="handleCancelModel">{{ $t('app.cancel') }}</el-button>
            </template>
        </el-dialog>
        <el-dialog
            v-if="showUpload"
            v-model="showUpload"
            :close-on-click-modal="false"
            :title="t('install.pleaseUpload') + pgkName"
        >
            <div class="dialog-content" style="padding-bottom: 0px">
                <div class="suggest-info">
                    <span>{{ t('install.downloadSuggest') }}：</span
                    ><el-link :href="pgkType == 'node' ? formData.nodeurl : formData.gaussurl">{{
                        pgkType == 'node' ? formData.nodeurl : formData.gaussurl
                    }}</el-link>
                </div>

                <el-upload
                    v-model:file-list="fileList"
                    drag
                    :http-request="upload"
                    :limit="1"
                    :show-file-list="true"
                    :on-exceed="handleExceed"
                    :before-upload="uploadBefore"
                >
                    <el-icon class="el-icon--upload"><plus /></el-icon>
                    <div class="el-upload__text">{{ t('install.uploadInfo') }}</div>
                </el-upload>
                <div>
                    <el-progress v-if="showProgress" :percentage="progressPercent" :format="progressFormat" />
                    <el-button type="primary" :icon="RefreshRight" @click="retryUpload" v-if="retry">
                        {{ t('install.continueUpload') }}
                    </el-button>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { cloneDeep } from 'lodash-es'
import { FormRules, FormInstance, ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import WebSocketClass from '@/utils/websocket'
import moment from 'moment'
import { Plus, Warning, RefreshRight } from '@element-plus/icons-vue'
import type { UploadProps } from 'element-plus'
import restRequest from '@/request/restful'
import { el } from 'element-plus/es/locale'
const { t } = useI18n()

const visible = ref(false)
const props = withDefaults(
    defineProps<{
        show: boolean
    }>(),
    {}
)
watch(
    () => props.show,
    (newValue) => {
        visible.value = newValue
    },
    { immediate: true }
)

// form data
const initFormData = {
    nodeId: '',
    hostId: '',
    port: '9090',
    exporterPort: '9595',
    httpPort: '9596',
    installMode: 'online',
    path: '',
    nodepkg: '',
    nodesrc: '',
    nodeurl: '',
    gausspkg: '',
    gausssrc: '',
    gaussurl: '',
    fileList: [],
    uploadPath: '',
}
const formData = reactive(cloneDeep(initFormData))
const connectionFormRef = ref<FormInstance>()
const connectionFormRules = reactive<FormRules>({
    nodeId: [{ required: true, message: t('install.collectorRules[0]'), trigger: 'blur' }],
    exporterPort: [{ required: true, message: t('install.collectorRules[2]'), trigger: 'blur' }],
    httpPort: [{ required: true, message: t('install.collectorRules[3]'), trigger: 'blur' }],
})

// cluster component
const handleClusterValue = (val: any) => {
    formData.nodeId = val.length > 1 ? val[1] : ''
    if (formData.nodeId) {
        formData.path = `${basePath.value}/${formData.nodeId}/exports`
    }
}
const clusterList = ref<any[]>()
const getClusterList = (val: any) => {
    clusterList.value = val
}

const started = ref(false)
const installSucceed = ref(false)
const ws = reactive({
    name: '',
    webUser: '',
    connectionName: '',
    sessionId: '',
    instance: null,
})
const install = async () => {
    let result = await connectionFormRef.value?.validate()
    if (!result) return
    started.value = true
    // Determine whether the server is installed
    restRequest
        .get('/observability/v1/environment/prometheus', '')
        .then((res) => {
            if (!res || res.length === 0) {
                ElMessage({
                    showClose: true,
                    message: t('install.installServerAlert'),
                    type: 'warning',
                })
                started.value = false
            } else {
                ws.name = moment(new Date()).format('YYYYMMDDHHmmss') as string // websocket connection name
                ws.sessionId = moment(new Date()).format('YYYYMMDDHHmmss') as string // websocket connection id
                ws.instance = new WebSocketClass(ws.name, ws.sessionId, onWebSocketMessage)
                sendData()
            }
        })
        .catch(() => {
            started.value = false
        })
}
const sendData = async () => {
    const sendData = {
        key: 'exporter',
        nodeId: formData.nodeId,
        path: formData.path,
        exporterPort: formData.exporterPort,
        httpPort: formData.httpPort,
        language: localStorage.getItem('locale') === 'en-US' ? 'en_US' : 'zh_CN',
    }
    ws.instance.send(sendData)
}
const onWebSocketMessage = (data: Array<any>) => {
    // if (Array.isArray(installData.value)) installData.value = JSON.parse(data);
    if (data) {
        try {
            installData.value = JSON.parse(data)
        } catch (error) {
            installData.value.forEach((item) => {
                if (item.state === 'ERROR') {
                    item['error'] = data
                    dialogWith.value = '800px'
                }
            })
        }
    }
}

// action
const back = () => {
    started.value = false
    dialogWith.value = '500px'
    ws.instance.close()
    installData.value = []
}

// list Data
const installData = ref<Array<any>>([])
const dialogWith = ref<string>('500px')
const doingIndex = computed(() => {
    for (let index = 0; index < installData.value.length; index++) {
        const element = installData.value[index]
        if (element.state === 'DOING' || element.state === 'ERROR') return index
    }
    if (installData.value.length > 0 && !installSucceed.value) installSucceed.value = true
    return installData.value.length
})

// dialog
const emit = defineEmits(['changeModal', 'installed'])
const handleCancelModel = () => {
    visible.value = false
    if (installSucceed.value) emit('installed')
    emit('changeModal', visible.value)
}
const closeDialog = () => {
    visible.value = false
    if (installSucceed.value) emit('installed')
    emit('changeModal', visible.value)
}

const basePath = ref<string>('')
const getInstallPath = () => {
    restRequest.get(`/observability/v1/environment/basePath`).then((res) => {
        if (res) {
            basePath.value = res + (res.endsWith('/') ? 'data' : '/data')
        }
    })
}

onMounted(() => {
    getInstallPath()
})
onBeforeUnmount(() => {
    if (ws.instance) ws.instance.close()
})
</script>
<style lang="scss" scoped>
@import '@/assets/style/style1.scss';
.upload-icon {
    margin-left: 5px;
    font-size: 13px;
}

.suggest-info {
    margin-bottom: 10px;
}
</style>
