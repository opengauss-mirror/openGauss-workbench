<template>
    <div class="dialog">
        <el-dialog :width="dialogWith" :title="t('install.installAgent')" v-model="visible" :close-on-click-modal="false" draggable @close="closeDialog">
            <div class="dialog-content" v-show="installData.length != 0">
                <div>
                    <el-steps direction="vertical" :active="doingIndex">
                        <el-step v-for="item in installData" :key="item.name" :title="item.name" >
                            <template #description >
                                <div v-for="msg in item.msg"><b>{{ msg }}</b></div>
                                <el-input v-if="item.error"
                                    v-model="item.error"
                                    :rows="5"
                                    type="textarea"
                                    readonly
                                />
                            </template>
                        </el-step>
                    </el-steps>
                </div>
            </div>
            <div class="dialog-content" v-loading="started" v-show="installData.length === 0">
                <el-form :model="formData" :rules="connectionFormRules" ref="connectionFormRef">
                    <el-form-item :label="t('install.collectInstance')" prop="nodeId">
                        <ClusterCascader width="300" instanceValueKey="nodeId" @getCluster="handleClusterValue" autoSelectFirst notClearable />
                    </el-form-item>
                    <el-form-item :label="t('install.rootPWD')" prop="rootPassword">
                        <el-input v-model="formData.rootPassword" show-password style="width: 300px; margin: 0 4px" />
                    </el-form-item>
                    <el-form-item :label="t('install.proxyPort')" prop="port">
                        <el-input v-model="formData.port" style="width: 300px; margin: 0 4px" />
                    </el-form-item>
                    <el-form-item :label="t('install.callbackPath')" prop="callbackPath">
                        <el-input v-model="formData.callbackPath" style="width: 300px; margin: 0 4px" />
                    </el-form-item>
                    <el-form-item :label="t('install.installPath')" prop="path">
                        <el-input v-model="formData.path" style="width: 300px; margin: 0 4px" />
                    </el-form-item>
                </el-form>
            </div>

            <template #footer>
                <el-button v-if="installData.length === 0" :loading="started" style="padding: 5px 20px" type="primary" @click="install">{{ $t('install.install') }}</el-button>
                <el-button v-if="installData.length != 0" style="padding: 5px 20px" @click="back">{{ $t('app.back') }}</el-button>
                <el-button style="padding: 5px 20px" @click="handleCancelModel">{{ $t('app.cancel') }}</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { cloneDeep } from 'lodash-es'
import { FormRules, FormInstance, ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import WebSocketClass from '../../../utils/websocket'
import { encryptPassword } from '../../../utils/jsencrypt'
import ogRequest from '../../../request'
import moment from 'moment'
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
    nodeId: "",
    rootPassword: "",
    port: "2321",
    callbackPath: location.protocol + "//" + window.location.host,
    path: ""
};
const formData = reactive(cloneDeep(initFormData));
const connectionFormRef = ref<FormInstance>();
const connectionFormRules = reactive<FormRules>({
    nodeId: [{ required: true, message: t('install.collectorRules[0]'), trigger: 'blur' }],
    rootPassword: [{ required: true, message: t('install.collectorRules[1]'), trigger: 'blur' }],
    port: [{ required: true, message: t('install.proxyRules[2]'), trigger: 'blur' }],
    callbackPath: [{ required: true, message: t('install.proxyRules[3]'), trigger: 'blur' }],
    path: [{ required: true, message: t('install.proxyRules[4]'), trigger: 'blur' }],
})
// cluster component
const handleClusterValue = (val: any) => {
    formData.nodeId = val.length > 1 ? val[1] : ''
    formData.path = `${basePath.value}/${formData.nodeId}/agent`
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
    let result = await connectionFormRef.value?.validate();
    if (!result) return;
    started.value = true;
    ws.name = moment(new Date()).format("YYYYMMDDHHmmss") as string; // websocket connection name
    ws.sessionId = moment(new Date()).format("YYYYMMDDHHmmss") as string; // websocket connection id
    ws.instance = new WebSocketClass(ws.name, ws.sessionId, onWebSocketMessage);
    sendData();
};
const sendData = async () => {
    const encryptPwd = await encryptPassword(formData.rootPassword)
    const sendData = {
        key: 'agent',
        nodeId: formData.nodeId,
        rootPassword: encryptPwd,
        port: formData.port,
        path: formData.path,
        callbackPath: formData.callbackPath,
        language: localStorage.getItem('locale') === 'en-US' ? 'en_US' : 'zh_CN'
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
};

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
    ogRequest.get(`/observability/v1/environment/basePath`).then(res => {
        if(res) {
            basePath.value = res + (res.endsWith('/') ? 'data' : '/data');
        }
    })
}
onMounted(() => {
    getInstallPath();
});
onBeforeUnmount(() => {
    if (ws.instance) ws.instance.close()
})
</script>
<style lang="scss" scoped>
@import '../../../assets/style/style1.scss';
</style>
