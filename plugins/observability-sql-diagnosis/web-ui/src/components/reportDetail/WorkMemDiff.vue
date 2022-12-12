<script setup lang="ts">
import { formatValue } from './common';

const props = withDefaults(
    defineProps<{
        workmemAndPeakmemArr: Array<{ peakMem: string, workMem: string }>
    }>(),
    {}
);

</script>

<template>
    <div class="workMem-container">
        <div class="workMem-header">{{ $t('report.workMemTitle') }}</div>
        <div class="workMem-body">
            <el-table :data="props.workmemAndPeakmemArr" border>
                <el-table-column label="" prop="unit" />
                <el-table-column :label="$t('report.workMemObj.consumesMemory')" prop="peakMem" :formatter="(r: any) => formatValue(r.peakMem)" />
                <el-table-column label="work_mem" prop="workMem" :formatter="(r: any) => formatValue(r.workMem)" />
            </el-table>
        </div>
    </div>
</template>
 
<style scoped lang="scss">
.workMem-container {
    font-size: 14px;
    color: var(--el-text-color-og);
    margin-bottom: 12px;
}

.workMem-header {
    font-weight: 700;
    margin-bottom: 10px;
}

.workMem-body {

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
