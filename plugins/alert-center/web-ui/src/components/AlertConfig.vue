<template>
    <div>
        <div class="page-header">
            <div class="icon"></div>
            <div class="title">{{ t('app.setAlertConfig') }}</div>
            <div class="seperator"></div>
            <div class="alert-title">{{ t('app.setAlertConfig') }} </div>
            <div class="alert-seperator">&nbsp;/</div>
        </div>
        <div class="tip">
            <div class="content"><svg-icon name="tip" style="margin-right: 4px;margin-left: 10px;" />{{
                t('app.disConfigTip') }}</div>
        </div>
        <el-form :model="formData" :rules="formRules" ref="formRef" size="default" label-position="left"
            label-width="100px">
            <el-form-item :label="$t('AlertClusterNodeConf.alertIp')" prop="alertIp">
                <el-input v-model="formData.alertIp" :disabled="!alertConfigEnv.isInstall"></el-input>
            </el-form-item>
            <el-form-item :label="$t('AlertClusterNodeConf.alertPort')" prop="alertPort">
                <el-input v-model="formData.alertPort" :disabled="!alertConfigEnv.isInstall"></el-input>
            </el-form-item>
        </el-form>
        <el-row>
            <el-button :disabled="!alertConfigEnv.isInstall" type="primary" @click="confirm">{{ t('app.confirm')
            }}</el-button>
            <el-button @click="cancel">{{ t('app.cancel') }}</el-button>
        </el-row>
        <el-dialog v-model="dialogVisible" width="578px" height="293px" :show-close="false" top="0">
            <el-row>
                <el-col :span="7">
                    <svg-icon name="datakit-menu" style="width: 163px;height: 215px;" />
                </el-col>
                <el-col :span="17">
                    <div class="title">{{ t('app.uninstallTitleTip') }}</div>
                    <div class="content">
                        {{ t('app.uninstallTip') }}
                    </div>
                </el-col>
            </el-row>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="closeTip">
                        {{ t('app.cancel') }}
                    </el-button>
                    <el-button type="primary" @click="gotoInstall">
                        {{ t('app.gotoInstall') }}
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
import type { FormInstance, FormRules } from 'element-plus'

const { t } = useI18n();
const dialogVisible = ref<boolean>(false)

const alertConfigEnv = useAlertConfigStore();
const formData = ref<any>({
    id: '',
    alertIp: '',
    alertPort: ''
})

const formRef = ref<FormInstance>()
const formRules = reactive<FormRules>({
    alertIp: [
        { required: true, message: t('app.inputTip') + t('AlertClusterNodeConf.alertIp'), trigger: 'blur' },
    ],
    alertPort: [
        { required: true, message: t('app.inputTip') + t('AlertClusterNodeConf.alertPort'), trigger: 'blur' },
    ],
})

const confirm = () => {
    formRef.value?.validate((valid, fields) => {
        if (valid) {
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
    })
}

const cancel = () => {

}
const gotoInstall = () => {

}
const closeTip = () => {
    dialogVisible.value = false
}

onMounted(() => {
    formData.value.alertIp = window.location.hostname
    formData.value.alertPort = window.location.port
    if (!alertConfigEnv.isInstall) {
        dialogVisible.value = true
    } else {
        dialogVisible.value = false
    }
})
</script>
<style scoped lang='scss'>
.tip {
    display: flex;
    height: 32px;
    width: 50%;
    flex-direction: column;
    justify-content: center;
    align-items: flex-start;
    gap: 6px;
    flex-shrink: 0;
    display: flex;
    border: 1px solid #94BFFF;
    background: var(--background-color-5);
    border-radius: 2px;
    margin-top: 8px;
    margin-bottom: 10px;

    .content {
        font-size: 14px;
        line-height: 16px;
        font-family: "Source Han Sans CN";
    }
}

.el-dialog {
    .el-dialog__header {
        padding: 0 !important;
        border: none !important;
    }
    .title {
        font-size: 16px;
        font-family: Source Han Sans CN;
        font-weight: 600;
        line-height: 24px;
        margin-bottom: 17px;
    }
    .content {
        font-size: 14px;
        font-family: Source Han Sans CN;
        line-height: 24px;
    }
}
</style>