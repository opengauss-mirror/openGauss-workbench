<template>
    <div class="dialog">
        <el-dialog width="400px" :title="t('install.installAgent')" v-model="visible" :close-on-click-modal="false" draggable @close="closeDialog">
            <div class="dialog-content" v-show="installData.length != 0">
                <div>
                    <el-steps direction="vertical" :active="doingIndex">
                        <el-step v-for="item in installData" :key="item.name" :title="item.name" />
                    </el-steps>
                </div>
            </div>
            <div class="dialog-content" v-loading="started" v-show="installData.length === 0">
                <el-form :model="formData" :rules="connectionFormRules" ref="connectionFormRef">
                    <el-form-item :label="t('install.collectInstance')" prop="nodeId">
                        <ClusterCascader width="200" instanceValueKey="nodeId" @getCluster="handleClusterValue" autoSelectFirst notClearable />
                    </el-form-item>
                    <el-form-item :label="t('install.rootPWD')" prop="rootPassword">
                        <el-input v-model="formData.rootPassword" show-password style="width: 200px; margin: 0 4px" />
                    </el-form-item>
                </el-form>
            </div>

            <template #footer>
                <el-button v-if="installData.length === 0" :loading="started" style="padding: 5px 20px" type="primary" @click="install">{{ $t("install.install") }}</el-button>
                <el-button v-if="installData.length != 0" style="padding: 5px 20px" @click="back">{{ $t("app.back") }}</el-button>
                <el-button style="padding: 5px 20px" @click="handleCancelModel">{{ $t("app.cancel") }}</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { cloneDeep } from "lodash-es";
import { FormRules, FormInstance } from "element-plus";
import { useI18n } from "vue-i18n";
import WebSocketClass from "../../../utils/websocket";
import { encryptPassword } from "../../../utils/jsencrypt";
import moment from "moment";
const { t } = useI18n();

const visible = ref(false);
const props = withDefaults(
    defineProps<{
        show: boolean;
    }>(),
    {}
);
watch(
    () => props.show,
    (newValue) => {
        visible.value = newValue;
    },
    { immediate: true }
);

// form data
const initFormData = {
    nodeId: "",
    rootPassword: "",
    port: "9090",
};
const formData = reactive(cloneDeep(initFormData));
const connectionFormRef = ref<FormInstance>();
const connectionFormRules = reactive<FormRules>({
    nodeId: [{ required: true, message: t("install.collectorRules[0]"), trigger: "blur" }],
    rootPassword: [{ required: true, message: t("install.collectorRules[1]"), trigger: "blur" }],
});
// cluster component
const handleClusterValue = (val: any) => {
    formData.nodeId = val.length > 1 ? val[1] : "";
};

const started = ref(false);
const installSucceed = ref(false);
const ws = reactive({
    name: "",
    webUser: "",
    connectionName: "",
    sessionId: "",
    instance: null,
});
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
    const encryptPwd = await encryptPassword(formData.rootPassword);
    const sendData = {
        key: "agent",
        nodeId: formData.nodeId,
        rootPassword: encryptPwd,
    };
    ws.instance.send(sendData);
};
const onWebSocketMessage = (data: Array<any>) => {
    if (Array.isArray(installData.value)) installData.value = JSON.parse(data);
};

// action
const back = () => {
    started.value = false;
    ws.instance.close();
    installData.value = [];
};

// list Data
const installData = ref<Array<any>>([]);
const doingIndex = computed(() => {
    for (let index = 0; index < installData.value.length; index++) {
        const element = installData.value[index];
        if (element.state === "DOING" || element.state === "ERROR") return index;
    }
    if (!installSucceed.value) installSucceed.value = true;
    return installData.value.length;
});

// dialog
const emit = defineEmits(["changeModal", "installed"]);
const handleCancelModel = () => {
    visible.value = false;
    if (installSucceed.value) emit("installed");
    emit("changeModal", visible.value);
};
const closeDialog = () => {
    visible.value = false;
    if (installSucceed.value) emit("installed");
    emit("changeModal", visible.value);
};

onBeforeUnmount(() => {
    if (ws.instance) ws.instance.close();
});
</script>
<style lang="scss" scoped>
@import "../../../assets/style/style1.scss";
</style>
