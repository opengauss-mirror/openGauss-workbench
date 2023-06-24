<template>
    <div>
        <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ t('app.setAlertConfig') }}</el-breadcrumb-item>
        </el-breadcrumb>
        <el-divider />
        <el-form :model="formData" :rules="formRules" ref="formRef" label-position="right" label-width="100px">
            <el-form-item :label="$t('AlertClusterNodeConf.alertIp')">
                <el-input v-model="formData.alertIp" :disabled="!alertConfigEnv.isInstall"></el-input>
            </el-form-item>
            <el-form-item :label="$t('AlertClusterNodeConf.alertPort')">
                <el-input v-model="formData.alertPort" :disabled="!alertConfigEnv.isInstall"></el-input>
            </el-form-item>
            <el-form-item :label="$t('AlertClusterNodeConf.interfaceAddr')">
                <span>{{ 'http://' + (formData.alertIp ? formData.alertIp : 'IP') + ':' + (formData.alertPort ?
                    formData.alertPort : 'port') + '/plugins/alert-center/alertCenter/api/v1/alerts' }}</span>
            </el-form-item>
        </el-form>
        <el-row>
            <el-button :disabled="!alertConfigEnv.isInstall" type="primary" @click="confirm">{{ t('app.confirm') }}</el-button>
        </el-row>
        <el-dialog v-model="dialogVisible" :title="t('app.tip')" width="30%">
            <span v-if="!alertConfigEnv.isInstall">
                {{ t('app.uninstallTip') }}
            </span>
            <span v-if="!alertConfigEnv.isConfig">
                {{ t('app.disConfigTip') }}
            </span>
            <template #footer>
                <span class="dialog-footer">
                    <el-button type="primary" @click="closeTip">
                        {{ t('app.confirm') }}
                    </el-button>
                </span>
            </template>
        </el-dialog>

    </div>
</template>

<script setup lang='ts'>
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import { useAlertConfigStore } from "@/store/alertConfig";

const { t } = useI18n();
const dialogVisible = ref<boolean>(false)

const alertConfigEnv = useAlertConfigStore();
const formData = ref<any>({
    id: '',
    alertIp: '',
    alertPort: ''
})

const confirm = () => {
    request.post(`/alertCenter/api/v1/alertConf`, formData.value).then((res: any) => {
        if (res && res.code === 200) {
            ElMessage({
                message: t('app.saveSuccess'),
                type: 'success'
            })
            alertConfigEnv.isConfig = true
            alertConfigEnv.showMain = true
        } else {
            ElMessage({
                message: t('app.saveFail'),
                type: 'error'
            })
        }
    })
}

const closeTip = () => {
    dialogVisible.value = false
}

onMounted(() => {
    if (!alertConfigEnv.showMain) {
        dialogVisible.value = true
    }
})
</script>
<style scoped lang='scss'></style>