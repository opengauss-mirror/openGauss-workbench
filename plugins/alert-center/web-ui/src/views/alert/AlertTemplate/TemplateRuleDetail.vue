<template>
    <div>
        <div class="page-header" v-if="titleList.length > 0">
            <div class="icon"></div>
            <div class="title">{{ titleList[0] }}</div>
            <div class="seperator"></div>
            <span v-for="(item, index) in titleList" :key="item">
                <span class="alert-title">{{ item }} </span>
                <span class="alert-seperator" v-if="index === 0 || index !== titleList.length - 1">&nbsp;/&nbsp;</span>
            </span>
        </div>
        <el-descriptions :title="$t('alertRule.alertTitle')"></el-descriptions>
        <el-form :model="formData" :rules="formRules" ref="formRef" label-position="left" label-width="100px">
            <el-form-item :label="$t('alertRule.ruleName')" prop="ruleName">
                <el-input v-model="formData.ruleName" disabled
                    :placeholder="$t('alertRule.ruleNamePlaceholder')"></el-input>
            </el-form-item>
            <el-form-item :label="$t('alertRule.ruleType')" prop="ruleType">
                <el-radio-group v-model="formData.ruleType" disabled>
                    <el-radio v-for="item in ruleTypeList" :key="item" :label="item">{{ $t(`alertRule.${item}`)
                    }}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('alertRule.level')" prop="level">
                <el-radio-group v-model="formData.level">
                    <el-radio v-for="item in levelList" :key="item" :label="item">{{ $t(`alertRule.${item}`) }}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('alertRule.ruleItem')">
                <el-card class="box-card" v-for="item in formData.alertRuleItemList" :key="item.id"
                    body-style="padding: 5px">
                    <span style="margin: 5px 0;">{{ $t('alertRule.ruleItemNum') }}:</span><el-input v-model="item.ruleMark"
                        disabled style="width: 30px;margin: 5px;"></el-input>
                    <span style="margin: 5px 0 10px 5px;">{{ $t('alertRule.ruleItemExp') }}:</span>
                    <el-select v-model="item.ruleExpName" disabled style="width: 100px;margin: 5px 0 5px 5px;">
                        <el-option v-for="item0 in ruleItemExpList" :key="item0.name" :value="item0.name"
                            :label="item0.i18nName" />
                    </el-select>
                    <span v-if="item.itemParamList && item.itemParamList.length > 0">
                        <el-input v-model="item0.paramValue" style="width: 80px;margin: 1px;" disabled
                            v-for="item0 in item.itemParamList" :key="item0.id"></el-input>
                    </span>
                    <el-select v-model="item.action" disabled style="width: 100px;margin: 5px;">
                        <el-option v-for="item0 in actionList" :key="item0.value" :value="item0.value"
                            :label="item0.name" />
                    </el-select>
                    <el-input v-model="item.operate" style="width: 50px;margin: 5px;" disabled></el-input>
                    <el-input v-model="item.limitValue" style="width: 100px;margin: 5px;"></el-input>
                    <span style="width: 50px;margin: 5px;">{{ item.unit }}</span>
                </el-card>
            </el-form-item>
            <el-form-item :label="$t('alertRule.ruleExpComb')" prop="ruleExpComb">
                <span v-for="item in ruleExpComb" :key="item">
                    <el-select :value="item" v-if="showLogicSelect(item)" style="width: 50px;" disabled>
                        <el-option v-for="item in logicSymbolList" :key="item" :value="item"
                            :label="$t(`alertRule.${item}`)" />
                    </el-select>
                    <el-input :value="item" style="width: 50px;" v-else disabled></el-input>
                </span>
            </el-form-item>
            <el-row>
                <el-col :span="14">
                    <el-form-item :label="$t('alertRule.ruleContent')" prop="ruleContent" style="margin-bottom: 0px;">
                        <el-input v-model="formData.ruleContent" @blur="preview"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="2" style="padding-left: 5px;">
                    <el-button @click="reset" style="display: flex;">{{ t('app.reset') }}</el-button>
                </el-col>
                <el-col :span="8" style="margin-bottom: 0px;">
                    <div class="alert-param">
                        <div class="title"><svg-icon name="tip" />{{ t('alertRule.alertContentTitle') }}</div>
                        <div class="content">
                            <span>{{ t('alertRule.alertContentTip') }}</span>
                            <el-row style="margin-top: 10px;">
                                <el-col :span="12" v-for="item in paramNameList" :key="item">{{ `${item}:
                                                                    ${alertContentParam[item]['name']}` }}</el-col>
                            </el-row>
                        </div>
                    </div>
                </el-col>
                <el-col :span="14">
                    <el-form-item>
                        <span>{{ previewContent }}</span>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item :label="$t('alertRule.notifyDuration')" prop="notifyDuration">
                <el-input v-model="formData.notifyDuration" style="width: 100px;margin-right: 5px;" disabled></el-input>
                <el-select v-model="formData.notifyDurationUnit" disabled style="width: 80px;">
                    <el-option v-for="item in durationUnitList" :key="item.name" :value="item.value"
                        :label="$t(`alertRule.${item.name}`)" />
                </el-select>
            </el-form-item>
            <el-form-item :label="$t('alertRule.alertDesc')" prop="alertDesc">
                <el-input v-model="formData.alertDesc" style="width: 50%;"></el-input>
            </el-form-item>
        </el-form>
        <el-descriptions :title="$t('alertRule.notifyTitle')"></el-descriptions>
        <el-form :model="formData" :rules="formRules" ref="formRef" label-position="right" label-width="100px">
            <el-form-item :label="$t('alertRule.alertNotify')" prop="notifyDuration">
                <el-checkbox-group v-model="alertNotifyList">
                    <el-checkbox label="firing">{{ $t('alertRule.firing') }}</el-checkbox>
                    <el-checkbox label="recover">{{ $t('alertRule.recover') }}</el-checkbox>
                </el-checkbox-group>
            </el-form-item>
            <el-form-item :label="$t('alertRule.repeat')" prop="isRepeat">
                <el-radio-group v-model="formData.isRepeat">
                    <el-radio :label="1">{{ $t('alertRule.isRepeat') }}</el-radio>
                    <el-radio :label="0">{{ $t('alertRule.isNotRepeat') }}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('alertRule.silence')" prop="isSilence">
                <el-radio-group v-model="formData.isSilence">
                    <el-radio :label="1" key="1">{{ $t('alertRule.isSilence') }}</el-radio>
                    <el-radio :label="0" key="0">{{ $t('alertRule.isNotSilence') }}</el-radio>
                </el-radio-group>
                <span style="margin-left: 30px;" v-if="formData.isSilence === 1 || formData.isSilence === '1'">{{ $t('alertRule.silenceTime')
                }}</span>
                <span style="width: 250px;margin-left: 5px;">
                    <el-date-picker v-if="formData.isSilence === 1 || formData.isSilence === '1'" v-model="silenceTimes" type="datetimerange"
                        range-separator="~" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss"
                        :start-placeholder="t(`alertRecord.startTimePlaceholder`)"
                        :end-placeholder="t(`alertRecord.endTimePlaceholder`)" />
                </span>
            </el-form-item>
            <el-form-item :label="$t('alertRule.notifyWay')">
                <el-select v-model="notifyWayIdArr" multiple>
                    <el-option v-for="item in notifyWayList" :key="item.id" :value="item.id + ''" :label="item.name" />
                </el-select>
            </el-form-item>
        </el-form>
        <el-row>
            <el-button type="primary" @click="confirm">{{ t('app.confirm') }}</el-button>
            <el-button @click="cancel">{{ t('app.cancel') }}</el-button>
        </el-row>
    </div>
</template>

<script setup lang='ts'>
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { i18n } from "@/i18n";
import { ElMessageBox, ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
import SvgIcon from "@/components/SvgIcon.vue";
import { parseContent } from "@/utils/commonUtil"
const { t } = useI18n();

const alertContentParam = ref<any>()
const paramNameList = ref<any>()
const previewContent = ref<string>()

const props = withDefaults(
    defineProps<{
        ruleId: number,
        templateRuleId: number,
        titleList: string[]
    }>(),
    {
        titleList: () => []
    }
);
const emit = defineEmits(["updateTemplateRuleSuccess", "cancelUpdateTemplateRule"]);

const ruleItemExpList = ref<any[]>()
const actionList = ref<any[]>([{
    name: t('alertRule.normalAction'), value: 'normal'
}, {
    name: t('alertRule.increaseAction'), value: 'increase'
}, {
    name: t('alertRule.decreaseAction'), value: 'decrease'
}])
const durationUnitList = ref<any[]>([{
    name: 'second',
    value: 's'
}, {
    name: 'minute',
    value: 'm'
}, {
    name: 'hour',
    value: 'h'
}, {
    name: 'day',
    value: 'd'
}])
const notifyWayIdArr = ref<string[]>([])
const notifyWayList = ref<string[]>([])
const logicSymbolList = ref<string[]>(['and', 'or'])
const formData = ref<any>({
    ruleName: '',
    ruleType: '',
    level: '',
    ruleExpComb: '',
    isRepeat: 1,
    isSilence: 0,
    alertRuleItemList: []
})
const ruleTypeList = reactive(['index', 'log'])
const levelList = reactive(['serious', 'warn', 'info'])
const ruleExpComb = ref<string[]>([])
const alertNotifyList = ref<string[]>([])
// const isRepeat = ref<string>('')
// const isSilence = ref<string>('')
const silenceTimes = ref<any[]>([])
const showLogicSelect = (logicSymbol: string) => {
    return logicSymbolList.value.includes(logicSymbol)
}

const ruleContent = ref<string>('')
const reset = () => {
    formData.value.ruleContent = ruleContent.value
    preview()
}

const { data: ruleDetail, run: requestData } = useRequest(
    (templateRuleId, ruleId) => {
        if (templateRuleId) {
            return request.get(`/alertCenter/api/v1/alertTemplate/ruleList/${templateRuleId}`)
        } else {
            return request.get(`/alertCenter/api/v1/alertRule/${ruleId}`)
        }
    },
    { manual: true }
)
watch(ruleDetail, (ruleDetail: any) => {
    if (ruleDetail && ruleDetail.code === 200) {
        formData.value = ruleDetail.data
        ruleContent.value = formData.value?.ruleContent
        if (formData.value?.ruleExpComb) {
            ruleExpComb.value = formData.value.ruleExpComb.split(',')
        }
        if (formData.value?.alertNotify) {
            alertNotifyList.value = formData.value.alertNotify.split(',')
        }
        if (formData.value.notifyWayIds) {
            notifyWayIdArr.value = formData.value.notifyWayIds.split(',')
        }
        if (formData.value.silenceStartTime && formData.value.silenceEndTime) {
            silenceTimes.value = [formData.value.silenceStartTime, formData.value.silenceEndTime]
        }
        if (alertContentParam.value && formData.value.ruleContent) {
            let param = {}
            let keys = Object.keys(alertContentParam.value);
            for (let key of keys) {
                param[key] = alertContentParam.value[key]['preVal']
            }
            previewContent.value = parseContent(formData.value.ruleContent, param)
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

const requestNotifyWayData = () => {
    request.get(`/alertCenter/api/v1/notifyWay/list`).then((res: any) => {
        if (res && res.code === 200) {
            notifyWayList.value = res.data
        }
    })
}
const requestRuleItemList = () => {
    request.get(`/alertCenter/api/v1/alertRule/ruleItem/properties`).then((res: any) => {
        if (res && res.code === 200) {
            ruleItemExpList.value = res.data
        }
    })
}

const confirm = () => {
    formData.value.ruleExpComb = ruleExpComb.value.join(',')
    formData.value.alertNotify = alertNotifyList.value.join(',')
    formData.value.notifyWayIds = notifyWayIdArr.value.join(',')
    if (silenceTimes.value.length > 0) {
        formData.value.silenceStartTime = silenceTimes.value[0]
        formData.value.silenceEndTime = silenceTimes.value[1]
    }
    if (formData.value.alertRuleItemList && formData.value.alertRuleItemList.length > 0) {
        for (let row of formData.value.alertRuleItemList) {
            if (row.ruleId) {
                row.ruleId = undefined
            }
            if (row.itemParamList && row.itemParamList.length > 0) {
                for (let row0 of row.itemParamList) {
                    row0.ruleItemParamId = undefined
                    row0.itemId = undefined
                }
            }
        }
    }
    request.post(`/alertCenter/api/v1/alertTemplate/templateRule`, formData.value).then((res: any) => {
        if (res && res.code === 200) {
            let data = res.data
            emit("updateTemplateRuleSuccess", data)
        }
    })
}
const cancel = () => {
    emit("cancelUpdateTemplateRule")
}

const requestAlertContentParam = () => {
    request.get(`/alertCenter/api/v1/environment/alertContentParam`, { type: 'alert' }).then((res: any) => {
        if (res && res.code === 200) {
            alertContentParam.value = res.data
            paramNameList.value = Object.keys(alertContentParam.value)
            if (alertContentParam.value && formData.value.ruleContent) {
                let param = {}
                let keys = Object.keys(alertContentParam.value);
                for (let key of keys) {
                    param[key] = alertContentParam.value[key]['preVal']
                }
                previewContent.value = parseContent(formData.value.ruleContent, param)
            }
        }
    })
}

const preview = () => {
    if (alertContentParam.value && formData.value.ruleContent) {
        let param = {}
        let keys = Object.keys(alertContentParam.value);
        for (let key of keys) {
            param[key] = alertContentParam.value[key]['preVal']
        }
        previewContent.value = parseContent(formData.value.ruleContent, param)
    }
}

onMounted(() => {
    requestNotifyWayData()
    requestRuleItemList()
    requestAlertContentParam();
    if (props.templateRuleId || props.ruleId) {
        requestData(props.templateRuleId, props.ruleId)
    }

    const wujie = window.$wujie;
    // Judge whether it is a plug-in environment or a local environment through wujie
    if (wujie) {
        // Monitoring platform language change
        wujie?.bus.$on('opengauss-locale-change', (val: string) => {
            console.log('log-search catch locale change');
            nextTick(() => {
                actionList.value = [{
                    name: t('alertRule.normalAction'), value: 'normal'
                }, {
                    name: t('alertRule.increaseAction'), value: 'increase'
                }, {
                    name: t('alertRule.decreaseAction'), value: 'decrease'
                }]
            });
        });
        // wujie?.bus.$on('opengauss-theme-change', (val: string) => {
        //     nextTick(() => {
        //         requestPisData()
        //     });
        // });
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
        font-weight: 800;
        font-size: 12px;
        line-height: 18px;
        // color: #4E5969;
    }
}
</style>