<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { formatValue, partitionInfoType } from './common';

const { t } = useI18n();

const props = withDefaults(
    defineProps<{
        partitionInfoList: Array<partitionInfoType>;
    }>(),
    {}
);

const data = reactive<{
    partiList: Array<partitionInfoType>;
}>({
    partiList: []
});

const formatPartstrategyValue = (v: string) => {
    let resText = "";
    switch (v) {
    case 'r':
        resText = t('report.partitionObj.rangePartition');
        break;
    case 'v':
        resText = t('report.partitionObj.numericalPartition');
        break;
    case 'i':
        resText = t('report.partitionObj.intervalPartition');
        break;
    case 'l':
        resText = t('report.partitionObj.listPartition');
        break;
    case 'h':
        resText = t('report.partitionObj.hashPartition');
        break;
    case 'n':
        resText = t('report.partitionObj.invalidPartition');
        break;
    } 
    return resText;
}

watch(() => props.partitionInfoList, list => {
    if (Array.isArray(list)) {
        data.partiList = list.map(item => {
            if (Array.isArray(item.partKey)) {
                item.partKey = item.partKey.join(",")
            }
            return item;
        })
    }
})

</script>

<template>
    <div class="partition-container">
        <div class="partition-header">{{ $t('report.partitionTitle') }}</div>
        <div class="partition-body">
            <el-table :data="data.partiList" border>
                <el-table-column :label="$t('report.partitionObj.partStrategy')" prop="partStrategy" :formatter="(r: any) => formatPartstrategyValue(r.partStrategy)" />
                <el-table-column :label="$t('report.partitionObj.partKey')" prop="partKey" />
                <el-table-column :label="$t('report.partitionObj.interval')" prop="interval" :formatter="(r: any) => formatValue(r.interval)" />
            </el-table>
        </div>
    </div>
</template>
 
<style scoped lang="scss">
.partition-container {
    font-size: 14px;
    color: var(--el-text-color-og);
    margin-bottom: 12px;
}

.partition-header {
    font-weight: 700;
    margin-bottom: 10px;
}

.partition-body {

    :deep(.el-table) {
        background-color: var(--el-bg-color-og);
    }

    :deep(.el-table__header-wrapper th) {
        background-color: var(--el-bg-color-og);
        color: var(--el-text-color-og);
    }

    :deep(.el-table__row) {
        background-color: var(--el-bg-color-og);
    }
    :deep(.el-table__row th) {
        color: var(--el-text-color-og);
    }
}

</style>
