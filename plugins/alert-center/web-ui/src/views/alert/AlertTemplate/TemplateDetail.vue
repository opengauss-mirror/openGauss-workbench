<template>
    <div v-if="showMain">
        <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ t('alertTemplate.title') }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ title }}</el-breadcrumb-item>
        </el-breadcrumb>
        <el-divider />
        <!-- <el-descriptions :title="$t('alertRule.detailTitle')"></el-descriptions> -->
        <el-form :model="formData" :rules="formRules" ref="formRef" label-position="right" label-width="100px">
            <el-form-item :label="$t('alertTemplate.templateName')" prop="templateName">
                <el-input v-model="formData.templateName"
                    :placeholder="$t('alertTemplate.templateNamePlaceholder')"></el-input>
            </el-form-item>
            <el-form-item :label="$t('alertTemplate.selectRule')">
                <el-table size="small" :data="tableDatas" ref="ruleTable" style="width: 100%"
                    header-cell-class-name="grid-header" border>
                    <el-table-column type="selection" width="55" />
                    <el-table-column :label="$t('alertRule.table[0]')" prop="ruleName" width="140"></el-table-column>
                    <el-table-column :label="$t('alertRule.table[1]')" prop="ruleType" width="100">
                        <template #default="scope">
                            <span>{{ $t(`alertRule.${scope.row.ruleType}`) }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="level" :label="$t('alertRule.table[2]')" width="100">
                        <template #default="scope">
                            <span>{{ $t(`alertRule.${scope.row.level}`) }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="ruleExpDesc" :label="$t('alertRule.table[8]')" min-width="200">
                        <template #default="scope">
                            <span v-html="scope.row.ruleExpDesc"></span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="ruleExpComb" :label="$t('alertRule.table[9]')">
                    </el-table-column>
                    <el-table-column prop="isRepeat" :label="$t('alertRule.table[3]')" width="100">
                        <template #default="scope">
                            <span>{{ scope.row.isRepeat === 1 ? $t('alertRule.isRepeat') :
                                $t('alertRule.isNotRepeat') }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="isSilence" :label="$t('alertRule.table[4]')" width="200">
                        <template #default="scope">
                            <span>{{ scope.row.isSilence === 1 ? scope.row.silenceStartTime + $t('alertRule.to') +
                                scope.row.silenceEndTime : $t('alertRule.isNotSilence') }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="alertNotify" :label="$t('alertRule.table[5]')" width="120">
                        <template #default="scope">
                            <span v-text="showAlertNotify(scope.row.alertNotify)"></span>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('alertRule.table[7]')" fixed="right">
                        <template #default="scope">
                            <el-button link type="primary" size="small"
                                @click.prevent="edit(scope.row.templateRuleId, scope.row.ruleId)">
                                {{ t('app.edit') }}
                            </el-button>
                        </template>
                    </el-table-column>
                    <!-- <el-table-column prop="ruleContent" :label="$t('alertRule.table[6]')" /> -->
                </el-table>
            </el-form-item>
        </el-form>
        <el-row>
            <el-button type="primary" @click="confirm">{{ t('app.confirm') }}</el-button>
            <el-button @click="cancel">{{ t('app.cancel') }}</el-button>
        </el-row>
    </div>
    <div v-else>
        <TemplateRuleDetail :ruleId="editRuleId" :templateRuleId="editTemplateRuleId" :titleList="titleList"
            @updateTemplateRuleSuccess="updateTemplateRuleSuccess" @cancelUpdateTemplateRule="cancelUpdateTemplateRule" />
    </div>
</template>

<script setup lang='ts'>
// import { } from "@element-plus/icons-vue";
import "element-plus/es/components/message-box/style/index";
import { ref, nextTick } from 'vue'
import { useRequest } from "vue-request";
import request from "@/request";
import { i18n } from "@/i18n";
import { ElMessageBox, ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import type { FormInstance, FormRules } from 'element-plus'
import TemplateRuleDetail from "@/views/alert/AlertTemplate/TemplateRuleDetail.vue";
const { t } = useI18n();
const props = withDefaults(
    defineProps<{
        templateId: number,
        state: string,
    }>(),
    {
        state: 'add'
    }
);
const emit = defineEmits(["updateTemplate", "cancelTemplate"]);

const title = ref<string>()
const showMain = ref<boolean>(true)
const ruleTable = ref();
const formData = ref<any>({
    templateName: '',
    templateRuleDtoList: []
})
const tableDatas = ref<any[]>([])

const selectedRuleRows = ref<any[]>([])
const editTemplateRuleId = ref<number>()
const editRuleId = ref<number>()
const titleList = ref<string[]>([])

const formRef = ref<FormInstance>()
const formRules = reactive<FormRules>({
    templateName: [
        { required: true, message: t('alertTemplate.templateNamePlaceholder'), trigger: 'blur' },
    ]
})

const showAlertNotify = (val: string) => {
    if (!val) {
        return ''
    }
    let arr = val.split(',');
    let result = arr.map(item => t(`alertRule.${item}`)).join(',')
    return result
}
const { data: ruleRes, run: requestRuleData } = useRequest(
    () => {
        return request.get(`/alertCenter/api/v1/alertRule/ruleList`)
    },
    { manual: true }
);
watch(ruleRes, (ruleRes: any) => {
    if (ruleRes && ruleRes.code === 200) {
        tableDatas.value = ruleRes.data
        let selectDatas = formData.value.templateRuleDtoList || [];
        if (selectDatas.length > 0) {
            tableDatas.value = tableDatas.value.map(item => {
                let datas = selectDatas.filter((item0: any) => item0.ruleId === item.ruleId) || []
                if (datas.length > 0) {
                    nextTick(() => {
                        ruleTable.value.toggleRowSelection(datas[0], true)
                    })
                    return datas[0]
                }
                return item;
            })
        }
    } else {
        tableDatas.value = []
    }
});

const { data: templateRes, run: requestData } = useRequest(
    (id) => {
        return request.get(`/alertCenter/api/v1/alertTemplate/${id}`)
    },
    { manual: true }
);
watch(templateRes, (templateRes: any) => {
    if (templateRes && templateRes.code === 200) {
        formData.value = templateRes.data
        let selectDatas = formData.value.templateRuleDtoList || [];
        if (tableDatas.value.length > 0) {
            tableDatas.value = tableDatas.value.map(item => {
                let datas = selectDatas.filter((item0: any) => item0.ruleId === item.ruleId) || []
                if (datas.length > 0) {
                    nextTick(() => {
                        ruleTable.value.toggleRowSelection(datas[0], true)
                    })
                    return datas[0]
                }
                return item;
            })
        }
    } else {
        tableDatas.value = []
    }
});

const confirm = () => {
    if (!formRef.value) return
    formRef.value?.validate((valid, fields) => {
        if (!valid) {
            return;
        }
        const rows = ruleTable.value.getSelectionRows() || []
        if (rows.length === 0) {
            ElMessage({
                message: t('app.selectDatasTip'),
                type: 'warning'
            })
            return;
        }
        let templateRuleReqList = rows.map((item: any) => {
            return { templateRuleId: item.templateRuleId, ruleId: item.ruleId }
        })
        let param = {
            id: formData.value.id,
            templateName: formData.value.templateName,
            templateRuleReqList
        }
        request.post(`/alertCenter/api/v1/alertTemplate`, param).then((res: any) => {
            if (res && res.code === 200) {
                ElMessage({
                    message: t('app.saveSuccess'),
                    type: 'success'
                })
                formData.value = {
                    templateName: '',
                    templateRuleDtoList: []
                }
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
    })
}
const cancel = () => {
    formData.value = {
        templateName: '',
        templateRuleDtoList: []
    }
    emit("cancelTemplate")
}

const edit = (templateRuleId: any, ruleId: any) => {
    selectedRuleRows.value = ruleTable.value.getSelectionRows() || []
    // let selectedRuleIdArr = selectedRuleRows.value.map((item: any) => item.ruleId) || []
    // if (!selectedRuleIdArr.includes(ruleId)) {
    //     ElMessage({
    //         message: t('app.selectUpdateDataTip'),
    //         type: 'warning'
    //     })
    //     return;
    // }
    editTemplateRuleId.value = templateRuleId
    editRuleId.value = ruleId
    let curTitle = title.value || ''
    titleList.value = [t('alertTemplate.title'), curTitle, t('alertRule.editTitle')]
    showMain.value = false
}

const updateTemplateRuleSuccess = (templateRule: any) => {
    for (let i = 0; i < tableDatas.value.length; i++) {
        if (tableDatas.value[i].ruleId === templateRule.ruleId) {
            tableDatas.value[i].templateRuleId = templateRule.templateRuleId
            tableDatas.value[i].level = templateRule.level
            tableDatas.value[i].ruleExpDesc = templateRule.ruleExpDesc
            tableDatas.value[i].ruleExpComb = templateRule.ruleExpComb
            tableDatas.value[i].isRepeat = templateRule.isRepeat
            tableDatas.value[i].isSilence = templateRule.isSilence
            tableDatas.value[i].isSilence = templateRule.silenceStartTime
            tableDatas.value[i].isSilence = templateRule.silenceEndTime
        }
    }
    showMain.value = true
    nextTick(() => {
        for (let row of selectedRuleRows.value) {
            ruleTable.value.toggleRowSelection(row, true)
        }
    })
}

const cancelUpdateTemplateRule = () => {
    showMain.value = true
    nextTick(() => {
        for (let row of selectedRuleRows.value) {
            ruleTable.value.toggleRowSelection(row, true)
        }
    })
}

onMounted(() => {
    title.value = props.state === 'add' ? t('alertTemplate.addTitle') : props.state === 'edit' ? t('alertTemplate.editTitle') : t('alertTemplate.detailTitle')
    requestRuleData()
    if (props.templateId) {
        requestData(props.templateId)
    }
})

</script>
<style scoped lang='scss'></style>