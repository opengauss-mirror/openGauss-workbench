<template>
    <div>
        <el-row>
            <el-col :span="2">
                <el-checkbox v-model="emailConfig.enable" :true-label="1" :false-label="0">{{ $t('notifyConfig.emailConfig')
                }}</el-checkbox>
            </el-col>
            <el-col :span="20" class="form-inline">
                <el-form :inline="true" :model="emailConfig" ref="emailForm" :rules="emailRules" label-position="left" label-width="100px">
                    <el-form-item :label="$t('notifyConfig.senderEmail')" prop="email">
                        <el-input v-model="emailConfig.email" :disabled="emailConfig.enable === 0"
                            :placeholder="$t('notifyConfig.inputTip') + $t('notifyConfig.senderEmail')" />
                    </el-form-item>
                    <el-form-item :label="$t('notifyConfig.sender')" prop="sender">
                        <el-input v-model="emailConfig.sender" :disabled="emailConfig.enable === 0"
                            :placeholder="$t('notifyConfig.inputTip') + $t('notifyConfig.sender')" />
                    </el-form-item>
                    <el-form-item :label="$t('notifyConfig.sever')" prop="sever">
                        <el-input v-model="emailConfig.sever" :disabled="emailConfig.enable === 0"
                            :placeholder="$t('notifyConfig.inputTip') + $t('notifyConfig.sever')" />
                    </el-form-item>
                    <el-form-item :label="$t('notifyConfig.port')" prop="port">
                        <el-input v-model="emailConfig.port" :disabled="emailConfig.enable === 0"
                            :placeholder="$t('notifyConfig.inputTip') + $t('notifyConfig.port')" />
                    </el-form-item>
                    <el-form-item :label="$t('notifyConfig.account')" prop="account">
                        <el-input v-model="emailConfig.account" :disabled="emailConfig.enable === 0"
                            :placeholder="$t('notifyConfig.inputTip') + $t('notifyConfig.account')" />
                    </el-form-item>
                    <el-form-item :label="$t('notifyConfig.passwd')" prop="passwd">
                        <el-input v-model="emailConfig.passwd" :disabled="emailConfig.enable === 0"
                            :placeholder="$t('notifyConfig.inputTip') + $t('notifyConfig.passwd')" />
                    </el-form-item>
                </el-form>
            </el-col>
            <el-col :span="2">
                <el-button style="margin: 5px 10px" type="info" @click="testNotify('email')">{{ t('notifyConfig.test')
                }}</el-button>
            </el-col>
        </el-row>
        <el-row style="margin-top: 10px;">
            <el-col :span="2">
                <el-checkbox v-model="weComConfig.enable" :true-label="1" :false-label="0">{{ t('notifyConfig.weComConfig')
                }}</el-checkbox>
            </el-col>
            <el-col :span="20" class="form-inline">
                <el-form :inline="true" :model="weComConfig" ref="weComForm" :rules="weComRules" label-position="right" label-width="100px">
                    <el-form-item :label="$t('notifyConfig.weComAppKey')" prop="appKey">
                        <el-input v-model="weComConfig.appKey" :disabled="weComConfig.enable === 0"
                            :placeholder="$t('notifyConfig.inputTip') + $t('notifyConfig.weComAppKey')" />
                    </el-form-item>
                    <el-form-item :label="$t('notifyConfig.agentId')" prop="agentId">
                        <el-input v-model="weComConfig.agentId" :disabled="weComConfig.enable === 0"
                            :placeholder="$t('notifyConfig.inputTip') + $t('notifyConfig.agentId')" />
                    </el-form-item>
                    <el-form-item :label="$t('notifyConfig.weComSecret')" prop="secret">
                        <el-input v-model="weComConfig.secret" :disabled="weComConfig.enable === 0"
                            :placeholder="$t('notifyConfig.inputTip') + $t('notifyConfig.weComSecret')" />
                    </el-form-item>
                </el-form>
            </el-col>
            <el-col :span="2">
                <el-button style="margin: 5px 10px" type="info">{{ t('notifyConfig.test') }}</el-button>
            </el-col>
        </el-row>
        <el-row style="margin-top: 10px;">
            <el-col :span="2">
                <el-checkbox v-model="dingTalkConfig.enable" :true-label="1" :false-label="0">{{
                    t('notifyConfig.dingTalkConfig') }}</el-checkbox>
            </el-col>
            <el-col :span="20" class="form-inline">
                <el-form :inline="true" :model="dingTalkConfig" ref="dingTalkForm" :rules="dingTalkRules" label-position="right" label-width="100px">
                    <el-form-item :label="$t('notifyConfig.appKey')" prop="appKey">
                        <el-input v-model="dingTalkConfig.appKey" :disabled="dingTalkConfig.enable === 0"
                            :placeholder="$t('notifyConfig.inputTip') + $t('notifyConfig.appKey')" />
                    </el-form-item>
                    <el-form-item :label="$t('notifyConfig.agentId')" prop="agentId">
                        <el-input v-model="dingTalkConfig.agentId" :disabled="dingTalkConfig.enable === 0"
                            :placeholder="$t('notifyConfig.inputTip') + $t('notifyConfig.agentId')" />
                    </el-form-item>
                    <el-form-item :label="$t('notifyConfig.dingSecret')" prop="secret">
                        <el-input v-model="dingTalkConfig.secret" :disabled="dingTalkConfig.enable === 0"
                            :placeholder="$t('notifyConfig.inputTip') + $t('notifyConfig.dingSecret')" />
                    </el-form-item>
                </el-form>
            </el-col>
            <el-col :span="2">
                <el-button style="margin: 5px 10px" type="info">{{ t('notifyConfig.test') }}</el-button>
            </el-col>
        </el-row>
        <el-row style="margin-top: 10px;">
            <el-button type="primary" @click="confirm">{{ t('app.confirm') }}</el-button>
        </el-row>
        <el-dialog v-model="dialogVisible" width="30%" :title="t('notifyConfig.test')" :before-close="handleClose">
            <el-form-item :label="$t('alertRule.notifyWay')">
                <el-select v-model="notifyWayId">
                    <el-option v-for="item in notifyWayList" :key="item.id" :value="item.id" :label="item.name" />
                </el-select>
            </el-form-item>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="handleClose">{{ t('app.cancel') }}</el-button>
                    <el-button type="primary" @click="testNotifyConfig">
                        {{ t('app.confirm') }}
                    </el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang='ts'>
import { ElLoading } from 'element-plus'
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessageBox, ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import { cloneDeep } from 'lodash-es';
import type { FormInstance, FormRules } from 'element-plus'
const { t } = useI18n();

const theme = localStorage.getItem('theme');
const color = ref<string>(theme === 'dark' ? '#d4d4d4' : '#1d212a')
const background = ref<string>(theme === 'dark' ? '#303030' : '#F5F7FB')

const baseConfig = {
    id: null,
    type: '',
    email: '',
    sender: '',
    sever: '',
    port: null,
    account: '',
    passwd: '',
    agentId: '',
    appKey: '',
    secret: '',
    enable: 0
}
const emailConfig = ref<any>(Object.assign(cloneDeep(baseConfig), { type: 'email' }))
const weComConfig = ref<any>(Object.assign(cloneDeep(baseConfig), { type: 'WeCom' }))
const dingTalkConfig = ref<any>(Object.assign(cloneDeep(baseConfig), { type: 'DingTalk' }))

const dialogVisible = ref<boolean>(false)
const notifyWayId = ref<any>();
const notifyWayList = ref<any[]>([])
const curType = ref<any>();

const emailForm = ref<FormInstance>()
const emailRules = reactive<FormRules>({
    email: [
        { required: true, message: t('notifyConfig.inputTip') + t('notifyConfig.email'), trigger: 'blur' },
    ],
    sender: [
        { required: true, message: t('notifyConfig.inputTip') + t('notifyConfig.sender'), trigger: 'blur' },
    ],
    sever: [
        { required: true, message: t('notifyConfig.inputTip') + t('notifyConfig.sever'), trigger: 'blur' },
    ],
    port: [
        { required: true, message: t('notifyConfig.inputTip') + t('notifyConfig.port'), trigger: 'blur' },
    ],
    account: [
        { required: true, message: t('notifyConfig.inputTip') + t('notifyConfig.account'), trigger: 'blur' },
    ],
    passwd: [
        { required: true, message: t('notifyConfig.inputTip') + t('notifyConfig.passwd'), trigger: 'blur' },
    ]
})
const weComForm = ref<FormInstance>()
const weComRules = reactive<FormRules>({
    appKey: [
        { required: true, message: t('notifyConfig.inputTip') + t('notifyConfig.weComAppKey'), trigger: 'blur' },
    ],
    agentId: [
        { required: true, message: t('notifyConfig.inputTip') + t('notifyConfig.agentId'), trigger: 'blur' },
    ],
    secret: [
        { required: true, message: t('notifyConfig.inputTip') + t('notifyConfig.weComSecret'), trigger: 'blur' },
    ]
})
const dingTalkForm = ref<FormInstance>()
const dingTalkRules = reactive<FormRules>({
    appKey: [
        { required: true, message: t('notifyConfig.inputTip') + t('notifyConfig.appKey'), trigger: 'blur' },
    ],
    agentId: [
        { required: true, message: t('notifyConfig.inputTip') + t('notifyConfig.agentId'), trigger: 'blur' },
    ],
    secret: [
        { required: true, message: t('notifyConfig.inputTip') + t('notifyConfig.dingSecret'), trigger: 'blur' },
    ]
})

const { data: res, run: requestData } = useRequest(
    () => {
        return request.get("/alertCenter/api/v1/notifyConfig/list")
    },
    { manual: true }
);
watch(res, (res: any) => {
    if (res && res.code === 200) {
        let list = res.data || []
        let emailList = list.filter((item: any) => item.type === 'email') || []
        if (emailList.length > 0) {
            emailConfig.value = emailList[0]
        }
        let weComList = list.filter((item: any) => item.type === 'WeCom') || []
        if (weComList.length > 0) {
            weComConfig.value = weComList[0]
        }
        let dingTalkList = list.filter((item: any) => item.type === 'DingTalk') || []
        if (dingTalkList.length > 0) {
            dingTalkConfig.value = dingTalkList[0]
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
const requestNotifyWayData = (notifyType: string) => {
    request.get(`/alertCenter/api/v1/notifyWay/list`, { notifyType }).then((res: any) => {
        if (res && res.code === 200) {
            notifyWayList.value = res.data
        }
    })
}
const testNotify = (type: string) => {
    dialogVisible.value = true
    notifyWayId.value = null
    curType.value = type
    requestNotifyWayData(type)
}
const loading = ref(false) // loading
const openLoading = () => {
    loading.value = ElLoading.service({
        lock: true,
        text: 'Loading',
        background: 'rgba(0, 0, 0, 0.7)'
    })
}
const closeLoading = () => {
    loading.value.close()
}
const testNotifyConfig = () => {
    let param = {}
    if (curType.value === 'email') {
        param = Object.assign(param, emailConfig.value)
    } else if (curType.value === 'WeCom') {
        param = Object.assign(param, weComConfig.value)
    } else if (curType.value === 'DingTalk') {
        param = Object.assign(param, dingTalkConfig.value)
    }
    param = Object.assign(param, { notifyWayId: notifyWayId.value })
    openLoading()
    request.post(`/alertCenter/api/v1/notifyConfig/testConfig`, param).then((res: any) => {
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
        closeLoading()
    }).catch(() => {
        ElMessage({
            message: t('app.testFail'),
            type: 'error'
        })
        closeLoading()
    })
}
const handleClose = () => {
    dialogVisible.value = false
    notifyWayId.value = null
    curType.value = null
}

const confirm = async() => {
    if (emailConfig.value.enable === 1) {
        let valid = await emailForm.value?.validate(async(valid, fields) => {
            return valid
        })
        if (!valid) {
            return
        }
    }
    if (weComConfig.value.enable === 1) {
        let valid = await weComForm.value?.validate(async(valid, fields) => {
            return valid
        })
        if (!valid) {
            return
        }
    }
    if (dingTalkConfig.value.enable === 1) {
        let valid = await dingTalkForm.value?.validate(async(valid, fields) => {
            return valid
        })
        if (!valid) {
            return
        }
    }
    let params = [emailConfig.value, weComConfig.value, dingTalkConfig.value]
    request.post(`/alertCenter/api/v1/notifyConfig/list`, params).then((res: any) => {
        if (res && res.code === 200) {
            ElMessage({
                message: t('app.saveSuccess'),
                type: 'success'
            })
            requestData()
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
onMounted(() => {
    requestData()

    const wujie = window.$wujie;
    if (wujie) {
        wujie?.bus.$on('opengauss-theme-change', (val: string) => {
            nextTick(() => {
                color.value = theme === 'dark' ? '#d4d4d4' : '#1d212a'
                background.value = theme === 'dark' ? '#303030' : '#F5F7FB'
            });
        });
    }
})
</script>
<style scoped lang='scss'>
.form-inline {
    box-sizing: border-box;
    color: v-bind(color);
    background: v-bind(background);
    // background: #FBFBFB;

    border: 1px solid #EAEBEE;
    border-radius: 2px;
}

.el-form-item--small {
    margin: 10px;
}

.testBtn {
    box-sizing: border-box;
    justify-content: center;
    align-items: center;
    width: 60px;
    height: 32px;
    background: #FFFFFF;
    border: 1px solid #D9D9D9;
    border-radius: 2px;
    margin: 5px 16px;
}
</style>