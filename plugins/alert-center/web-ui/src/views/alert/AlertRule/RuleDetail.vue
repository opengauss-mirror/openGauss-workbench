<template>
    <div>
        <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ t('alertRule.title') }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="state === 'add'">{{ t('alertRule.addTitle') }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="state === 'edit'">{{ t('alertRule.editTitle') }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="state === 'detail'">{{ t('alertRule.detailTitle') }}</el-breadcrumb-item>
        </el-breadcrumb>
        <el-divider />
        <el-descriptions :title="$t('alertRule.alertTitle')"></el-descriptions>
        <el-form :model="formData" :rules="formRules" ref="formRef" label-position="right" label-width="100px">
            <el-form-item :label="$t('alertRule.ruleName')" prop="ruleName">
                <el-input v-model="formData.ruleName" :placeholder="$t('alertRule.ruleNamePlaceholder')"
                    :disabled="disabled"></el-input>
            </el-form-item>
            <el-form-item :label="$t('alertRule.ruleType')" prop="ruleType">
                <el-radio-group v-model="formData.ruleType" :disabled="disabled">
                    <el-radio v-for="item in ruleTypeList" :key="item" :label="item">{{ $t(`alertRule.${item}`)
                    }}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('alertRule.level')" prop="level">
                <el-radio-group v-model="formData.level" :disabled="disabled">
                    <el-radio v-for="item in levelList" :key="item" :label="item">{{ $t(`alertRule.${item}`) }}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('alertRule.ruleItem')" prop="">
                <el-card class="box-card" v-for="item in formData.alertRuleItemList" :key="item.id"
                    body-style="padding: 5px">
                    <span style="margin: 5px 0;">{{ $t('alertRule.ruleItemNum') }}:</span><el-input v-model="item.ruleMark"
                        :disabled="disabled" style="width: 30px;margin: 5px;"></el-input>
                    <span style="margin: 5px 0 10px 5px;">{{ $t('alertRule.ruleItemExp') }}:</span>
                    <el-select v-model="item.ruleExpName" :disabled="disabled" style="width: 100px;margin: 5px 0 5px 5px;">
                        <el-option v-for="item0 in ruleItemExpList" :key="item0.name" :value="item0.name"
                            :label="item0.i18nName" />
                    </el-select>
                    <span v-if="item.itemParamList && item.itemParamList.length > 0">
                        <el-input v-model="item0.paramValue" style="width: 80px;margin: 1px;" :disabled="disabled"
                            v-for="item0 in item.itemParamList" :key="item0.id"></el-input>
                    </span>
                    <el-select v-model="item.action" :disabled="disabled" style="width: 100px;margin: 5px;">
                        <el-option v-for="item0 in actionList" :key="item0.value" :value="item0.value"
                            :label="item0.name" />
                    </el-select>
                    <el-input v-model="item.operate" style="width: 50px;margin: 5px;" :disabled="disabled"></el-input>
                    <el-input v-model="item.limitValue" style="width: 100px;margin: 5px;" :disabled="disabled"></el-input>
                    <span style="width: 50px;margin: 5px;">{{ item.unit }}</span>
                </el-card>
            </el-form-item>
            <el-form-item :label="$t('alertRule.ruleExpComb')" prop="ruleExpComb">
                <span v-for="item in ruleExpComb" :key="item">
                    <el-select :value="item" v-if="showLogicSelect(item)" style="width: 50px;" :disabled="disabled">
                        <el-option v-for="item in logicSymbolList" :key="item" :value="item"
                            :label="$t(`alertRule.${item}`)" />
                    </el-select>
                    <el-input :value="item" style="width: 50px;" v-else :disabled="disabled"></el-input>
                </span>
            </el-form-item>
            <el-form-item :label="$t('alertRule.ruleContent')" prop="ruleContent">
                <el-input v-model="formData.ruleContent" :disabled="disabled"></el-input>
            </el-form-item>
            <el-form-item :label="$t('alertRule.notifyDuration')" prop="notifyDuration">
                <el-input v-model="formData.notifyDuration" style="width: 100px;margin-right: 5px;"
                    :disabled="disabled"></el-input>
                <el-select v-model="formData.notifyDurationUnit" :disabled="disabled" style="width: 80px;">
                    <el-option v-for="item in durationUnitList" :disabled="disabled" :key="item.name" :value="item.value"
                        :label="$t(`alertRule.${item.name}`)" />
                </el-select>
            </el-form-item>
            <el-form-item :label="$t('alertRule.alertDesc')" prop="alertDesc">
                <el-input v-model="formData.alertDesc" :disabled="disabled"></el-input>
            </el-form-item>
        </el-form>
        <el-descriptions :title="$t('alertRule.notifyTitle')"></el-descriptions>
        <el-form :model="formData" :rules="formRules" ref="formRef" label-position="right" label-width="100px">
            <el-form-item :label="$t('alertRule.alertNotify')" prop="notifyDuration">
                <el-checkbox-group v-model="alertNotifyList" :disabled="disabled">
                    <el-checkbox label="firing">{{ $t('alertRule.firing') }}</el-checkbox>
                    <el-checkbox label="recover">{{ $t('alertRule.recover') }}</el-checkbox>
                </el-checkbox-group>
            </el-form-item>
            <el-form-item :label="$t('alertRule.repeat')" prop="isRepeat">
                <el-radio-group v-model="isRepeat" :disabled="disabled">
                    <el-radio label="1">{{ $t('alertRule.isRepeat') }}</el-radio>
                    <el-radio label="0">{{ $t('alertRule.isNotRepeat') }}</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('alertRule.silence')" prop="isSilence">
                <el-radio-group v-model="isSilence" :disabled="disabled">
                    <el-radio label="1" key="1">{{ $t('alertRule.isSilence') }}</el-radio>
                    <el-radio label="0" key="0">{{ $t('alertRule.isNotSilence') }}</el-radio>
                </el-radio-group>
                <span style="margin-left: 30px;" v-if="isSilence === 1 || isSilence === '1'">{{ $t('alertRule.silenceTime')
                }}</span>
                <span style="width: 250px;margin-left: 5px;">
                    <el-date-picker :disabled="disabled" v-if="isSilence === 1 || isSilence === '1'" v-model="silenceTimes"
                        type="datetimerange" range-separator="~" format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DD HH:mm:ss" :start-placeholder="t(`alertRecord.startTimePlaceholder`)"
                        :end-placeholder="t(`alertRecord.endTimePlaceholder`)" />
                </span>
            </el-form-item>
            <el-form-item :label="$t('alertRule.notifyWay')">
                <el-select v-model="notifyWayIdArr" multiple :disabled="disabled">
                    <el-option v-for="item in notifyWayList" :key="item.id" :value="item.id + ''" :label="item.name" />
                </el-select>
            </el-form-item>
        </el-form>
        <el-row style="margin-top: 10px;">
            <!-- <el-button type="primary" @click="confirm">{{ t('app.confirm') }}</el-button> -->
            <el-button type="primary" @click="cancel">{{ t('app.cancel') }}</el-button>
        </el-row>
    </div>
</template>

<script setup lang='ts'>
import { Search, Refresh, Delete } from "@element-plus/icons-vue";
import "element-plus/es/components/message-box/style/index";
import { useRequest } from "vue-request";
import request from "@/request";
import { i18n } from "@/i18n";
import { ElMessageBox, ElMessage } from "element-plus";
import { useI18n } from "vue-i18n";
const { t } = useI18n();

const props = withDefaults(
    defineProps<{
        ruleId: number,
        state: string,
    }>(),
    {
        state: 'detail'
    }
);

const emit = defineEmits(["updateRule", "cancelRule"]);

const disabled = ref<boolean>(true)
const ruleItemExpList = ref<any[]>()
const actionList = ref<any[]>([{
    name: t('alertRule.normalAction'), value: 'normal'
}, {
    name: t('alertRule.increaseAction'), value: 'increase'
}, {
    name: t('alertRule.decreaseAction'), value: 'decrease'
}])
const logicSymbolList = ref<string[]>(['and', 'or'])
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
const formData = ref<any>({
    ruleName: '',
    ruleType: '',
    level: '',
    ruleExpComb: '',
    isRepeat: 1,
    isSilence: 0,
})
const ruleTypeList = reactive(['index', 'log'])
const levelList = reactive(['serious', 'warn', 'info'])
const ruleExpComb = ref<string[]>([])
const alertNotifyList = ref<string[]>([])
const isRepeat = ref<string>('')
const isSilence = ref<string>('')
const silenceTimes = ref<any[]>([])
const showLogicSelect = (logicSymbol: string) => {
    return logicSymbolList.value.includes(logicSymbol)
}

const { data: ruleDetail, run: requestData } = useRequest(
    (id) => {
        return request.get(`/alertCenter/api/v1/alertRule/${id}`)
    },
    { manual: true }
)
watch(ruleDetail, (ruleDetail: any) => {
    if (ruleDetail && ruleDetail.code === 200) {
        formData.value = ruleDetail.data
        if (formData.value?.ruleExpComb) {
            ruleExpComb.value = formData.value?.ruleExpComb.split(',')
        }
        if (formData.value?.alertNotify) {
            alertNotifyList.value = formData.value?.alertNotify.split(',')
        }
        if (formData.value?.isRepeat) {
            isRepeat.value = formData.value?.isRepeat + ''
        }
        if (formData.value?.isSilence) {
            isSilence.value = formData.value?.isSilence + ''
        } else {
            isSilence.value = '0'
        }
        if (formData.value.notifyWayIds) {
            notifyWayIdArr.value = formData.value.notifyWayIds.split(',')
        }
        if (formData.value.silenceStartTime && formData.value.silenceEndTime) {
            silenceTimes.value = [formData.value.silenceStartTime, formData.value.silenceEndTime]
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

const cancel = () => {
    emit("cancelRule")
}

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
onMounted(() => {
    requestNotifyWayData()
    requestData(props.ruleId)
    requestRuleItemList()
    if (props.state !== 'detail') {
        disabled.value = false
    } else {
        disabled.value = true
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
<style scoped lang='scss'></style>