<template>
    <div>
        <div class="page-header">
            <div class="icon"></div>
            <div class="title" v-if="state === 'add'">{{ t('notifyTemplate.addTitle') }}</div>
            <div class="title" v-if="state === 'edit'">{{ t('notifyTemplate.editTitle') }}</div>
            <div class="title" v-if="state === 'detail'">{{ t('notifyTemplate.detailTitle') }}</div>
            <div class="seperator"></div>
            <div class="alert-title">{{ t('notifyTemplate.title') }} </div>
            <div class="alert-seperator">&nbsp;/&nbsp;</div>
            <div class="alert-title" v-if="state === 'add'">{{ t('notifyTemplate.addTitle') }}</div>
            <div class="alert-title" v-if="state === 'edit'">{{ t('notifyTemplate.editTitle') }}</div>
            <div class="alert-title" v-if="state === 'detail'">{{ t('notifyTemplate.detailTitle') }}</div>
        </div>
        <el-form style="margin-top: 8px;" :model="formData" size="default" :rules="formRules" ref="formRef" label-position="left" label-width="120px">
            <el-form-item :label="$t('notifyTemplate.templateName')" prop="notifyTemplateName">
                <el-input v-model="formData.notifyTemplateName" :placeholder="$t('notifyTemplate.templateNamePlaceholder')"
                    :disabled="disabled"></el-input>
            </el-form-item>
            <el-form-item :label="$t('notifyTemplate.templateType')" prop="notifyTemplateType">
                <el-select v-model="formData.notifyTemplateType" :disabled="disabled" style="width: 100px;margin: 5px;">
                    <el-option v-for="item in templateTypeList" :key="item.value" :value="item.value" :label="item.name" />
                </el-select>
            </el-form-item>
            <el-form-item :label="$t('notifyTemplate.templateDesc')" prop="notifyTemplateDesc">
                <el-input v-model="formData.notifyTemplateDesc" :placeholder="$t('notifyTemplate.templateDescPlaceholder')"
                    :disabled="disabled"></el-input>
            </el-form-item>
            <el-form-item :label="$t('notifyTemplate.notifyTitle')" prop="notifyTitle">
                <el-input v-model="formData.notifyTitle" :placeholder="$t('notifyTemplate.notifyTitlePlaceholder')"
                    :disabled="disabled"></el-input>
            </el-form-item>
            <!-- <el-form-item :label="$t('notifyTemplate.notifyContent')" prop="notifyContent">
                <el-input v-model="formData.notifyContent" type="textarea" :rows="10"
                    :placeholder="$t('notifyTemplate.notifyContentPlaceholder')" :disabled="disabled"></el-input>
            </el-form-item> -->
            <el-row>
                <el-col :span="14">
                    <el-form-item :label="$t('notifyTemplate.notifyContent')" prop="notifyContent">
                        <el-input v-model="formData.notifyContent" type="textarea" :rows="10"
                        :placeholder="$t('notifyTemplate.notifyContentPlaceholder')" :disabled="disabled"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="2">
                    <div style="margin: 5px">
                        <el-button @click="reset">{{ t('app.reset') }}</el-button>
                    </div>
                    <div style="margin: 5px">
                        <el-button @click="preview">{{ t('app.preview') }}</el-button>
                    </div>
                </el-col>
                <el-col :span="8">
                    <div class="alert-param">
                        <div class="title"><svg-icon name="tip" />{{ t('notifyTemplate.notifyContentTitle') }}</div>
                        <div class="content">
                            <span>{{ t('notifyTemplate.notifyContentTip') }}</span>
                            <el-row style="margin-top: 10px;">
                                <el-col :span="12" v-for="item in paramNameList" :key="item">{{ `${item}:
                                                                    ${alertContentParam[item]['name']}` }}</el-col>
                            </el-row>
                        </div>
                    </div>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="14">
                    <el-form-item :label="$t('notifyTemplate.previewContent')">
                        <el-input v-model="previewContent" type="textarea" :rows="10" disabled></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <el-row style="margin-top: 10px;">
            <el-button type="primary" @click="confirm" v-if="state !== 'detail'">{{ t('app.confirm') }}</el-button>
            <el-button @click="cancel">{{ t('app.cancel') }}</el-button>
        </el-row>
    </div>
</template>

<script setup lang='ts'>
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import { cloneDeep } from 'lodash-es';
import type { FormInstance, FormRules } from 'element-plus'
import SvgIcon from "@/components/SvgIcon.vue";
import { parseContent } from "@/utils/commonUtil"

const alertContentParam = ref<any>()
const paramNameList = ref<any>()
const previewContent = ref<string>()
const notifyContent = ref<string>('')

const { t } = useI18n();
const props = withDefaults(
    defineProps<{
        id: number,
        state: string,
    }>(),
    {
        state: 'detail'
    }
)

const emit = defineEmits(["updateTemplate", "cancelTemplate"])

const disabled = ref<boolean>(true)
const initFormData = {
    id: '',
    notifyTemplateName: '',
    notifyTemplateType: '',
    notifyTemplateDesc: '',
    notifyTitle: '',
    notifyContent: ''
}
const formData = ref<any>(cloneDeep(initFormData));
const templateTypeList = ref<any[]>([{
    name: t('notifyTemplate.email'), value: 'email'
}, {
    name: t('notifyTemplate.WeCom'), value: 'WeCom'
}, {
    name: t('notifyTemplate.DingTalk'), value: 'DingTalk'
}])

const formRef = ref<FormInstance>()
const formRules = reactive<FormRules>({
    notifyTemplateName: [
        { required: true, message: t('notifyTemplate.templateNamePlaceholder'), trigger: 'blur' },
    ],
    notifyTemplateType: [
        { required: true, message: t('notifyTemplate.templateTypePlaceholder'), trigger: 'blur' },
    ],
    notifyTitle: [
        { required: true, message: t('notifyTemplate.notifyTitlePlaceholder'), trigger: 'blur' },
    ],
    notifyContent: [
        { required: true, message: t('notifyTemplate.notifyContentPlaceholder'), trigger: 'blur' },
    ],

})

const { data: res, run: requestData } = useRequest(
    (id) => {
        return request.get(`/alertCenter/api/v1/notifyTemplate/${id}`)
    },
    { manual: true }
)
watch(res, (res: any) => {
    if (res && res.code === 200) {
        formData.value = res.data
        notifyContent.value = formData.value.notifyContent
        if (alertContentParam.value && formData.value.notifyContent) {
            let param = {}
            let keys = Object.keys(alertContentParam.value);
            for (let key of keys) {
                param[key] = alertContentParam.value[key]['preVal']
            }
            previewContent.value = parseContent(formData.value.notifyContent, param)
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

const confirm = () => {
    if (!formRef) return
    formRef.value?.validate((valid, fields) => {
        if (valid) {
            request.post(`/alertCenter/api/v1/notifyTemplate`, formData.value).then((res: any) => {
                if (res && res.code === 200) {
                    ElMessage({
                        message: t('app.saveSuccess'),
                        type: 'success'
                    })
                    formData.value = {}
                    formData.value = cloneDeep(initFormData)
                    emit("updateTemplate")
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
    })
}
const cancel = () => {
    formData.value = {}
    formData.value = cloneDeep(initFormData)
    emit("cancelTemplate")
}
const requestNotifyContentParam = () => {
    request.get(`/alertCenter/api/v1/environment/alertContentParam`, { type: 'notify' }).then((res: any) => {
        if (res && res.code === 200) {
            alertContentParam.value = res.data
            paramNameList.value = Object.keys(alertContentParam.value)
            if (alertContentParam.value && formData.value.notifyContent) {
                let param = {}
                let keys = Object.keys(alertContentParam.value);
                for (let key of keys) {
                    param[key] = alertContentParam.value[key]['preVal']
                }
                previewContent.value = parseContent(formData.value.notifyContent, param)
            }
        }
    })
}
const reset = () => {
    formData.value.notifyContent = notifyContent.value
    preview()
}
const preview = () => {
    if (alertContentParam.value && formData.value.notifyContent) {
        let param = {}
        let keys = Object.keys(alertContentParam.value);
        for (let key of keys) {
            param[key] = alertContentParam.value[key]['preVal']
        }
        previewContent.value = parseContent(formData.value.notifyContent, param)
    } else {
        previewContent.value = ''
    }
}

onMounted(() => {
    requestNotifyContentParam()
    if (props.id) {
        requestData(props.id)
    }
    if (props.state !== 'detail') {
        disabled.value = false
    } else {
        disabled.value = true
    }
})
</script>
<style scoped lang='scss'>
.alert-param {
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    position: absolute;
    align-items: flex-start;
    padding: 9px 16px;
    gap: 6px;
    // background: #F4FAFF;
    border: 1px solid #94BFFF;
    border-radius: 2px;

    .title {
        font-style: normal;
        font-weight: 800;
        font-size: 14px;
        line-height: 24px;
        // color: #1D2129;
    }

    .content {
        font-style: normal;
        font-weight: 400;
        font-size: 12px;
        line-height: 18px;
        // color: #4E5969;
    }
}
</style>