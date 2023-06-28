<template>
    <div>
        <el-form :model="formData" :rules="formRules" ref="formRef" label-position="left" label-width="100px">
            <el-form-item :label="$t('notifyWay.name')" prop="name">
                <el-input v-model="formData.name" :placeholder="$t('notifyWay.namePlaceholder')"
                    :disabled="disabled"></el-input>
            </el-form-item>
            <el-form-item :label="$t('notifyWay.notifyType')" prop="notifyType">
                <el-select v-model="formData.notifyType" :disabled="disabled" style="margin: 5px;"
                    @change="notifyTypeChange">
                    <el-option v-for="item in templateTypeList" :key="item.value" :value="item.value" :label="item.name" />
                </el-select>
            </el-form-item>
            <el-form-item :label="$t('notifyTemplate.email')" prop="email" v-if="formData.notifyType === 'email'"
                :required="true">
                <el-input v-model="formData.email" :placeholder="$t('notifyWay.emailPlaceholder')"
                    :disabled="disabled"></el-input>
            </el-form-item>
            <el-form-item :label="$t('notifyWay.personId')"
                v-if="formData.notifyType === 'WeCom' || formData.notifyType === 'DingTalk'">
                <el-input v-model="formData.personId" :placeholder="$t('notifyWay.personIdPlaceholder')"
                    :disabled="disabled"></el-input>
            </el-form-item>
            <el-form-item :label="$t('notifyWay.deptId')"
                v-if="formData.notifyType === 'WeCom' || formData.notifyType === 'DingTalk'">
                <el-input v-model="formData.deptId" :placeholder="$t('notifyWay.deptIdPlaceholder')"
                    :disabled="disabled"></el-input>
            </el-form-item>
            <el-form-item
                :label="formData.notifyType === 'email' ? $t('notifyWay.emailTemplate') : formData.notifyType === 'WeCom' ? $t('notifyWay.WeComTemplate') : formData.notifyType === 'DingTalk' ? $t('notifyWay.DingTalkTemplate') : ''"
                prop="notifyTemplateId" v-if="formData.notifyType">
                <el-select v-model="formData.notifyTemplateId" :disabled="disabled" style="margin: 5px;">
                    <el-option v-for="item in templateList" :key="item.id" :value="item.id"
                        :label="item.notifyTemplateName" />
                </el-select>
            </el-form-item>
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

const validateEmailNotNull = (rule: any, value: any, callback: any) => {
    if (formData.value.notifyType === 'email') {
        if (!value) {
            callback(new Error(t('notifyWay.emailPlaceholder')))
        }
    }
    callback()
}
const { t } = useI18n();
const props = withDefaults(
    defineProps<{
        id: number,
        state: string,
    }>(),
    {
        state: 'detail',
    }
)

const emit = defineEmits(["updateNotifyWay", "cancelNotifyWay"])

const disabled = ref<boolean>(true)
const initFormData = {
    id: '',
    name: '',
    notifyType: '',
    email: '',
    personId: '',
    deptId: '',
    notifyTemplateId: null
}
const formData = ref<any>(cloneDeep(initFormData));
const templateTypeList = ref<any[]>([{
    name: t('notifyTemplate.email'), value: 'email'
}, {
    name: t('notifyTemplate.WeCom'), value: 'WeCom'
}, {
    name: t('notifyTemplate.DingTalk'), value: 'DingTalk'
}])
const templateList = ref<any[]>([])

const formRef = ref<FormInstance>()
const formRules = reactive<FormRules>({
    name: [
        { required: true, message: t('notifyWay.namePlaceholder'), trigger: 'blur' },
    ],
    notifyType: [
        { required: true, message: t('notifyWay.notifyTypePlaceholder'), trigger: 'blur' },
    ],
    email: [
        { validator: validateEmailNotNull, message: t('notifyWay.emailPlaceholder'), trigger: 'blur' },
    ],
    notifyTemplateId: [
        { required: true, message: t('notifyWay.templatePlaceholder'), trigger: 'blur' },
    ],

})

const { data: res, run: requestData } = useRequest(
    (id) => {
        return request.get(`/alertCenter/api/v1/notifyWay/${id}`)
    },
    { manual: true }
)
watch(res, (res: any) => {
    if (res && res.code === 200) {
        formData.value = res.data
        if (formData.value.notifyType) {
            requestTemplateData(formData.value.notifyType)
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

const { data: templateRes, run: requestTemplateData } = useRequest(
    (notifyTemplateType) => {
        return request.get(`/alertCenter/api/v1/notifyTemplate/list`, { notifyTemplateType })
    },
    { manual: true }
)
watch(templateRes, (templateRes: any) => {
    if (templateRes && templateRes.code === 200) {
        templateList.value = templateRes.data || []
    } else {
        const msg = t("app.queryFail");
        ElMessage({
            showClose: true,
            message: msg,
            type: "error",
        });
    }
});

const notifyTypeChange = () => {
    formData.value.notifyTemplateId = null
    if (!formData.value.notifyType) {
        templateList.value = []
        return
    }
    formData.value.email = ''
    formData.value.personId = ''
    formData.value.deptId = ''
    requestTemplateData(formData.value.notifyType)
}

const confirm = () => {
    if (!formRef) return
    formRef.value?.validate((valid, fields) => {
        if (valid) {
            if ((formData.value.notifyType === 'WeCom' || formData.value.notifyType === 'DingTalk') && !formData.value.personId && !formData.value.deptId) {
                const msg = t("notifyWay.existOneOfTargetTip");
                ElMessage({
                    showClose: true,
                    message: msg,
                    type: "error",
                });
                return
            }
            request.post(`/alertCenter/api/v1/notifyWay`, formData.value).then((res: any) => {
                if (res && res.code === 200) {
                    ElMessage({
                        message: t('app.saveSuccess'),
                        type: 'success'
                    })
                    formData.value = {}
                    formData.value = cloneDeep(initFormData)
                    emit("updateNotifyWay")
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
    emit("cancelNotifyWay")
}
onMounted(() => {
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
<style scoped lang='scss'></style>