<script setup lang="ts">
import { OptionType, baseInfoOptionTypeA, baseInfoOptionTypeB } from './common';
import { toFixed } from '../../shared';
import { useI18n } from 'vue-i18n';

const { t } = useI18n()

const props = withDefaults(
    defineProps<{
        metaData: Record<string, string>,
        nodeType: string,
    }>(),
    {
        nodeType: ""
    }
);

const data = reactive<{
    infoOption: Array<Array<OptionType>>,
    signRedContent: Array<string>
}>({
    infoOption: [],
    signRedContent: []
})

const isRedValue = (value: string) => {
    return data.signRedContent.includes(value);
}

const getValue = (key: string, curData: Record<string, string>) => {
    if (curData != null && curData[key] != null) {
        if (key === 'object_type') {
            const map: Record<string, string> = {
                r: t('sql.objStructureOther.commonTable'),
                i: t('sql.objStructureOther.indexes'),
                S: t('sql.objStructureOther.sequence'),
                t: t('sql.objStructureOther.toastTable'),
                v: t('sql.objStructureOther.view'),
                m: t('sql.objStructureOther.materializedView'),
                c: t('sql.objStructureOther.combinationType'),
                f: t('sql.objStructureOther.externalTable'),
                p: t('sql.objStructureOther.partitionTable'),
                I: t('sql.objStructureOther.partitionIndex')
            }
            return `${map[curData[key]] || curData[key]}` || t('sql.objStructureOther.none');
        }
        if (key === 'object_size') {
            return `${toFixed(Number.parseFloat(curData[key]) / 1048576)}MB` || t('sql.objStructureOther.none');
        }
        if (key === 'dead_tup_ratio') {
            return curData[key].indexOf(".") === 0 ? `0${curData[key]}` : curData[key];
        }
        return `${curData[key]}` || t('sql.objStructureOther.none');
    }
    return t('sql.objStructureOther.none');
}

const getTableNum = (curData: Record<string, string>) => {
    return Number.parseFloat(curData["n_live_tup"]) + Number.parseFloat(curData["n_dead_tup"]);
}

onMounted(() => {
    const curNodeType = props.nodeType;
    if (curNodeType === 'PlanChangedToPartitionTable' || curNodeType === 'PlanRecommendedToQueryBasedOnPartition') {
        data.infoOption = baseInfoOptionTypeA;
        data.signRedContent = ['n_live_tup'];
    } else if (curNodeType === 'PlanRecommendedToDoVacuumCleaning') {
        data.infoOption = baseInfoOptionTypeB;
        data.signRedContent = ['n_dead_tup', 'dead_tup_ratio'];
    }
})

</script>

<template>
    <div class="baseInfo-container">
        <div class="baseInfo-header">{{ $t('report.baseInfoTitle') }}</div>
        <div class="baseInfo-body">
            <el-row v-for="(item, index) in data.infoOption" :key="index">
                <el-col class="s-i-el-col" :span="4">{{ $t(`report.baseInfoObj.${item[0].label}`) }}</el-col>
                <el-col :span="6">
                    <div :style="{color: isRedValue(item[0].value) ? 'red': ''}">{{ getValue(item[0].value, props.metaData) }}</div>
                </el-col>
                <el-col class="s-i-el-col" :span="6">{{ $t(`report.baseInfoObj.${item[1].label}`) }}</el-col>
                <el-col :span="8">
                    <div :style="{color: isRedValue(item[1].value) ? 'red': ''}">
                        {{ item[1].value === "table_num" ? getTableNum(props.metaData) : getValue(item[1].value, props.metaData) }}
                    </div>
                </el-col>
            </el-row>
        </div>
    </div>
</template>

<style scoped lang="scss">
.baseInfo-container {
    font-size: 14px;
    color: var(--el-text-color-og);
    margin: 12px 0;
}

.baseInfo-header {
    font-weight: 700;
    margin-bottom: 10px;
}

.baseInfo-body {
    border-top: 1px solid $og-border-color;
    .s-i-el-col {
        color: var(--el-text-color-og);
        background-color: var(--el-bg-color-sub);
    }

    &:deep(.el-row) {
        line-height: 39px;
        border: 1px solid $og-border-color;
        border-top: none;
    }

    &:deep(.el-row:first-child) {
        border-right: 1px solid $og-border-color;
    }

    &:deep(.el-col) {
        padding-left: 16px;
        overflow: hidden;
    }
}

</style>
